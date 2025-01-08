package org.example.mediaplayereasv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.w3c.dom.events.MouseEvent;

import java.util.ArrayList;
import java.util.List;


public class HelloController {
    @FXML
    private Label connectionStatus;

    @FXML
    private ListView<String> lvAllPlayLists;
    @FXML
    private ListView<String> lvCurrentPlayList;



    @FXML
    public void initialize() {
        checkDatabaseConnection();
        // Makes a new playlist and puts it in the playlist
        lvAllPlayLists.setItems(FXCollections.observableArrayList("All Songs"));

        loadAllSongs();
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




}