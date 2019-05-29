package com.example.shashanksinha.photoblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class NewPostActivity extends AppCompatActivity {

    private Toolbar newPostToolbar;

    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;

    private Uri postImageUri = null;

    private ProgressDialog newpost_progress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private String currentUserId;

    private Bitmap compressedImageFile;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // Importing UI...
        newPostToolbar = findViewById(R.id.new_post_toolbar);
        newPostImage = findViewById(R.id.add_new_post_img);
        newPostDesc = findViewById(R.id.add_desc_new_post);
        newPostBtn = findViewById(R.id.add_post_btn);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        newpost_progress = new ProgressDialog(this);


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        newPostToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Getting Post Image...
        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setMinCropResultSize(512,512)
                        .start(NewPostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc = newPostDesc.getText().toString();

                if(!TextUtils.isEmpty(desc) && postImageUri != null)
                {

                    newpost_progress.setTitle("Posting Blog");
                    newpost_progress.setMessage("Please wait while we post your blog!");
                    newpost_progress.setCanceledOnTouchOutside(false);
                    newpost_progress.setCancelable(false);
                    newpost_progress.show();


                    // Generating Random String for file name
                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = storageReference.child("post_images").child(randomName+".jpg");

                    filePath.putFile(postImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if(task.isSuccessful())
                            {
                                Uri downloadUri = task.getResult();

                                compressBitmap(desc, randomName, downloadUri);


                            }
                        }
                    });


                } else {

                    Toast.makeText(NewPostActivity.this,"Image / Description field cannot be empty.",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void compressBitmap(final String desc, String randomName, final Uri downloadUri) {

        File newImageFile = new File(postImageUri.getPath());

        try {

            // Reducing Image Quality...
            compressedImageFile = new Compressor(NewPostActivity.this)
                    .setMaxWidth(100)
                    .setMaxHeight(100)
                    .setQuality(2)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {

            String errorMessage = e.getMessage();
            Toast.makeText(NewPostActivity.this,"Upload Error 1 : "+errorMessage,Toast.LENGTH_LONG).show();

        }



        // Getting downloadable Thumb Uri...

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] thumbData = baos.toByteArray();



        // File path for Thumb Image...

        final StorageReference thumbFilePath = storageReference.child("post_images/thumbs").child(randomName+".jpg");

        //Uploading of thumb img...
        final UploadTask uploadTask = thumbFilePath.putBytes(thumbData);

        //Getting URI of thumb img...
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return thumbFilePath.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull final Task<Uri> task) {


                if(task.isSuccessful())
                {

                    // Downloadable Thumb URI...
                    final Uri thumb_Uri = task.getResult();


                    // Success Listener for uploading Thumb URI...
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            Toast.makeText(NewPostActivity.this,"The Image is Uploaded",Toast.LENGTH_LONG).show();
                            storeFirestore(desc,thumb_Uri,downloadUri);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            String errorMessage = e.getMessage();
                            Toast.makeText(NewPostActivity.this,"Upload Error 2 : "+errorMessage,Toast.LENGTH_LONG).show();

                        }
                    });

                } else {

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(NewPostActivity.this,"Upload Error 3 : "+errorMessage,Toast.LENGTH_LONG).show();

                }

            }
        });



    }



    private void storeFirestore(String desc, Uri downloadThumbUri, Uri downloadUri) {





        // Storing data on Firestore...

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_uri",downloadUri.toString());
        postMap.put("image_thumb",downloadThumbUri.toString());
        postMap.put("desc",desc);
        postMap.put("user_id",currentUserId);
        postMap.put("timestamp",FieldValue.serverTimestamp());


        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if(task.isSuccessful())
                {

                    Toast.makeText(NewPostActivity.this,"Post Added!",Toast.LENGTH_SHORT).show();
                    sendToMain();

                } else {

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(NewPostActivity.this,"Upload Error 5 : "+errorMessage,Toast.LENGTH_LONG).show();
                }

                newpost_progress.dismiss();

            }
        });
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();

                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }


}
