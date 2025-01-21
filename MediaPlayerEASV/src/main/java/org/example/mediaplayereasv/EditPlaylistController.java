package org.example.mediaplayereasv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.SQLException;

public class EditPlaylistController
{
    @FXML private Label lblPlaylistName;
    @FXML private ListView<String> lvAvailableSongs, lvSongsInPlaylist;


    private PlaylistServ playlistService = new PlaylistServ();
    private SongServ songService = new SongServ();
    private String playlistName;
    private String songName;
    private static Connection con;

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
    }

    private void loadAvailableSongs()
    {
        availableSongList.addAll(songService.getAllSongs());

    }

    private void loadPlaylistSongs()
    {

    }

    @FXML
    private void onAddSongClicked(ActionEvent event) throws SQLException
    {
        String selectedSong = lvAvailableSongs.getSelectionModel().getSelectedItem();

        if(selectedSong == null)
        {
            mainController.showAlert("Error", "Please select a song to add. ");
            return;
        }
        playlistService.addSongToPlaylist(playlistName, selectedSong);
        mainController.showAlert("Success", "Song added to playlist: " + selectedSong);
    }
}
