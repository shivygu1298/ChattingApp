package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText etDisplayName;
    EditText etEmail;
    EditText etPassword;
    Button btnContinue;

    private DatabaseReference mDataBase;
    private FirebaseAuth mAuth;

    private Toolbar mToolBar;

    private ProgressDialog mprogressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //FireBase
        mAuth = FirebaseAuth.getInstance();

        //Progrees Dialog Box
        mprogressDialog = new ProgressDialog(this);

        //Edit Text
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        
        btnContinue = findViewById(R.id.btnContinue);
        
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = etDisplayName.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(password)){

                    mprogressDialog.setTitle("Registering User");
                    mprogressDialog.setMessage("Please wait while we register your account");
                    mprogressDialog.setCanceledOnTouchOutside(false);
                    mprogressDialog.show();

                    register_user(displayName , email , password);
                }

            }
        });

        //Tool Bar
        mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Register Page");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void register_user(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = currentUser.getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                            HashMap<String , String> userMap = new HashMap<>();

                            userMap.put("Name" , displayName);
                            userMap.put("Status" , "Hi there i am using Bhatsapp");
                            userMap.put("Image" , "default");
                            userMap.put("Thumb_image" , "default");
                            userMap.put("device_token" , device_token);

                            mDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mprogressDialog.dismiss();
                                        Intent i = new Intent(RegisterActivity.this , MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            });




                }else{
                    mprogressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot sign in user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
