package ru.webim.chatview;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ortiz.touchview.TouchImageView;

public class ImageDetailFragment extends DialogFragment {
    private String imageUrl = null;
    private TouchImageView imageView;
    private ProgressBar progressBar;
    private static String EXTRA_URL = "extra_url";

    private final RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            progressBar.setVisibility(View.GONE);
            return false;
        }
    };

    public static ImageDetailFragment withUri(String url) {
        ImageDetailFragment fragment = new ImageDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public int getTheme() {
        return R.style.DialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_detail, container, false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(view.findViewById(R.id.toolbar));
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
        }
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle = getArguments();
        if (bundle != null) {
            imageUrl = bundle.getString(EXTRA_URL);
            if (imageUrl != null) {
                loadImage();
            }
        }
    }

    private void loadImage() {
        Glide.with(requireContext())
            .load(imageUrl)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .addListener(requestListener)
            .into(imageView);
    }

    private void showMessage(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showMessage(int messageId) {
        Toast toast = Toast.makeText(getContext(), getString(messageId), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.image_menu, menu);
    }

    private void downloadImage() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                1);
        } else {
            String imgName = getString(R.string.def_image_name, System.currentTimeMillis());
            showMessage(getString(R.string.saving_file, imgName));

            DownloadManager manager = (DownloadManager) requireActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
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
        int itemId = item.getItemId();
        if (itemId == R.id.save_image) {
            new AlertDialog.Builder(requireContext())
                .setMessage(R.string.save_image)
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {})
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> downloadImage())
                .create()
                .show();
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
}
