package org.example.mediaplayereasv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class HelloController {

    //region Variables
    @FXML
    private Label connectionStatus;

    @FXML
    private ListView<String> lvAllPlayLists;
    @FXML
    private ListView<String> lvCurrentPlayList;

    @FXML
    private Button btnCreatePlaylist;
    @FXML
    private TextField tfPlaylistName;

    //endregion



    @FXML
    public void initialize() {
        checkDatabaseConnection();
        // Makes a new playlist and puts it in the playlist
        lvAllPlayLists.setItems(FXCollections.observableArrayList("All Songs"));

        loadAllSongs();
    }

    private void loadPlaylists()
    {
        String sql = "SELECT PlaylistName FROM Playlists";
        DB.selectSQL(sql);
    }

    // Method to Create a brand-new playlist
    @FXML
    void createPlaylist()
    {
        String playlistName = tfPlaylistName.getText().trim();
        if (playlistName.isEmpty())
        {
            System.out.println("Playlist name is empty!");
            return;
        }

        // Enters into the database
        String sql = "INSERT INTO Playlists (PlaylistName) VALUES (?)";

        try{
            if(DB.insertSQL("INSERT INTO Playlists (PlaylistName) VALUES ('" + playlistName + "')"))
            {
                System.out.println("Playlist created successfully!" + playlistName);
                lvAllPlayLists.getItems().add(playlistName);
            }
            else
            {
                System.out.println("Error creating playlist!");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }



    // Loads all the songs from the current playlist in the right Listview
    private void loadAllSongs()
    {
        ArrayList<String> songs = getSongsFromDatabase();

        ObservableList<String> songsList = FXCollections.observableArrayList(songs);
        lvCurrentPlayList.setItems(songsList);
    }

    // Creates an Arraylist that loads all songs from the database
    private ArrayList<String> getSongsFromDatabase()
    {
        ArrayList<String> songs = new ArrayList<>();

        DB.selectSQL("SELECT Title, Artist FROM Songs");

        while(true)
        {
            String title = DB.getData();
            if (title.equals(DB.NOMOREDATA)) break;
            String artist = DB.getData();
            songs.add(title + " - " + artist);
        }

        return songs;
    }

    // Handler to select with a mouse click Playlists from our left Listview
    @FXML
    private void handlePlaylistSelected(MouseEvent event)
    {
        String selectedPlaylist = lvCurrentPlayList.getSelectionModel().getSelectedItem();
        if(selectedPlaylist != null)
        {
            System.out.println("Selected song: " + selectedPlaylist);
        }
    }


    // Uses the top left label to show if you connected to the database
    private void checkDatabaseConnection() {
        if (DB.testConnection()) {
            connectionStatus.setText("✅ Database Connected!");
            connectionStatus.setStyle("-fx-text-fill: green;");
        } else {
            connectionStatus.setText("❌ Connection Failed!");
            connectionStatus.setStyle("-fx-text-fill: red;");
        }
    }

        private MediaPlayer mediaPlayer;

        @FXML
        private void musicFinder(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");

            // Set the initial directory using an absolute path
            File musicFolder = new File("C:/temp/");
            if (musicFolder.exists() && musicFolder.isDirectory()) {
                fileChooser.setInitialDirectory(musicFolder);
                System.out.println("Music folder found at: " + musicFolder.getAbsolutePath());
            } else {
                System.out.println("Music folder not found.");
            }

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