package com.astier.bts.client_tcp_prof;

import com.astier.bts.client_tcp_prof.exceptions.DiagnosticException;
import com.astier.bts.client_tcp_prof.multicast_diffusion.MulticastDiffusion;
import com.astier.bts.client_tcp_prof.outils.Interfaces;
import com.astier.bts.client_tcp_prof.tcp.TCPClient;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
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
    public ChoiceBox choiceBoxInterfaces;
    static public TCPClient tcp;
    static boolean enRun = false;
    String adresse,port;
    static MulticastDiffusion multicastDiffusion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            getConfig();
            fillChoiceBox();
            connecter.setOnMouseClicked(_ -> this.connecter());
            deconnecter.setOnMouseClicked(_ -> this.deconnecter());
            button.setOnMouseClicked(_ -> this.envoyer());
            TextFieldRequette.setOnAction(_ -> this.envoyer());
            // DAMN
            choiceBoxInterfaces.getSelectionModel().selectedItemProperty().addListener((observable, ancienneValeur, nouvelleValeur) -> {
                setConfig((String) nouvelleValeur);
            });
        } catch (Exception e) {
            DiagnosticException.afficheException(e);
        }

        voyant.setFill(RED);
        connecter.setDisable(false);
        deconnecter.setDisable(true);
    }

    private void fillChoiceBox() {
        try {
            Interfaces.getIps().forEach(ip -> {
                choiceBoxInterfaces.getItems().add(ip.interfaceName());
            });
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void getConfig() {
        try {
            multicastDiffusion = new MulticastDiffusion();
            Thread.sleep(1000);
            TextFieldIP.setText(multicastDiffusion.connexion.addressAsString());
            TextFieldPort.setText(String.valueOf(multicastDiffusion.connexion.portTCP()));
        } catch (Exception ex) {
            System.err.println(DiagnosticException.afficheException(ex));
        }
    }

    private void setConfig(String val) {
        try {
            multicastDiffusion = new MulticastDiffusion();
            Thread.sleep(1000);
            TextFieldIP.setText(multicastDiffusion.connexion.addressAsString());
            TextFieldPort.setText(String.valueOf(multicastDiffusion.connexion.portTCP()));
        } catch (Exception ex) {
            System.err.println(DiagnosticException.afficheException(ex));
        }
    }

    private void envoyer() {
        if (!enRun) return;

        String s = TextFieldRequette.getText();
        if (s.isEmpty()) return;

        if (s.equalsIgnoreCase("exit")) {
            this.deconnecter();
            return;
        }

        tcp.requette(s);
    }

    private void deconnecter() {
        if (!enRun) return;

        tcp.deconnection();
        enRun = false;

        // Nettoyage
        TextAreaReponses.clear();
        TextFieldRequette.setText("");
    }

    private void connecter() {
        if (TextFieldIP.getText().isEmpty() || TextFieldPort.getText().isEmpty() || enRun) return;

        adresse = TextFieldIP.getText();
        port = TextFieldPort.getText();

        try {
            tcp = new TCPClient(InetAddress.getByName(adresse), Integer.parseInt(port), this); // Initialisation
        } catch (Exception e) {
            DiagnosticException.afficheException(e);
        }
        tcp.connection();

        enRun = true;
    }

}