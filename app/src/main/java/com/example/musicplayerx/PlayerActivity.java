package com.example.musicplayerx;

import android.content.Intent;
import android.os.Bundle;

import com.example.musicplayerx.service.MediaPlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class PlayerActivity extends AppCompatActivity {

    ImageButton playBtn, previousBtn, nextBtn, repeatBtn, shuffleBtn;
    TextView startTime, endTime;
    SeekBar seekBar;
    ImageView songIcon2;
    TextView songName2, songArtist2;

    /*
    //Action performed when item is clicked
    public class BtnOnClickListener implements View.OnClickListener
    {
        Callable<Void> function;
        public BtnOnClickListener(Callable<Void> function) {
            this.function = function;
        }

        @Override
        public void onClick(View v)
        {
            try {
                function.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        setUpView();

        //To get the arguments from intent
        getInfo();

        //Match button
        setUpListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        /*
        Intent receivedIntent = getIntent();
        ArrayList<Audio> audioList = (ArrayList<Audio>) receivedIntent.getSerializableExtra("songs");
        Audio activeAudio = (Audio) receivedIntent.getSerializableExtra("activeSong");
        */
        songName2.setText(MediaPlayerService.activeAudio.getTitle());

    }

    private void setUpListener(){
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerService.transportControls.play();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerService.transportControls.skipToPrevious();
            };
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerService.transportControls.skipToNext();
            };
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerService.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
            };
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerService.transportControls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            };
        });
    }
}