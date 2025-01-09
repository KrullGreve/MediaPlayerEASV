package org.example.mediaplayereasv;

import java.util.ArrayList;

public class PlaylistServ
{
    // Method to load all playlists and show them on the left ListView
    public ArrayList<String> getAllPlaylists() {
        ArrayList<String> playlists = new ArrayList<>();
        String sql = "SELECT PlaylistName FROM Playlists";
        DB.selectSQL(sql);

        while (true) {
            String playlist = DB.getData();
            if (playlist.equals(DB.NOMOREDATA)) break;
            playlists.add(playlist);
        }

        return playlists;
    }

    // Method to create playlist with the + Button
    public boolean createPlaylist(String name) {
        String sql = "INSERT INTO Playlists (PlaylistName) VALUES ('" + name + "')";
        return DB.insertSQL(sql);
    }
}
