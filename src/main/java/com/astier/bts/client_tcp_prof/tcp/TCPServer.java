package com.astier.bts.client_tcp_prof.tcp;

import com.astier.bts.client_tcp_prof.OUTILS.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.configuration.LectureJson;
import com.astier.bts.client_tcp_prof.modeles.ConfigAES;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TCPServer {

    private static final int PORT = 4000;

    private static final String[] commandsDesc = {"HELP", "HELLO", "TIME", "ECHO", "WHOAREYOU?", "YOU", "WHOAMI?", "ME", "FIN", "EXIT"};
    private static final String serveurPrefix = "[Serveur] ";
    private static final String clientPrefix = "[Client] ";
    private static ConfigAES configAes;
    private static Aes_cbc aes;

    static void main(String[] args) throws IOException {
        configAes = LectureJson.getConfigAES();
        aes = new Aes_cbc(configAes.passwordAsBytes(), configAes.ivAsBytes());

        ServerSocket serveur = new ServerSocket(PORT);
        System.out.println("Serveur en fonctionnement sur le port " + PORT + ".");
        while (true) {
            try {
                Socket client = serveur.accept();

                InputStream entree = client.getInputStream();
                OutputStream sortie = client.getOutputStream();

                System.out.println("Connexion avec : " + client);
                sortie.write(aes.cryptage(("Bonjour, voici la liste des commandes : " + String.join(", ", commandsDesc)).getBytes(StandardCharsets.UTF_8)));

                byte [] trame;
                String reponse;
                boolean flag = false;

                trame = aes.decryptage(entree.readAllBytes());

                System.out.println("Test 1");
                if (trame.length != 0) {
                    System.out.println("Test 2");
                    String msgRecu = new String(trame, StandardCharsets.UTF_8);
                    System.out.println("Message reçu : " + msgRecu);

                    switch (msgRecu.toUpperCase()) {
                        case "HELLO" -> reponse = serveurPrefix + "Salut!";
                        case "TIME" -> reponse = serveurPrefix + "Voici la date et l'heure : " + recuperDateHeure();
                        case String s when s.trim().startsWith("ECHO") -> reponse = serveurPrefix + "Votre message : " + msgRecu.substring(4).trim();
                        case "WHOAREYOU?", "YOU" -> reponse = serveurPrefix + "adresse:port : " + InetAddress.getLocalHost() + ":" + serveur.getLocalPort();
                        case "WHOAMI?", "ME" -> reponse = clientPrefix + "adresse:port : " + client.getRemoteSocketAddress().toString().replace("/","");
                        case "HELP" -> reponse = String.join(", ", commandsDesc);
                        case "EXIT", "FIN" -> {
                            reponse = serveurPrefix + "Deconnexion du serveur ...";
                            flag = true;
                        }
                        default -> reponse = serveurPrefix + "Commande invalide";
                    };

                    sortie.write(aes.cryptage(reponse.getBytes(StandardCharsets.UTF_8)));
                    System.out.println("Message émis : " + reponse);

                    if (flag) {
                        entree.close();
                        sortie.close();
                        break;
                    }
                }
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
