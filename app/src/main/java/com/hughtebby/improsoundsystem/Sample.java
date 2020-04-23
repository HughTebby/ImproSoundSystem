package com.hughtebby.improsoundsystem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Sample {

    private String name;
    private int startTime;
    private int duration;
    private int i;

    private Uri uri;
    private Context context;
    private MediaPlayer currentPlayer;
    private MediaPlayer tmpPlayer;
    private boolean isLooping = false;
    private String formattedDuration;
    private Button playButton;
    private Button loopButton;
    private float playVolume;

    public Sample(String name, File file, int fullDuration, Context context, String category) {

        //split name with #, get name and startTime
        if (name.contains("#")) {
            String[] parts = name.split("#");
            this.name = parts[0];
            if(parts.length > 2) {
                for (int i = 1; i < parts.length - 1; i++) {
                    this.name += "#" + parts[i];
                }
            }

            //check if last part is int
            this.startTime = Integer.valueOf(parts[parts.length-1]) * 100; // convert to milliseconds
        } else {
            this.name = name;
            this.startTime = 0;
        }

        this.context = context;

        uri = Uri.fromFile(new File(file.getAbsolutePath()));

        this.duration = fullDuration - this.startTime;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showDuration = sharedPref.getBoolean("show_duration", false);

        if (showDuration) {

            if (this.duration > 59999) {
                formattedDuration = String.format("%dm%02d",
                    TimeUnit.MILLISECONDS.toMinutes(this.duration),
                    TimeUnit.MILLISECONDS.toSeconds(this.duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this.duration))
                );
            } else {
                formattedDuration = Long.toString(TimeUnit.MILLISECONDS.toSeconds(this.duration)) + "s";
            }
            //sample.setName(sample.getName() + "|");
            this.name += " (" + formattedDuration + ")";
        }

        this.playVolume = 1.0f;

    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public void setPlayButton(Button playButton) {
        this.playButton = playButton;
    }

    public void setLoopButton(Button loopButton) {
        this.loopButton = loopButton;
    }

    public void setVolume(int volume, int maxVolume) {

        float realVolume;
        realVolume = (float) (1 - Math.log(maxVolume-volume)/Math.log(maxVolume));

        this.playVolume = realVolume;
        if (currentPlayer != null) {
            currentPlayer.setVolume(realVolume, realVolume);
        }
    }

    public void play(boolean isLooped) {
        isLooping = isLooped;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean multiplePlay = sharedPref.getBoolean("multiple_play", true);
        if (!multiplePlay) {
            MainActivity activity = (MainActivity) context;
            activity.stopAll();
        }

        if (currentPlayer != null) {
            //currentPlayer.seekTo(0);
            //currentPlayer.stop();
        } else {
            currentPlayer = MediaPlayer.create(context, uri);
            currentPlayer.seekTo(startTime);
            currentPlayer.setLooping(isLooped);
            currentPlayer.setVolume(playVolume, playVolume);
            currentPlayer.start();
        }

    }

    public void setLooping() {

        if (currentPlayer != null) {
            currentPlayer.setLooping(true);
        }

    }

    public void stop() {
        try {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.release();
                currentPlayer = null;

                if (playButton.isSelected()) {
                    playButton.performClick();
                }
                if (isLooping) {
                    loopButton.performClick();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        isLooping = false;
    }

    public boolean isLooping() {
        return isLooping;
    }

}
