package com.astier.bts.client_tcp_prof.tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TCPBinServer {

    private static final int PORT = 4000;

    private static final String[] commandsDesc = {"HELP", "HELLO", "TIME", "ECHO", "WHOAREYOU?", "YOU", "WHOAMI?", "ME", "FIN", "EXIT"};
    private static final String serveurPrefix = "[Serveur] ";
    private static final String clientPrefix = "[Client] ";

    static void main(String[] args) throws IOException {
        ServerSocket serveur = new ServerSocket(PORT);
        System.out.println("Serveur en fonctionnement sur le port " + PORT + ".");
        while (true) {
            try {
                Socket client = serveur.accept();

                InputStream entree = client.getInputStream();
                OutputStream sortie = client.getOutputStream();

                System.out.println("Connexion avec : " + client);
                sortie.write(("Bonjour, voici la liste des commandes : " + String.join(", ", commandsDesc)).getBytes(StandardCharsets.UTF_8));

                String messageRecu;
                String reponse;
                boolean flag = false;

//                while ((messageRecu = entree.readLine()) != null) {
//                    System.out.println("Message reçu : " + messageRecu);
//
//                    switch (messageRecu.toUpperCase()) {
//                        case "HELLO" -> reponse = serveurPrefix + "Salut!";
//                        case "TIME" -> reponse = serveurPrefix + "Voici la date et l'heure : " + recuperDateHeure();
//                        case String s when s.trim().startsWith("ECHO") -> reponse = serveurPrefix + "Votre message : " + messageRecu.substring(4).trim();
//                        case "WHOAREYOU?", "YOU" -> reponse = serveurPrefix + "adresse:port : " + InetAddress.getLocalHost() + ":" + serveur.getLocalPort();
//                        case "WHOAMI?", "ME" -> reponse = clientPrefix + "adresse:port : " + client.getRemoteSocketAddress().toString().replace("/","");
//                        case "HELP" -> reponse = String.join(", ", commandsDesc);
//                        case "EXIT", "FIN" -> {
//                            reponse = serveurPrefix + "Deconnexion du serveur ...";
//                            flag = true;
//                        }
//                        default -> reponse = serveurPrefix + "Commande invalide";
//                    };
//
//                    sortie.println(reponse);
//                    System.out.println("Message émis : " + reponse);
//
//                    if (flag) {
//                        entree.close();
//                        sortie.close();
//                        break;
//                    }
//                }
            } catch (IOException e) {
                System.err.println("Connexion interrompue : " + e.getMessage());
            }
            System.out.println("Client déconnecté.");

        }
    }

    static String recuperDateHeure() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
