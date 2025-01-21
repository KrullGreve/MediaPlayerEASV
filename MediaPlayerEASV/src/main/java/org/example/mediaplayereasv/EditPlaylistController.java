package org.example.mediaplayereasv;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class EditPlaylistController
{
    @FXML private Label lblPlaylistName;
    @FXML private ListView<String> lvAvailableSongs, lvSongsInPlaylist;


    private PlaylistServ playlistService = new PlaylistServ();
    private SongServ songService = new SongServ();
    private String playlistName;
    private static Connection con;

    HelloController mainController = new HelloController();


    private ObservableList<String> availableSongList = FXCollections.observableArrayList();
    private ObservableList<String> playlistSongs = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        lvAvailableSongs.setItems(availableSongList);
        lvSongsInPlaylist.setItems(playlistSongs);
    }


    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
        lblPlaylistName.setText("Editing Playlist: " + playlistName);

        loadAvailableSongs();
        loadPlaylistSongs();

    }

    private void loadAvailableSongs()
    {
        availableSongList.clear();
        availableSongList.addAll(songService.getAllSongs());

    }

    private void loadPlaylistSongs()
    {
        playlistSongs.clear();
        playlistSongs.addAll(getSongsInPlaylist());
    }

    private ArrayList<String> getSongsInPlaylist()
    {
        ArrayList<String> songsInPlaylist = new ArrayList<>();
        DB.selectSQL("SELECT s.Title, s.Artist FROM Songs s " +
                "INNER JOIN PlaylistSongs ps ON s.SongId = ps.SongId " +
                "INNER JOIN Playlists p ON ps.PlaylistId = p.PlaylistId " +
                "WHERE p.PlaylistName = '" + playlistName + "'");

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

    @FXML
    private void onRemoveSongClicked() throws SQLException
    {
        String selectedSong = lvSongsInPlaylist.getSelectionModel().getSelectedItem();

        if(selectedSong == null)
        {
            mainController.showAlert("Error", "No Song Selected");
            return;
        }
        playlistService.removeSongFromPlaylist(playlistName, selectedSong);
        mainController.showAlert("Success", "Song Removed");

        loadPlaylistSongs();
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

        loadPlaylistSongs();

    }
}
