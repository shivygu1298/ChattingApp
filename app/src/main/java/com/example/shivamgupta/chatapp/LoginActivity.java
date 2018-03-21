package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogEmail;
    private EditText etLogPasword;

    private Button btnLogin;

    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;

    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etLogEmail = findViewById(R.id.etLogEmail);
        etLogPasword = findViewById(R.id.etLogPassword);

        btnLogin = findViewById(R.id.btnLogin);

        mDialog = new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etLogEmail.getText().toString();
                String password = etLogPasword.getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mDialog.setTitle("Logging In");
                    mDialog.setMessage("Please wait your credentials are being verified");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    
                    loginUser(email , password);
                }
            }
        });

        //Tool Bar
        mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Login Page");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mDialog.dismiss();

                    Intent i = new Intent(LoginActivity.this , MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    mDialog.hide();
                    Toast.makeText(LoginActivity.this, "Cannot sign in user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
