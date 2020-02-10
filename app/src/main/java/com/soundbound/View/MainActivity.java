package com.soundbound.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.services.youtube.YouTubeScopes;

import com.soundbound.R;
import com.soundbound.SongHistoryOverviewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity  {
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private String PREF_ACCOUNT_NAME = "accountName";

    GoogleAccountCredential mCredential;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String ytKey = getString(R.string.youtube_ID);



    }
    public void but(View v){
        Intent contin = new Intent(this, Main2Activity.class);
        startActivity(contin);
    }
    public void goOn(View v){
        Intent contin = new Intent(this, SongHistoryOverviewActivity.class);
        startActivity(contin);
    }
}
/*
 YouTubePlayerFragment fragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        fragment.initialize(ytKey, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if(!b){
                    youTubePlayer.cueVideo("nCgQDjiotG0");
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());




        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth")
                Uri.parse("https://www.googleapis.com/oauth2/v4/token")
        );

        YouTubePlayerFragment fragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        fragment.initialize(ytKey, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if(!b){
                    youTubePlayer.cueVideo("nCgQDjiotG0");
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
 */