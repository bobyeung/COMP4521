package com.example.musicplayerx.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.renderscript.RenderScript;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.musicplayerx.Audio;
import com.example.musicplayerx.MainActivity;
import com.example.musicplayerx.R;
import com.example.musicplayerx.StorageUtil;

import java.io.IOException;
import java.util.ArrayList;

import static android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM;
import static com.example.musicplayerx.MainActivity.ACTION_MAIN;

//Seems like the service is not forever running, same as MediaPLayer, only running when required (startService)
//For 1 activity, can check by seeing the binding of service, when it is in bind, then some media already passed to it
//So only change the index, but may need change
//startService only first time call its onCreate, AND also once for onStart BECAUSE we use broadcast to play audio
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    //For keeping track status to build notification
    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    // For media session use. To notify which action is triggered from the MediaSession callback listener
    public static final String ACTION_PLAY = "com.example.musicplayerx.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayerx.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.musicplayerx.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.musicplayerx.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.musicplayerx.ACTION_STOP";

    // This class is for manipulating this mediaplayer for thoroughly
    ////Originally private non-static
    public static MediaPlayer mediaPlayer;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    //for handling audio focus
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //List of available Audio files
    private ArrayList<Audio> audioList;
    private int resumePosition;         //Used to pause/resume MediaPlayer
    private int audioIndex = -1;        //Position of the song in the list that is playing
    public static Audio activeAudio;    ////private non-static initially

    //MediaSession allows interaction with media controllers, volume keys, media buttons, and transport controls
    //Instance is created to to publish media playback information or handle media keys (by using MetaData)
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    public static MediaControllerCompat.TransportControls transportControls;    ////private non-static initially

    //AudioPlayer notification ID (to identify instance, if 2 of them then 2 notifications)
    private static final int NOTIFICATION_ID = 101;
    public static boolean isServiceRunning = false;
    private Notification notification;

    //////Binders

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    //////Basic Overriding methods for MediaPlayer

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //Stop the service
        stopSelf();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    //////Basic Service Class Methods

    //The system calls this method when an activity, requests the service be started
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ////Originally just get from mediaFile from the Extra
        ////and requesting audio focus, and initMediaPlayer
        boolean flag = true;    ////Extra added, not sure useful or not
        try {
            //Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            Log.d("active1", "hi1");
            audioList = storage.loadAudio();
            Log.d("active1", "hi2");
            audioIndex = storage.loadAudioIndex();
            Log.d("active1", "hi3");

            //***Active audio won't change, using last song if index is inapproriate (not related to -1, just some weird behaviour only)
            //-1 is for detecting StorageUtil.loadIndex
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
                flag = false;
            }

            Log.d("active audio is ", "hihi");

        } catch (NullPointerException e) {
            Log.w("Service onStart", "null pointer");
            stopSelf();
            flag = false;
        }

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
            flag = false;
        }

        //MediaSession is for controlling mediaplayer
        if (mediaSessionManager == null && flag) {
            try {
                Log.d("Service","Initing Media");
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                Log.d("Media", "Fail Init");
                e.printStackTrace();
                stopSelf();
            }

            ////buildNotification(PlaybackStatus.PLAYING);
            buildNotification(PlaybackStatus.PAUSED);

            ////If it is the first time called by MainActivity, foreground service will run as well
            if (intent.getAction().equals(MainActivity.ACTION_START_SERVICE) && !isServiceRunning){
                Log.d("Foreground", "ok");
                isServiceRunning = true;
                startForeground(NOTIFICATION_ID, notification);
            }
            else{
                stopMyService();
            }
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        ////return super.onStartCommand(intent, flags, startId);
        //will stick to the notification top
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        Log.d("Created", "service");

        /// Register Broadcast Receivers
        // Manage incoming phone calls during playback. Pause MediaPlayer on incoming call, Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        register_becomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    //It is called when service is destroyed and MediaPlayer resource needs to be released
    @Override
    public void onDestroy() {
        isServiceRunning = false;
        super.onDestroy();

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }

        removeAudioFocus();

        ////removeNotification();

        /// Unregister BroadcastReceivers
        if (phoneStateListener != null) {   //Disable the PhoneStateListener
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    //////MediaPlayer, should be automatically play after prepared

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set the data source according to activeAudio instead of mediaFile
            mediaPlayer.setDataSource(activeAudio.getData());
            Log.d("Media", activeAudio.getTitle());
        } catch (IOException e) {
            Log.d("Media", "Error");
            e.printStackTrace();
            stopSelf();
        }

        //Initial Start Service should not automatically play song
        if (isServiceRunning){
            mediaPlayer.prepareAsync();
        }
    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    //////AudioFocus things

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    //////Broadcast Receivers

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        //pause audio on ACTION_AUDIO_BECOMING_NOISY
        pauseMedia();
        buildNotification(PlaybackStatus.PAUSED);
        }
    };

    //When user wants to play another audio
    //***just similar to onStartCommand
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Log.v("Service broadcast", "broadcast received");

        //Get the new media index form SharedPreferences
        StorageUtil storage = new StorageUtil(getApplicationContext());
        audioIndex = storage.loadAudioIndex();
        ////TODO tried change the audioList
        audioList = storage.loadAudio();

        //***Active audio won't change, using last song if index is inapproriate
        if (audioIndex != -1 && audioIndex < audioList.size()) {
            //index is in a valid range
            activeAudio = audioList.get(audioIndex);
        } else {
            //seems useless, still play the activeAudio, if add return can prevent
            stopSelf();
        }

        //A PLAY_NEW_AUDIO action received
        //reset mediaPlayer to play the new Audio
        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        //If this filter is met, then the action playNewAudio is executed
        //This specific condition is self-made, not inside system
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void register_becomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    //////MediaSession and Controls

    ////Maybe No need mediaSessionManager
    //For setting the MediaSession callbacks to handle events coming from the notification buttons
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return;

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        //Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "MusicPlayerX");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        //through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        //Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.d("Callbacked", "Skip next");
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                ////removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        ////TODO to give icon according to song
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.songicon_drum);  //replace with media albumArt

        ////Not sure what does this means
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());

    }

    private void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        mediaPlayer.reset();
        initMediaPlayer();
    }

    //////Notification and Interactions

    //Building the notification UI and setting up all the events that will trigger when a user clicks a notification button
    //Using PendingIntent to pass the actions
    //Everytime playbackStatus changed, the notification needs to change correspondingly
    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        ////For onClick notification
        Intent onClickIntent = new Intent(getApplicationContext(), MainActivity.class);
        onClickIntent.setAction(ACTION_MAIN);  // A string containing the action name
        onClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent onClickPendingIntent = PendingIntent.getActivity(this, 0, onClickIntent, 0);

        //For display and bind new intent according to playbackStatus
        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        ////Newly added Notification Channel, try to compatiable different API
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("my_service_urgent", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            //channel.EnableVibration( false );
            //channel.LockscreenVisibility = NotificationVisibility.Secret;

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        ////TODO to give icon according to song
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.songicon_drum); //replace with your own image

        // Create a new Notification (seems setting priority makes it worse)
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                // Control whether the timestamp is shown
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                // Attach our MediaSession token
                .setMediaSession(mediaSession.getSessionToken())
                // Show our playback controls in the compact notification view.
                .setShowActionsInCompactView(0, 1, 2))
                ////.setDeleteIntent()
                // Set the Notification color and can be changed
                .setColor(getResources().getColor(R.color.design_default_color_on_primary))
                // Set the small icons and can be changed
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setLargeIcon(largeIcon)                // Set the large and small icons
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                ////.setContentInfo(activeAudio.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2))
                //// Add onClick action
                .setContentIntent(onClickPendingIntent)
                .setOngoing(true);

        //Also belongs to above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(channel.getId());
        }

        //Set up the real Notification
        notification = notificationBuilder.build();

        //Build the notification using Builder
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);

        //Start it as foreground service
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void stopMyService() {
        // For foreground service
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }

    //Used by Building Notification to give action, this bind all the intents to the notification
    //so when certain action/intent is performed, it will transit to handleIncomingActions
    //which is handled by MediaSession and Transport Controls to control the media player
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    //Depending on PendingIntent, and transit the action to MediaSession to control the MediaPlayer
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

}