package ru.webim.android.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        setContentView(R.layout.activity_splash_screen);
        ProgressBar progressBar = findViewById(R.id.splashProgress);
        ValueAnimator loadAnimation = ValueAnimator.ofInt(0, 100);
        int duration = 1000;
        loadAnimation.setDuration(duration);
        loadAnimation.setInterpolator(new AccelerateInterpolator());
        loadAnimation.addUpdateListener(animation -> progressBar.setProgress((int) animation.getAnimatedValue()));
        loadAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        });
        loadAnimation.start();
    }
}
