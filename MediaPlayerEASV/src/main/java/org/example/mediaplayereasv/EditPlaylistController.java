package org.example.mediaplayereasv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML private ListView<String> lvAvailableSongs, lvSongsInPlaylist, lvAllPlaylists;
    @FXML private ListView<String> lvPlaylists;

    private String currentPlaylistName;
    private SongServ songService = new SongServ();

    private String playlistName;


    HelloController mainController = new HelloController();

    private ObservableList<String> availableSongList = FXCollections.observableArrayList();  // The list to hold Song objects

    @FXML
    public void initialize()
    {
        setPlaylistName("");

        loadAvailableSongs();
        lvAvailableSongs.setItems(availableSongList);
    }

    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
        lblPlaylistName.setText("Editing Playlist: " + playlistName);
        if(playlistName != null)
        {
            System.out.println("Current playlist: " + playlistName);
        }
    }

    private void loadAvailableSongs()
    {
        availableSongList.addAll(songService.getAllSongs());

    }

    @FXML
    private void onAddSong()
    {

    }
}
