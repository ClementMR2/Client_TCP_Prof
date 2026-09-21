package com.astier.bts.client_tcp_prof.tcp;

import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.OUTILS.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.OUTILS.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.configuration.LectureJson;
import com.astier.bts.client_tcp_prof.modeles.ConfigAES;
import javafx.application.Platform;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

public class TCPBinClient extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    OutputStream outBin;
    InputStream inBin;
    HelloController fxmlCont;
    byte[] buffer = new byte[65535];

    byte[] password = "mot de passe aes".getBytes(StandardCharsets.UTF_8);
    byte[] iv = "ici vecteur d'in".getBytes(StandardCharsets.UTF_8);
    Aes_cbc aes = new Aes_cbc(password, iv);

    LectureJson lectureJson;

    public TCPBinClient() {
    }

    public TCPBinClient(InetAddress serveur, int port, HelloController fxmlCont) {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;
        System.out.println("@ serveur: " + serveur + " port: " + port);
    }

    public void connection() {
        if (this.isAlive()) return;

        socket = new Socket();
        SocketAddress endpoint = new InetSocketAddress(serveur, port);

        try {
            socket.connect(endpoint, 2000);
            //socket.setSoTimeout(5000);

            inBin = socket.getInputStream();
            outBin = socket.getOutputStream();
        } catch (Exception ex) {
            updateMessage(DiagnosticException.afficheException(ex));
        }

        marche = true;
        this.start();

        fxmlCont.connecter.setDisable(true);
        fxmlCont.deconnecter.setDisable(false);
        fxmlCont.voyant.setFill(GREEN);
    }

    public void testJson() throws FileNotFoundException {
        lectureJson = new LectureJson("configuration.json");
        ConfigAES configAes = lectureJson.getConfigAES();
        System.out.println(configAes.password());
        System.out.println(configAes.iv());
    }

    public void deconnection() {
        if (!this.isAlive()) return;

        marche = false;

        try {
            //outBin.write("exit\n".getBytes(StandardCharsets.UTF_8));
            //outBin.flush();
            Thread.sleep(100);
            inBin.close();
            outBin.close();
            socket.close();
        } catch (Exception ex) {
            updateMessage(DiagnosticException.afficheException(ex));
        }

        fxmlCont.connecter.setDisable(false);
        fxmlCont.deconnecter.setDisable(true);
        fxmlCont.voyant.setFill(RED);
    }

    public void requette(String laRequette) {
        try {
            outBin.write(aes.cryptage((laRequette + "\n").getBytes(StandardCharsets.UTF_8)));
            outBin.flush();
            System.out.println("la requette " + laRequette);
        } catch (Exception ex) {
            updateMessage(DiagnosticException.afficheException(ex));
        }
    }

    public void run() {
        while (marche) {
            try {
                int nbsLusBin = inBin.read(buffer);
                if (nbsLusBin > 0) {
                    byte[] trameUtile = Arrays.copyOfRange(buffer, 0, nbsLusBin);

                    String message = new String(aes.decryptage(trameUtile));
                    updateMessage(message);

                    //System.out.println(jsonReader.getPassword());
                    //System.out.println(jsonReader.getIV());
                }
            } catch (Exception ex) {
                updateMessage(DiagnosticException.afficheException(ex));
            }
        }
    }

    /*
    Pour déclencher une opération graphique en dehors du thread graphique  utiliser
    javafx.application.Platform.runLater(java.lang.Runnable)
    Cette méthode permet d'éxécuter le code du runnable par le thread graphique de JavaFX.
    */
    protected void updateMessage(String message) {
        Platform.runLater(() -> fxmlCont.TextAreaReponses.appendText("    MESSAGE SERVEUR >  \n      " + message + "\n"));
    }
}
