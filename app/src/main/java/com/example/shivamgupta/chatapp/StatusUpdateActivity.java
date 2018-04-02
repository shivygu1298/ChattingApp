package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusUpdateActivity extends AppCompatActivity {

    private EditText etChangeStatus;
    private Button btnChangeNewStatus;

    private FirebaseUser currentUser;
    private DatabaseReference mDataBase;

    private Toolbar mToolBar;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        mDialog = new ProgressDialog(this);

        String old_status = getIntent().getStringExtra("new_status");

        etChangeStatus = findViewById(R.id.etNewStatus);
        btnChangeNewStatus = findViewById(R.id.btnChangeNewStatus);

        etChangeStatus.setText(old_status);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //Tool Bar
        mToolBar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnChangeNewStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Progress
                mDialog.setTitle("Changing Status");
                mDialog.setMessage("Please wait your Status is being Changed");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                String status = etChangeStatus.getText().toString();

                mDataBase.child("Status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mDialog.dismiss();
                        }
                        else{
                            Toast.makeText(StatusUpdateActivity.this, "An error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}
