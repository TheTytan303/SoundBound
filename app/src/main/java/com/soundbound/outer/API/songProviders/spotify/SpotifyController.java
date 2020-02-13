package com.soundbound.outer.API.songProviders.spotify;

import android.app.Activity;
import android.widget.ProgressBar;

import com.soundbound.outer.API.songProviders.Models.SimpleSong;
import com.soundbound.View.player.BackPlayer;
import com.soundbound.View.player.Player;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;

public class SpotifyController implements Connector.ConnectionListener, SongDownloader.SongDowloaderListener, Player {

    //---------------------------------------------------------------------------------Values
    private static String CLIENT_ID;
    private static String REDIRECT_URI ;
    private String token;
    private SongDownloader downloader;
    int REQUEST_CODE = 1337;
    SpotifyAppRemote remote;
    Activity context;
    RemoteControllerListener listener;
    BackPlayer backPlayer;
    //---------------------------------------------------------------------------------Interfaces
    public interface RemoteControllerListener{
        void onCurrentTrackReturned(Track track);
        void onTrackReturned(SimpleSong track);
        void waitingForConnection();
        void connected();
        void connectionError();
        void onSongsSearched(List<SimpleSong> songs);
    }

    //---------------------------------------------------------------------------------Constructors
    public SpotifyController(Activity context, String client_id, String redirectUri, RemoteControllerListener rcl){
        CLIENT_ID = client_id;
        REDIRECT_URI = redirectUri;
        this.context = context;
        listener = rcl;
        reconnect();
    }
    //---------------------------------------------------------------------------------Public
    public boolean isConnected(){
        return remote.isConnected();
    }
    public void setRemoteControllerListener(RemoteControllerListener rcl){
        listener = rcl;
    }
    public void getCurrentTrack(){
        if(!reconnect()){
            return;
        }
        remote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            listener.onCurrentTrackReturned(playerState.track);
        });
    }
    public void getTrackInfo( String trackUri){
        if(!reconnect()){
            return;
        }
        if(token == null){
            obtainToken();
            return;
        }else {
            downloader.downloadSong(trackUri, context);
        }
    }
    public void searchForTracks(String name, ProgressBar pb){
        if(!reconnect()){
            return;
        }
        if(token == null){
            obtainToken();
            return;
        }else {
            downloader.searchForSongs(name, context, pb);
        }
    }
    public void disconnect(){
        if(remote.isConnected())
            SpotifyAppRemote.disconnect(remote);
    }
    public void obtainToken(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(context, REQUEST_CODE, request);
    }
    public SpotifyAppRemote getPureRemote(){
        return remote;
    }
    public void tokenGranted(String token){
        downloader.setToken(token);
        this.token = token;
    }
    public void setBackPlayer(BackPlayer backPlayer) {
        this.backPlayer = backPlayer;
    }

    //---------------------------------------------------------------------------------Private
    private boolean reconnect(){
        if(remote == null){
            listener.waitingForConnection();
            ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(true).build();
            SpotifyAppRemote.connect(context, params, this);
            return false;
        }
        if(remote.isConnected()){
            return true;
        }
        listener.waitingForConnection();
        ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(true).build();
        SpotifyAppRemote.connect(context, params, this);
        return remote.isConnected();
    }


    //---------------------------------------------------------------------------------Overrides
    @Override
    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
        remote = spotifyAppRemote;
        downloader = new SongDownloader(token, remote.getImagesApi(), this, "PL");
        obtainToken();
        listener.connected();
    }
    @Override
    public void onFailure(Throwable throwable) {
        listener.connectionError();
    }
    @Override
    public void onSingleSongReturned(SimpleSong ss) {
        listener.onTrackReturned(ss);
    }
    @Override
    public void onSongsSearched(List<SimpleSong> songs) {
        listener.onSongsSearched(songs);
    }
    //----------Player

    @Override
    public void playAt(long time) {
        remote.getConnectApi().connectSwitchToLocalDevice();
        remote.getPlayerApi().seekToRelativePosition(time);
        remote.getPlayerApi().resume();
    }

    @Override
    public void pause() {
        remote.getPlayerApi().pause()
                .setErrorCallback(throwable -> {
            System.err.println(throwable.getMessage());
        });
    }

    @Override
    public void play(SimpleSong ss) {
        remote.getPlayerApi().play("spotify:track:"+ss.id);
    }

    @Override
    public void resume(){
        remote.getConnectApi().connectSwitchToLocalDevice();
        remote.getPlayerApi().resume();
    }

    public void getPlayedTime(){
        remote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
            backPlayer.getPlayedTime(playerState.playbackPosition);
        });
    }
}















//remote.getPlayerApi().play(trackUri).setResultCallback(empty ->{
//    remote.getPlayerApi().pause().setResultCallback(empty1 -> {
//        this.getCurrentTrack();
//    });
//});
//remote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
//    listener.onCurrentTrackReturned(playerState.track);
//});



//JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, this::foundResponse, new Response.ErrorListener() {
//    @Override
//    public void onErrorResponse(VolleyError volleyError) {
//        String message = null;
//        if (volleyError instanceof NetworkError) {
//            message = "Cannot connect to Internet...Please check your connection!";
//        } else if (volleyError instanceof ServerError) {
//            message = "The server could not be found. Please try again after some time!!";
//        } else if (volleyError instanceof AuthFailureError) {
//            message = "Cannot connect to Internet...Please check your connection!";
//        } else if (volleyError instanceof ParseError) {
//            message = "Parsing error! Please try again after some time!!";
//        } else if (volleyError instanceof NoConnectionError) {
//            message = "Cannot connect to Internet...Please check your connection!";
//        } else if (volleyError instanceof TimeoutError) {
//            message = "Connection TimeOut! Please check your internet connection.";
//        }
//        System.out.println(message);
//        System.out.println(volleyError.getLocalizedMessage());
//    }
//}){
//    @Override
//    public Map<String, String> getHeaders() throws AuthFailureError{
//        Map<String, String> headers = new ArrayMap<>();
//        headers.put("Authorization", "Bearer" + token);
//        return headers;
//    }};













/*

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(url, null,this::foundResponse, volleyError -> {
            String message = null;
            if (volleyError instanceof NetworkError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (volleyError instanceof ServerError) {
                message = "The server could not be found. Please try again after some time!!";
            } else if (volleyError instanceof AuthFailureError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (volleyError instanceof ParseError) {
                message = "Parsing error! Please try again after some time!!";
            } else if (volleyError instanceof NoConnectionError) {
                message = "Cannot connect to Internet...Please check your connection!";
            } else if (volleyError instanceof TimeoutError) {
                message = "Connection TimeOut! Please check your internet connection.";
            }
            System.out.println(message);
            System.out.println(volleyError.getLocalizedMessage());
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new ArrayMap<>();
                headers.put("Authorization", "Bearer" + token);
                return headers;
        }};
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());

            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new ArrayMap<>();
                headers.put("Authorization", "Bearer" + token);
                return headers;
            }
        };
        queue.add(sr);
        queue.start();


 */









