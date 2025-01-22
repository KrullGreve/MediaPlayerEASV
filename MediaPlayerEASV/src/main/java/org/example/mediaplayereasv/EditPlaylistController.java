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


    /**
     * Sets the playlist Name to the one selected from the Edit button
     * @param playlistName
     */
    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
        lblPlaylistName.setText("Editing Playlist: " + playlistName);

        loadAvailableSongs();
        loadPlaylistSongs();

    }

    /**
     * Fills the lvAvailableSongs listview with all songs in our database
     */
    private void loadAvailableSongs()
    {
        availableSongList.clear();
        availableSongList.addAll(songService.getAllSongs());

    }

    /**
     * Fills the lvSongsInPlaylist listview with songs from our
     * database bridge table that matches IDs with the chosen playlist
     * which the getSongsInPlaylist method finds
     */
    private void loadPlaylistSongs()
    {
        playlistSongs.clear();
        playlistSongs.addAll(getSongsInPlaylist());
    }

    /**
     * Runs a query and stores the result in an Arraylist
     * @return
     */
    public ArrayList<String> getSongsInPlaylist()
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

    /**
     * Select a song to remove with the button action, then refreshes the listview
     * @throws SQLException
     */
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


    /**
     * Select a song to add from lvAvailableSongs then use the button action to add it
     * @param event
     * @throws SQLException
     */
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
