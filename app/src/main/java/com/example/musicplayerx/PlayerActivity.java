package com.example.musicplayerx;

import android.os.Bundle;

import com.example.musicplayerx.service.MediaPlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    ImageButton playBtn, previousBtn, nextBtn, repeatBtn, shuffleBtn;
    TextView startTime, endTime;
    SeekBar seekBar;
    ImageView songIcon2;
    TextView songName2, songArtist2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        setUpView();

        //To get the arguments from intent
        getInfo();
    }

    private void setUpView(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        playBtn = findViewById(R.id.playPause);
        previousBtn = findViewById(R.id.previousSong);
        nextBtn = findViewById(R.id.nextSong);
        repeatBtn = findViewById(R.id.repeatSong);
        shuffleBtn = findViewById(R.id.shuffleSong);
        seekBar = findViewById(R.id.seekBar);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        songIcon2 = findViewById(R.id.songIcon2);
        songName2 = findViewById(R.id.songName2);
        songArtist2 = findViewById(R.id.songArtist2);
    }

    private void getInfo(){

    }
}