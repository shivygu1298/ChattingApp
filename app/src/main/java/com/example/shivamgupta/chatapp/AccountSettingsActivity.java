package com.example.shivamgupta.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;

    private FirebaseUser currentUser;
    private DatabaseReference mDataBase;
    private StorageReference mStorageRef;

    TextView tvDisplayName;
    TextView tvStatus;

    CircleImageView ivProfileImage;

    Button btnChangeStaus;
    Button btnChangeImage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvStatus = findViewById(R.id.tvStatus);

        ivProfileImage = findViewById(R.id.ivProfileImage);

        btnChangeStaus = findViewById(R.id.btnChangeStaus);
        btnChangeImage = findViewById(R.id.btnChangeImage);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = currentUser.getUid();

        mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDataBase.keepSynced(true);

        mDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();
                final String image = dataSnapshot.child("Image").getValue().toString();

                if (!image.equals("default")) {
                    Picasso.with(AccountSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.dp_androidnew).into(ivProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with((AccountSettingsActivity.this)).load(image).placeholder(R.drawable.dp_androidnew).into(ivProfileImage);
                        }
                    });
                }

                tvDisplayName.setText(name);
                tvStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnChangeStaus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_new = tvStatus.getText().toString();

                Intent i = new Intent(AccountSettingsActivity.this , StatusUpdateActivity.class);
                i.putExtra("new_status" , status_new);
                startActivity(i);
            }
        });

        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i , "SELECT IMAGE") ,RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1 , 1)
                    .start(this);
            //Toast.makeText(this, "" + imageUri, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(AccountSettingsActivity.this);
                mProgressDialog.setTitle("Loading");
                mProgressDialog.setMessage("Plaes wait");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filepath = new File(resultUri.getPath());

                String current_userid = currentUser.getUid();

                Bitmap thumb_image = null;
                try {
                    thumb_image = new Compressor(this).setMaxWidth(200).setMaxHeight(200)
                            .setQuality(75).compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = mStorageRef.child("profile_images").child(current_userid + ".jpg");
                final StorageReference thumb_filePath = mStorageRef.child("profile_images")
                        .child("thumbs").child(current_userid + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    String thumb_downloadURL = task.getResult().getDownloadUrl().toString();

                                    if (task.isSuccessful()){

                                        Map update_hashmap = new HashMap();
                                        update_hashmap.put("Image" , download_url);
                                        update_hashmap.put("Thumb_image" , thumb_downloadURL);

                                        mDataBase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(AccountSettingsActivity.this,
                                                "Error in thumb", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(AccountSettingsActivity.this,
                                    "Error", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser != null) {
            mDataBase.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(currentUser != null) {
            mDataBase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
