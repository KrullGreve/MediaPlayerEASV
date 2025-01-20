package org.example.mediaplayereasv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistServ
{

    private Map<Integer, String> playlists = new HashMap<>();

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

    // Method to add a playlist to the map
    public void addPlaylist(int playlistId, String playlistName) {
        playlists.put(playlistId, playlistName);
    }

    // Method to retrieve a playlist name by its ID
    public String getPlaylistNameById(int playlistId) {
        return playlists.get(playlistId);
    }

    // Method to get a playlist by its name (if needed)
    public Integer getPlaylistIdByName(String playlistName) {
        for (Map.Entry<Integer, String> entry : playlists.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(playlistName)) {
                return entry.getKey();
            }
        }
        return null;  // Return null if no playlist found
    }

    // Method to display all playlists (just for testing purposes)
    public void displayPlaylists() {
        for (Map.Entry<Integer, String> entry : playlists.entrySet()) {
            System.out.println("Playlist ID: " + entry.getKey() + ", Name: " + entry.getValue());
        }
    }

}
