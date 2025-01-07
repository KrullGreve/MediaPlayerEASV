package org.example.mediaplayereasv;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloController {
    private MediaPlayer mediaPlayer;

    @FXML
    private void onHelloButtonClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        // Set the initial directory
        fileChooser.setInitialDirectory(new File("C:\\Users\\mikco\\Desktop\\Skole\\Kode folder\\MediaPlayerEASV\\MediaPlayerEASV\\src\\main\\java\\Music"));

        // Show the file chooser dialog
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            // Initialize and play the MediaPlayer
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(selectedFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        } else {
            System.out.println("File selection cancelled.");
        }
    }
}