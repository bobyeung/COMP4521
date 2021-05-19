package com.example.musicplayerx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
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

//For displaying on the recycler view
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    ArrayList<Audio> songs;
    ArrayList<Integer> iconList;
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
            /*
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.playAudio(position);

            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("songs", songs);
            intent.putExtra("activeSong", MediaPlayerService.activeAudio);
             */
            MainActivity mainActivity = (MainActivity) context;
            Intent intent = new Intent(context, PlayerActivity.class);
            Utility.playAudio(context, position, null);
            context.startActivity(intent);
        }
    };

    public SongAdapter(Context context, ArrayList<Integer> iconList, ArrayList<Audio> songs){
        this.context = context;
        this.iconList = iconList;
        this.songs = songs;
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
        holder.songIcon.setImageResource(iconList.get(position));
        holder.songName.setText(songs.get(position).getTitle());
        holder.songArtist.setText(songs.get(position).getArtist());

        clickListener = new SongOnClickListener(position);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

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
