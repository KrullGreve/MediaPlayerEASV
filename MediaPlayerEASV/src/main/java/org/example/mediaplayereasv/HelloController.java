package org.example.mediaplayereasv;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void initialize(URL location, ResourceBundle resources)
    {
        File mediaFolder = new File("src/main/resources/media");
    }
}