package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;


public class HelloController {

    private double previousVolume = 0.5; // Stores last volume before mute

    @FXML private ImageView bntMuteIcon;
    @FXML private ListView<String> lvAllPlayLists;
    @FXML private ListView<String> lvCurrentPlayList;
    @FXML private TextField tfPlaylistName;
    @FXML private ImageView ivMainImage;
    @FXML private Label myDuration, songDisplay, fullDuration;
    @FXML private ComboBox<String> cbSearchBar;
    @FXML private Button btnAddPlaylist, btnDeletePlaylist, btnConfirmPlaylist, btnCancelPlaylist, btnEditPlaylist;
    @FXML private Slider mySliderDuration, mySliderVolume;
    @FXML private HBox HBoxMainButtons, HBoxEditMode;

    private MediaPlayer mediaPlayer;

    // Calls to the service classes
    private SongServ songService = new SongServ();
    private PlaylistServ playlistService = new PlaylistServ();

    // Stores the Deletion of playlists
    private String pendingDeletePlaylist = null;

    private String pendingEditPlaylist = null;




    @FXML
    public void initialize() {

        // Set default playlist on the right Listview
        lvAllPlayLists.setItems(FXCollections.observableArrayList("All Songs"));
        lvAllPlayLists.getSelectionModel().select(0);
        if(DB.testConnection()) {
            System.out.println("Loading Database for Songs and Playlists");

            loadPlaylists();
            loadSongs();
            playListDuration();
        }
            refreshPlaylists();


    }

    /**
     * Play and Pause song feature for the media player
     * made for the user to be able to pause and play the song
     */
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

