package ru.webim.android.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ru.webim.android.demo.util.WebimSessionDirector;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimSession;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentDelegate {

    public static String EXTRA_NEED_OPEN_CHAT = "need_open_chat";
    public static String EXTRA_SHOW_RATE_OPERATOR = "show_rate_operator";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        if (needOpenChat()) {
            onOpenChat();
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        WebimSessionDirector.createSessionBuilderWithAnonymousVisitor(this, new WebimSessionDirector.OnSessionBuilderCreatedListener() {
                @Override
                public void onSessionBuilderCreated(Webim.SessionBuilder sessionBuilder) {
                    WebimSession session = sessionBuilder.build();
                    initFragment(session);
                }

                @Override
                public void onError(WebimSessionDirector.SessionBuilderError error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initFragment(WebimSession webimSession) {
        String fragmentByTag = MainFragment.class.getSimpleName();
        MainFragment currentFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(fragmentByTag);

        if (currentFragment == null) {
            currentFragment = new MainFragment();
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContainerView, currentFragment, fragmentByTag)
                .commit();
        }
        currentFragment.setWebimSession(webimSession);
    }

    private boolean needOpenChat() {
        return getIntent().getBooleanExtra(EXTRA_NEED_OPEN_CHAT, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (needOpenChat()) {
            onOpenChat();
        }
    }

    @Override
    public void onOpenChat() {
        Intent intent = new Intent(MainActivity.this, WebimChatActivity.class);
        if (getIntent().getBooleanExtra(EXTRA_SHOW_RATE_OPERATOR, false)) {
            intent.putExtra(WebimChatActivity.EXTRA_SHOW_RATING_BAR_ON_STARTUP, true);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onOpenSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}