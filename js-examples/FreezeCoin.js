const Integer = Java.type("java.lang.Integer")
const Long = Java.type("java.lang.Long")
const Address = Java.type("org.ergoplatform.appkit.Address")
const RestApiErgoClient = Java.type("org.ergoplatform.appkit.RestApiErgoClient")
const ErgoClientException = Java.type("org.ergoplatform.appkit.ErgoClientException")
const ConstantsBuilder = Java.type("org.ergoplatform.appkit.ConstantsBuilder")
const ErgoToolConfig = Java.type("org.ergoplatform.appkit.config.ErgoToolConfig")
const Parameters = Java.type("org.ergoplatform.appkit.Parameters")

const amountToPay = Long.parseLong(process.argv[2]);
const conf = ErgoToolConfig.load("freeze_coin_config.json");
const nodeConf = conf.getNode();
const newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));
const ownerAddress = Address.create(conf.getParameters().get("ownerAddress"));

const ergoClient = RestApiErgoClient.create(nodeConf);

const txJson = ergoClient.execute(function (ctx) {
    const wallet = ctx.getWallet();
    const totalToSpend = amountToPay + Parameters.MinFee;
    const boxes = wallet.getUnspentBoxes(totalToSpend);
    if (!boxes.isPresent())
        throw new ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, null);

    const prover = ctx.newProverBuilder()
        .withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
        .build();

    const txB = ctx.newTxBuilder();
    const newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                .item("ownerPk", ownerAddress.getPublicKey())
                .build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && ownerPk }"))
        .build();
    const tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();

    const signed = prover.sign(tx);
    const txId = ctx.sendTransaction(signed);
    return signed.toJson(true);
});

print(txJson);