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

    public Song()
    {

    }

    public int getSongID()
    {
        return songID;
    }

    public void setSongID(int songID)
    {
        this.songID = songID;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getArtist()
    {
        return artist;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public String getDuration()
    {
        return Duration;
    }

    public void setDuration(String duration)
    {
        Duration = duration;
    }

    @Override
    public String toString()
    {
        return title + " - " + artist;
    }
}
