package com.soundbound.outer.API.songProviders.youTube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.soundbound.outer.API.songProviders.Models.SimpleSong;
import com.soundbound.outer.API.songProviders.phoneSongs.PhoneController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SimpleSongYTDownloader extends AsyncTask<Void,Integer, SimpleSong> {
    String id;
    SimpleSong returnVale;
    YTSingleSongListener listener;
    private com.google.api.services.youtube.YouTube service;
    private OkHttpClient client;

    public interface YTSingleSongListener {
        void onSongsSearched(SimpleSong song);
    }

    public SimpleSongYTDownloader(String id, YouTube service, YTSingleSongListener listener) {
        this.listener = listener;
        this.service = service;
        this.id = id;
    }

    @Override
    protected void onPreExecute() {
        client = new OkHttpClient();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected SimpleSong doInBackground(Void... voids) {
        try {
            //YouTube.Videos.List result = service.videos().list(name);
            //SearchListResponse result = service.search().list("Snippet").setQ(name).setType("video").setMaxResults(20l).setVideoCategoryId("10").execute();
            YouTube.Videos.List result = service.videos().list("snippet").setId(id);
            //System.out.println(result.toString());
            //int count = searchResult.size();
            //int i = 0;
            returnVale = new SimpleSong();
            returnVale.id = result.getId();
            String title = (String) result.get("title");
            String[] tab = title.split("-");
            if (tab.length != 2) {
                returnVale.title = title;
                returnVale.author = (String) result.get("channelTitle");
            } else {
                returnVale.title = tab[1];
                returnVale.author = tab[0];
            }
            returnVale.type = SimpleSong.Type.YOUTUBE;
            returnVale.album = (String) result.get("channelTitle");
            //TODO change address - yt images
            returnVale.cover = downloadBmp("https://i.ytimg.com/vi/"+returnVale.id+"/default.jpg");
            returnVale.duration = 100000;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(SimpleSong simpleSong) {
        listener.onSongsSearched(returnVale);
        super.onPostExecute(simpleSong);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private Bitmap downloadBmp(String url) {
        Request imageRequest = new Request.Builder().get().url(url).build();
        try {
            Response response = client.newCall(imageRequest).execute();
            InputStream is = response.body().byteStream();
            Bitmap tmp = BitmapFactory.decodeStream(is);
            int height = tmp.getHeight();
            int width = tmp.getWidth();
            return PhoneController.scaleBitmap(tmp, 200f / width, 200f / height);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
