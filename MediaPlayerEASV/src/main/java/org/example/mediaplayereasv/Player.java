package org.example.mediaplayereasv;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.time.Duration;

public class Player {
    static MediaPlayer mediaPlayer;



    public static void set(String file) {
    Media m = new Media(new File(file).toURI().toString());
    mediaPlayer = new MediaPlayer(m);

    mediaPlayer.play();

    }

    public static void play() {
        mediaPlayer.play();

    }

    public static void pause() {
        mediaPlayer.pause();
    }

    public static void stop() {
        mediaPlayer.stop();
        mediaPlayer.dispose();

    }

    public static String getTime() {
        String TimeStamp = mediaPlayer.getCurrentTime().toHours() + ":" + mediaPlayer.getCurrentTime().toMinutes() + ":" + mediaPlayer.getCurrentTime().toSeconds();
        return TimeStamp;
    }

    public static void setLoop(boolean loop) {
        mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
    }

    public static boolean isPlaying() {
        if (mediaPlayer != null) {
            return true;

        }
        else {return false;}
    }

    public static String getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getMedia().getDuration().toHours() + ":" + mediaPlayer.getMedia().getDuration().toMinutes() + ":" + mediaPlayer.getMedia().getDuration().toSeconds();

        }
        else {return null;}
    }
    public static void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }
}
