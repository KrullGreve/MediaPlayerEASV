package org.example.mediaplayereasv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloController {

    @FXML private Label connectionStatus;
    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private Button btnCreatePlaylist;
    @FXML private TextField tfPlaylistName;

    private MediaPlayer mediaPlayer;
    // Calls to the service classes
    private PlaylistServ playlistService = new PlaylistServ();
    private SongServ songService = new SongServ();


    @FXML
    public void initialize() {
        checkDatabaseConnection(); // Calls the method to check connection - Can be removed eventually

        // Set default playlist on the right Listview
        lvAllPlayLists.setItems(FXCollections.observableArrayList("All Songs"));

        loadPlaylists();
        loadAllSongs();
    }

    private void loadPlaylists() {
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    private void loadAllSongs() {
        lvCurrentPlayList.setItems(FXCollections.observableArrayList(songService.getAllSongs()));
    }

    @FXML
    private void createPlaylist() {
        String playlistName = tfPlaylistName.getText().trim();
        if (playlistName.isEmpty()) {
            System.out.println("Playlist name is empty!");
            return;
        }

        if (playlistService.createPlaylist(playlistName)) {
            System.out.println("Playlist created successfully: " + playlistName);
            lvAllPlayLists.getItems().add(playlistName);
        } else {
            System.out.println("Error creating playlist!");
        }
        tfPlaylistName.clear();
    }

    // Handles the playlist selected on the left Listview and shows the songs on the right Listview
    @FXML
    private void handlePlaylistSelected() {
        String selectedPlaylist = lvAllPlayLists.getSelectionModel().getSelectedItem();
        if (selectedPlaylist != null) {
            System.out.println("Selected playlist: " + selectedPlaylist);
        }
    }


    @FXML
    private void deleteSelectedPlaylist() {
        // Get the selected playlist from the ListView
        String selectedPlaylist = lvAllPlayLists.getSelectionModel().getSelectedItem();

        // Ensure a playlist is selected
        if (selectedPlaylist == null) {
            showAlert("Error", "No playlist selected. Please select a playlist to delete.");
            return;
        }

        // Try to delete the selected playlist
        boolean isDeleted = playlistService.deletePlaylist(selectedPlaylist);

        // Check the result and provide feedback to the user
        if (isDeleted) {
            showAlert("Success", "Playlist '" + selectedPlaylist + "' was deleted successfully.");
            refreshPlaylists(); // Calls refresh method
        } else {
            showAlert("Error", "Failed to delete the selected playlist. Please try again.");
        }
    }

    // Refreshes the playlist, so the deleted or added playlists are shown correctly
    private void refreshPlaylists() {
        // Reload the playlists from the database after deletion
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    // Checks if you are connected to the database - Will be removed later
    private void checkDatabaseConnection() {
        if (DB.testConnection()) {
            connectionStatus.setText("✅ Database Connected!");
            connectionStatus.setStyle("-fx-text-fill: green;");
        } else {
            connectionStatus.setText("❌ Connection Failed!");
            connectionStatus.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void musicFinder(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Music File");

        File musicFolder = new File("C:/temp/");
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            fileChooser.setInitialDirectory(musicFolder);
        }

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(selectedFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    // Helper method to show simple errors as alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
