Long = Java.type("java.lang.Long")
JInteger = Java.type("java.lang.Integer")  # to avoid conflict with Ruby's Integer
Address = Java.type("org.ergoplatform.appkit.Address")
RestApiErgoClient = Java.type("org.ergoplatform.appkit.RestApiErgoClient")
ErgoClientException = Java.type("org.ergoplatform.appkit.ErgoClientException")
ConstantsBuilder = Java.type("org.ergoplatform.appkit.ConstantsBuilder")
ErgoToolConfig = Java.type("org.ergoplatform.appkit.config.ErgoToolConfig")
Parameters = Java.type("org.ergoplatform.appkit.Parameters")

amountToPay = Long.parseLong(ARGV[0])
conf = ErgoToolConfig.load("freeze_coin_config.json")
newBoxSpendingDelay = JInteger.parseInt(conf.getParameters().get("newBoxSpendingDelay"))
ownerAddress = Address.create(conf.getParameters().get("ownerAddress"))

nodeConf = conf.getNode()
ergoClient = RestApiErgoClient.create(nodeConf)

txJson = ergoClient.execute(lambda {|ctx|  # note, we use lambda to convert code block to Proc object
    wallet = ctx.getWallet()
    txFee = Parameters["MinFee"]
    totalToSpend = amountToPay + txFee
    boxes = wallet.getUnspentBoxes(totalToSpend)
    if !boxes.isPresent()
        raise ErgoClientException, "Not enough coins in the wallet to pay " + totalToSpend, Nil
    end

    prover = ctx.newProverBuilder()
        .withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword())
        .build()

    txB = ctx.newTxBuilder()
    newBox = txB.outBoxBuilder()
        .value(amountToPay)
        .contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                .item("ownerPk", ownerAddress.getPublicKey())
                .build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && ownerPk }"))
        .build()
    tx = txB.boxesToSpend(boxes.get())
        .outputs(newBox)
        .fee(txFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build();

    signed = prover.sign(tx);
    txId = ctx.sendTransaction(signed);
    signed.toJson(true);
})

print(txJson);