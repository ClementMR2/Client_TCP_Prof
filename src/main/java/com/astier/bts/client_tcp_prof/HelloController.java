package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.OUTILS.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.multicast_diffusion.MulticastDiffusion;
import com.astier.bts.client_tcp_prof.tcp.TCPClient;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import static javafx.scene.paint.Color.*;

public class HelloController implements Initializable {
    public Button button;
    public Button connecter;
    public Button deconnecter;
    public TextField TextFieldIP;
    public TextField TextFieldPort;
    public TextField TextFieldRequette;
    public Circle voyant;
    public TextArea TextAreaReponses;
    static public TCPClient tcp;
    static boolean enRun = false;
    String adresse,port;
    static MulticastDiffusion multicastDiffusion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getMulticastConfig();

        connecter.setOnMouseClicked(e -> {
            try {
                this.connecter();
            } catch (IOException ex) {
                System.err.println(DiagnosticException.afficheException(ex));
            }
        });

        deconnecter.setOnMouseClicked(e -> {
            try {
                this.deconnecter();
            } catch (InterruptedException ex) {
                System.err.println(DiagnosticException.afficheException(ex));
            }
        });

        button.setOnMouseClicked(e -> {
            try {
                this.envoyer();
            } catch (InterruptedException ex) {
                System.err.println(DiagnosticException.afficheException(ex));
            }
        });

        voyant.setFill(RED);
        connecter.setDisable(false);
        deconnecter.setDisable(true);
    }

    private void getMulticastConfig() {
        try {
            multicastDiffusion = new MulticastDiffusion();
            Thread.sleep(1000);
            TextFieldIP.setText(multicastDiffusion.connexion.addressAsString());
            TextFieldPort.setText(String.valueOf(multicastDiffusion.connexion.portTCP()));
        } catch (Exception ex) {
            System.err.println(DiagnosticException.afficheException(ex));
        }
    }

    private void envoyer() throws InterruptedException {
        if (!enRun) return;

        String s = TextFieldRequette.getText();
        if (s.isEmpty()) return;

        if (s.equalsIgnoreCase("exit")) this.deconnecter();

        tcp.requette(s);
    }

    private void deconnecter() throws InterruptedException {
        if (!enRun) return;

        tcp.deconnection();
        enRun = false;

        // Nettoyage
        TextAreaReponses.clear();
        TextFieldRequette.setText("");
    }

    private void connecter() throws IOException {
        if (TextFieldIP.getText().isEmpty() || TextFieldPort.getText().isEmpty() || enRun) return;

        adresse = TextFieldIP.getText();
        port = TextFieldPort.getText();

        tcp = new TCPClient(InetAddress.getByName(adresse), Integer.parseInt(port), this); // Initialisation
        tcp.connection();

        enRun = true;
    }

}