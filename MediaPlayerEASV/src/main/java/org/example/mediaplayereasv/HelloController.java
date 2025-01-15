package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;


public class HelloController {

    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private TextField tfPlaylistName;
    @FXML private ImageView ivMainImage;
    @FXML private Button btnAddPlaylist, btnDeletePlaylist, btnConfirmPlaylist, btnCancelPlaylist;
    @FXML private Slider mySliderDuration;

    private MediaPlayer mediaPlayer;

    // Calls to the service classes
    private SongServ songService = new SongServ();
    private PlaylistServ playlistService = new PlaylistServ();

    // Stores the Deletion of playlists
    private String pendingDeletePlaylist = null;
    private final DoubleProperty currentTimeProperty = new SimpleDoubleProperty();


    @FXML
    public void initialize() {

        // Set default playlist on the right Listview
        lvAllPlayLists.setItems(FXCollections.observableArrayList("All Songs"));

        if(DB.testConnection()) {
            System.out.println("Connected to DB");
            loadPlaylists();
            loadSongs();
        }else{
            System.out.println("offline connection");
            OfflineloadMusicFiles();

        }

    }
    @FXML
    private void onPausePlay() {
        if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }else{
            mediaPlayer.play();
        }
    }
    @FXML
    private void onStopPlay() {
        if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }
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
    private void OfflineloadMusicFiles() {
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
                                lvCurrentPlayList.getItems().add(file.getName());
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
        String selectedSong = lvCurrentPlayList.getSelectionModel().getSelectedItem();
        if (selectedSong != null)
        {
            try {
                String titleOnly = selectedSong.split(" - ")[0];
                URL songUrl = getClass().getResource("/Music/" + titleOnly +".mp3");
                URL offlineSongUrl = getClass().getResource("/Music/" + titleOnly);

                if (songUrl != null || offlineSongUrl != null) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop(); // Stop any currently playing song
                    }
                    if(offlineSongUrl != null) {
                        mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(offlineSongUrl.toURI().toString()));

                    }if(songUrl != null) {
                        mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(songUrl.toURI().toString()));

                    }
                    if(!mediaPlayer.isAutoPlay()) {
                        mediaPlayer.setAutoPlay(true);
                    }
                    mediaPlayer.play();
                } else {
                    System.out.println("Song file not found: " + titleOnly);
                }
            } catch (URISyntaxException e) {
                System.out.println("Error playing song: " + e.getMessage());
            }


        }
        ImageLoader();
    }

    private void ImageLoader() {
        // Load a random image from the Images folder
        try {
            File imagesFolder = new File(Objects.requireNonNull(getClass().getResource("/Images")).toURI());

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

    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpeg");
    }

    // Loads our playlists into the listview
    private void loadPlaylists() {
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    /**
     * Handles the action when the "Add Playlist" button is clicked.
     * This method updates the user interface to initiate the process of creating a new playlist.
     * It hides the "Add" and "Delete" playlist buttons and displays the text field for entering
     * the playlist name along with the "Confirm" and "Cancel" buttons. The text field is also
     * cleared to ensure no pre-existing content is displayed.
     * Key UI Changes:
     * - Hides the "Add Playlist" button.
     * - Hides the "Delete Playlist" button.
     * - Displays the playlist name text field.
     * - Displays the "Confirm" button.
     * - Displays the "Cancel" button.
     * - Clears the content of the playlist name text field.
     * This method prepares the UI for the user to input the name for a new playlist.
     */
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


    /**
     * Resets the playlist-related UI components to their default state,
     * like showing the + and - buttons only and the confirm/cancel buttons and the text field
     * This method is typically called after a playlist-related operation (adding or deleting),
     * to reset the UI for further interactions.
     */
    private void resetPlaylistUI()
    {
        btnAddPlaylist.setVisible(true);
        btnDeletePlaylist.setVisible(true);

        tfPlaylistName.setVisible(false);
        btnConfirmPlaylist.setVisible(false);
        btnCancelPlaylist.setVisible(false);

        btnConfirmPlaylist.setText("✔");

        tfPlaylistName.clear();
    }

    /**
     * Handles the action for deleting a selected playlist when the delete button is clicked.
     * This method retrieves the selected playlist from the ListView (lvAllPlayLists) and sets it as
     * the pending playlist to delete.
     * If no playlist is selected, an error alert is displayed to notify the user.
     * If a playlist is selected, it updates the UI by hiding the "Add" and "Delete" buttons and
     * displaying the "Confirm" and "Cancel" buttons.
     */
    @FXML
    private void onDeletePlaylistClicked() {
        pendingDeletePlaylist = lvAllPlayLists.getSelectionModel().getSelectedItem();

        if (pendingDeletePlaylist == null) {
            showAlert("Error", "No playlist selected. Please select one.");
            return;
        }

        btnAddPlaylist.setVisible(false);
        btnDeletePlaylist.setVisible(false);

        btnConfirmPlaylist.setText("✅");  // Set confirm button to delete mode
        btnConfirmPlaylist.setVisible(true);
        btnCancelPlaylist.setVisible(true);
    }


    /**
     * Handles the action of the confirmation button.
     * This method checks if the symbol is equal to the one used for deleting or for adding,
     * If it is associated with deleting, it deletes, if not it adds a playlist.
     * Displays an error if you haven't selected a playlist
     * Makes it so we can reuse the same button for multiple functions.
     */
    @FXML
    private void onConfirmPlaylist() {
        if ("✅".equals(btnConfirmPlaylist.getText())) {
            deleteSelectedPlaylist();
        } else {
            String playlistName = tfPlaylistName.getText().trim();
            if (!playlistName.isEmpty()) {
                playlistService.createPlaylist(playlistName); // Pass the playlist name
                refreshPlaylists();
            } else {
                showAlert("Error", "Playlist name is empty!");
            }
        }
        resetPlaylistUI();
    }

    @FXML
    private void deleteSelectedPlaylist() {
        if (pendingDeletePlaylist == null) {
            showAlert("Error", "No playlist selected. Please select a playlist to delete.");
            return;
        }

        boolean isDeleted = playlistService.deletePlaylist(pendingDeletePlaylist);
        if (isDeleted) {
            showAlert("Success", "Playlist '" + pendingDeletePlaylist + "' was deleted successfully.");
            refreshPlaylists();
        } else {
            showAlert("Error", "Failed to delete the selected playlist. Please try again.");
        }

        pendingDeletePlaylist = null;  // Reset the stored playlist
    }

    @FXML
    private void onCancelPlaylist()
    {
        resetPlaylistUI();
    }


    // Refreshes the playlist, so the deleted or added playlists are shown correctly
    private void refreshPlaylists() {
        // Reload the playlists from the database after deletion
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
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
