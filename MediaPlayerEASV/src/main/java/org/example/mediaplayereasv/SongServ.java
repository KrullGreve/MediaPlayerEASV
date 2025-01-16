package org.example.mediaplayereasv;

import java.util.ArrayList;

public class SongServ
{
    // Method to get All songs from our database
    public ArrayList<String> getAllSongs() {
        ArrayList<String> songs = new ArrayList<>();
        DB.selectSQL("SELECT Title, Artist FROM Songs");

        while (true) {
            String title = DB.getData();
            if (title.equals(DB.NOMOREDATA))
            {
                break;
            }
            String artist = DB.getData();
            songs.add(title + " - " + artist);
        }

        return songs;
    }
    public String getSongDuration(String songTitle)
    {
        String sql = "SELECT Duration FROM Songs WHERE Title = '" + songTitle + "'";
        DB.selectSQL(sql);

        String duration = DB.getData();

        if (duration.equals(DB.NOMOREDATA)) {
            System.out.println("Song not found: " + songTitle);
            return "Unknown";
        }

        try{
            int totalSeconds = Integer.parseInt(duration);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }catch (Exception e) {
            return "00:00";
        }
    }
}
