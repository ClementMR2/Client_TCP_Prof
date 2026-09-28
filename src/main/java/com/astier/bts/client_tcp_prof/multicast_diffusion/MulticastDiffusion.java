package com.astier.bts.client_tcp_prof.multicast_diffusion;

import com.astier.bts.client_tcp_prof.OUTILS.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.modeles.Connexion;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MulticastDiffusion {
    private final String MON_INTERFACE = "ethernet_32769";
    private InetAddress ip = InetAddress.getByName("10.0.4.10");
    private byte [] data = "Tu es qui?".getBytes();
    private int port = 5555;
    private int portReponse = 5556;
    private byte ttl = 60;
    private byte [] bufferReponse = new byte[27];
    private DatagramPacket dp;
    private MulticastSocket ms;
    private DatagramSocket dsReponse;
    public Connexion connexion;

    public MulticastDiffusion() throws IOException {
        ms = new MulticastSocket();
        NetworkInterface ni = NetworkInterface.getByName(MON_INTERFACE);
        ms.setNetworkInterface(ni);
        ms.setTimeToLive(ttl);
        dp = new DatagramPacket(data, data.length, ip, port);
        dsReponse = new DatagramSocket(portReponse);
        ms.send(dp);

        new Thread(() -> {
            dp = new DatagramPacket(bufferReponse, bufferReponse.length);
            System.out.println("Attente");
            try {
                dsReponse.receive(dp);
            } catch (IOException e) {
                System.err.println(DiagnosticException.afficheException(e));
            }
            String reponseServeur = new String(dp.getData(), 0, dp.getLength());
            String [] reponseSplitted = reponseServeur.split(";");

            try {
                connexion = new Connexion(InetAddress.getByName(reponseSplitted[0]), Integer.parseInt(reponseSplitted[1]), Integer.parseInt(reponseSplitted[2]));
                //connexion = new Connexion(InetAddress.getByName("10.0.4.10"), 1000, 2000);
            } catch (UnknownHostException e) {
                System.err.println(DiagnosticException.afficheException(e));
            }
        }).start();
    }
}
