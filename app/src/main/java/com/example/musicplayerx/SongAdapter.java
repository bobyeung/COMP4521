package com.example.musicplayerx;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//For displaying on the recycler view
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    List<Audio> songs;
    List<Integer> iconList;
    Context context;    //Probably who is calling this function

    //For click
    private NewOnClickListener clickListener;

    public class NewOnClickListener implements View.OnClickListener
    {

        int position;
        public NewOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v)
        {
            //read your lovely variable
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.playAudio(position);
        }

    };

    public SongAdapter(Context context, List<Integer> iconList, List<Audio> songs){
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
        holder.myIcon.setImageResource(iconList.get(position));
        holder.songName.setText(songs.get(position).getTitle());
        holder.songArtist.setText(songs.get(position).getArtist());

        clickListener = new NewOnClickListener(position);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView myIcon;
        TextView songName;
        TextView songArtist;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            myIcon = itemView.findViewById(R.id.songIcon);
            songName = itemView.findViewById(R.id.songName);
            songArtist = itemView.findViewById(R.id.songArtist);
        }
    }
}
