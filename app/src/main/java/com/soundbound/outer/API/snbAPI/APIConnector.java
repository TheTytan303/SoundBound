package com.soundbound.outer.API.snbAPI;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.soundbound.outer.API.requestHelper.Expections.ConnectionException;
import com.soundbound.outer.API.requestHelper.RequestTask;
import com.soundbound.outer.API.requestHelper.Requester;
import com.soundbound.outer.API.songProviders.Models.Room;
import com.soundbound.outer.API.songProviders.Models.SimpleSong;
import com.soundbound.outer.API.songProviders.Models.User;
import com.soundbound.outer.API.songProviders.spotify.SpotifyController;
import com.soundbound.outer.API.songProviders.youTube.SimpleSongYTDownloader;
import com.soundbound.outer.API.songProviders.youTube.YoutubeConroller;
import com.spotify.android.appremote.api.ImagesApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIConnector {

    private static String API = "http://192.168.1.9:8080";
    private static APIConnector instance;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Request request;
    private OkHttpClient client;


    public interface ConnectorListener{
        void onVotesCollected(Map<String, SimpleSong> votes);
        void errorOcurred(Exception e);
    }
    public static APIConnector getInstance(){
        if(instance == null){
            instance = new APIConnector();
        }
        return instance;
    };

    public User createUser(String nickname, String password) throws ExecutionException, InterruptedException, JSONException {
        String body = "{\n" +
                "  \"nickname\": \""+nickname+"\",\n" +
                "  \"password\": \""+password+"\"\n" +
                "}";
        String url = API + "/user";
        RequestTask request = new RequestTask(RequestTask.RequestType.POST,url,body,null, null);
        Future<String> futureResponse = Requester.getResponse(request);
        User returnVale;
        JSONObject jsonObject = new JSONObject(futureResponse.get());
        returnVale = new User(jsonObject.getInt("id"),nickname,password);
        return authorizeUser(returnVale);
    }

    public User authorizeUser(User user) throws ExecutionException, InterruptedException {
        String body = "{\n" +
                "  \"id\": \""+user.getId()+"\",\n" +
                "  \"password\": \""+user.getPassword()+"\"\n" +
                "}";
        String url = API + "/authorize";
        RequestTask request = new RequestTask(RequestTask.RequestType.POST,url,body,null, null);
        Future<String> futureResponse = Requester.getResponse(request);
        user.setToken(futureResponse.get());
        return user;
    }

    public SimpleSong addSong(SimpleSong song){
        String type = typeToString(song.type);
        String body =
                "{\n" +
                        "  \"id\": \""+type+song.id+"\",\n" +
                        "  \"title\": \""+song.title+"\",\n" +
                        "  \"artist\": \""+song.author+"\",\n" +
                        "  \"duration\": \""+song.duration+"\"\n" +
                        "}";
        String url = API + "/song";
        RequestTask request = new RequestTask(RequestTask.RequestType.POST,url,body,null, null);
        Requester.getResponse(request);
        return song;
    }

    public void vote(User user, Room room, SimpleSong song){
        String type = typeToString(song.type);
        String body = "{\n" +
                "    \"song_id\": \"" + type+song.id + "\"\n" +
                "}";
        String url = API + "/room/"+room.getId()+"/vote";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization",user.getToken());
        addSong(song);
        RequestTask request = new RequestTask(RequestTask.RequestType.PUT,url,body,headers,null);
        Requester.getResponse(request);
    }

    public void getVotes(User user, Room room, ConnectorListener listener, YoutubeConroller conroller, SpotifyController spotifyController){
        String url = API + "/room/"+room.getId()+"/vote";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization",user.getToken());
        RequestTask request = new RequestTask(RequestTask.RequestType.GET,url,"",headers,null);
        Future<String> response = Requester.getResponse(request);
        VotesCollector collector = new VotesCollector(response, listener, conroller, spotifyController);
        collector.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public Room createRoom(String name, User host){
        String url = API + "/room";
        String body ="{\n" +
                "  \"name\": \""+name+"\"\n" +
                "}";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization",host.getToken());
        RequestTask request = new RequestTask(RequestTask.RequestType.POST,url,body,headers,null);
        Future<String> response = Requester.getResponse(request);
        Room returnVale = null;
        try {
            returnVale = deJSONRoom(response.get());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return returnVale;
    }
    public String getRoomToken(Room room, User host){
        String url = API + "/room/"+room.getId()+"/owner/token";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization",host.getToken());
        RequestTask request = new RequestTask(RequestTask.RequestType.GET,url,"",headers,null);
        Future<String> futureResponse = Requester.getResponse(request);
        try {
            JSONObject json = new JSONObject(futureResponse.get());
            return json.getString("owner_token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Room joinRoom(String id, String token, User client) throws ExecutionException, InterruptedException, JSONException {
        String url = API + "/room/"+id+"/owner";
        String body ="{\n" +
                "  \"owner_token\": \""+token+"\"\n" +
                "}";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization",client.getToken());
        RequestTask request = new RequestTask(RequestTask.RequestType.PUT,url,body,headers,null);
        Future<String> response = Requester.getResponse(request);
        Room returnVale = null;
        returnVale = deJSONRoom(response.get());
        //TODO service already joined
        return returnVale;
    }
    private Room deJSONRoom(String JSON) throws JSONException {
        JSONObject json = new JSONObject(JSON);
        Room returnVale = new Room(json.getString("name"), json.getInt("id"));
        returnVale.setOwner(json.getString("host"));
        return returnVale;
    }


    private class VotesCollector extends AsyncTask<Context, Integer, Map<String, SimpleSong>>
        implements SimpleSongYTDownloader.YTSingleSongListener {
        Map<String, String> map;
        ConnectorListener listener;
        Map<String, SimpleSong> votes;
        Future<String> response;
        YoutubeConroller conroller;
        SpotifyController spotifyController;
        VotesCollector(Future<String> response,ConnectorListener listener, YoutubeConroller conroller, SpotifyController spotifyController){
            this.listener =listener;
            this.response = response;
            this.conroller = conroller;
            this.spotifyController = spotifyController;
            //JSONObject(response.get());
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, SimpleSong> doInBackground(Context... contexts) {
            Type mapType = new TypeToken<Map<String, String>>(){}.getType();
            try {
                map = new Gson().fromJson(response.get(), mapType);
            } catch (Exception e) {
                //e.printStackTrace();
                listener.errorOcurred(e);
                return null;
            }
            for(Map.Entry<String, String> entry: map.entrySet()){
                String[] tab = entry.getValue().split("_");
                if(tab[0].compareTo("spotify") == 0){
                    spotifyController.getTrackInfo(tab[1]);
                }else {
                    if(tab[0].compareTo("youtube") == 0){
                        conroller.loadSong(tab[1], this);
                    }else{
                        if(tab[0].compareTo("phone") == 0){
                            //TODO search phone songs
                        }
                    }
                }

            }
            return votes;
        }
        @Override
        protected void onPostExecute(Map<String, SimpleSong> stringSimpleSongMap) {
            super.onPostExecute(stringSimpleSongMap);
            listener.onVotesCollected(votes);
        }

        @Override
        public void onSongsSearched(SimpleSong song) {
            for(Map.Entry<String, String > entry: map.entrySet()){
                if(entry.getValue().compareTo(typeToString(song.type)+song.id) == 0){
                    votes.put(entry.getKey(),song);
                }
            }
        }
    }
    private String typeToString(SimpleSong.Type type){
        switch (type){
            case YOUTUBE:
                return "youtube_";
            case SPOTIFY:
                return "spotify_";
            default:
            case PHONE:
                return  "phone_";
        }
    }
}
