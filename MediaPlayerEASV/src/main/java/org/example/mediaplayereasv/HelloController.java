package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;




public class HelloController {

    @FXML private Label connectionStatus;
    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private TextField tfPlaylistName;
    @FXML private ImageView ivMainImage;
    @FXML private Button btnAddPlaylist, btnDeletePlaylist, btnConfirmPlaylist, btnCancelPlaylist;

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

    // Loads songs from the database and matches them with the music files
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

            // Load a random image from the Images folder
            try {
                File imagesFolder = new File(getClass().getResource("/Images").toURI());

                if (imagesFolder.exists() && imagesFolder.isDirectory()) {
                    File[] imageFiles = imagesFolder.listFiles(file -> file.isFile() && isImageFile(file.getName()));

                    if (imageFiles != null && imageFiles.length > 0) {
                        // Select a random image
                        File randomImage = imageFiles[(int) (Math.random() * imageFiles.length)];

                        // Load the random image
                        String imagePath = randomImage.toURI().toString();
                        ivMainImage.setImage(new javafx.scene.image.Image(imagePath));
                    } else {
                        System.err.println("No image files found in the Images folder. Using placeholder.");
                        ivMainImage.setImage(new javafx.scene.image.Image("https://via.placeholder.com/500"));
                    }
                } else {
                    System.err.println("Images folder not found. Using placeholder.");
                    ivMainImage.setImage(new javafx.scene.image.Image("https://via.placeholder.com/500"));
                }
            } catch (Exception e) {
                System.err.println("Error loading random image: " + e.getMessage());
                ivMainImage.setImage(new javafx.scene.image.Image("https://via.placeholder.com/500"));
            }

            ivMainImage.setFitWidth(300); // Set to desired width
            ivMainImage.setFitHeight(200); // Set to desired height

            // Preserve the aspect ratio
            ivMainImage.setPreserveRatio(true);
        }
    }

    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpeg");
    }

    // Loads our playlists into the listview
    private void loadPlaylists() {
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    @FXML
    private void onAddPlaylist()
    {
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

    @FXML
    private void onAddPlaylistClicked()
    {
        // Hides the plus and minus buttons
        btnAddPlaylist.setVisible(false);
        btnDeletePlaylist.setVisible(false);

        // Show text field and confirm/cancel buttons
        tfPlaylistName.setVisible(true);
        btnConfirmPlaylist.setVisible(true);
        btnCancelPlaylist.setVisible(true);

        // Clears the text field
        tfPlaylistName.clear();
    }

    @FXML
    private void onConfirmPlaylist()
    {
        String playlistName = tfPlaylistName.getText().trim();

        if (playlistName.isEmpty())
        {
            showAlert("Error", "Playlist name is empty!");
            return;
        }

        if (playlistService.getAllPlaylists().contains(playlistName))
        {
            showAlert("Error", "Playlist already exists!");
        }

        else
        {
            if (playlistService.createPlaylist(playlistName))
            {
                showAlert("Success", "Playlist created successfully: " + playlistName);
                lvAllPlayLists.getItems().add(playlistName);
            }
            else
            {
                showAlert("Error", "Failed to create playlist!");
            }
        }

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
