package com.astier.bts.client_tcp_prof.tcp;

import com.astier.bts.client_tcp_prof.aes.Aes_cbc;
import com.astier.bts.client_tcp_prof.configuration.LectureJson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TCPClientTest {
    @Test
    void commandeChiffreeEtFermetureDuServeur() throws Exception {
        try (ServerSocket serveur = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            FutureTask<Void> echange = new FutureTask<>(() -> {
                try (Socket socket = serveur.accept()) {
                    socket.setSoTimeout(3000);
                    BigInteger p = lireNombre(socket.getInputStream());
                    BigInteger g = lireNombre(socket.getInputStream());
                    BigInteger A = lireNombre(socket.getInputStream());
                    assertTrue(p.isProbablePrime(100));
                    assertTrue(g.compareTo(BigInteger.ONE) > 0 && g.compareTo(p) < 0);

                    BigInteger b = BigInteger.valueOf(123456789);
                    BigInteger K = A.modPow(b, p);
                    for (int essai = 0; K.bitLength() != 128 && essai < 10000; essai++) {
                        b = b.add(BigInteger.ONE);
                        K = A.modPow(b, p);
                    }
                    byte[] cle = K.toByteArray();
                    assertEquals(17, cle.length);
                    Aes_cbc aes = new Aes_cbc(Arrays.copyOfRange(cle, 1, 17), LectureJson.getConfigAES().ivAsBytes());
                    socket.getOutputStream().write(g.modPow(b, p).toByteArray());
                    socket.getOutputStream().flush();

                    byte[] requete = socket.getInputStream().readNBytes(16);
                    assertEquals("HELLO\n", new String(aes.decryptage(requete), StandardCharsets.UTF_8));
                    socket.getOutputStream().write(aes.cryptage("Salut élève !".getBytes(StandardCharsets.UTF_8)));
                    socket.getOutputStream().flush();
                }
                return null;
            });
            Thread pair = new Thread(echange);
            pair.setDaemon(true);
            pair.start();

            ClientControle client = new ClientControle(serveur.getLocalPort());
            try {
                client.connection();
                assertTrue(client.connecte.await(2, TimeUnit.SECONDS));
                client.requette("HELLO");
                assertEquals("Échange Diffie-Hellman terminé.", client.messages.poll(2, TimeUnit.SECONDS));
                assertEquals("Salut élève !", client.messages.poll(2, TimeUnit.SECONDS));
                assertEquals("Le serveur a fermé la connexion.", client.messages.poll(2, TimeUnit.SECONDS));
                client.join(2000);
                assertFalse(client.isAlive());
                assertFalse(client.etatConnecte);
                echange.get(3, TimeUnit.SECONDS);
            } finally {
                client.deconnection();
            }
        }
    }

    @Test
    void fermetureAvantBNeDemarrePasLeClient() throws Exception {
        try (ServerSocket serveur = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            FutureTask<Void> echange = new FutureTask<>(() -> {
                try (Socket socket = serveur.accept()) {
                    socket.setSoTimeout(3000);
                    for (int i = 0; i < 3; i++) {
                        lireNombre(socket.getInputStream());
                    }
                }
                return null;
            });
            Thread pair = new Thread(echange);
            pair.setDaemon(true);
            pair.start();
            ClientControle client = new ClientControle(serveur.getLocalPort());
            client.connection();
            assertFalse(client.etatConnecte);
            assertEquals(Thread.State.NEW, client.getState());
            assertNotNull(client.messages.poll(2, TimeUnit.SECONDS));
            echange.get(3, TimeUnit.SECONDS);
        }
    }

    @Test
    void connexionRefuseeNeContinuePasVersDiffieHellman() throws Exception {
        int port;
        try (ServerSocket serveur = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = serveur.getLocalPort();
        }
        ClientControle client = new ClientControle(port);
        client.connection();
        assertFalse(client.etatConnecte);
        assertEquals(Thread.State.NEW, client.getState());
        assertNull(client.inBin);
        assertNull(client.outBin);
        assertNotNull(client.messages.poll(2, TimeUnit.SECONDS));
    }

    private static BigInteger lireNombre(InputStream entree) throws IOException {
        byte[] buffer = new byte[65535];
        int longueur = entree.read(buffer);
        if (longueur < 1) {
            throw new IOException("Nombre Diffie-Hellman manquant.");
        }
        return new BigInteger(Arrays.copyOf(buffer, longueur));
    }

    private static class ClientControle extends TCPClient {
        final BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        final CountDownLatch connecte = new CountDownLatch(1);
        volatile boolean etatConnecte;

        ClientControle(int port) {
            super(InetAddress.getLoopbackAddress(), port, null);
        }

        @Override
        protected void updateConnection(boolean connected) {
            etatConnecte = connected;
            if (connected) {
                connecte.countDown();
            }
        }

        @Override
        protected void updateMessage(String message) {
            messages.add(message);
        }
    }
}
