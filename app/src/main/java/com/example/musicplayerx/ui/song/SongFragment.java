package com.example.musicplayerx.ui.song;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.io.Serializable;

public class SongFragment extends Fragment {

    private SongViewModel mViewModel;

    ArrayList<Audio> audioList;

    ArrayList<Integer> iconId = new ArrayList<Integer>();
    ArrayList<Integer> iconList;

    RecyclerView recyclerView;

    public static SongFragment newInstance(ArrayList<Audio> list) {
        for (Audio a:list) {
            Log.d("Frag", a.getTitle());
        }
        SongFragment fragment = new SongFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable("Audio", list);
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

        //Recycler View Adapter
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            if (field.getName().startsWith("songicon")) {
                try {
                    iconId.add(field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                };
            }
        }

        //Repeat based on audioList
        audioList = (ArrayList<Audio>) getArguments().getSerializable("Audio");
        iconList = Utility.repeatElements(iconId, audioList.size());

        //Set up the view
        SongAdapter songAdapter = new SongAdapter(getContext(), iconList, audioList);
        recyclerView.setAdapter(songAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));   //just use for set orientation

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        // TODO: Use the ViewModel
    }

}