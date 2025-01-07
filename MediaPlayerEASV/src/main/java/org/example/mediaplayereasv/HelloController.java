package org.example.mediaplayereasv;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class HelloController {
    @FXML
    private Label connectionStatus;


    @FXML
    public void initialize() {
        checkDatabaseConnection();
    }

    private void checkDatabaseConnection() {
        if (DB.testConnection()) {
            connectionStatus.setText("✅ Database Connected!");
            connectionStatus.setStyle("-fx-text-fill: green;");
        } else {
            connectionStatus.setText("❌ Connection Failed!");
            connectionStatus.setStyle("-fx-text-fill: red;");
        }
    }
}