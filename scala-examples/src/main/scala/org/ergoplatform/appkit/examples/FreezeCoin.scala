package org.ergoplatform.appkit.examples

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.ErgoToolConfig

object FreezeCoinScala {
  /**
   * Create and send transaction creating a box with the given amount using parameters from the given config file.
   *
   * @param amountToSend   amount of NanoErg to put into new box
   * @param configFileName name of the configuration file relative to the current directory.
   * @return json string of the signed transaction
   */
  def sendTx(amountToSend: Long, configFileName: String): String = {
    val conf = ErgoToolConfig.load(configFileName)
    val newBoxSpendingDelay = conf.getParameters.get("newBoxSpendingDelay").toInt
    val ownerAddress = Address.create(conf.getParameters.get("ownerAddress"))

    val nodeConf = conf.getNode
    val ergoClient = RestApiErgoClient.create(nodeConf)

    val txJson = ergoClient.execute((ctx: BlockchainContext) => {
      val wallet = ctx.getWallet
      val totalToSpend = amountToSend + Parameters.MinFee
      val boxes = wallet.getUnspentBoxes(totalToSpend)
      if (!boxes.isPresent)
        throw new ErgoClientException(s"Not enough coins in the wallet to pay $totalToSpend", null)

      val prover = ctx.newProverBuilder
        .withMnemonic(nodeConf.getWallet.getMnemonic, nodeConf.getWallet.getPassword)
        .build()
      val txB = ctx.newTxBuilder
      val newBox = txB.outBoxBuilder
        .value(amountToSend)
        .contract(
          ctx.compileContract(
            ConstantsBuilder.create()
              .item("freezeDeadline", ctx.getHeight + newBoxSpendingDelay)
              .item("ownerPk", ownerAddress.getPublicKey)
              .build(),
            "{ sigmaProp(HEIGHT > freezeDeadline) && ownerPk }"))
        .build()
      val tx = txB.boxesToSpend(boxes.get)
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress)
        .build()
      val signed = prover.sign(tx)
      val txId = ctx.sendTransaction(signed)
      signed.toJson(true)
    })
    txJson
  }

  def main(args: Array[String]): Unit = {
    val amountToSend = args(0).toLong
    val txJson = sendTx(amountToSend, "freeze_coin_config.json")
    System.out.println(txJson)
  }

}
