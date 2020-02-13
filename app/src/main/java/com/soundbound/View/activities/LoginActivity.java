package com.soundbound.View.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.soundbound.R;
import com.soundbound.outer.API.snbAPI.APIConnector;
import com.soundbound.outer.API.songProviders.Models.User;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void showCreate(View v){
        View tmp = findViewById(R.id.la_create);
        tmp.setVisibility(View.GONE);
        tmp = findViewById(R.id.la_login);
        tmp.setVisibility(View.VISIBLE);
        tmp = findViewById(R.id.la_create);
        tmp.setVisibility(View.VISIBLE);
        tmp = findViewById(R.id.la_login);
        tmp.setVisibility(View.GONE);
        //.setVisibility(View.GONE);
        // = findViewById(R.id.asho_player_fragment);
        //.setVisibility(View.VISIBLE);
    }
    public void create(View v){
        String name = ((EditText)findViewById(R.id.la_create_nickname)).getText().toString();
        String password = ((EditText)findViewById(R.id.la_create_password)).getText().toString();
        APIConnector connector = APIConnector.getInstance();
        User user;
        try {
            user = connector.createUser(name, password);

            Intent contin = new Intent(this, menuActivity.class);
            contin.putExtra("user", user);
            startActivity(contin);
            //System.out.println(user.getName());
        } catch (ExecutionException | InterruptedException |JSONException e) {
            e.printStackTrace();
        }
    }

    public void showLogin(View v){

        //.setVisibility(View.GONE);
        // = findViewById(R.id.asho_player_fragment);
        //.setVisibility(View.VISIBLE);
    }
}
