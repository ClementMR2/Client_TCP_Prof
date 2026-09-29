package com.astier.bts.client_tcp_prof.outils;

import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.modeles.Ipv4;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;

public class Interfaces {
    public static ArrayList<Ipv4> getIps() throws SocketException {
        ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        ArrayList<Ipv4> ipv4 = new ArrayList<>();

        interfaces.forEach(networkInterface -> {
            try {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Collections.list(networkInterface.getInetAddresses()).forEach(currentInetAddress -> {
                        ipv4.add(new Ipv4(networkInterface.getDisplayName(), networkInterface.getName(), currentInetAddress.getHostAddress()));
                    });
                }
            } catch (SocketException e) {
                System.err.println(DiagnosticException.afficheException(e));
            }
        });

        return ipv4;
    }
}
