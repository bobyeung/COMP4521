package com.example.musicplayerx;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.musicplayerx.service.MediaPlayerService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

//audioList should could follow Global one because it needs to in sync with noification
public class PlayerActivity extends AppCompatActivity {

    ImageButton playBtn, pauseBtn, previousBtn, nextBtn, repeatBtn, shuffleBtn;
    TextView startTime, endTime;
    SeekBar seekBar;
    ImageView songIcon2;
    TextView songName2, songArtist2;

    MediaControllerCompat.TransportControls controls = MediaPlayerService.transportControls;
    MediaPlayer mediaPlayer = MediaPlayerService.mediaPlayer;

    private Handler mHandler = new Handler();

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

        //Basic set up
        setUpView();

        //Set the arguments to update view
        setInfo();

        //Running Thread
        setThread();

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

        playBtn = findViewById(R.id.playSong);
        pauseBtn = findViewById(R.id.pauseSong);
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

        if (mediaPlayer.isPlaying()){
            playBtn.setEnabled(false);
            playBtn.setVisibility(View.INVISIBLE);
        }else{
            pauseBtn.setEnabled(false);
            pauseBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void setInfo(){
        Audio activeAudio = MediaPlayerService.activeAudio;
        Log.d("active audio", activeAudio.getTitle() + " " + activeAudio.getListPosition());
        if (activeAudio.getAlbumArt() != null){
            songIcon2.setImageBitmap(activeAudio.getAlbumArtBitmap());
        }
        else{
            ////StorageUtil storage = new StorageUtil(getParent()); //try getting parent which is main activity
            ////int idPos = storage.loadAudioIndex() % MainActivity.iconList.size();
            //int idPos = activeAudio.getListPosition() % MainActivity.iconList.size();
            songIcon2.setImageResource(MainActivity.iconList.get(MediaPlayerService.idPos));
        }
        songName2.setText(activeAudio.getTitle());
        songArtist2.setText(activeAudio.getArtist());

        int max = mediaPlayer.getDuration() / 1000;
        endTime.setText((max / 60) + ":" + (max % 60));
    }

    private void setThread(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
            }
        },0,1000);
    }

    private void setUpListener(){
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.play();
                playBtn.setEnabled(false);
                playBtn.setVisibility(View.INVISIBLE);

                pauseBtn.setEnabled(true);
                pauseBtn.setVisibility(View.VISIBLE);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.pause();
                pauseBtn.setEnabled(false);
                pauseBtn.setVisibility(View.INVISIBLE);

                playBtn.setEnabled(true);
                playBtn.setVisibility(View.VISIBLE);
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.skipToPrevious();
                finish();
                startActivity(getIntent());
            };
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controls.skipToNext();
                finish();
                startActivity(getIntent());
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

        seekBar.setMax(mediaPlayer.getDuration() / 1000);    //milleseconds originally
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int min, sec;
                int mCurrentPosition = seekBar.getProgress();

                min = mCurrentPosition / 60;
                sec = mCurrentPosition % 60;

                startTime.setText(min + ":" + String.format("%02d", sec));

                if(fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                    seekBar.setProgress(progress);
                }
                /*
                if (fromUser) {
                    Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            int min, sec;
                            //Checking if the music player is null or not otherwise it may throw an exception
                            if (mediaPlayer != null) {
                                Log.d("mp", String.valueOf(mediaPlayer.getCurrentPosition()));
                                Log.d("sb", String.valueOf(seekBar.getProgress()));
                                Log.d("sb2", String.valueOf(seeked_progress));
                                int mCurrentPosition = seekBar.getProgress();

                                min = mCurrentPosition / 60;
                                sec = mCurrentPosition % 60;

                                Log.e("Music Player Activity", "Minutes : " + min + " Seconds : " + sec);

                                startTime.setText(min + ":" + sec);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    };
                    mRunnable.run();

                }*/
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
