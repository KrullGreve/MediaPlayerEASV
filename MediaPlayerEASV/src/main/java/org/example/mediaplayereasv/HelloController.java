package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class HelloController {

    @FXML private Label connectionStatus;
    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private TextField tfPlaylistName;

    private MediaPlayer mediaPlayer;
    // Calls to the service classes
    private PlaylistServ playlistService = new PlaylistServ();
    private SongServ songService = new SongServ();


    @FXML
    public void initialize() {
        checkDatabaseConnection(); // Calls the method to check connection - Can be removed eventually

        // Set default playlist on the right Listview
        lvAllPlayLists.setItems(FXCollections.observableArrayList());

        loadPlaylists();
        loadSongs();
    }

    void loadSongs() {
        // Get song names from the database
        var databaseSongs = songService.getAllSongs();

        if (databaseSongs == null || databaseSongs.isEmpty()) {
            System.out.println("No songs found in database.");
            return; // Stop execution if no songs are found
        }

        // Get the Music folder URL
        URL musicFolderUrl = getClass().getResource("/Music");

        if (musicFolderUrl != null) {
            try {
                File musicFolder = Paths.get(musicFolderUrl.toURI()).toFile();

                if (musicFolder.exists() && musicFolder.isDirectory()) {
                    File[] musicFiles = musicFolder.listFiles();

                    if (musicFiles != null) {
                        // Convert music file names into a Set for quick lookup
                        HashSet<String> availableMusicFiles = new HashSet<>();
                        for (File file : musicFiles) {
                            if (file.isFile() && file.getName().endsWith(".mp3")) {
                                availableMusicFiles.add(file.getName()); // Store actual filenames
                            }
                        }

                        // Create a list of songs that exist in both the database and filesystem
                        ArrayList<String> validSongs = new ArrayList<>();
                        for (String song : databaseSongs) {
                            String[] parts = song.split(" - ", 2); // Split by " - ", only once
                            String titleOnly = parts[0].trim(); // Extract only the song title
                            String expectedFileName = titleOnly + ".mp3"; // Expected filename format

                            if (availableMusicFiles.contains(expectedFileName)) {
                                validSongs.add(song); // Add "Title - Artist" to the ListView
                            } else {
                                System.out.println("Skipping song (not found): " + expectedFileName);
                            }
                        }

                        System.out.println("Valid songs being added to ListView: " + validSongs); // Debugging
                        Platform.runLater(() -> {
                            lvCurrentPlayList.setItems(FXCollections.observableArrayList(validSongs));
                            lvCurrentPlayList.refresh();
                        });

                        if (validSongs.isEmpty()) {
                            System.out.println("No valid songs to add to ListView.");
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
        String selectedSong = lvCurrentPlayList.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            try {
                String titleOnly = selectedSong.split(" - ")[0];

                URL songUrl = getClass().getResource("/Music/" + titleOnly + ".mp3");
                if (songUrl != null) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop(); // Stop any currently playing song
                    }
                    mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(songUrl.toURI().toString()));
                    mediaPlayer.play();
                } else {
                    System.out.println("Song file not found: " + titleOnly);
                }
            } catch (URISyntaxException e) {
                System.out.println("Error playing song: " + e.getMessage());
            }
        }
    }

    private void loadPlaylists() {
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
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

    // Helper method to show simple errors as alerts in scene builder instead of the console
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
