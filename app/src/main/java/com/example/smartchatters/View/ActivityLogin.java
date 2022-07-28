package com.example.smartchatters.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.smartchatters.R;

public class ActivityLogin extends AppCompatActivity implements  View.OnClickListener {

    private EditText username;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        username=(EditText) findViewById(R.id.usernameInputField);
        login= (Button)  findViewById(R.id.loginButton);
        setListener();

    }


    @Override
    public void onClick(View v){
        if (v.getId() == R.id.loginButton) {
            String user = username.getText().toString();
            if (!user.trim().equals("")){
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("username",user);
                startActivity(intent);
            }
        }
    }

    private void setListener() {
        login.setOnClickListener(this);
    }

}