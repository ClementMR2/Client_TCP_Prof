package com.astier.bts.client_tcp_prof.modeles;

import java.net.InetAddress;

public record Connexion(InetAddress adresseServeur, int portTCP, int portUDP) {
    public String addressAsString() {
        return String.valueOf(adresseServeur).replace("/", "");
    }
}
