package com.example.musicplayerx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.os.Bundle;

import com.example.musicplayerx.service.MediaPlayerService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    MediaControllerCompat.TransportControls controls = MediaPlayerService.transportControls;
    Audio activeAudio = MediaPlayerService.activeAudio;
    MediaPlayer mediaPlayer = MediaPlayerService.mediaPlayer;

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
        Log.d("Player", "1");
        setUpView();

        //To get the arguments from intent
        getInfo();

        //Match button
        setUpListener();
        Log.d("Player", "2");
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
        //byte[] decodedString = Base64.decode(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, Base64.DEFAULT);
        //byte[] decodedString = mmr.getEmbeddedPicture();
        //songIcon2.setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));

        Log.d("player album", String.valueOf(activeAudio.getAlbumArt()));
        songIcon2.setImageBitmap(activeAudio.getAlbumArtBitmap());
        songName2.setText(activeAudio.getTitle());
        songArtist2.setText(activeAudio.getArtist());

        endTime.setText(String.valueOf(activeAudio.getDuration()));

    }

    private void setUpListener(){
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.play();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.skipToPrevious();
                getInfo();
            };
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.skipToNext();
                getInfo();
            };
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
            };
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            };
        });

        seekBar.setMax(activeAudio.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                controls.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}