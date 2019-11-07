package com.soundbound.phoneSongs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ProgressBar;

import com.soundbound.R;
import com.soundbound.SimpleSong;
import com.soundbound.player.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhoneController extends AsyncTask<Void, Integer, List<SimpleSong>> implements Player {
    private Bitmap exampleBitmap;
    Context context;
    int count;
    ProgressBar pb;
    MediaPlayer mediaPlayer;
    private List<SimpleSong> songList;
    private psRemoteControllerListener listener;
    public interface psRemoteControllerListener{
        void onSongsFound();
        void onSongsSearched(List<SimpleSong> songs);
    }



    public List<SimpleSong> getSongList() {
        return songList;
    }

    public PhoneController(Context context, psRemoteControllerListener listener){
        this.context = context;
        exampleBitmap = decodeSampledBitmapFromResource(context.getResources(), R.drawable.note_sign2, 200,200);
        int height = exampleBitmap.getHeight();
        int width = exampleBitmap.getWidth();
        exampleBitmap = scaleBitmap(exampleBitmap,200f/width, 200f/height);
        this.listener = listener;
    }

    public List<SimpleSong> querryForSongs(ProgressBar pb){
        this.pb = pb;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return songList;
    }
    public void searchForSongs(String name, ProgressBar pb){
       SongSearcher searcher = new SongSearcher(name);
       searcher.pb = pb;
       searcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //-------------------------------------------------------Overrides
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected void onPostExecute(List<SimpleSong> simpleSongs) {
        super.onPostExecute(simpleSongs);
        listener.onSongsFound();
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        pb.setProgress(values[0]);
        super.onProgressUpdate(values);
    }
    @Override
    protected List<SimpleSong> doInBackground(Void... params) {
        ContentResolver musicResolver = context.getContentResolver();
        List<SimpleSong> returnVale = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);
        int i=0;

        if(musicCursor!=null && musicCursor.moveToFirst()){
            count = musicCursor.getCount();
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int authorColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int duractionColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int coverColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int column = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do{
                publishProgress((int) ((i / (float) count) * 100));
                i++;
                String duration = musicCursor.getString(duractionColumn);
                String albumID = musicCursor.getString(coverColumn);

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumID));
                Bitmap cover;
                InputStream is;
                try {
                    //System.out.println(musicCursor.getString(column));
                    //System.out.println(musicCursor.getString(idColumn));
                    is = musicResolver.openInputStream(uri);
                    Bitmap tmp = BitmapFactory.decodeStream(is);
                    int height = tmp.getHeight();
                    int width = tmp.getWidth();
                    cover = PhoneController.scaleBitmap(tmp,200f/width, 200f/height);
                } catch (FileNotFoundException ignored) {
                    cover = exampleBitmap;
                }
                SimpleSong ss = new SimpleSong(
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(authorColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getString(column),
                        cover,
                        Long.parseLong(duration),
                        SimpleSong.Type.PHONE);
                returnVale.add(ss);
            }while(musicCursor.moveToNext());
        }
        songList = returnVale;
        return returnVale;
    }
    @Override
    protected void onCancelled(List<SimpleSong> simpleSongs) {
        super.onCancelled(simpleSongs);
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    class SongSearcher extends AsyncTask<Void, Integer, List<SimpleSong>>{
        String name;
        ProgressBar pb;
        List<SimpleSong> returnVale;
        SongSearcher(String name){
            this.name = name;
            returnVale = new ArrayList<>();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected List<SimpleSong> doInBackground(Void... voids) {
            int i=0;
            name = name.toLowerCase();
            List<SimpleSong> returnVale2 = new ArrayList<>();
            for(SimpleSong ss: songList){
                publishProgress((int) ((i / (float) songList.size()) * 100));
                i++;
                if(ss.title.toLowerCase().contains(name)){
                    returnVale.add(ss);
                }
                else{
                    if(ss.album.toLowerCase().contains(name) || ss.author.toLowerCase().contains(name)){
                        returnVale2.add(ss);
                    }
                }
            }
            returnVale.addAll(returnVale2);
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(List<SimpleSong> simpleSongs) {
            super.onPostExecute(simpleSongs);
            listener.onSongsSearched(returnVale);
        }
    }

    //------------Player
    @Override
    public void resume() {
        mediaPlayer.start();
    }

    @Override
    public void playAt(long time) {

    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        mediaPlayer.stop();
    }

    @Override
    public void play(SimpleSong ss) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(context,Uri.parse(ss.id));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    public static Bitmap scaleBitmap(Bitmap sticker, float scaleX, float scaleY){
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX,scaleY);
        return Bitmap.createBitmap(sticker,0,0,sticker.getWidth(),sticker.getHeight(),matrix, true);
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
/*
List<SimpleSong> returnVale = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int authorColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int duractionColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int coverColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            do{
                SimpleSong ss = new SimpleSong();
                ss.title = musicCursor.getString(titleColumn);
                ss.album = musicCursor.getString(albumColumn);

                ss.id = musicCursor.getString(idColumn);
                String duration = musicCursor.getString(duractionColumn);
                ss.duration = Long.parseLong(duration);
                ss.author = musicCursor.getString(authorColumn);
                String albumID = musicCursor.getString(coverColumn);

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumID));
                InputStream is;
                try {
                    is = musicResolver.openInputStream(uri);
                    Bitmap tmp = BitmapFactory.decodeStream(is);
                    int height = tmp.getHeight();
                    int width = tmp.getWidth();
                    ss.cover = scaleBitmap(tmp,200f/width, 200f/height);
                } catch (FileNotFoundException ignored) {
                    ss.cover = exampleBitmap;
                }
                returnVale.add(ss);
            }while(musicCursor.moveToNext());
        }

*/