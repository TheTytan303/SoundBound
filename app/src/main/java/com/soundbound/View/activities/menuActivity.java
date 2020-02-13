package com.soundbound.View.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.soundbound.R;
import com.soundbound.outer.API.snbAPI.APIConnector;
import com.soundbound.outer.API.songProviders.Models.Room;
import com.soundbound.outer.API.songProviders.Models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class menuActivity extends AppCompatActivity {
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.user = (User)getIntent().getSerializableExtra("user");
        System.out.println("damn");
    }



    public void join(View v){
        String id = ((EditText)findViewById(R.id.am_join_room_id)).getText().toString();
        String token = ((EditText)findViewById(R.id.am_join_room_token)).getText().toString();
        Room room = null;
        try {
            room = APIConnector.getInstance().joinRoom(id,token,user);
        } catch (Exception e) {
            ((TextView)findViewById(R.id.am_error_tv)).setText(e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println("damn");
        Intent contin = new Intent(this, SongHistoryOverviewActivity.class);
        contin.putExtra("host", false);
        contin.putExtra("room", room);
        contin.putExtra("user", user);
        startActivity(contin);
    }

    public void create(View v){
        String title = ((EditText)findViewById(R.id.am_create_room_name)).getText().toString();
        Room room = APIConnector.getInstance().createRoom(title,user);
        System.out.println("damn");
        Intent contin = new Intent(this, SongHistoryOverviewActivity.class);
        contin.putExtra("host", true);
        contin.putExtra("room", room);
        contin.putExtra("user", user);
        startActivity(contin);

    }
}
