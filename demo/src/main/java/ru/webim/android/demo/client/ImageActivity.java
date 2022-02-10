package ru.webim.android.demo.client;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.ortiz.touchview.TouchImageView;
import ru.webim.android.demo.client.R;

public class ImageActivity extends AppCompatActivity {
    private Uri imageUri = null;
    private TouchImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        initActionBar();
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            imageUri = callingActivityIntent.getData();
            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(
                            Bitmap resource,
                            GlideAnimation<? super Bitmap> glideAnimation) {
                                progressBar.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setImageBitmap(resource);
                            }
                        }
                    );
            }
        }
    }

    private void initActionBar() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    private void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showMessage(int messageId) {
        Toast toast = Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    private void downloadImage() {
        if (ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    1);
        } else {
            String imgName = getString(R.string.def_image_name, System.currentTimeMillis());
            showMessage(getString(R.string.saving_file, imgName));

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                DownloadManager.Request request = new DownloadManager.Request(imageUri);
                request.setTitle(imgName);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imgName);

                manager.enqueue(request);
            } else {
                showMessage(getString(R.string.saving_failed));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_image:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.save_image)
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> downloadImage())
                        .create()
                        .show();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadImage();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
