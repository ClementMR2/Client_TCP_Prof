package com.astier.bts.client_tcp_prof.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UDPServer {
    private static final int PORT = 5000;
    private static final int TAILLE_MAXIMALE = 512;

    static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Serveur UDP en fonctionnement sur le port " + PORT + ".");

            while (true) {
                byte[] buffer = new byte[TAILLE_MAXIMALE];
                DatagramPacket paquetRecu = new DatagramPacket(buffer, buffer.length);
                socket.receive(paquetRecu);

                // Lecture directe des octets réellement reçus.
                byte[] octetsRecus = Arrays.copyOfRange(
                        paquetRecu.getData(),
                        paquetRecu.getOffset(),
                        paquetRecu.getOffset() + paquetRecu.getLength());

                System.out.println("Octets reçus : " + Arrays.toString(octetsRecus));
                System.out.println("Client : " + paquetRecu.getAddress()
                        + ":" + paquetRecu.getPort());

                // Le décodage est nécessaire uniquement pour transformer le texte.
                String requete = new String(octetsRecus, StandardCharsets.UTF_8);
                String texteMajuscule = requete.toUpperCase();
                byte[] octetsReponse = texteMajuscule.getBytes(StandardCharsets.UTF_8);

                DatagramPacket paquetReponse = new DatagramPacket(
                        octetsReponse,
                        octetsReponse.length,
                        paquetRecu.getAddress(),
                        paquetRecu.getPort());
                socket.send(paquetReponse);
            }
        } catch (Exception e) {
            System.err.println("Erreur du serveur UDP : " + e.getMessage());
        }
    }
}
