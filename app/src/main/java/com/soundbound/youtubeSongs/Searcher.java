package com.soundbound.youtubeSongs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.google.api.services.youtube.YouTube;
import com.soundbound.SimpleSong;

import com.google.api.services.youtube.model.*;
import com.soundbound.outer.API.songProviders.phoneSongs.PhoneController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Searcher extends AsyncTask<Void,Integer, List<SimpleSong>> {
    String name;
    List<SimpleSong> returnVale;
    private com.google.api.services.youtube.YouTube service;
    private OkHttpClient client;
    YTSearcherListener listener;
    private ProgressBar pb;

    public interface YTSearcherListener {
        void onSongsSearched(List<SimpleSong> songs);
    }

    Searcher(String name, YouTube service, YTSearcherListener listener, ProgressBar progressBar) {
        this.pb = progressBar;
        this.listener = listener;
        this.service = service;
        this.name = name;
        returnVale = new ArrayList<>();
    }

    @Override
    protected void onPreExecute() {
        client = new OkHttpClient();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pb.setProgress(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected List<SimpleSong> doInBackground(Void... voids) {
        try {
            //YouTube.Videos.List result = service.videos().list(name);
            SearchListResponse result = service.search().list("Snippet").setQ(name).setType("video").setMaxResults(20l).setVideoCategoryId("10").execute();
            System.out.println(result.toString());
            List<SearchResult> searchResult = result.getItems();
            int count = searchResult.size();
            int i = 0;
            for (SearchResult sr : searchResult) {
                SimpleSong tmp = new SimpleSong();
                SearchResultSnippet snippet = sr.getSnippet();
                tmp.type = SimpleSong.Type.YOUTUBE;
                String title = snippet.getTitle();
                String[] tab = title.split("-");
                if (tab.length != 2) {
                    tmp.title = snippet.getTitle();
                    tmp.author = snippet.getChannelTitle();
                } else {
                    tmp.title = tab[1];
                    tmp.author = tab[0];
                }
                tmp.album = snippet.getChannelTitle();
                tmp.cover = downloadBmp(snippet.getThumbnails().getDefault().getUrl());
                tmp.id = sr.getId().getVideoId();
                tmp.duration = 100000;
                returnVale.add(tmp);
                i++;
                publishProgress((int) ((i / (float) count) * 100));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<SimpleSong> simpleSongs) {
        listener.onSongsSearched(returnVale);
        super.onPostExecute(simpleSongs);
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