package com.hughtebby.improsoundsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.os.CountDownTimer;

import java.util.ArrayList;

public class SampleAdapter extends BaseAdapter {

    private Context context;
    private MainActivity activity;
    private ArrayList<Sample> samples;
    private boolean isHistory;

    public SampleAdapter(MainActivity context, ArrayList<Sample> samples, boolean isHistory) {
        this.context = context;
        this.activity = context;
        this.samples = samples;
        this.isHistory = isHistory;
    }

    @Override
    public int getCount() {
        return samples.size();
    }

    @Override
    public Sample getItem(int position) {
        return samples.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Sample sample = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sound_clip_row, parent, false);
        }

        final Button loopButton = (Button) convertView.findViewById(R.id.loop);
        final Button playButton = (Button) convertView.findViewById(R.id.play);

        sample.setPlayButton(playButton);
        sample.setLoopButton(loopButton);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //Volume control
        final SeekBar volumeSlider = (SeekBar) convertView.findViewById(R.id.volumeSlider);
        volumeSlider.setProgress(volumeSlider.getMax());
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sample.setVolume(seekBar.getProgress(), seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        Boolean showVolume = sharedPref.getBoolean("show_volume", false);
        if (showVolume) {
            volumeSlider.setVisibility(View.VISIBLE);
        } else {
            volumeSlider.setVisibility(View.GONE);
        }


        activity.addVolumeSlider(volumeSlider);

        //set size
        String buttonSize = sharedPref.getString("button_size", "1");

        playButton.setTextSize(10 * Float.parseFloat(buttonSize)) ; //TypedValue.COMPLEX_UNIT_DIP,14);
        float scale = MainActivity.SCALE;
        playButton.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (100 * scale * Float.parseFloat(buttonSize) + 0.5f),
                (int) (30 * scale * Float.parseFloat(buttonSize) + 0.5f)
        ));
        loopButton.setTextSize(20 * Float.parseFloat(buttonSize)) ; //TypedValue.COMPLEX_UNIT_DIP,14);
        loopButton.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (30 * scale * Float.parseFloat(buttonSize) + 0.5f),
                (int) (30 * scale * Float.parseFloat(buttonSize) + 0.5f)
        ));
        volumeSlider.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (130 * scale * Float.parseFloat(buttonSize) + 0.5f),
                (int) (30 * scale + 0.5f)
        ));

        //get sound length
        final int soundDuration = sample.getDuration();

        playButton.setText(sample.getName());

        final CountDownTimer countDown = new CountDownTimer(soundDuration, 1000) {
            public void onTick(long millisUntilFinished) {
                playButton.setText(millisUntilFinished / 1000 + "\n" + sample.getName());
            }

            public void onFinish() {
                playButton.performClick();
            }
        };

        playButton.setOnClickListener(new View.OnClickListener() {

            private boolean clickedOnce = false;

            @Override
            public void onClick (View v){

                if (sample.isLooping()) {
                    loopButton.setSelected(false);
                    sample.stop();
                }
                if (v.isSelected()) {
                    v.setSelected(false);

                    if (clickedOnce) {
                        playButton.setBackgroundColor(playButton.getContext().getResources().getColor(R.color.played));
                    }

                    countDown.cancel();
                    sample.stop();
                    playButton.setText(sample.getName());

                } else {
                    if (!clickedOnce && !isHistory) {
                        //add to history
                        activity.addToHistory(sample);
                    }
                    playButton.setBackgroundColor(playButton.getContext().getResources().getColor(R.color.playing));
                    countDown.start();
                    v.setSelected(true);
                    sample.play(false);
                }

                //detect and store that button has been clicked
                this.clickedOnce = true;

            }
        });

        if (!sample.isLooping()) {
            loopButton.setSelected(false);
        } else {
            loopButton.setSelected(true);
        }
        loopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v){
            if (!sample.isLooping()) {
                v.setSelected(true);
                countDown.cancel();
                sample.setLooping();
                sample.play(true);
            } else {
                v.setSelected(false);
                countDown.cancel();
                sample.stop();
            }
            }

        });

        return convertView;
    }

}
/*
public class ButtonCountDownTimer extends CountDownTimer {

    private Button playButton;

    public ButtonCountDownTimer(long millisInFuture, long countDownInterval, Button playButton ) {
        this.playButton = playbutton;
    }
}
*/