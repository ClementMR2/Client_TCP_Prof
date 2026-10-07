package com.astier.bts.client_tcp_prof.totp;

import java.util.Timer;
import java.util.TimerTask;

public class Totp {
    public static String cleTotp = "IIW6JG7FYVRBF5S47QZCDYFHPVBLAYAW"; // Code authgear
    //public static GoogleAuthenticator gAuth = new GoogleAuthenticator();;

    public static boolean testTotp(int code) {
        //return gAuth.getTotpPassword(cleTotp) == code;
        return true;
    }
}

