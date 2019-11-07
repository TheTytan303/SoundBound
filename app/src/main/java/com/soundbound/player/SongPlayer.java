package com.soundbound.player;

import android.net.IpSecManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayer;
import com.soundbound.SimpleSong;
import com.soundbound.phoneSongs.PhoneController;
import com.soundbound.recyclerView.SimpleSongViewAdapter;
import com.soundbound.spotifySongs.SpotifyController;
import com.soundbound.youtubeSongs.YoutubeConroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import views.CircleImageView;

public class SongPlayer implements Runnable, BackPlayer, YoutubeConroller.loadListener {
    private static int PB_RATE = 1;

    private ConcurrentLinkedQueue<SimpleSong> queue;
    private SimpleSongViewAdapter queueAdapter;
    private List<SimpleSong> history;
    private SimpleSongViewAdapter historyAdapter;
    private SimpleSong currentplayed;
    private Long msPlayed;
    private SpotifyController spotifyRemote;
    private YoutubeConroller youtubeRemote;
    private PhoneController phoneRemote;
    private TextView titleTV;
    private TextView authorTV;
    private CircleImageView imageCIV;
    private ProgressBar pb;
    private Timer updateTimer, prevTimer;
    private LinearLayout playerLayout;



    public List<SimpleSong>getHistory(){return history;}

    public SongPlayer(ConcurrentLinkedQueue<SimpleSong> queue,List<SimpleSong> history, SimpleSongViewAdapter historyAdapter,SimpleSongViewAdapter queueAdapter, TextView titleTV,TextView authorTv, CircleImageView civ, ProgressBar pb, LinearLayout playerLayout){
        this.queue = queue;
        this.history =history;
        this.historyAdapter = historyAdapter;
        this.titleTV = titleTV;
        this.authorTV=authorTv;
        this.imageCIV = civ;
        this.pb=pb;
        this.queueAdapter =queueAdapter;
        updateTimer = new Timer();
        this.playerLayout = playerLayout;
    }

    public void setSpotifyRemote(SpotifyController spotifyRemote) {
        this.spotifyRemote = spotifyRemote;
    }
    public void setYoutubeRemote(YoutubeConroller youtubeRemote) {
        this.youtubeRemote = youtubeRemote;
        youtubeRemote.setlaodListener(this);
    }
    public void setPhoneRemote(PhoneController phoneRemote) {
        this.phoneRemote = phoneRemote;
    }

    public void play(){
        if(currentplayed == null){
            setCurrentplayed(queue.poll());
            play(0l);
            return;
        }
        if(msPlayed == null){
            this.play(0l);
        }
        else {
            this.play(msPlayed);
        }
    }
    public void pause(){
        updateTimer.cancel();
        updateTimer.purge();
        if(this.currentplayed == null){
            return;
        }

        switch(currentplayed.type) {
            case PHONE:
                phoneRemote.pause();
                break;
            case SPOTIFY:
                spotifyRemote.pause();
                break;
            case YOUTUBE:
                youtubeRemote.pause();
                break;
            default:
                break;
        }
    }
    public void skip(){
        this.pause();
        if(currentplayed!=null)
            this.history.add(currentplayed);
        //historyAdapter.refreshData();
        historyAdapter.notifyDataSetChanged();
        setCurrentplayed(queue.poll());
        //queueAdapter.notifyDataSetChanged();
        queueAdapter.refreshData();
        if(currentplayed!=null)
        play();
    }
    public void updateProgress(){
        pb.setProgress(pb.getProgress()+1);
        System.out.println("progress: " + pb.getProgress()+" / " + pb.getMax());
    }
    private void play(Long played){
        if(currentplayed==null)
            return;
        switch(currentplayed.type){
            case PHONE:
                phoneRemote.play(currentplayed);
                break;
            case SPOTIFY:
                spotifyRemote.play(currentplayed);
                break;
            case YOUTUBE:
                youtubeRemote.play(currentplayed);
                this.pb.setProgress(0);
                updateTimer.cancel();
               return;
            default:
               break;
        }
        this.pb.setProgress(0);
        this.pb.setMax((int)(currentplayed.duration/1000)*PB_RATE);
        prevTimer = updateTimer;
        prevTimer.cancel();
        prevTimer.purge();
        updateTimer = new Timer();
        updateTimer.schedule(new updateTask(),0, 1000/PB_RATE);
        //pb.getHandler().removeCallbacksAndMessages(null);
        //pb.postDelayed(new updateTask(),1000/PB_RATE);

        playerLayout.getHandler().removeCallbacksAndMessages(null);
        playerLayout.postDelayed(new SkipTask3(currentplayed), currentplayed.duration);
    }
    private void setCurrentplayed(SimpleSong ss){
        if(ss == null)
            return;
        this.currentplayed = ss;
        this.titleTV.setText(ss.title);
        this.authorTV.setText(ss.author);
        this.imageCIV.setImageBitmap(ss.cover);
    }
    @Override
    public void run() {
    }

    @Override
    public void getPlayedTime(Long time) {
    }

    @Override
    public void loaded(Long milis) {
        this.pb.setProgress(0);
        this.pb.setMax((int)(currentplayed.duration/1000)*PB_RATE);
        updateTimer = new Timer();
        updateTimer.schedule(new updateTask(),0, 1000/PB_RATE);

        playerLayout.getHandler().removeCallbacksAndMessages(null);
        playerLayout.postDelayed(new SkipTask3(currentplayed), currentplayed.duration);
    }



    private class SkipTask3 implements Runnable{
        private SimpleSong ss;
        SkipTask3(SimpleSong ss){
            this.ss = ss;
        }
        @Override
        public void run() {
            if(ss == currentplayed)
            skip();
        }
    }

    private class updateTask extends TimerTask {
        @Override
        public void run() {
            updateProgress();
        }
    }
}


