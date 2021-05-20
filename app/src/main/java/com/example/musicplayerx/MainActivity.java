package com.example.musicplayerx;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.example.musicplayerx.service.MediaPlayerService;
import com.example.musicplayerx.ui.song.SongFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.musicplayerx.PlayNewAudio";

    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_MAIN = "ACTION_MAIN";

    ////Originally private
    public ArrayList<Audio> audioList;

    // Default Navigation Menu
    private AppBarConfiguration mAppBarConfiguration;
    private Toolbar toolbar;

    public static MediaPlayerService player;    ////Originally private non-static
    boolean serviceBound = false;


    /*
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

     */

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    /*
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

     */


    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Log.d("Service", "Bound");
            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    //////Basic Activity Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Loading files, need to be first
        loadAudio();

        setUpToolbar();
        setUpFloatingButton();
        setUpViewPager2();
        setUpDrawer();

        ////Testing, play the first audio in the ArrayList, be sure to have 1 audio for now or otherwise will crash
        playAudio(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            // If want run in background even app is closed, comment out this, but
            // TODO**in service notification should have these when pressed "closed"
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    ////// For Navigataion Menu

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_home:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //////Basic Setup Methods

    private void setUpToolbar(){
        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpViewPager2(){
        //ViewPager and Tabs, **Migrated with new ViewPager2
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.addFragments(SongFragment.newInstance(audioList));
        viewPagerAdapter.addFragments(BlankFragment.newInstance(null, null));
        ViewPager2 viewPager2 = findViewById(R.id.view_pager2);
        viewPager2.setAdapter(viewPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);   //Use ViewPager to dynamically add tabs
        new TabLayoutMediator(tabs, viewPager2, (tab, position) -> tab.setText("OBJECT " + (position + 1))).attach(); // originally "tabs.setupWithViewPager(viewPager);"
    }

    private void setUpDrawer(){
        //Drawer and Navigation
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        toggle.setHomeAsUpIndicator(null);  //R.drawable.side_menu
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpFloatingButton(){
        //Floating Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    ////// MediaPlayerService Related Methods

    public void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {

            //Instead of passing the Serializable audioList to Extra of Intent
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            //startService only first time call its onCreate, AND also once for onStart BECAUSE we use broadcast to play audio
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            ////Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
            ////playerIntent.setAction(MainActivity.ACTION_START_SERVICE);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE); //seems binding needs time to process

            Log.d("main unbound", "hi");

        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Utility.playAudio(this, audioIndex, audioList);
            Log.d("main bound", "hi");
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        //Need load from specific directory
        //Uri uri = Uri.parse("content://com.android.externalstorage.documents/document");
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";    //Make sure it is music
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";   //Ascending order of title
        //Cursor is used to retrieve data from storage, to loop over
        //To display data, uses Adapter with ListView
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        Log.d("Test cursor", String.valueOf(cursor.getCount()));
        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                Log.d("metadata", data + " " + title + " " + album + " " + artist);

                // Save to audioList
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

}