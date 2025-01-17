package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;


public class HelloController {

    private double previousVolume = 0.5; // Stores last volume before mute
    private final String MUTE_IMAGE = "/bntImages/bntMute.png";  // Path to mute icon
    private final String UNMUTE_IMAGE = "/bntImages/sVolume.png";  // Path to unmute icon

    @FXML private ImageView bntMuteIcon;
    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private TextField tfPlaylistName;
    @FXML private ImageView ivMainImage;
    @FXML private Label myDuration, songDisplay;
    @FXML private ComboBox<String> cbSearchBar;
    @FXML private Button btnAddPlaylist, btnDeletePlaylist, btnConfirmPlaylist, btnCancelPlaylist, bntMute;
    @FXML private Slider mySliderDuration, mySliderVolume;

    private MediaPlayer mediaPlayer;

    // Calls to the service classes
    private SongServ songService = new SongServ();
    private PlaylistServ playlistService = new PlaylistServ();

    // Stores the Deletion of playlists
    private String pendingDeletePlaylist = null;



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
        if(mediaPlayer != null)
        {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }
    @FXML
    private void onStopPlay() {
        if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }
    }
    @FXML
    private void onRepeatEnable(){
        if(mediaPlayer.isAutoPlay()){
            mediaPlayer.setAutoPlay(false);
            mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.stop());
        }else {
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });
        }

    }
    @FXML
    private void nextSong(){
        lvCurrentPlayList.getSelectionModel().select(lvCurrentPlayList.getSelectionModel().getSelectedIndex()+1);
        System.out.println(lvCurrentPlayList.getSelectionModel().getSelectedItem());
        checkCurrentSong();
        imageLoader();
        songNameDisplay();
        String titleOnly = lvCurrentPlayList.getSelectionModel().getSelectedItem().split(" - ")[0];
        durationAdder(titleOnly);

    }
    @FXML
    private void previousSong(){
        if(lvCurrentPlayList.getSelectionModel().getSelectedIndex() > 0){
            lvCurrentPlayList.getSelectionModel().select(lvCurrentPlayList.getSelectionModel().getSelectedIndex()-1);
            checkCurrentSong();
            imageLoader();
            songNameDisplay();
            String titleOnly = lvCurrentPlayList.getSelectionModel().getSelectedItem().split(" - ")[0];
            durationAdder(titleOnly);
        }

    }



    private void checkCurrentSong() {
        if(mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
            try {
                String nextTitleOnly = lvCurrentPlayList.getSelectionModel().getSelectedItem().split(" - ")[0];
                URL nextURL = getClass().getResource("/Music/" + nextTitleOnly + ".mp3");
                if (nextURL != null) {
                    mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(nextURL.toURI().toString()));
                }
                mediaPlayer.play();
            }catch (URISyntaxException e){
                System.out.println("Error loading music files: " + e.getMessage());
            }
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
                            setCbSearchBar();

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
        URL offlineMusicFolderUrl = getClass().getResource("/Music");
        if (offlineMusicFolderUrl != null) {
            try {
                File musicFolder = Paths.get(offlineMusicFolderUrl.toURI()).toFile();

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
    public void onSongSelected(MouseEvent ignoredEvent) {
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
                        songNameDisplay();

                    }if(songUrl != null) {
                        mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(songUrl.toURI().toString()));
                        songNameDisplay();
                    }
                    durationAdder(titleOnly);

                    mediaPlayer.play();
                } else {
                    System.out.println("Song file not found: " + titleOnly);
                }
            } catch (URISyntaxException e) {
                System.out.println("Error playing song: " + e.getMessage());
            }


        }
        imageLoader();
    }

    private void durationAdder(String titleOnly) {
        String durationStr = songService.getSongDuration(titleOnly);
        int totalSeconds = parseDuration(durationStr);
        Duration totalDuration = Duration.seconds(totalSeconds);

        mediaPlayer.setOnReady(() -> {
            mySliderDuration.setMax(totalDuration.toSeconds()); // Set max slider value
            updateDurationLabel(Duration.ZERO, totalDuration);
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!mySliderDuration.isValueChanging()) {
                mySliderDuration.setValue(newTime.toSeconds());
            }
            updateDurationLabel(newTime, totalDuration);
        });
    }

    private void imageLoader() {
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

        ivMainImage.setFitWidth(500); // Set to desired width
        ivMainImage.setFitHeight(400); // Set to desired height

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
    private void songNameDisplay(){
        songDisplay.setText(lvCurrentPlayList.getSelectionModel().getSelectedItem());
    }
    private void setCbSearchBar(){
        cbSearchBar.setEditable(true);
        cbSearchBar.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (!cbSearchBar.isShowing()) {
                cbSearchBar.show(); // Show the dropdown if it's not already visible
            }
            ObservableList<String> filteredItems =
                    FXCollections.observableArrayList(lvCurrentPlayList.getSelectionModel().getSelectedItems());
            for (String item : lvCurrentPlayList.getItems()) {
                if (item.toLowerCase().contains(newText.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            cbSearchBar.setItems(filteredItems);

            // Set the user's input text in the editor
            cbSearchBar.getEditor().setText(newText);
            cbSearchBar.getEditor().end();// Move the caret to the end of the text
            String searchBarResult = cbSearchBar.getSelectionModel().getSelectedItem();
            lvCurrentPlayList.getSelectionModel().select(searchBarResult);


            try {


            if (searchBarResult != null) {

                String sbResult = lvCurrentPlayList.getSelectionModel().getSelectedItem();
                String titleOnly = sbResult.split(" - ")[0];
                URL searchBarURl = getClass().getResource("/Music/" + titleOnly + ".mp3");
                if (searchBarURl != null) {
                    mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(searchBarURl.toURI().toString()));
                    songNameDisplay();
                    imageLoader();
                }
                if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.stop();
                }else{
                    durationAdder(titleOnly);
                    mediaPlayer.play();
                }

                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    private void shufflePlaylist() {
        ObservableList<String> ShuffledPlaylists = FXCollections.observableArrayList(lvCurrentPlayList.getItems());
        Collections.shuffle(ShuffledPlaylists);
        lvCurrentPlayList.setItems(ShuffledPlaylists);
        lvCurrentPlayList.refresh();

    }




    // Helper method to show simple errors as alerts in scene builder instead of the console
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onSliderDragged() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(mySliderDuration.getValue()));
        }
    }

    @FXML
    private void updateDurationLabel(Duration current, Duration total) {
        String currentTime = formatDuration(current);
        String totalTime = formatDuration(total);
        myDuration.setText(currentTime + " / " + totalTime);
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Convert "MM:SS" from database to total seconds
    private int parseDuration(String durationStr) {
        try {
            String[] parts = durationStr.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (minutes * 60) + seconds;
        } catch (Exception e) {
            return 0; // Default to 0 if parsing fails
        }
    }

    @FXML
    private void onMuteToggle() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isMute()) {
                // Unmute: Restore previous volume
                mediaPlayer.setMute(false);
                mediaPlayer.setVolume(previousVolume);
                mySliderVolume.setValue(previousVolume * 100);
                updateMuteIcon(false);
            } else {
                // Mute: Store current volume and set to 0
                previousVolume = mediaPlayer.getVolume();
                mediaPlayer.setMute(true);
                mediaPlayer.setVolume(0);
                mySliderVolume.setValue(0);
                updateMuteIcon(true);
            }
        }
    }

    private void updateMuteIcon(boolean isMuted) {
        String imagePath = isMuted ? MUTE_IMAGE : UNMUTE_IMAGE;
        bntMuteIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
    }

    @FXML
    private void onVolumeChange() {
        if (mediaPlayer != null) {
            double volume = mySliderVolume.getValue() / 100.0; // Convert 0-100 to 0-1 range
            mediaPlayer.setVolume(volume);
        }
    }



}
