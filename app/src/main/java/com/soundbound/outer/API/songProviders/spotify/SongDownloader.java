package com.soundbound.outer.API.songProviders.spotify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.soundbound.outer.API.songProviders.Models.SimpleSong;
import com.soundbound.outer.API.songProviders.phoneSongs.PhoneController;
import com.spotify.android.appremote.api.ImagesApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SongDownloader{
    private String MARKET = "PL";
    private String LIMIT = "20";
    private String token;
    private ImagesApi imagesApi;
    private OkHttpClient client;
    private SongDowloaderListener sdl;
    public interface SongDowloaderListener{
        void onSingleSongReturned(SimpleSong ss);
        void onSongsSearched(List<SimpleSong> songs);
    }
    SongDownloader(String token, ImagesApi ia, SongDowloaderListener songDowloaderListener, String market){
        this.MARKET = market;
        this.sdl = songDowloaderListener;
        this.token = token;
        this.imagesApi = ia;
        client = new OkHttpClient();
    }

    public void setToken(String token) {
        this.token = token;
    }

    public class SingleSongDownloader extends AsyncTask<Context, Integer, SimpleSong> {
        String trackUri;
        SimpleSong response;
        public SingleSongDownloader(String trackuri, ImagesApi ia) {
            trackUri = trackuri;
            //token = clienttoken;
            imagesApi = ia;
            client = new OkHttpClient();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected SimpleSong doInBackground(Context... contexts) {
            String url = "https://api.spotify.com/v1/tracks/"+ trackUri;
            System.out.println(url);
            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
            httpBuilder.addQueryParameter("market", MARKET);
            Request request = new Request.Builder().url(httpBuilder.build()).addHeader("Authorization" , "Bearer " + token).get().build();
            try(Response response = client.newCall(request).execute()){
                if(response.code()==200){
                    this.response = deJsonToSS(response.body().string());
                }
                else {
                    System.out.println(response.body().string());
                }
                return this.response;
            }catch (Exception e){
                System.out.println("expection");
            }
            return null;
        }

        @Override
        protected void onPostExecute(SimpleSong simpleSong) {
            super.onPostExecute(simpleSong);
            sdl.onSingleSongReturned(this.response);
        }
    }

    public class SongSearcher extends AsyncTask<Context, Integer, List<SimpleSong>> {
        String name;
        List<SimpleSong> results;
        ProgressBar pb;
        SongSearcher(String name, ProgressBar pb){
            this.name = name;
            results = new ArrayList<>();
            this.pb = pb;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<SimpleSong> doInBackground(Context... contexts) {
            String url = "https://api.spotify.com/v1/search";
            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
            httpBuilder.addQueryParameter("q", name);
            httpBuilder.addQueryParameter("type", "track");
            httpBuilder.addQueryParameter("limit", LIMIT);
            httpBuilder.addQueryParameter("market", MARKET);
            Request request = new Request.Builder().url(httpBuilder.build()).addHeader("Authorization" , "Bearer " + token).get().build();
            try(Response response = client.newCall(request).execute()){
                System.out.println("spotify backgound");
                if(response.code()==200) {
                    results.clear();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    jsonObject = jsonObject.getJSONObject("tracks");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    int count = jsonArray.length();
                    for(int i=0; i<jsonArray.length();i++){
                        publishProgress((int) ((i / (float) count) * 100));
                        results.add(deJsonToSS(jsonArray.getJSONObject(i).toString()));
                    }
                    System.out.println("response");

                }
                else {
                    System.err.println(response.body().string());
                }
                return null;
            }catch (Exception e){
                System.out.println("expection");
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<SimpleSong> simpleSongs) {
            super.onPostExecute(simpleSongs);
            if(results.size()>0)
            sdl.onSongsSearched(results);
            pb.setProgress(100);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[0]);
        }
    }


    void downloadSong(String trackUri, Context context){
        SingleSongDownloader ssd = new SingleSongDownloader(trackUri, imagesApi);
        ssd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void searchForSongs(String name, Context context, ProgressBar pb){
        SongSearcher songSearcher = new SongSearcher(name, pb);
        songSearcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private SimpleSong deJsonToSS(String JSON){
        SimpleSong returnVale = new SimpleSong();
        try {
            JSONObject jsonObject = new JSONObject(JSON);
            returnVale.title = jsonObject.getString("name");
            JSONObject album = jsonObject.getJSONObject("album");
            returnVale.album =  album.getString("name");
            returnVale.duration =jsonObject.getLong("duration_ms");
            returnVale.type = SimpleSong.Type.SPOTIFY;
            returnVale.id = jsonObject.getString("id");
            JSONArray ja = jsonObject.getJSONArray("artists");
            returnVale.author = ja.getJSONObject(0).getString("name");
            JSONArray imageUris = album.getJSONArray("images");
            JSONObject singleImageUri = imageUris.getJSONObject(0);
            String imageURL =(String) singleImageUri.get("url");
            Request imageRequest =new Request.Builder().get().url(imageURL).build();
            try{
                Response response = client.newCall(imageRequest).execute();
                InputStream is = response.body().byteStream();
                Bitmap tmp = BitmapFactory.decodeStream(is);
                int height = tmp.getHeight();
                int width = tmp.getWidth();
                returnVale.cover = PhoneController.scaleBitmap(tmp,200f/width, 200f/height);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnVale;
    }
}
