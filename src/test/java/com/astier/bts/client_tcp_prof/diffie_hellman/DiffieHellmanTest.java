package com.astier.bts.client_tcp_prof.diffie_hellman;

import com.astier.bts.client_tcp_prof.tcp.TCPClient;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiffieHellmanTest {
    @Test
    void echangeDansLOrdreDuPdfEtCalculeLeMemeSecret() throws Exception {
        for (int i = 0; i < 5; i++) {
            TCPClient tcp = new TCPClient(InetAddress.getLoopbackAddress(), 5555, null);
            List<byte[]> envois = new ArrayList<>();
            tcp.outBin = new OutputStream() {
                @Override
                public void write(int b) {
                    fail("Les nombres doivent être envoyés en tableaux d'octets.");
                }

                @Override
                public void write(byte[] bytes) {
                    envois.add(bytes.clone());
                }
            };

            DiffieHellman dh = new DiffieHellman(tcp, 128);
            assertEquals(3, envois.size());
            BigInteger p = new BigInteger(envois.get(0));
            BigInteger g = new BigInteger(envois.get(1));
            BigInteger A = new BigInteger(envois.get(2));
            assertTrue(p.isProbablePrime(100));
            assertEquals(128, p.bitLength());
            assertTrue(g.compareTo(BigInteger.ONE) > 0 && g.compareTo(p) < 0);
            assertTrue(A.signum() > 0 && A.compareTo(p) < 0);

            BigInteger b = BigInteger.valueOf(123456789);
            BigInteger B = g.modPow(b, p);
            tcp.inBin = new ByteArrayInputStream(B.toByteArray());
            assertArrayEquals(A.modPow(b, p).toByteArray(), dh.recuperParams());

            for (BigInteger invalide : List.of(BigInteger.ZERO, BigInteger.valueOf(-1), p)) {
                tcp.inBin = new ByteArrayInputStream(invalide.toByteArray());
                assertThrows(IOException.class, dh::recuperParams);
            }
            tcp.inBin = new ByteArrayInputStream(new byte[0]);
            assertThrows(IOException.class, dh::recuperParams);
            tcp.inBin = new ByteArrayInputStream(new byte[18]);
            assertThrows(IOException.class, dh::recuperParams);
        }
    }
}
