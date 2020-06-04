package com.example.galleryimagesdemo.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.galleryimagesdemo.R;

public class DisplayImageActivity extends AppCompatActivity {

    private ImageView arrowBack;
    private TextView imageName;
    private ImageView display_gallery_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        displayToolBar();
        arrowBack = findViewById(R.id.arrowBack);
        imageName = findViewById(R.id.imageName);

        Bundle bundle = getIntent().getExtras();
        String displayImage = bundle.getString("Display_Image");
        String[] imageStringName = displayImage.split("/");

        imageName.setText(imageStringName[imageStringName.length - 1]);
        display_gallery_image = findViewById(R.id.display_gallery_image);

        Glide.with(getApplicationContext()).load(displayImage)

                .into(display_gallery_image);

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void displayToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }
}
