package com.astier.bts.client_tcp_prof.tcp;


import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class TCPServer {

    private static int PORT;
    private static final int PORT_UDP_ECOUTE = 5555;
    private static final SecureRandom rand = new SecureRandom();
    private static final String[] cmd = {"HELP", "HELLO", "TIME", "ECHO", "WHOAREYOU?", "YOU", "WHOAMI?", "ME", "FIN", "EXIT"};
    private static final String serveurPrefix = "[Serveur] ";
    private static final String clientPrefix = "[Client] ";
    private static final String demandeClient = "Tu es qui?";
    private static BigInteger p, g, A, b, B;

    static void main(String[] args) throws IOException {
        PORT = rand.nextInt(500) + 1024;

        Thread monThread = new Thread(() -> {
            try {
                InetAddress groupeMulticast = InetAddress.getByName("224.0.0.250");
                int portReponse = 5556;

                MulticastSocket ms = new MulticastSocket(PORT_UDP_ECOUTE);
                ms.joinGroup(groupeMulticast);

                byte[] buffer = new byte[1024];
                System.out.println("Serveur UDP -> " + groupeMulticast.getHostAddress() + ":" + PORT_UDP_ECOUTE);

                while (true) {
                    DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                    ms.receive(paquetRecu);

                    String message = new String(paquetRecu.getData(), 0, paquetRecu.getLength()).trim();
                    System.out.println("Mon message : " + message);

                    if (demandeClient.equalsIgnoreCase(message)) {
                        String strReponse = InetAddress.getLocalHost().getHostAddress() + ";" + PORT + ";" + PORT_UDP_ECOUTE;
                        byte[] donneesReponse = strReponse.getBytes(StandardCharsets.UTF_8);

                        DatagramSocket dsReponse = new DatagramSocket();
                        DatagramPacket paquetReponse = new DatagramPacket(
                                donneesReponse,
                                donneesReponse.length,
                                paquetRecu.getAddress(),
                                portReponse
                        );

                        dsReponse.send(paquetReponse);
                        dsReponse.close();
                    }
                }
            } catch (IOException e) {
                System.err.println(DiagnosticException.afficheException(e));
            }
        });

        if (!monThread.isAlive()) {
            monThread.start();
        }

        ServerSocket serveur = new ServerSocket(PORT);
        System.out.println("Serveur TCP -> " + PORT);

        while (true) {
            try {
                Socket client = serveur.accept();

                new Thread(() -> {
                    try (InputStream entree = client.getInputStream();
                         OutputStream sortie = client.getOutputStream()) {

                        System.out.println("Connexion TCP établie avec : " + client);
                        client.setSoTimeout(15000);
                        p = lireNombre(entree);
                        g = lireNombre(entree);
                        A = lireNombre(entree);
                        do {
                            b = new BigInteger(p.bitLength(), rand);
                        } while (b.compareTo(p.subtract(BigInteger.ONE)) >= 0);

                        B = g.modPow(b, p);
                        byte[] params = A.modPow(b, p).toByteArray();
                        Aes_cbc aes = new Aes_cbc(Arrays.copyOfRange(params, 1, 17), Arrays.copyOfRange(params, 17, 33));
                        sortie.write(B.toByteArray());
                        sortie.flush();
                        client.setSoTimeout(0);

                        sortie.write(aes.cryptage(("Bonjour, voici la liste des commandes : " + String.join(", ", cmd)).getBytes(StandardCharsets.UTF_8)));

                        boolean flag = false;
                        byte[] buffer = new byte[65535];
                        int nbOctets;

                        while ((nbOctets = entree.read(buffer)) != -1) {
                            byte[] blocChiffre = new byte[nbOctets];
                            System.arraycopy(buffer, 0, blocChiffre, 0, nbOctets);
                            byte[] trame = aes.decryptage(blocChiffre);

                            String reponse = "";

                            if (trame != null && trame.length != 0) {
                                String msgRecu = new String(trame, StandardCharsets.UTF_8).trim();
                                System.out.println("Message recu : " + msgRecu);

                                switch (msgRecu.toUpperCase()) {
                                    case "HELLO" -> reponse = serveurPrefix + "Salut!";
                                    case "TIME" -> reponse = serveurPrefix + "Voici la date et l'heure : " + recuperDateHeure();
                                    case String s when s.trim().startsWith("ECHO") -> reponse = serveurPrefix + "Votre message : " + msgRecu.substring(4).trim();
                                    case "WHOAREYOU?", "YOU" -> reponse = serveurPrefix + "adresse:port : " + InetAddress.getLocalHost() + ":" + PORT;
                                    case "WHOAMI?", "ME" -> reponse = clientPrefix + "adresse:port : " + client.getRemoteSocketAddress().toString().replace("/", "");
                                    case "HELP" -> reponse = String.join(", ", cmd);
                                    case "EXIT", "FIN" -> flag = true;
                                    default -> reponse = serveurPrefix + "Commande invalide";
                                };

                                if (!reponse.isEmpty()) {
                                    sortie.write(aes.cryptage(reponse.getBytes(StandardCharsets.UTF_8)));
                                    sortie.flush();
                                    System.out.println("Message emis : " + reponse);
                                }

                                if (flag) {
                                    break;
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println(DiagnosticException.afficheException(e));
                    } finally {
                        try {
                            client.close();
                        } catch (IOException e) {
                            System.err.println("Erreur fermeture socket client : " + e.getMessage());
                        }
                        System.out.println("Client deconnecte");
                    }
                }).start();

            } catch (IOException e) {
                System.err.println("Erreur lors de l'acceptation d'une connexion TCP : " + e.getMessage());
            }
        }
    }

    private static BigInteger lireNombre(InputStream entree) throws IOException {
        byte[] buffer = new byte[1024];
        int longueur = entree.read(buffer);
        if (longueur > 0) {
            return new BigInteger(Arrays.copyOf(buffer, longueur));
        }

        return null;
    }

    static String recuperDateHeure() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
