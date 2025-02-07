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

    /**
     * Stores all playlists in an Arraylist
     * @return
     */
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

    /**
     * SQL query to INSERT a playlist into our database
     * @param name
     * @return
     */
    public boolean createPlaylist(String name)
    {
        String sql = "INSERT INTO Playlists (PlaylistName) VALUES ('" + name + "')";
        return DB.insertSQL(sql);
    }

    /**
     * SQL Query to DELETE a playlist from our database
     * @param name
     * @return
     */
    public boolean deletePlaylist(String name)
    {
        String sql = "DELETE FROM Playlists WHERE PlaylistName = '" + name + "'";
        return DB.deleteSQL(sql);
    }

    /**
     * Creates an Array to store the Title and Artist so it matches in our database.
     * 3 steps of SQL Queries,
     * 1st get the Song ID where Artist and Title matches
     * 2nd get the Playlist ID where the Playlist Name matches
     * 3rd INSERT into our bridge table matching the two ID's
     *
     * @param playlistName
     * @param songWithArtist
     * @throws SQLException
     */
    public void addSongToPlaylist(String playlistName, String songWithArtist) throws SQLException
    {
        Connection con = DB.getConnection();

        if (con != null && !con.isClosed())
        {
            try
            {
                String[] parts = songWithArtist.split(" - ", 2);
                if (parts.length < 2)
                {
                    System.err.println("Invalid song format: " + songWithArtist);
                    return;
                }
                String songTitle = parts[0].trim();
                String artistName = parts[1].trim();

                // Get the SongId
                String getSongIdQuery = "SELECT SongId FROM Songs WHERE Title = ? AND Artist = ?";
                PreparedStatement getSongIdStmt = con.prepareStatement(getSongIdQuery);
                getSongIdStmt.setString(1, songTitle);
                getSongIdStmt.setString(2, artistName);
                ResultSet songIdResult = getSongIdStmt.executeQuery();

                int songId = -1;
                if (songIdResult.next())
                {
                    songId = songIdResult.getInt("SongId");
                } else
                {
                    System.err.println("Song not found: " + songWithArtist);
                    return;
                }

                // Get the PlaylistId
                String getPlaylistIdQuery = "SELECT PlaylistId FROM Playlists WHERE PlaylistName = ?";
                PreparedStatement getPlaylistIdStmt = con.prepareStatement(getPlaylistIdQuery);
                getPlaylistIdStmt.setString(1, playlistName);
                ResultSet playlistIdResult = getPlaylistIdStmt.executeQuery();

                int playlistId = -1;
                if (playlistIdResult.next())
                {
                    playlistId = playlistIdResult.getInt("PlaylistId");
                } else
                {
                    System.err.println("Playlist not found: " + playlistName);
                    return;
                }

                // Insert into PlaylistSongs
                String insertQuery = "INSERT INTO PlaylistSongs (PlaylistId, SongId) VALUES (?, ?)";
                PreparedStatement insertStmt = con.prepareStatement(insertQuery);
                insertStmt.setInt(1, playlistId);
                insertStmt.setInt(2, songId);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0)
                {
                    System.out.println("Song added successfully!");
                } else
                {
                    System.out.println("No rows were added.");
                }

            } catch (SQLException e)
            {
                System.err.println("SQL Error: " + e.getMessage());
            } finally
            {
                con.close();
            }
        } else
        {
            System.err.println("Database connection is closed or invalid.");
        }
    }

    /**
     * Creates an Array to store the Title and Artist so it matches in our database.
     * 3 steps of SQL Queries,
     * 1st get the Song ID where Artist and Title matches
     * 2nd get the Playlist ID where the Playlist Name matches
     * 3rd DELETE the ID's in the bridge table if they match
     *
     * @param playlistName
     * @param selectedSong
     * @throws SQLException
     */
    public void removeSongFromPlaylist(String playlistName, String selectedSong) throws SQLException
    {
        // Extract Title and Artist from the selectedSong
        String[] songParts = selectedSong.split(" - ");
        if (songParts.length != 2)
        {
            System.err.println("Invalid song format: " + selectedSong);
            return;
        }

        String songTitle = songParts[0];
        String artist = songParts[1];

        Connection con = DB.getConnection();
        if (con != null && !con.isClosed())
        {
            try
            {
                String getPlaylistIdSQL = "SELECT PlaylistId FROM Playlists WHERE PlaylistName = ?";
                PreparedStatement playlistStmt = con.prepareStatement(getPlaylistIdSQL);
                playlistStmt.setString(1, playlistName);

                ResultSet playlistRs = playlistStmt.executeQuery();
                int playlistId = -1;
                if (playlistRs.next())
                {
                    playlistId = playlistRs.getInt("PlaylistId");
                }

                if (playlistId == -1)
                {
                    System.err.println("Playlist not found: " + playlistName);
                    return;
                }

                // Query to get the SongId using Title and Artist
                String getSongIdSQL = "SELECT SongId FROM Songs WHERE Title = ? AND Artist = ?";
                PreparedStatement songStmt = con.prepareStatement(getSongIdSQL);
                songStmt.setString(1, songTitle);
                songStmt.setString(2, artist);

                ResultSet songRs = songStmt.executeQuery();
                int songId = -1;
                if (songRs.next())
                {
                    songId = songRs.getInt("SongId");
                }

                if (songId == -1)
                {
                    System.err.println("Song not found: " + songTitle + " by " + artist);
                    return;

                }
                String removeSongSQL = "DELETE FROM PlaylistSongs WHERE PlaylistId = ? AND SongId = ?";
                PreparedStatement removeStmt = con.prepareStatement(removeSongSQL);
                removeStmt.setInt(1, playlistId);
                removeStmt.setInt(2, songId);

                int rowsAffected = removeStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Song removed successfully!");
                } else {
                    System.err.println("Song not found in playlist.");
                }

            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
            } finally {
                con.close();
            }
        } else {
            System.err.println("Database connection is closed or invalid.");
        }
    }
}
