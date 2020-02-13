package com.soundbound.outer.API.songProviders.youTube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
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
            //SearchListResponse result = service.search().list("Snippet").setQ(name).setType("video").setMaxResults(20l).setVideoCategoryId("10").execute();
            VideoListResponse result = service.videos().list("snippet").setId(id).execute();
            returnVale = new SimpleSong();
            returnVale.type = SimpleSong.Type.YOUTUBE;
            for(Video v: result.getItems()){
                returnVale.id =v.getId();
                String title = v.getSnippet().getTitle();
                String[] tab = title.split("-");
                if (tab.length < 2) {
                    returnVale.title = title;
                    returnVale.author = (String) result.get("channelTitle");
                } else {
                    returnVale.title = tab[1];
                    if(tab.length > 2)
                    for(int i =2 ;i<tab.length;i++){
                        returnVale.title =returnVale.title.concat(tab[i]);
                    }
                    returnVale.author = tab[0];
                }
                returnVale.album = v.getSnippet().getChannelTitle();
                returnVale.cover = downloadBmp(v.getSnippet().getThumbnails().getStandard().getUrl());
                //returnVale.cover = downloadBmp("https://i.ytimg.com/vi/"+returnVale.id+"/default.jpg");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVale;
    }

    @Override
    protected void onPostExecute(SimpleSong simpleSong) {
        listener.onSongsSearched(returnVale);
        super.onPostExecute(returnVale);
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
