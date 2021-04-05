# Ergo Appkit Examples
Examples of using [Ergo Appkit](https://github.com/ergoplatform/ergo-appkit) for Polyglot Development of Ergo Applications.

### Using from Java 

Among other things, Appkit library allows to communicate with Ergo nodes via
REST API. Let's see how we can write FreezeCoin - a simple Java console application (similar to
[ergo-tool](https://github.com/ergoplatform/ergo-tool) utility)
which uses Appkit library. The app will allow to create and send a new transaction
to an Ergo node which, for example, can be started locally and thus available at
`http://localhost:9052/`. Suppose we [set up a full
node](https://github.com/ergoplatform/ergo/wiki/Set-up-a-full-node) and started
it using the following command.
```shell
$ java -jar -Xmx4G target/scala-2.12/ergo-4.0.8.jar --testnet -c ergo-testnet.conf
```

We will need some configuration parameters which can be loaded from
`freeze_coin_config.json` file which looks like this
```json
{
  "node": {
    "nodeApi": {
      "apiUrl": "http://localhost:9051/",
      "apiKey": "82344a18c24adc42b78f52c58facfdf19c8cc38858a5f22e68070959499076e1"
    },
    "wallet": {
      "mnemonic": "slow silly start wash bundle suffer bulb ancient height spin express remind today effort helmet",
      "password": "",
      "mnemonicPassword": ""
    },
    "networkType": "TESTNET"
  },
  "parameters": {
    "newBoxSpendingDelay": "30",
    "ownerAddress": "3WzR39tWQ5cxxWWX6ys7wNdJKLijPeyaKgx72uqg9FJRBCdZPovL"
  }
}
```
Here `apiKey` is the secret key required for API authentication which can be
obtained as described
[here](https://github.com/ergoplatform/ergo/wiki/Ergo-REST-API#setting-an-api-key).
And mnemonic is the secret phrase obtained during [setup of a new
wallet](https://github.com/ergoplatform/ergo/wiki/Wallet-documentation).

Our example app also reads the amount of NanoErg to put into a new box from command line arguments
```java
public static void main(String[] args) {
    long amountToPay = Long.parseLong(args[0]);
    ErgoToolConfig conf = ErgoToolConfig.load("freeze_coin_config.json");
    int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));
    // the rest of the code shown below 
    ...
}
```

Next we connect to the running testnet node from our Java application by creating
`ErgoClient` instance.
```java
ErgoNodeConfig nodeConf = conf.getNode();
ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);
```

Using `ErgoClient` we can
[execute](https://github.com/ergoplatform/ergo-appkit/blob/master/lib-api/src/main/java/org/ergoplatform/appkit/ErgoClient.java)
any block of code in the current blockchain context.

```java
String txJson = ergoClient.execute((BlockchainContext ctx) -> {
    // here we will use ctx to create and sign a new transaction
    // which then be sent to the node and also serialized into Json
});
```

The lambda passed to `execute` is called when the current blockchain context 
is loaded from the node. This is where we shall put our application logic.
We start with some auxiliary steps.
```java
// access wallet embedded in Ergo node
ErgoWallet wallet = ctx.getWallet();

// calculate total amount of NanoErgs we need to create the new box 
// and pay transaction fees
long totalToSpend = amountToPay + Parameters.MinFee;

// request wallet for unspent boxes that cover required amount of NanoErgs
Optional<List<InputBox>> boxes = wallet.getUnspentBoxes(totalToSpend);
if (!boxes.isPresent())
    throw new ErgoClientException(
        "Not enough coins in the wallet to pay " + totalToSpend, null);
    
// create a so called prover, a special object which will be used for signing the transaction
// the prover should be configured with secrets, which are nessesary to generate signatures (aka proofs)
ErgoProver prover = ctx.newProverBuilder()
    .withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
    .build();
```

Now that we have the input boxes to spend in the transaction, we need to create 
an output box with the requested `amountToPay` and the specific contract protecting 
that box.

```java
// the only way to create transaction is using builder obtained from the context
// the builder keeps relationship with the context to access nessary blockchain data.
UnsignedTransactionBuilder txB = ctx.newTxBuilder();

// create new box using new builder obtained from the transaction builder
// in this case we compile new ErgoContract from source ErgoScript code
OutBox newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
                ConstantsBuilder.create()
                        .item("freezeDeadline", ctx.getHeight() + newBoxDelay)
                        .item("pkOwner", prover.getP2PKAddress().pubkey())
                        .build(),
                "{ sigmaProp(HEIGHT > freezeDeadline) && pkOwner }"))
        .build();
```
Note, in order to compile `ErgoContract` from source code the `compileContract`
method requires to provide values for named constants which are used in the script.
If no such constants are used, then `ConstantsBuilder.empty()` can be passed.

In this specific case we pass public key of the `prover` for `pkOwner` 
placeholder of the script meaning the box can be spend only by the owner of the
Ergo node we are working with. 

Next create an unsigned transaction using all the data collected so far.
```java
// tell transaction builder which boxes we are going to spend, which outputs
// to create, amount of transaction fees and address for change coins.
UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();
```

And finally we use prover to sign the transaction, obtain a new
`SignedTransaction` instance and use context to send it to the Ergo node.
The resulting `txId` can be used to refer to this transaction later and
is not really used here.

```java
SignedTransaction signed = prover.sign(tx);
String txId = ctx.sendTransaction(signed);
return signed.toJson(true);
```
As the last step we serialize signed transaction into Json with turned on pretty
printing. Please see the [full source
code](java-examples/src/main/java/org/ergoplatform/appkit/examples/FreezeCoin.java) of the
example.

### Using from JavaScript

Before running JavaScript example it my be helpful to run Java example
first to make sure everything is set up correctly.

GraalVM can [run JavaScript and
Node.js](https://www.graalvm.org/docs/reference-manual/languages/js/)
applications out of the box and it is compatible with the [ECMAScript 2019
specification](http://www.ecma-international.org/ecma-262/10.0/index.html).
Additionally, `js` and `node` launchers accept special `--jvm` and `--polyglot`
command line options which allow JS script to access Java objects and classes.

That said, a [JS example of FreezeCoin](js-examples/FreezeCoin.js) can be executed using
Node.js
```shell
$ node --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar \
  js-examples/FreezeCoin.js  1000000000
```

Start session for debugging
```shell
$ node --jvm --inspect --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar \
  js-examples/FreezeCoin.js  1000000000
```

### Using from Python

Before running Python example it my be helpful to run Java example
first to make sure everything is set up correctly.

GraalVM can [run Python
scripts](https://www.graalvm.org/docs/reference-manual/languages/python/), though
the Python implementation is still experimental (see also
[compatibility section](https://www.graalvm.org/docs/reference-manual/languages/python/#python-compatibility)
for details).

[Python example of FreezeCoin](python-examples/FreezeCoin.py) can be executed using the
following command
```shell
$ graalpython --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar \
  --polyglot python-examples/FreezeCoin.py 1900000000
```

### Using from Ruby

Before running Ruby example it may be helpful to run Java example
first to make sure everything is set up correctly.

GraalVM can [run Ruby
scripts](https://www.graalvm.org/docs/reference-manual/languages/ruby/), though
the Ruby implementation is still experimental (see also
[compatibility section](https://www.graalvm.org/docs/reference-manual/languages/ruby/#compatibility)
for details).

[Ruby example of FreezeCoin](ruby-examples/FreezeCoin.rb) can be executed using the
following command
```shell
$ ruby --polyglot --jvm --vm.cp=target/scala-2.12/ergo-appkit-3.1.0.jar \
    ruby-examples/FreezeCoin.rb 1900000000
```

