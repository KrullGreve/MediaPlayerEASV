package org.example.mediaplayereasv;

import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public void addSongToPlaylist(String playlistName, String songWithArtist) throws SQLException {
        Connection con = DB.getConnection();  // Get the database connection

        if (con != null && !con.isClosed()) {
            try {
                // Step 1: Split songWithArtist to get the title and artist
                String[] parts = songWithArtist.split(" - ", 2); // Split into "Song name" and "Artist Name"
                if (parts.length < 2) {
                    System.err.println("Invalid song format: " + songWithArtist);
                    return;
                }
                String songTitle = parts[0].trim();
                String artistName = parts[1].trim();

                // Step 2: Get the SongId
                String getSongIdQuery = "SELECT SongId FROM Songs WHERE Title = ? AND Artist = ?";
                PreparedStatement getSongIdStmt = con.prepareStatement(getSongIdQuery);
                getSongIdStmt.setString(1, songTitle);
                getSongIdStmt.setString(2, artistName);
                ResultSet songIdResult = getSongIdStmt.executeQuery();

                int songId = -1; // Default value in case no result is found
                if (songIdResult.next()) {
                    songId = songIdResult.getInt("SongId");
                } else {
                    System.err.println("Song not found: " + songWithArtist);
                    return; // Exit if the song is not found
                }

                // Step 3: Get the PlaylistId
                String getPlaylistIdQuery = "SELECT PlaylistId FROM Playlists WHERE PlaylistName = ?";
                PreparedStatement getPlaylistIdStmt = con.prepareStatement(getPlaylistIdQuery);
                getPlaylistIdStmt.setString(1, playlistName);
                ResultSet playlistIdResult = getPlaylistIdStmt.executeQuery();

                int playlistId = -1; // Default value in case no result is found
                if (playlistIdResult.next()) {
                    playlistId = playlistIdResult.getInt("PlaylistId");
                } else {
                    System.err.println("Playlist not found: " + playlistName);
                    return; // Exit if the playlist is not found
                }

                // Step 4: Insert into PlaylistSongs
                String insertQuery = "INSERT INTO PlaylistSongs (PlaylistId, SongId) VALUES (?, ?)";
                PreparedStatement insertStmt = con.prepareStatement(insertQuery);
                insertStmt.setInt(1, playlistId);
                insertStmt.setInt(2, songId);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Song added successfully!");
                } else {
                    System.out.println("No rows were added.");
                }

            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
            } finally {
                con.close(); // Always close the connection
            }
        } else {
            System.err.println("Database connection is closed or invalid.");
        }
    }


}
