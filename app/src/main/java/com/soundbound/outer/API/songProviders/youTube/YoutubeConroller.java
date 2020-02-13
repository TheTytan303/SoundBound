package com.soundbound.outer.API.songProviders.youTube;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.soundbound.outer.API.songProviders.Models.SimpleSong;
import com.soundbound.View.player.Player;

import java.util.Arrays;
import java.util.List;

public class YoutubeConroller implements Searcher.YTSearcherListener, Player, YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener {
    Context context;
    private YouTube service;
    private GoogleAccountCredential credential;
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };
    private youtubeListener listener;
    YouTubePlayerFragment fragment;
    YouTubePlayer player;
    ConstraintLayout player_parent;
    SimpleSong currnetplayed;
    loadListener loadListener;
    public boolean isConnected;

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoaded(String s) {
        player.play();
        currnetplayed.duration = player.getDurationMillis();
    }

    @Override
    public void onAdStarted() {

    }

    @Override
    public void onVideoStarted() {
        loadListener.loaded((long)player.getDurationMillis());
    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {

    }

    public interface youtubeListener{
        void tryToConnect();
        void connectedYT();
        void onSongsSearched(List<SimpleSong> songs);
    }

    public interface loadListener{
        void loaded(Long milis);
    }

    public YoutubeConroller(Context c, youtubeListener listener, YouTubePlayerFragment fragment, ConstraintLayout player_parent){
        isConnected = false;
        this.listener = listener;
        this.context = c;
        this.fragment = fragment;
        this.player_parent = player_parent;
    }
    public GoogleAccountCredential getCredential(){
        return credential;
    }
    public void connect(){
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        credential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        service = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Sound&Bound")
                .build();
        listener.tryToConnect();
    }
    public void connected(){
        listener.connectedYT();
    }
    public void connected(String name){
        this.credential.setSelectedAccountName(name);
        isConnected = true;
        listener.connectedYT();
    }
    public void search(String name, ProgressBar pb){
        Searcher searcher = new Searcher(name, service, this, pb);
        searcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void loadSong(String id, SimpleSongYTDownloader.YTSingleSongListener listener){
        SimpleSongYTDownloader downloader = new SimpleSongYTDownloader(id,service, listener);
        downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onSongsSearched(List<SimpleSong> songs) {
        listener.onSongsSearched(songs);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.player = youTubePlayer;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        System.out.println(youTubeInitializationResult);
    }


    public void setlaodListener(loadListener l){
        this.loadListener = l;
    }

    //-----------------Player
    @Override
    public void resume() {

    }

    @Override
    public void playAt(long time) {

    }

    @Override
    public void pause() {
        player_parent.setVisibility(View.GONE);
        player.pause();
    }

    @Override
    public void play(SimpleSong ss) {
        currnetplayed = ss;
        player.cueVideo(ss.id);
        player.setPlayerStateChangeListener(this);
        player_parent.setVisibility(View.VISIBLE);
    }

    //----------------------------------------------------- Private:
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            System.out.println(connectionStatusCode);
        }
    }
}
