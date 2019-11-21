package org.ergoplatform.appkit.examples;

import org.ergoplatform.appkit.*;
import org.ergoplatform.appkit.config.ErgoNodeConfig;
import org.ergoplatform.appkit.config.ErgoToolConfig;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.SignedWord;
import org.graalvm.word.UnsignedWord;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public class ErgoToolJava {

    /**
     * Create and send transaction creating a box with the given amount using parameters from the given config file.
     *
     * @param amountToSend   amount of NanoErg to put into new box
     * @param configFileName name of the configuration file relative to the current directory.
     * @return json string of the signed transaction
     */
    public static String sendTx(long amountToSend, String configFileName) throws FileNotFoundException {
        ErgoToolConfig conf = ErgoToolConfig.load(configFileName);
        ErgoNodeConfig nodeConf = conf.getNode();
        int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));

        ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);

        String txJson = ergoClient.execute(ctx -> {
            ErgoWallet wallet = ctx.getWallet();
            long totalToSpend = amountToSend + Parameters.MinFee;
            Optional<List<InputBox>> boxes = wallet.getUnspentBoxes(totalToSpend);
            if (!boxes.isPresent())
                throw new ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, null);

            ErgoProver prover = ctx.newProverBuilder()
                    .withMnemonic(
                            nodeConf.getWallet().getMnemonic(),
                            nodeConf.getWallet().getPassword())
                    .build();

            UnsignedTransactionBuilder txB = ctx.newTxBuilder();
            OutBox newBox = txB.outBoxBuilder()
                    .value(amountToSend)
                    .contract(ctx.compileContract(
                            ConstantsBuilder.create()
                                    .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                                    .item("walletOwnerPk", prover.getP2PKAddress().pubkey())
                                    .build(),
                            "{ sigmaProp(HEIGHT > freezeDeadline) && walletOwnerPk }"))
                    .build();
            UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
                    .outputs(newBox)
                    .fee(Parameters.MinFee)
                    .sendChangeTo(prover.getP2PKAddress())
                    .build();

            SignedTransaction signed = prover.sign(tx);
            String txId = ctx.sendTransaction(signed);
            return signed.toJson(true);
        });
        return txJson;
    }

    public static void main(String[] args) {
        try {
            long amountToSend = Long.parseLong(args[0]);
            String txJson = sendTx(amountToSend, "ErgoTool.json");
            System.out.println(txJson);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point callable from C which wraps {@link ErgoToolJava#sendTx}
     */
    @CEntryPoint(name = "sendTx")
    public static void sendTxEntryPoint(
            IsolateThread thread,
            SignedWord amountToSendW,
            CCharPointer configFileNameC,
            CCharPointer resBuffer, UnsignedWord bufferSize) throws FileNotFoundException {
        long amountToSend = amountToSendW.rawValue();
        // Convert the C strings to the target Java strings.
        String configFileName = CTypeConversion.toJavaString(configFileNameC);
        String txJson = sendTx(amountToSend, configFileName);

        // put resulting string into provided buffer
        CTypeConversion.toCString(txJson, resBuffer, bufferSize);
    }
}
