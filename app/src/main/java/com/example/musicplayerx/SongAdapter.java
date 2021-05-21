package com.example.musicplayerx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayerx.service.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;

//For displaying on the recycler view, collaborating with the views
//Same as fragment, audioList should be local as each one has its own
//and this is just a utility class to generate recycler view
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    ArrayList<Audio> songAdapList;
    Context context;    //Probably who is calling this function

    //For click
    private SongOnClickListener clickListener;

    //Action performed when item is clicked
    public class SongOnClickListener implements View.OnClickListener
    {
        int position;
        public SongOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v)
        {
            MainActivity mainActivity = (MainActivity) context;
            Intent intent = new Intent(context, PlayerActivity.class);

            //maybe used sharedPreference because it may have been shuffled last time
            ////Originally just null for saved sharedPreference
            Utility.playAudio(context, position, null);
            context.startActivity(intent);
        }
    };

    public SongAdapter(Context context, ArrayList<Audio> songAdapList){
        this.context = context;
        this.songAdapList = songAdapList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Use the info from Audio if have, otherwise use default such as iconList
        //Now it has its own position relative to the audioList given, not really need the recycler view position
        Log.d("SongAdapter", "1");

        Audio activeAudio = songAdapList.get(position);

        if (activeAudio.getAlbumArt() != null){
            holder.songIcon.setImageBitmap(activeAudio.getAlbumArtBitmap());
        }
        else{
            int idPos = position % MainActivity.iconList.size();
            holder.songIcon.setImageResource(MainActivity.iconList.get(idPos));
        }

        holder.songName.setText(activeAudio.getTitle());
        holder.songArtist.setText(activeAudio.getArtist());

        clickListener = new SongOnClickListener(position);
        holder.itemView.setOnClickListener(clickListener);
        Log.d("SongAdapter", "2");
    }

    @Override
    public int getItemCount() {
        return songAdapList.size();
    }

    //View-dependent
    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView songIcon;
        TextView songName;
        TextView songArtist;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            songIcon = itemView.findViewById(R.id.songIcon);
            songName = itemView.findViewById(R.id.songName);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
