import sys
import java

Integer = java.type("java.lang.Integer")
Long = java.type("java.lang.Long")
Address = java.type("org.ergoplatform.appkit.Address")
RestApiErgoClient = java.type("org.ergoplatform.appkit.RestApiErgoClient")
ErgoClientException = java.type("org.ergoplatform.appkit.ErgoClientException")
ConstantsBuilder = java.type("org.ergoplatform.appkit.ConstantsBuilder")
ErgoToolConfig = java.type("org.ergoplatform.appkit.config.ErgoToolConfig")
Parameters = java.type("org.ergoplatform.appkit.Parameters")

amountToPay = Long.parseLong(sys.argv[1])
conf = ErgoToolConfig.load("freeze_coin_config.json")
nodeConf = conf.getNode()
newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"))
ownerAddress = Address.create(conf.getParameters().get("ownerAddress"))

ergoClient = RestApiErgoClient.create(nodeConf)

def send(ctx):
    wallet = ctx.getWallet()
    totalToSpend = amountToPay + Parameters.MinFee
    boxes = wallet.getUnspentBoxes(totalToSpend)
    if not boxes.isPresent():
        raise ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, None);

    prover = ctx.\
        newProverBuilder().\
        withMnemonic(
            nodeConf.getWallet().getMnemonic(),
            nodeConf.getWallet().getPassword()).\
        build()

    txB = ctx.newTxBuilder()

    newBox = txB.outBoxBuilder().\
        value(amountToPay).\
        contract(ctx.compileContract(
            ConstantsBuilder.create()
                .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                .item("ownerPk", ownerAddress.getPublicKey()).build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && ownerPk }")).\
            build()

    tx = txB.boxesToSpend(boxes.get()).\
        outputs(newBox).\
        fee(Parameters.MinFee).\
        sendChangeTo(prover.getP2PKAddress()).\
        build();

    signed = prover.sign(tx)
    txId = ctx.sendTransaction(signed)
    return signed.toJson(True)

txJson = ergoClient.execute(send)

print(txJson)
