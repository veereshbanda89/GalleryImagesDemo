package com.example.galleryimagesdemo.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.galleryimagesdemo.R;
import com.example.galleryimagesdemo.adapter.GridViewAdapter;
import com.example.galleryimagesdemo.constant.Util;
import com.example.galleryimagesdemo.model.ImagePath;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;


public class PhotosActivity extends AppCompatActivity {

    private static final String TAG = "PhotosActivity";
    private FirebaseStorage firebaseStorage;
    private String deviceIdentifier;
    private StorageReference storageReference;
    private int int_position;
    private GridView gridView;
    private GridViewAdapter adapter;
    private int listSize;
    private Button syncButton;
    private ImageView arrowBack;
    private TextView folderName;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        displayToolBar();
        arrowBack = findViewById(R.id.arrowBack);
        folderName = findViewById(R.id.folderName);
        gridView = findViewById(R.id.gv_folder);
        syncButton = findViewById(R.id.syncButton);

        int_position = getIntent().getIntExtra("value", 0);
        listSize = GalleryActivity.al_images.get(int_position).getAl_imagepath().size();
        adapter = new GridViewAdapter(this, GalleryActivity.al_images, int_position);
        gridView.setAdapter(adapter);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("ImagePath");
        getInstallationIdentifier();

        folderName.setText(GalleryActivity.al_images.get(int_position).getStr_folder());
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Util.isNetworkAvailable(getApplicationContext())) {
                    addToCloudStorage(listSize, getApplicationContext());
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    protected synchronized String getInstallationIdentifier() {
        if (deviceIdentifier == null) {
            SharedPreferences sharedPrefs = this.getSharedPreferences(
                    "DEVICE_ID", Context.MODE_PRIVATE);
            deviceIdentifier = sharedPrefs.getString("DEVICE_ID", null);
            if (deviceIdentifier == null) {
                deviceIdentifier = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("DEVICE_ID", deviceIdentifier);
                editor.commit();
            }
        }
        return deviceIdentifier;
    }

    private void addToCloudStorage(int listSize, Context context) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.uploading));

        for (int i = 0; i < listSize; i++) {
            if (!GalleryActivity.al_images.get(int_position).getAl_imagepath().get(i).isSynced()) {
                String filePath = GalleryActivity.al_images.get(int_position).getAl_imagepath().get(i).getImagePath();
                File f = new File(filePath);
                Uri picUri = Uri.fromFile(f);
                final String cloudFilePath = deviceIdentifier + picUri.getLastPathSegment();

                progressDialog.show();
                StorageReference uploadeRef = storageReference.child(cloudFilePath);

                final int finalI = i;
                uploadeRef.putFile(picUri).addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Failed to upload picture to cloud storage");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        if (finalI == 0) {
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.upload_successful),
                                    Toast.LENGTH_SHORT).show();
                        }
                        ImagePath imagePath = new ImagePath(GalleryActivity.al_images.get(int_position).getAl_imagepath().get(finalI).getImagePath(), true);
                        String uploadId = databaseReference.push().getKey();
                        databaseReference.child(uploadId).setValue(imagePath);
                        GalleryActivity.al_images.get(int_position).getAl_imagepath().get(finalI).setSynced(true);
                    }
                });
            }
        }

    }

    public void displayToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }
}
