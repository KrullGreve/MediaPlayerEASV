package org.example.mediaplayereasv;

import java.util.ArrayList;
import java.util.List;

public class Playlist
{
    private int playlistID;
    private String playlistName;
    private List<Song> songs;

    public Playlist(int playlistID, String playlistName)
    {
        this.playlistID = playlistID;
        this.playlistName = playlistName;
        this.songs = new ArrayList<>();
    }

    public int getPlaylistID()
    {
        return playlistID;
    }

    public void setPlaylistID(int playlistID)
    {
        this.playlistID = playlistID;
    }

    public String getPlaylistName()
    {
        return playlistName;
    }

    public void setPlaylistName(String playlistName)
    {
        this.playlistName = playlistName;
    }

    public List<Song> getSongs()
    {
        return songs;
    }

    public void addSong(Song song)
    {
        this.songs.add(song);
    }

    public void removeSong(Song song)
    {
        this.songs.remove(song);
    }

    @Override
    public String toString()
    {
        return playlistName + " (" + songs.size() + " songs)";
    }
}
