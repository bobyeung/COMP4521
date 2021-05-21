package com.example.musicplayerx.ui.song;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.musicplayerx.Audio;
import com.example.musicplayerx.MainActivity;
import com.example.musicplayerx.R;
import com.example.musicplayerx.SongAdapter;
import com.example.musicplayerx.Utility;
import com.example.musicplayerx.service.MediaPlayerService;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.io.Serializable;

//May not be able to use global audioList --> songFragList, because user can browse other fragments
//and each fragments should have its own list
public class SongFragment extends Fragment {

    private SongViewModel mViewModel;

    ArrayList<Audio> songFragList;

    /*
    // Should be just match it one by one, because this fragment is unique to this arraylist
    ArrayList<Integer> iconId = new ArrayList<Integer>();
    ArrayList<Integer> iconList;
     */

    RecyclerView recyclerView;

    public static SongFragment newInstance(ArrayList<Audio> songFragList) {
        SongFragment fragment = new SongFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("songs", songFragList);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Find the views for UI
        recyclerView = view.findViewById(R.id.songRecycler);

        /*
        Log.d("SongFrag", "1");
        //Recycler View Adapter
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            if (field.getName().startsWith(getResources().getString(R.string.builtin_icon))) {
                try {
                    iconId.add(field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                };
            }
        }

        //Repeat based on songFragList
        songFragList = (ArrayList<Audio>) getArguments().getSerializable("songs");
        iconList = Utility.repeatElements(iconId, songFragList.size());
         */

        //Each fragment should have different songFragList, storing the audio with different position
        songFragList = (ArrayList<Audio>) getArguments().getSerializable("songs");
        for(int i = 0; i < songFragList.size(); i++){
            songFragList.get(i).setListPosition(i);
        }

        /*
        //Set back the albumArt if null
        for (int i = 0; i < songFragList.size(); i++) {
            if (songFragList.get(i).getAlbumArt() == null){
                Drawable d = getResources().getDrawable(iconList.get(i)); // the drawable (Captain Obvious, to the rescue!!!)
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                songFragList.get(i).setAlbumArt(bitmapdata);
                Log.d("albumArt", i + String.valueOf(songFragList.get(i).getAlbumArt()));
            }
        }
         */

        //Set up the view
        SongAdapter songAdapter = new SongAdapter(getContext(), songFragList);
        recyclerView.setAdapter(songAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));   //just use for set orientation
        Log.d("SongFrag", "2");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        // TODO: Use the ViewModel
    }

}