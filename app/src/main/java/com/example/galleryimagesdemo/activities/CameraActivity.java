package com.example.galleryimagesdemo.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.example.galleryimagesdemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity {

    private static final int CONTENT_REQUEST = 1337;
    TextureView textureView;
    ImageView displayImage;
    private int REQUEST_CODE_PERMISSIONS = 101;
    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    public static void saveToTheGalley(String filePath, Context context) {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().hide();

        textureView = findViewById(R.id.view_finder);
        displayImage = findViewById(R.id.displayImage);

        if (allPermissionGranted()) {

            startCamera();
        } else {

            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        displayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startCamera() {

        CameraX.unbindAll();

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight());

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        findViewById(R.id.imgCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File folder = getFilesDir();
                File capturedImageFolder = new File(folder, "captured_image");
                capturedImageFolder.mkdir();

                File file = new File(capturedImageFolder, System.currentTimeMillis() + ".jpg");
                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        Glide.with(getApplicationContext()).load(file.getAbsolutePath()).into(displayImage);
                        saveToTheGalley(file.getAbsolutePath(), getApplicationContext());
                        String msg = "Pic captured at " + file.getAbsolutePath();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg = "Pic capture failed : " + message;
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                        if (cause != null) {
                            cause.printStackTrace();
                        }
                    }
                });
            }
        });

        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imgCap);
    }

    public void saveMyImage(String imageUrl) {

        Bitmap bmImg = loadBitmap(imageUrl);
        File filename = new File(imageUrl);
        try {
            FileOutputStream out = new FileOutputStream(imageUrl);
            bmImg.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            ContentValues image = new ContentValues();
            image.put(MediaStore.Images.Media.TITLE, "Camera");
            image.put(MediaStore.Images.Media.DISPLAY_NAME, "Camera Image");
            image.put(MediaStore.Images.Media.DESCRIPTION, "App Image");
            image.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            image.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            image.put(MediaStore.Images.Media.ORIENTATION, 0);
            File parent = filename.getParentFile();
            image.put(MediaStore.Images.ImageColumns.BUCKET_ID, parent.toString()
                    .toLowerCase().hashCode());
            image.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, parent.getName()
                    .toLowerCase());
            image.put(MediaStore.Images.Media.SIZE, filename.length());
            image.put(MediaStore.Images.Media.DATA, filename.getAbsolutePath());
            Uri result = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
            Toast.makeText(getApplicationContext(),
                    "File is Saved in  " + filename, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap loadBitmap(String imageUrl) {
        try {
            InputStream in = new java.net.URL(imageUrl).openStream();
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void updateTransform() {

        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }
        return true;
    }

}
