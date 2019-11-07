package com.soundbound.recyclerView;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soundbound.R;
import com.soundbound.SimpleSong;
import com.soundbound.phoneSongs.PhoneController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import views.CircleImageView;

public class SimpleSongViewAdapter extends RecyclerView.Adapter<SimpleSongViewAdapter.SongViewHolder> {
    private List<SimpleSong> tracks;
    ConcurrentLinkedQueue<SimpleSong> queue;
    songAdapterListener listener;
    public interface songAdapterListener{
        void onQueueChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,  View.OnLongClickListener {
        SimpleSong track;
        CircleImageView civ;
        TextView trackTitle;
        ConcurrentLinkedQueue<SimpleSong> queue;
        songAdapterListener listener;
        public SongViewHolder(View v, ConcurrentLinkedQueue<SimpleSong> queue, songAdapterListener listener){
            super(v);
            this.civ = v.findViewById(R.id.tv_main_image);
            this.trackTitle = v.findViewById(R.id.tv_track_title);
            this.queue = queue;
            this.listener=listener;
        }
        private void setTrack(SimpleSong track){
            this.track = track;
            civ.setClickable(true);
            civ.setOnClickListener(this);
            trackTitle.setText(track.title);
            trackTitle.setSelected(true);
            //remote.getPureRemote().getImagesApi().getImage(track.imageUri, Image.Dimension.SMALL).setResultCallback(civ::setImageBitmap);
            if(track.cover!=null)
            civ.setImageBitmap(track.cover);
        }
        @Override
        public void onClick(View v){
            LinearLayout contextInfo = LayoutInflater.from(v.getContext()).inflate(R.layout.track_context_info, null,false).findViewById(R.id.tci_main_layout);
            TextView tmp_tv = contextInfo.findViewById(R.id.track_content_info_album);
            tmp_tv.setText(track.album);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_author);
            tmp_tv.setText(track.author);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_title);
            tmp_tv.setText(track.title);
            tmp_tv = contextInfo.findViewById(R.id.track_content_info_duration);
            Button add = contextInfo.findViewById(R.id.tci_add);
            add.setOnClickListener(v1 -> {
                queue.add(track);
                listener.onQueueChanged();});
            long duration = track.duration;
            duration = duration/1000;
            int minutes =0;
            while (duration> 60)
            {
                duration -=60;
                minutes++;
            }
            String time = minutes+":"+duration;
            tmp_tv.setText(time);
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(contextInfo);
            dialog.show();

        }


        @Override
        public boolean onLongClick(View v) {
            queue.add(this.track);
            listener.onQueueChanged();
            return true;
        }
    }

//--------------------------------------------------------------------------------------Constructors
    public SimpleSongViewAdapter(Collection<SimpleSong> tracks, songAdapterListener listener, ConcurrentLinkedQueue<SimpleSong> queue) {
        this.queue = queue;
        if(tracks instanceof List){
            this.tracks =(List) tracks;
        }
        if(tracks instanceof ConcurrentLinkedQueue){
            this.queue = (ConcurrentLinkedQueue<SimpleSong>) tracks;
        }
        this.listener = listener;
    }
//-----------------------------------------------------------------------------------------Overrides

    public void refreshData(){
        this.tracks = new ArrayList<>(queue);
        this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_view, parent,false);
        return new SongViewHolder(v,queue, listener);
    }
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.setTrack(tracks.get(position));

    }
    @Override
    public int getItemCount() {
        if(tracks !=null){
            return tracks.size();
        }
        else {
            return queue.size();
        }
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

/*

            View.DragShadowBuilder shadow = new View.DragShadowBuilder(civ);
            ClipData.Item cdDescription = new ClipData.Item(track.title);
            ClipData dragData = new ClipData("textyyy", new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN }, cdDescription);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(dragData,myShadow,null,0);
            } else {
                v.startDrag(dragData,myShadow,null,0);
            }
            return true;
 */