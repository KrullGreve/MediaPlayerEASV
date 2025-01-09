package org.example.mediaplayereasv;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class songSelection {
    @FXML
    private ListView<String> musicListView;

    @FXML
    public void onHelloButtonClick() {
        StringBuilder message = new StringBuilder();

        URL musicFolderUrl = getClass().getResource("/Music");
        if (musicFolderUrl != null) {
            try {
                File musicFolder = Paths.get(musicFolderUrl.toURI()).toFile();

                if (musicFolder.exists() && musicFolder.isDirectory()) {
                    File[] musicFiles = musicFolder.listFiles();

                    if (musicFiles != null && musicFiles.length > 0) {
                        for (File file : musicFiles) {
                            if (file.isFile() && file.getName().endsWith(".mp3")) {
                                message.append("Found: ").append(file.getName()).append("\n");
                            }
                        }
                    } else {
                        message.append("No music files found.");
                    }
                } else {
                    message.append("Music folder not found.");
                }
            } catch (URISyntaxException e) {
                message.append("Error accessing Music folder: ").append(e.getMessage());
            }
        } else {
            message.append("Music folder not found.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Music Files");
        alert.setHeaderText("Music Files Found");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    private void loadMusicFiles() {
        URL musicFolderUrl = getClass().getResource("/Music");
        if (musicFolderUrl != null) {
            try {
                File musicFolder = Paths.get(musicFolderUrl.toURI()).toFile();

                if (musicFolder.exists() && musicFolder.isDirectory()) {
                    File[] musicFiles = musicFolder.listFiles();

                    if (musicFiles != null) {
                        for (File file : musicFiles) {
                            if (file.isFile() && file.getName().endsWith(".mp3")) {
                                // Add file names to the ListView
                                musicListView.getItems().add(file.getName());
                            }
                        }
                    }
                }
            } catch (URISyntaxException e) {
                System.out.println("Error loading music files: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onSongSelected(MouseEvent event) {
        String selectedSong = musicListView.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Song Selected");
            alert.setHeaderText("You selected:");
            alert.setContentText(selectedSong);
            alert.showAndWait();
            MediaPlayer mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(getClass().getResource("/Music/" + selectedSong).toString()));
            mediaPlayer.play();
        }
    }
}


