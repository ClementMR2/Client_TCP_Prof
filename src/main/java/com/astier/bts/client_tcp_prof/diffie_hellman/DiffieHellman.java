package com.astier.bts.client_tcp_prof.diffie_hellman;

import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.tcp.TCPClient;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class DiffieHellman {

    private BigInteger a, g, p, A, B;

    private final byte[] byte_B = new byte[65535];

    TCPClient tcp;
    int nb;

    public DiffieHellman(TCPClient tcp, int nb) throws IOException {
        this.tcp = tcp;
        this.nb = nb;

        a = new BigInteger(nb, new SecureRandom()); // Clé privée
        p = BigInteger.probablePrime(nb, new SecureRandom()); // Nombre premier

        do {
            g = new BigInteger(nb, new SecureRandom()); // Générateur
        } while (g.compareTo(p) >= 0);

        A = g.modPow(a, p); // Clé publique

        System.out.println("p = " + p);
        System.out.println("g = " + g);
        System.out.println("A = " + A);

        try {
            tcp.outBin.write(p.toByteArray());
            tcp.outBin.flush();

            Thread.sleep(100);

            tcp.outBin.write(g.toByteArray());
            tcp.outBin.flush();

            Thread.sleep(100);

            tcp.outBin.write(A.toByteArray());
            tcp.outBin.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(DiagnosticException.afficheException(e));
        }
    }

    public byte[] recuperParams() throws IOException {
        int size = tcp.inBin.read(byte_B);
        if (size > 0) {
            B = new BigInteger(Arrays.copyOfRange(byte_B, 0, size));

            System.out.println("Serveur : Clef publique B = " + B);
            BigInteger K = B.modPow(a, p);

            return K.toByteArray();
        }
        return null;
    }
}