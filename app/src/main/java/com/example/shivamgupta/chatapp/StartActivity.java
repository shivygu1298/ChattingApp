package com.example.shivamgupta.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    Button btnNewAccount;
    Button btnLoginCont;

    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Welcome");

        btnNewAccount = findViewById(R.id.btnNewAccount);
        btnLoginCont = findViewById(R.id.btnLoginCont);

        btnNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartActivity.this , RegisterActivity.class);
                startActivity(i);
            }
        });

        btnLoginCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent j = new Intent(StartActivity.this , LoginActivity.class);
                startActivity(j);
            }
        });
    }
}
