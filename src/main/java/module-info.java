module com.astier.bts.client_tcp_prof {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.net.http;
    requires jdk.jshell;
    requires com.google.gson;
    //requires googleauth;

    opens com.astier.bts.client_tcp_prof to javafx.fxml;
    opens com.astier.bts.client_tcp_prof.modeles to com.google.gson; // Donner l'accès à come.google.gson
    exports com.astier.bts.client_tcp_prof;
}