package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditPlaylistController
{
    @FXML private Label lblPlaylistName;
    @FXML private ListView<String> lvAvailableSongs, lvSongsInPlaylist;

    private String currentPlaylistName;
    private SongServ songService = new SongServ();

    @FXML
    public void initialize()
    {
        loadAvailableSongs();

        if(currentPlaylistName != null)
        {
            lblPlaylistName.setText("Editing Playlist: " + currentPlaylistName);
        }
    }

    public void setPlaylistName(String playlistName)
    {
        this.currentPlaylistName = playlistName;
    }

    private void loadAvailableSongs()
    {
        List<String> songs = getAllSongs();

    }

    private List<String> getAllSongs()
    {
        List<String> songs = new ArrayList<>();
        String sql = "SELECT Title FROM Song";

        
    }
}
