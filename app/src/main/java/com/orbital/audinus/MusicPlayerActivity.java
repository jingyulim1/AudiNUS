package com.orbital.audinus;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView titleTextView, currentTimeTextView, totalTimeTextView;
    SeekBar seekBar;
    ImageView playPauseButton, nextButton, previousButton, albumArt;
    ArrayList<AudioModel> songList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    boolean isRepeat;
    boolean isShuffle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mediaPlayer.setOnCompletionListener(this);

        titleTextView = findViewById(R.id.song_title);
        currentTimeTextView = findViewById(R.id.current_time);
        totalTimeTextView = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        playPauseButton = findViewById(R.id.pause_play);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        albumArt = findViewById(R.id.album_art);

        titleTextView.setSelected(true);

        songList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        if (MyMediaPlayer.prevIndex == MyMediaPlayer.currentIndex) {
            setResourcesWithoutPlaying();
        } else {
            setResourcesWithMusic();
        }

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTextView.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        playPauseButton.setImageResource(R.drawable.pause_48px);
                        MyMediaPlayer.currentTime = mediaPlayer.getCurrentPosition();
                    } else {
                        playPauseButton.setImageResource(R.drawable.play_arrow_48px);
                    }

                }

                new Handler().postDelayed(this,100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    MyMediaPlayer.currentTime = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    playPause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (MyMediaPlayer.currentTime >= mediaPlayer.getDuration()){
                    playNextSong();
                    seekBar.setProgress(0);
                    MyMediaPlayer.currentTime = 0;
                } else {
                    mediaPlayer.seekTo(MyMediaPlayer.currentTime);
                    seekBar.setProgress(MyMediaPlayer.currentTime);
                    playPause();
                }
            }
        });


    }

    void setResourcesWithMusic(){
        currentSong = songList.get(MyMediaPlayer.currentIndex);

        titleTextView.setText(currentSong.getTitle());

        totalTimeTextView.setText(convertToMMSS(currentSong.getDuration()));

        playPauseButton.setOnClickListener(v-> playPause());
        nextButton.setOnClickListener(v-> playNextSong());
        previousButton.setOnClickListener(v-> playPreviousSong());

        playMusic();
    }

    void setResourcesWithoutPlaying(){
        currentSong = songList.get(MyMediaPlayer.currentIndex);

        titleTextView.setText(currentSong.getTitle());

        totalTimeTextView.setText(convertToMMSS(currentSong.getDuration()));

        playPauseButton.setOnClickListener(v-> playPause());
        nextButton.setOnClickListener(v-> playNextSong());
        previousButton.setOnClickListener(v-> playPreviousSong());

        continueMusic();
    }


    private void playMusic(){

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (MyMediaPlayer.prevIndex == MyMediaPlayer.currentIndex){
                mediaPlayer.seekTo(MyMediaPlayer.currentTime);
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            } else {
                seekBar.setProgress(0);
                MyMediaPlayer.prevIndex = MyMediaPlayer.currentIndex;
            }

            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void continueMusic(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekBar.setMax(mediaPlayer.getDuration());
    }

    private void playNextSong(){
        if (MyMediaPlayer.currentIndex != songList.size() - 1) {
            MyMediaPlayer.currentIndex++;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
    }

    private void playPreviousSong(){
        if (MyMediaPlayer.currentIndex != 0) {
            MyMediaPlayer.currentIndex--;
            mediaPlayer.reset();
            setResourcesWithMusic();
        }
    }

    private void playPause(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp){

        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playMusic();
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            MyMediaPlayer.currentIndex = rand.nextInt((songList.size() - 1) + 1);
            //to be implemented
        } else{
            // no repeat or shuffle ON - play next song, if last song go to first song
            if (MyMediaPlayer.currentIndex >= (songList.size() - 1)) {
                MyMediaPlayer.currentIndex = -1;
            }
            playNextSong();
        }
    }


    @SuppressLint("DefaultLocale")
    public static String convertToMMSS(String duration){
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}