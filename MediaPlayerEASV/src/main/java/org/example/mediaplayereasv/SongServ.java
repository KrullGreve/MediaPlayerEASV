package org.example.mediaplayereasv;

import java.util.ArrayList;

public class SongServ
{
    public ArrayList<String> getAllSongs() {
        ArrayList<String> songs = new ArrayList<>();
        DB.selectSQL("SELECT Title, Artist FROM Songs");

        while (true) {
            String title = DB.getData();
            if (title.equals(DB.NOMOREDATA)) break;
            String artist = DB.getData();
            songs.add(title + " - " + artist);
        }

        return songs;
    }
}
