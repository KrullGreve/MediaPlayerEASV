package org.example.mediaplayereasv;

public class Song
{
    private int songID;
    private String Title;
    private String Artist;
    private String Duration;

    public Song(int songID, String Title, String Artist)
    {
        this.songID = songID;
        this.Title = Title;
        this.Artist = Artist;

    }

    public int getSongID()
    {
        return songID;
    }

    public String getTitle()
    {
        return Title;
    }

    public String getArtist()
    {
        return Artist;
    }

    @Override
    public String toString()
    {
        return Title + " - " + Artist;
    }
}
