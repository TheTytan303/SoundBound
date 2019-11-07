package com.soundbound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayerFragment;
import com.soundbound.phoneSongs.PhoneController;
import com.soundbound.player.SongPlayer;
import com.soundbound.recyclerView.SimpleSongViewAdapter;
import com.soundbound.spotifySongs.SpotifyController;
import com.soundbound.youtubeSongs.YoutubeConroller;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import net.openid.appauth.AuthorizationServiceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import pub.devrel.easypermissions.EasyPermissions;

public class SongHistoryOverviewActivity extends AppCompatActivity
        implements
        SpotifyController.RemoteControllerListener,
        PhoneController.psRemoteControllerListener,
        YoutubeConroller.youtubeListener,
        SimpleSongViewAdapter.songAdapterListener {


    //---------------------------------------------------------------------------------Values
    private static final int READ_EXTERNAL_STORAGE_PERMMISION_REQUEST_CODE=10;
    private static final int WRITE_EXTERNAL_STORAGE_PERMMISION_REQUEST_CODE=11;
    private static final int INTERNET_PERMMISION_REQUEST_CODE=12;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    int REQUEST_CODE = 1337;
    private List<SimpleSong> spotifyTrackHistory;
    private List<SimpleSong> phoneSongs;
    private List<SimpleSong> searchedSpotifySongs;
    private List<SimpleSong> searchedPhoneSongs;
    private List<SimpleSong> searchedYTSongs;
    private List<SimpleSong> history;
    private ConcurrentLinkedQueue<SimpleSong> queue;
    private ConstraintLayout currentFragment;
    SongPlayer player;

    private YoutubeConroller youtubeRemote;
    RecyclerView youtubeSongsRecyclerView;
    RecyclerView.Adapter youtubeSongsAdapter;
    RecyclerView.LayoutManager youtubeSongsLayoutManager;

    private ProgressBar loadingPB;

    private SpotifyController spotifyRemote;
    RecyclerView spotifyTrackHistoryRecyclerView;
    RecyclerView.Adapter spotifyTrackHistoryAdapter;
    RecyclerView.LayoutManager spotifyTraclHistoryViewLayoutManager;

    private PhoneController phoneRemote;
    RecyclerView phoneSongsRecyclerView;
    RecyclerView.Adapter phoneSongsAdapter;
    RecyclerView.LayoutManager phoneSongsLayoutManager;

    RecyclerView queueRecyclerView;
    SimpleSongViewAdapter queueAdapter;
    RecyclerView.LayoutManager queueLayoutManager;

    RecyclerView historyRecyclerView;
    SimpleSongViewAdapter historyAdapter;
    RecyclerView.LayoutManager historyLayoutManager;




    //---------------------------------------------------------------------------------Interfaces
    //---------------------------------------------------------------------------------Overrides
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_history_overview);
        getSupportActionBar().hide();
        this.queue = new ConcurrentLinkedQueue<>();
        phoneSongs = new ArrayList<>();
        findViewById(R.id.asho_player_ad).setOnClickListener(v -> Toast.makeText(getBaseContext(),"I don't give a fuck", Toast.LENGTH_SHORT).show());
        searchedPhoneSongs = new ArrayList<>();
        spotifyTrackHistory = new ArrayList<>();
        searchedSpotifySongs = new ArrayList<>();
        searchedYTSongs = new ArrayList<>();
        loadingPB = findViewById(R.id.asho_loadingPB);
        loadingPB.setVisibility(View.GONE);
        phoneRemote = new PhoneController(this, this);

        queueRecyclerView = findViewById(R.id.asho_comming_soon);
        queueAdapter = new SimpleSongViewAdapter(queue, this, queue);
        queueLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        queueRecyclerView.setLayoutManager(queueLayoutManager);
        queueRecyclerView.setAdapter(queueAdapter);


        history = new ArrayList<>();
        historyAdapter =new SimpleSongViewAdapter(history,this, queue);
        historyLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        historyRecyclerView = findViewById(R.id.asho_song_history);
        historyRecyclerView.setAdapter(historyAdapter);
        historyRecyclerView.setLayoutManager(historyLayoutManager);
        player = new SongPlayer(queue,history, historyAdapter,queueAdapter,
                findViewById(R.id.asho_cursong_title), findViewById(R.id.asho_cursong_author),
                findViewById(R.id.asho_player_civ), findViewById(R.id.asho_player_timebar),
                findViewById(R.id.asho_player_view));
        player.setPhoneRemote(phoneRemote);

        //Button getSong = findViewById(R.id.asho_refresh_button);
        //getSong.setVisibility(View.INVISIBLE);



       findViewById(R.id.get_phone_songs_button).setOnClickListener(v -> {
           if(!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
               requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE_PERMMISION_REQUEST_CODE);
               if(!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)){
                   Toast.makeText(getApplicationContext(), "Permission not granted yet :(", Toast.LENGTH_LONG).show();
                   return;
               }
           }
           if(!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
               requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_PERMMISION_REQUEST_CODE);
               if(!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                   Toast.makeText(getApplicationContext(), "Permission not granted yet :(", Toast.LENGTH_LONG).show();
                   return;
               }
           }
           findViewById(R.id.get_phone_songs_button).setVisibility(View.GONE);
           phoneSongs.clear();
           loadingPB.setVisibility(View.VISIBLE);
           phoneRemote.querryForSongs(loadingPB);
           //new Thread(phoneRemote).start();
       });
        //waitingForConnection();

        //---------YouTube
        String ytKey =getString(R.string.youtube_ID);
        YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youtubeRemote = new YoutubeConroller(this.getBaseContext(),this, youTubePlayerFragment, findViewById(R.id.player_parent));
        youTubePlayerFragment.initialize(ytKey, youtubeRemote);

        player.setYoutubeRemote(youtubeRemote);

        currentFragment = findViewById(R.id.asho_search_fragment);
        currentFragment.setVisibility(View.VISIBLE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    spotifyRemote.tokenGranted(response.getAccessToken());
                    break;
                case ERROR:
                    // Handle error response
                    break;
                default:
                    // Handle other cases
            }
        }
        else {
            switch(requestCode) {
                case REQUEST_GOOGLE_PLAY_SERVICES:
                    if (resultCode != RESULT_OK) {
                        System.out.println(
                                "This app requires Google Play Services. Please install " +
                                        "Google Play Services on your device and relaunch this app.");
                    } else {
                        youtubeRemote.connected();
                    }
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null &&
                            data.getExtras() != null) {
                        String accountName =
                                data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            SharedPreferences settings =
                                    getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_ACCOUNT_NAME, accountName);
                            editor.apply();
                            String name = accountName;
                            //mCredential.setSelectedAccountName(accountName);
                            youtubeRemote.connected(name);
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    if (resultCode == RESULT_OK) {
                        youtubeRemote.connected();
                    }
                    break;
                default: break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        pause(null);
    }


    @Override
    public void onQueueChanged() {
        queueAdapter.refreshData();
    }

    //------------ phone:
    @Override
    public void onSongsFound() {

        phoneSongs.addAll(phoneRemote.getSongList());
        searchedPhoneSongs.addAll(phoneSongs);
        loadingPB.setVisibility(View.GONE);
        //phoneSongsAdapter.notifyDataSetChanged();
        //---------Phone
        phoneSongsRecyclerView = findViewById(R.id.phone_songs);
        phoneSongsAdapter = new SimpleSongViewAdapter(searchedPhoneSongs, this, queue);
        phoneSongsLayoutManager = new GridLayoutManager(getBaseContext(), 1);
        phoneSongsRecyclerView.setLayoutManager(phoneSongsLayoutManager);
        phoneSongsRecyclerView.setAdapter(phoneSongsAdapter);
        //phoneRemote.cancel(true);
        //phoneRemote.play(searchedPhoneSongs.get(0));
    }

    //------------ Spotify:
    @Override
    public void onSongsSearched(List<SimpleSong> songs) {
        if(songs.size()==0)return;
        if(songs.get(0).type== SimpleSong.Type.SPOTIFY){
            searchedSpotifySongs.clear();
            searchedSpotifySongs.addAll(songs);
            spotifyTrackHistoryAdapter.notifyDataSetChanged();
            return;
        }
        if(songs.get(0).type==SimpleSong.Type.PHONE){
            searchedPhoneSongs.clear();
            searchedPhoneSongs.addAll(songs);
            phoneSongsAdapter.notifyDataSetChanged();
            return;
        }
        if(songs.get(0).type==SimpleSong.Type.YOUTUBE){
            searchedYTSongs.clear();
            searchedYTSongs.addAll(songs);
            youtubeSongsAdapter.notifyDataSetChanged();
            return;
        }

    }
    @Override
    public void onCurrentTrackReturned(Track track) {
        if(spotifyTrackHistory.size() == 0){
            spotifyTrackHistory.add(new SimpleSong(track, spotifyRemote.getPureRemote().getImagesApi()));
        }
        if(spotifyTrackHistory.get(spotifyTrackHistory.size()-1).title.compareTo(track.name) !=0){
            spotifyTrackHistory.add(new SimpleSong(track, spotifyRemote.getPureRemote().getImagesApi()));
        }
        searchedSpotifySongs.clear();
        searchedSpotifySongs.addAll(spotifyTrackHistory);
        spotifyTrackHistoryAdapter.notifyDataSetChanged();
    }
    @Override
    public void onTrackReturned(SimpleSong track) {
        if(spotifyTrackHistory.size() == 0){
            spotifyTrackHistory.add(track);
        }
        if(spotifyTrackHistory.get(spotifyTrackHistory.size()-1).title.compareTo(track.title) !=0){
            spotifyTrackHistory.add(track);
        }
        searchedSpotifySongs.clear();
        searchedSpotifySongs.addAll(spotifyTrackHistory);
        spotifyTrackHistoryAdapter.notifyDataSetChanged();
    }
    @Override
    public void connectionError(){
        findViewById(R.id.asho_connecting).setVisibility(View.INVISIBLE);
        findViewById(R.id.asho_connect_to_spotify).setVisibility(View.GONE);
        spotifyRemote = null;
    }
    @Override
    public void waitingForConnection() {
        findViewById(R.id.asho_connecting).setVisibility(View.VISIBLE);
        //Button bt = findViewById(R.id.asho_refresh_button);
        //bt.setClickable(false);
    }
    @Override
    public void connected() {
        findViewById(R.id.asho_connecting).setVisibility(View.GONE);
        //Button bt = findViewById(R.id.asho_refresh_button);
        //findViewById(R.id.asho_refresh_button).setVisibility(View.VISIBLE);
        //bt.setClickable(true);

        //---------Spotify
        findViewById(R.id.asho_connecting).setVisibility(View.GONE);
        spotifyTrackHistoryRecyclerView = findViewById(R.id.spotify_song_history);
        spotifyTrackHistoryAdapter = new SimpleSongViewAdapter(searchedSpotifySongs, this, queue);
        spotifyTrackHistoryRecyclerView.setAdapter(spotifyTrackHistoryAdapter);
        spotifyTraclHistoryViewLayoutManager = new GridLayoutManager(getBaseContext(), 1);
        spotifyTrackHistoryRecyclerView.setLayoutManager(spotifyTraclHistoryViewLayoutManager);
    }

    //------------YouTube:

    @Override
    public void tryToConnect() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                youtubeRemote.connected(accountName);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(youtubeRemote.getCredential().newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this,"This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    @Override
    public void connectedYT(){
        findViewById(R.id.connect_to_youtube).setVisibility(View.INVISIBLE);
        this.youtubeSongsRecyclerView = findViewById(R.id.yt_songs);
        youtubeSongsAdapter = new SimpleSongViewAdapter(searchedYTSongs, this, queue);
        youtubeSongsLayoutManager = new LinearLayoutManager(getBaseContext());
        youtubeSongsRecyclerView.setAdapter(youtubeSongsAdapter);
        youtubeSongsRecyclerView.setLayoutManager(youtubeSongsLayoutManager);

    }

    //---------------------------------------------------------------------------------Public
    //------------- Buttons:
    public void connectToSpotify(View v){
        if(spotifyRemote==null)
        spotifyRemote = new SpotifyController(this,getResources().getString(R.string.Client_ID),getResources().getString(R.string.Redirect_uri), this);
        findViewById(R.id.asho_connect_to_spotify).setVisibility(View.GONE);
        player.setSpotifyRemote(spotifyRemote);
    }
    public void onRefreshClick(View view){
        if(!checkPermission(Manifest.permission.INTERNET)){
            requestPermission(Manifest.permission.INTERNET, INTERNET_PERMMISION_REQUEST_CODE);
            if(!checkPermission(Manifest.permission.INTERNET)){
                Toast.makeText(getApplicationContext(), "Permission not granted yet :(", Toast.LENGTH_LONG).show();
                return;
            }
        }
        ClipboardManager clipboard =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(!clipboard.hasPrimaryClip()){
            spotifyRemote.getCurrentTrack();
        }else {
            ClipDescription cd = clipboard.getPrimaryClipDescription();
            if(cd.getLabel().toString().contains("otify")){
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String pasteData = item.getText().toString();
                String[] tab = pasteData.split("/");
                System.out.println("ds");
                clipboard.setPrimaryClip(ClipData.newPlainText("empty", ""));
                spotifyRemote.getTrackInfo( tab[tab.length-1].split("\\?")[0]);
            }
            else {
                spotifyRemote.getCurrentTrack();
            }
        }
    }
    public void onSearchClick(View view){
        EditText et = findViewById(R.id.asho_search_bar_et);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if(et.getText().toString().equals("")){
            return;
        }else {
            this.searchedPhoneSongs.clear();
            this.searchedSpotifySongs.clear();
            this.searchedYTSongs.clear();

            if(this.phoneSongsAdapter!=null)
            this.phoneSongsAdapter.notifyDataSetChanged();

            if(this.spotifyTrackHistoryAdapter!=null)
            this.spotifyTrackHistoryAdapter.notifyDataSetChanged();

            if(this.youtubeSongsAdapter!=null){
                this.youtubeSongsAdapter.notifyDataSetChanged();
            }
        }
        if(spotifyRemote!= null){
            ProgressBar pb =  findViewById(R.id.asho_spotify_part_progressBar);
            pb.setProgress(0);
            spotifyRemote.searchForTracks(et.getText().toString(),pb);
        }
        else{
            connectToSpotify(null);
        }
        if(phoneSongs.size()>0){
            ProgressBar pb = findViewById(R.id.asho_phone_part_progressBar);
            pb.setProgress(0);
            phoneRemote.searchForSongs(et.getText().toString(), pb);
        }
        if(youtubeRemote!=null && youtubeRemote.isConnected){
            youtubeRemote.search(et.getText().toString(), findViewById(R.id.asho_yt_part_progressBar));
        }
    }
    public void onSearchBack(View view){
        searchedSpotifySongs.clear();
        searchedSpotifySongs.addAll(spotifyTrackHistory);
        spotifyTrackHistoryAdapter.notifyDataSetChanged();

        searchedPhoneSongs.clear();
        searchedPhoneSongs.addAll(phoneSongs);
        phoneSongsAdapter.notifyDataSetChanged();
    }
    public void connectToYT(View view){
        youtubeRemote.connect();
    }
    public void play(View view){
        findViewById(R.id.asho_player_play).setVisibility(View.GONE);
        findViewById(R.id.asho_player_pause).setVisibility(View.VISIBLE);
        player.play();
        queueAdapter.refreshData();
    }
    public void pause(View view){
        findViewById(R.id.asho_player_play).setVisibility(View.VISIBLE);
        findViewById(R.id.asho_player_pause).setVisibility(View.GONE);
        player.pause();
        queueAdapter.refreshData();
    }
    public void skip(View view){
        ImageButton button = (ImageButton) view;
        button.setClickable(false);
        player.skip();
        button.setClickable(true);
    }
    public void tabSearch(View v){
        currentFragment.setVisibility(View.GONE);
        currentFragment = findViewById(R.id.asho_search_fragment);
        currentFragment.setVisibility(View.VISIBLE);
    }
    public void tabPlayer(View v){
        currentFragment.setVisibility(View.GONE);
        currentFragment = findViewById(R.id.asho_player_fragment);
        currentFragment.setVisibility(View.VISIBLE);
    }
    public void tabLists(View v){
        currentFragment.setVisibility(View.GONE);
        currentFragment = findViewById(R.id.asho_lists_fragment);
        currentFragment.setVisibility(View.VISIBLE);
    }

    public boolean checkPermission(@NonNull String permission){
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    public void requestPermission(@NonNull String permission, int code){
        ActivityCompat.requestPermissions(this, new String[] {permission},code);
    }
}
/*




* */