    /**
     * Stop button for when you want to stop the song completely
     * reset the song duration too
     */
    @FXML
    private void onStopPlay() {
        if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.stop();
        }
    }

    /**
     * Repeat function for the user to repeat a song
     */
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

    /**
     * Skips the current song and plays the next
     */
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

    /**
     * Plays the previous song only if the index number of the song isn't 0
     */
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

    /**
     * Checks if the media player is being used and makes so it start at zero seconds
     *
     */
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
                autoNextSong();
            }catch (URISyntaxException e){
                System.out.println("Error loading music files: " + e.getMessage());
            }
        }
    }

    /**
     * load the songs from the file and from the database and displays the song name + artist form the
     * database and set the songs that it find to valid song that we can use for the listview and other code
     */
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
                            lvCurrentPlayList.setItems(FXCollections.observableArrayList(getCurrentPlayList()));
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
    /**
     * OnSongSelected is a mouse on click event that plays the song the mouse click on and makes the mediaPlayer
     * And play the song and makes it so listview has an available index for the skip for and other functions
     * @param ignoredEvent
     */
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
                    autoNextSong();
                } else {
                    System.out.println("Song file not found: " + titleOnly);
                }
            } catch (URISyntaxException e) {
                System.out.println("Error playing song: " + e.getMessage());
            }


        }
        imageLoader();
    }

    /**
     * method for adding the duration of the song to the slider and label
     */
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

    /**
     * Loads images from the image folder using a relative path and displays them
     * if the user would like have other images than those that are there they can just add them to the folder
     */
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

    /**
     * make the image all the same for the image-loader
     * @param fileName
     * @return
     */
    private boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpeg");
    }

    /**
     * Load the playlist added and in the database
     */
    private void loadPlaylists() {
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    /**
     * button visibility for adding playlist
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
        switchToEditMode();
    }


    private void switchToEditMode()
    {
        HBoxMainButtons.setVisible(false);
        HBoxEditMode.setVisible(true);
    }

    private void switchToMainMode()
    {
        HBoxMainButtons.setVisible(true);
        HBoxEditMode.setVisible(false);
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

        switchToEditMode();
        tfPlaylistName.setVisible(false);
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
        switchToMainMode();
    }

    /**
     * Delete the chosen playlist and confirm the choice. it removes it from the database
     */
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

    /**
     * Cancel the progress of adding or deleting playlist
     */
    @FXML
    private void onCancelPlaylist()
    {
        resetPlaylistUI();
        switchToMainMode();
    }

    /**
     * Refresh the listview for the playlists
     */
    // Refreshes the playlist, so the deleted or added playlists are shown correctly
    private void refreshPlaylists() {
        // Reload the playlists from the database after deletion
        lvAllPlayLists.setItems(FXCollections.observableArrayList(playlistService.getAllPlaylists()));
    }

    /**
     * Displays the song title on the current song Label fxid SongDisplay
     * when songs are played
     */
    private void songNameDisplay(){
        songDisplay.setText(lvCurrentPlayList.getSelectionModel().getSelectedItem());
    }

    /**
     * Makes the combobox into a search function and play the songs from their index number
     */
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

                if (searchBarURl != null)
                {
                    if(mediaPlayer != null)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.seek(Duration.ZERO);
                    }
                    mediaPlayer = new MediaPlayer(new javafx.scene.media.Media(searchBarURl.toURI().toString()));

                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.stop();
                        mediaPlayer.seek(Duration.ZERO);
                    } else {
                        mediaPlayer.play();
                    }
                    songNameDisplay();
                    imageLoader();
                    autoNextSong();
                    durationAdder(titleOnly);
                    cbSearchBar.getEditor().clear();
                    filteredItems.clear();
                }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Shuffle the playlist via collections and refreshes after
     */
    @FXML
    private void shufflePlaylist() {
        ObservableList<String> ShuffledPlaylists =
                FXCollections.observableArrayList(lvCurrentPlayList.getItems());
        Collections.shuffle(ShuffledPlaylists);
        lvCurrentPlayList.setItems(ShuffledPlaylists);
        lvCurrentPlayList.refresh();

    }


    /**
     * Helper method to show simple errors as alerts in scene builder instead of the console
     * @param title
     * @param message
     */
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Drag function for the Duration slider
     */
    @FXML
    private void onSliderDragged() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(mySliderDuration.getValue()));
        }
    }

    /**
     * Update the label timer
     * @param current
     * @param total
     */
    @FXML
    private void updateDurationLabel(Duration current, Duration total) {
        String currentTime = formatDuration(current);
        String totalTime = formatDuration(total);
        myDuration.setText(currentTime + " / " + totalTime);
    }

    /**
     * Formats the duration
     * @param duration
     * @return
     */
    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Convert "MM:SS" from database to total seconds
     * @param durationStr
     * @return
     */
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

    /**
     * Mute button to mute the song playing
     */
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

    /**
     * Updates the mute button for vux use
     * @param isMuted
     */
    private void updateMuteIcon(boolean isMuted) {
        // Path to mute icon
        String MUTE_IMAGE = "/bntImages/bntMute.png";
        // Path to unmute icon
        String UNMUTE_IMAGE = "/bntImages/sVolume.png";
        String imagePath = isMuted ? MUTE_IMAGE : UNMUTE_IMAGE;
        bntMuteIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
    }

    /**
     * Slider for volume Changing
     */
    @FXML
    private void onVolumeChange() {
        if (mediaPlayer != null) {
            double volume = mySliderVolume.getValue() / 100.0; // Convert 0-100 to 0-1 range
            mediaPlayer.setVolume(volume);
        }
    }

    private void autoNextSong() {
        mediaPlayer.setOnEndOfMedia(this::nextSong);
    }
    @FXML
    private void onPlayListSelected(){
        ObservableList<String> playListItems = FXCollections.observableArrayList(getCurrentPlayList());
        lvCurrentPlayList.setItems(playListItems);
        lvAllPlayLists.getSelectionModel().getSelectedIndex();
        if(lvAllPlayLists.getSelectionModel().getSelectedIndex() != -1){
            playListDuration();
        }else {
            System.out.println("no playlist selected " + lvAllPlayLists.getSelectionModel().getSelectedItem());
        }
        lvCurrentPlayList.refresh();

    }
    public ArrayList<String> getCurrentPlayList()
    {
        ArrayList<String> songsInPlaylist = new ArrayList<>();
        DB.selectSQL("SELECT s.Title, s.Artist FROM Songs s " +
                "INNER JOIN PlaylistSongs ps ON s.SongId = ps.SongId " +
                "INNER JOIN Playlists p ON ps.PlaylistId = p.PlaylistId " +
                "WHERE p.PlaylistName = '" + lvAllPlayLists.getSelectionModel().getSelectedItem() + "'");

        while(true)
        {
            String title = DB.getData();
            if(title.equals(DB.NOMOREDATA))
            {
                break;
            }
            String artist = DB.getData();
            songsInPlaylist.add(title + " - " + artist);
        }
        return songsInPlaylist;
    }

    //Pending Changes missing
    private void playListDuration() {

        int totalDuration = 0; // Variable to store the total duration

        for (String song : songService.getAllSongs()) {
            if(lvCurrentPlayList.getItems().contains(song)) {


                String[] parts = song.split(" - ");
                if (parts.length > 0) {
                    String title = parts[0].trim(); // Extract and trim the title
                    String durationStr = songService.getSongDuration(title); // Get duration in MM:SS format

                    try {
                        // Convert MM:SS format to total seconds
                        int duration = parseDuration(durationStr);
                        totalDuration += duration;

                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing duration for: " + song);
                    }
                } else {
                    System.out.println("Invalid song format: " + song);
                }
            }
        }

        // Convert total duration to MM:SS format and update label
        fullDuration.setText(formatDuration(Duration.seconds(totalDuration)));
        System.out.println("Total duration of the selected playlist: " + formatDuration(Duration.seconds(totalDuration)));
    }


    @FXML
    private void onEditPlaylistClicked()
    {
        pendingEditPlaylist = lvAllPlayLists.getSelectionModel().getSelectedItem();
        System.out.println("Chosen playlist: " + pendingEditPlaylist);

        if(pendingEditPlaylist != null)
        {
            try
            {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("EditPlaylist.fxml"));
                Parent root = fxmlLoader.load();

                EditPlaylistController editController = fxmlLoader.getController();

                String selectedPlaylist = lvAllPlayLists.getSelectionModel().getSelectedItem();
                editController.setPlaylistName(selectedPlaylist);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Edit Playlist");
                stage.show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            showAlert("Error", "Playlist Not Found");
        }
    }
}
