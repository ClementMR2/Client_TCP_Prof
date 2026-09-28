package com.astier.bts.client_tcp_prof.modeles;

import com.astier.bts.client_tcp_prof.OUTILS.aes.Outils;
import com.google.gson.annotations.SerializedName;

public record ConfigAES(@SerializedName("motDePasse") String mdp, String iv) {
    public byte[] passwordAsBytes() {
        return Outils.normalizeChaine(mdp, 16);
    }

    public byte[] ivAsBytes() {
        return Outils.normalizeChaine(iv, 16);
    }
}
