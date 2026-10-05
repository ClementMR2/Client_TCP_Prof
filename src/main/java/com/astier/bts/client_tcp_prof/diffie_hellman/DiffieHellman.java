package com.astier.bts.client_tcp_prof.diffie_hellman;

import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.tcp.TCPClient;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class DiffieHellman {

    private BigInteger a, g, p, A, B;
    private int taille;

    private final byte[] byte_B = new byte[65535];

    TCPClient tcp;
    int nb;

    public DiffieHellman(TCPClient tcp, int nb) {
        this.tcp = tcp;
        this.nb = nb;

        a = new BigInteger(nb, new SecureRandom());
        p = BigInteger.probablePrime(nb, new SecureRandom());

        do {
            g = new BigInteger(nb, new SecureRandom());
        } while (g.compareTo(BigInteger.ONE) <= 0 || g.compareTo(p) >= 0);

        A = g.modPow(a, p);

        System.out.println("a = " + a);
        System.out.println("p = " + p);
        System.out.println("g = " + g);
        System.out.println("A = " + A);

        try {
            tcp.outBin.write(g.toByteArray());
            Thread.sleep(100);
            tcp.outBin.write(p.toByteArray());
            Thread.sleep(100);
            tcp.outBin.write(A.toByteArray());
        } catch (IOException | InterruptedException e) {
            System.err.println(DiagnosticException.afficheException(e));
        }
    }

    public byte[] recuperParams() {
        System.out.println("Attente de B");

        try {
            taille = tcp.inBin.read(byte_B);

            if (taille > 0) {
                B = new BigInteger(Arrays.copyOfRange(byte_B, 0, taille));

                System.out.println("Serveur : Clef publique B = " + B);

                BigInteger K = B.modPow(a, p);

                System.out.println("Clef commune K = " + K);

                return MessageDigest.getInstance("SHA-256")
                        .digest(K.toByteArray());
            }

        } catch (Exception e) {
            System.err.println(DiagnosticException.afficheException(e));
        }

        return null;
    }

    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}