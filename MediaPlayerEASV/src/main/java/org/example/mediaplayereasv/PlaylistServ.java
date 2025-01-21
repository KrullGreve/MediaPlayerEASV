package org.example.mediaplayereasv;

import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class PlaylistServ
{
    private static Connection con;

    // Method to load all playlists and show them on the left ListView
    public ArrayList<String> getAllPlaylists()
    {
        ArrayList<String> playlists = new ArrayList<>();
        String sql = "SELECT PlaylistName FROM Playlists";
        DB.selectSQL(sql);

        while (true)
        {
            String playlist = DB.getData();
            if (playlist.equals(DB.NOMOREDATA)) break;
            playlists.add(playlist);
        }

        return playlists;
    }

    // Method to create playlist with the + Button
    public boolean createPlaylist(String name)
    {
        String sql = "INSERT INTO Playlists (PlaylistName) VALUES ('" + name + "')";
        return DB.insertSQL(sql);
    }

    // Method to delete a playlist by name which is taken from the listview through a mouse click
    public boolean deletePlaylist(String name)
    {
        String sql = "DELETE FROM Playlists WHERE PlaylistName = '" + name + "'";
        return DB.deleteSQL(sql);
    }

    public static void addSongToPlaylist(String playlistName, String songName) throws SQLException
    {
        String sql = "INSERT INTO PlaylistSongs (PlaylistId, SongId) " +
                "SELECT p.PlaylistId, s.SongId FROM Playlists p, Songs s " +
                "WHERE p.PlaylistName = ? AND s.Title = ?";

        DB.getConnection();
        System.out.println("Connecting to database...");

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, playlistName);
        System.out.println("PlaylistName: " + playlistName);
        ps.setString(2, songName);
        System.out.println("SongName: " + songName);

        int rowsAffected = ps.executeUpdate();
        if(rowsAffected > 0)
        {
            System.out.println("Song added to playlist: " + songName);
        }
        else
        {
            throw new SQLException("Song could not be added to playlist: ");
        }

        ps.close();
    }
}
