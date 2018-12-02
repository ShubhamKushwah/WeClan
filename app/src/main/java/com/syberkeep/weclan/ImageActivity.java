package com.syberkeep.weclan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import id.zelory.compressor.Compressor;

public class ImageActivity extends AppCompatActivity {

    private Context thisContext = this;
    private static final int GALLERY_PICK_KEY = 101;
    private ImageButton mBtnCamera;
    private ImageButton mBtnGallery;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private ImageView mImageAvatarView;

    //Firebase
    private StorageReference mStorageRef;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseRef;
    private String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        init();
    }

    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_image_activity);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Select an image");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBtnCamera = (ImageButton) findViewById(R.id.btn_img_camera);
        mBtnGallery = (ImageButton) findViewById(R.id.btn_img_gallery);
        mImageAvatarView = (ImageView)findViewById(R.id.image_final_selected_avatar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uId = mCurrentUser.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);

        mProgressDialog = new ProgressDialog(this);

        mBtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * Texture view can also be used here.
                 */

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select an image"), GALLERY_PICK_KEY);
            }
        });
    }

    //Image Variable
    String image = "default";

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                image = dataSnapshot.child("profile_avatar").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(thisContext, "Failure!", Toast.LENGTH_SHORT).show();
            }
        });

        if(!image.equals("default")){
            //Network policy used for Firebase Offline capabilities.
            Picasso.with(thisContext).load(image).placeholder(R.mipmap.ic_launcher).networkPolicy(NetworkPolicy.OFFLINE).into(mImageAvatarView, new Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(thisContext, "Image shown!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    Picasso.with(thisContext).load(image).placeholder(R.mipmap.ic_launcher).into(mImageAvatarView);
                    Toast.makeText(thisContext, "Offline failed! Trying again!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK_KEY && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .start(this);

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                mProgressDialog.setTitle("Uploading");
                mProgressDialog.setMessage("Please wait uploading your avatar...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                final Uri resultUri = result.getUri();

                // - - - COMPRESSION STARTS HERE - - -

                final File thumb_file_path = new File(resultUri.getPath());

                Bitmap thumbBitmap = null;

                try{
                    thumbBitmap = new Compressor(thisContext)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_file_path);
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                // - - - COMPRESSION ENDS HERE - - -

                StorageReference filePathRef = mStorageRef.child("Avatar").child(uId + ".jpg");

                final StorageReference thumbFilePathRef = mStorageRef.child("Avatar").child("thumbnail").child(uId + ".jpg");

                filePathRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbFilePathRef.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                    String thumb_download_url = thumbTask.getResult().getDownloadUrl().toString();

                                    if(thumbTask.isSuccessful()){

                                        Map updateMap = new HashMap();
                                        updateMap.put("profile_avatar", downloadUrl);
                                        updateMap.put("thumbnail", thumb_download_url);

                                        mDatabaseRef.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                mProgressDialog.dismiss();
                                                if(task.isSuccessful()){
                                                    Toast.makeText(ImageActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();

                                                    //Displaying the image
                                                    String path = resultUri.getPath();

                                                    if(path != null){
                                                        Bitmap bmp = BitmapFactory.decodeFile(path);
                                                        mImageAvatarView.setImageBitmap(bmp);
                                                    }
                                                    else {
                                                        Toast.makeText(ImageActivity.this, "Display not available!", Toast.LENGTH_SHORT).show();
                                                    }

                                                }
                                                else{
                                                    Toast.makeText(ImageActivity.this, "Failure!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        Toast.makeText(thisContext, "An error occured!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }
                        else{
                            mProgressDialog.dismiss();
                            Toast.makeText(thisContext, "Failed to upload!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_done:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
