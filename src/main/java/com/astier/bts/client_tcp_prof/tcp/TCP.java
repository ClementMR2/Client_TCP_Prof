/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.astier.bts.client_tcp_prof.tcp;


import com.astier.bts.client_tcp_prof.HelloController;
import javafx.application.Platform;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Michael
 */
public class TCP extends Thread {
    int port;
    InetAddress serveur;
    Socket socket;
    boolean marche = false;
    boolean connection = false;
    PrintStream out;
    BufferedReader in;
    HelloController fxmlCont;

    public TCP() {
    }

    public TCP(InetAddress serveur, int port, HelloController fxmlCont) {
        this.port = port;
        this.serveur = serveur;
        this.fxmlCont = fxmlCont;
        System.out.println("@ serveur: " + serveur + " port: " + port);
    }

    public void connection() throws IOException {
       if (this.isAlive()) {
            System.out.println("Le client est déjà actif.");
            return;
        }

        socket = new Socket(serveur, port);
        //socket.setSoTimeout(5000);

        marche = true;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream(), true);

        this.start();
    }

    public void deconnection() throws InterruptedException {
        if (this.isAlive()) {
            System.out.println("Stopping the server ...");

            marche = false;

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void requette(String laRequette) throws IOException {
        out.println(laRequette);  // envoi reseau
        System.out.println("la requette " + laRequette);
    }

    public void run() {
        while (marche) {
            char[] buffer = new char[65535];
            try {
                int i = in.read(buffer);
                if (i > 0) {
                    String msg = new String(buffer, 0, i);
                    updateMessage(msg);
                }
            } catch (IOException e) {
                marche = false;
                System.err.println("Erreur : " + e.getMessage());
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