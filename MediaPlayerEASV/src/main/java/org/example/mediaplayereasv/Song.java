package org.example.mediaplayereasv;

public class Song
{
    private int songID;
    private String title;
    private String artist;
    private String Duration;

    public Song(int songID, String Title, String Artist)
    {
        this.songID = songID;
        this.title = Title;
        this.artist = Artist;

    }

    public int getSongID()
    {
        return songID;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    @Override
    public String toString()
    {
        return title + " - " + artist;
    }
}
