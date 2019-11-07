package com.soundbound.recyclerView;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soundbound.R;
import com.soundbound.spotifySongs.SpotifyController;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.Track;

import java.util.List;

import views.CircleImageView;

public class SpotifySongViewAdapter extends RecyclerView.Adapter<SpotifySongViewAdapter.SpotifySongViewHolder> {
    private List<Track> tracks;
    SpotifyController remote;

    public static class SpotifySongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Track track;
        CircleImageView civ;
        SpotifyController remote;
        TextView trackTitle;
        public SpotifySongViewHolder(View v, SpotifyController remote){
            super(v);
            this.civ = v.findViewById(R.id.tv_main_image);
            this.trackTitle = v.findViewById(R.id.tv_track_title);
            this.remote = remote;
        }
        private void setTrack(Track track){
            this.track = track;
            civ.setClickable(true);
            civ.setOnClickListener(this);
            trackTitle.setText(track.name);
            trackTitle.setSelected(true);
            remote.getPureRemote().getImagesApi().getImage(track.imageUri, Image.Dimension.SMALL).setResultCallback(civ::setImageBitmap);
            //civ.setImageBitmap(track.cover);
        }
        @Override
        public void onClick(View v){
            LinearLayout contextInfo = LayoutInflater.from(v.getContext()).inflate(R.layout.track_context_info, null,false).findViewById(R.id.tci_main_layout);
            TextView tmp_tv = contextInfo.findViewById(R.id.track_content_info_album);
            tmp_tv.setText("from: " + track.album.name);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_author);
            tmp_tv.setText("by: " + track.artist.name);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_title);
            tmp_tv.setText(track.name);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_duration);
            long duration = track.duration;
            duration = duration/1000;
            int minutes =0;
            while (duration> 60)
            {
                duration -=60;
                minutes++;
            }
            tmp_tv.setText(minutes+":"+duration);
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(contextInfo);
            dialog.show();
        }
    }

    //--------------------------------------------------------------------------------------Constructors
    public SpotifySongViewAdapter(List<Track> tracks, SpotifyController remote) {
        this.tracks = tracks;
        this.remote = remote;
    }
    //-----------------------------------------------------------------------------------------Overrides
    @NonNull
    @Override
    public SpotifySongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_view, parent,false);
        return new SpotifySongViewHolder(v, remote);
    }
    @Override
    public void onBindViewHolder(@NonNull SpotifySongViewHolder holder, int position) {
        holder.setTrack(tracks.get(position));
    }
    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
/*
public void onClick(View v){
            ConstraintLayout contextInfo = LayoutInflater.from(v.getContext()).inflate(R.layout.track_context_info, null,false).findViewById(R.id.tci_main_layout);
            TextView tmp_tv = contextInfo.findViewById(R.id.track_content_info_uri);
            tmp_tv.setText(track.uri);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_album);
            tmp_tv.setText(track.album.name);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_author);
            tmp_tv.setText(track.artist.name);
            List<Artist> tmp_artists = track.artists;
            String tmp_string = "";
            for(Artist a: tmp_artists){
                tmp_string = tmp_string.concat(" , "+ a.name);
            }
            if(tmp_string!=""){
                tmp_string = tmp_string.substring(3);
            }
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_artists);
            tmp_tv.setText(tmp_string);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_title);
            tmp_tv.setText(track.name);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_duration);
            long duration = track.duration;
            duration = duration/1000;
            int minutes =0;
            while (duration> 60)
            {
                duration -=60;
                minutes++;
            }
            tmp_tv.setText(minutes+":"+duration);
            Button tmp_button = v.findViewById(R.id.track_content_info_button);
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(contextInfo);
            dialog.show();
        }

*/