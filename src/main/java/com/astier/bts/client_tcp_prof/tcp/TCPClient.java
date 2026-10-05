package com.astier.bts.client_tcp_prof.tcp;

import com.astier.bts.client_tcp_prof.HelloController;
import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.diffie_hellman.DiffieHellman;
import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
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

public class TCPClient extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    public OutputStream outBin;
    public InputStream inBin;
    HelloController fxmlCont;
    byte[] buffer = new byte[65535];

    Aes_cbc aes;
    DiffieHellman dh;

    public TCPClient(InetAddress serveur, int port, HelloController fxmlCont) throws FileNotFoundException {
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
            inBin = socket.getInputStream();
            outBin = socket.getOutputStream();
            socket.setSoTimeout(5000); // Évite une attente sans fin.

            dh = new DiffieHellman(this, 1024);
            byte[] params = dh.recuperParams();
            byte[] password = Arrays.copyOfRange(params, 1, 17);
            byte[] iv = Arrays.copyOfRange(params, 17, 33);
            aes = new Aes_cbc(password, iv);

            updateMessage("""
                    \tclef   :    %s
                    \tIV     :    %s
                    """.formatted(toHex(password), toHex(iv)));

            socket.setSoTimeout(0); // Après l'échange, on peut attendre les réponses normalement.
        } catch (Exception ex) {
            updateMessage(DiagnosticException.afficheException(ex));
            try {
                socket.close();
            } catch (IOException e) {
                updateMessage(DiagnosticException.afficheException(e));
            }
            return;
        }

        marche = true;
        this.start();

        fxmlCont.connecter.setDisable(true);
        fxmlCont.deconnecter.setDisable(false);
        fxmlCont.voyant.setFill(GREEN);
        fxmlCont.choiceBoxInterfaces.setDisable(true);
    }

    public void deconnection() {
        if (!this.isAlive()) return;

        marche = false;

        try {
            outBin.write(aes.cryptage(("exit\n").getBytes(StandardCharsets.UTF_8))); // Message de sortie
            outBin.flush();
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
        fxmlCont.choiceBoxInterfaces.setDisable(false);
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
                    updateMessage(new String(aes.decryptage(trameUtile)));
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

    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
