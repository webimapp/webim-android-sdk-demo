package ru.webim.android.demo.client;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import ru.webim.android.demo.client.util.WebimSessionDirector;
import ru.webim.android.sdk.FatalErrorHandler;
import ru.webim.android.sdk.Webim;
import ru.webim.android.sdk.WebimError;
import ru.webim.android.sdk.WebimSession;

public class WebimChatActivity extends AppCompatActivity implements FatalErrorHandler {
    public static final String EXTRA_SHOW_RATING_BAR_ON_STARTUP = "extra_show_rating_bar_on_startup";
    private static boolean active;
    private WebimChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webim_chat);

        WebimSessionDirector
            .createSessionBuilderWithAnonymousVisitor(this, new WebimSessionDirector.OnSessionBuilderCreatedListener() {
                @Override
                public void onSessionBuilderCreated(Webim.SessionBuilder sessionBuilder) {
                    WebimSession session = sessionBuilder.setErrorHandler(WebimChatActivity.this).build();
                    initFragment(session);
                }

                @Override
                public void onError(WebimSessionDirector.SessionBuilderError error) {
                    Toast.makeText(WebimChatActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initFragment(WebimSession webimSession) {
        fragment = new WebimChatFragment();
        fragment.setWebimSession(webimSession);
        if (getIntent().getBooleanExtra(EXTRA_SHOW_RATING_BAR_ON_STARTUP, false)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(EXTRA_SHOW_RATING_BAR_ON_STARTUP, true);
            fragment.setArguments(bundle);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.webimChatContainer, fragment)
                .commit();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onError(@NonNull WebimError<FatalErrorType> error) {
        switch (error.getErrorType()) {
            default:
                if (!BuildConfig.DEBUG) {
                    FirebaseCrashlytics.getInstance().recordException(
                            new Throwable("Handled unknown webim error: " + error.getErrorString()));
                }
                showError(R.string.error_unknown_header,
                        R.string.error_unknown_desc, error.getErrorString());
                break;
            case ACCOUNT_BLOCKED:
                showError(R.string.error_account_blocked_header,
                        R.string.error_account_blocked_desc);
                break;
            case VISITOR_BANNED:
                showError(R.string.error_user_banned_header, R.string.error_user_banned_desc);
                break;
        }
    }

    private void showError(int errorHeaderId, int errorDescId, String... args) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.webimChatContainer,
                        ErrorFragment.newInstance(errorHeaderId, errorDescId, args))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true; // It's singleTask activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    @Override
    public void onBackPressed() {
        if (fragment.isChatMenuVisible()) {
            fragment.hideChatMenu();
            return;
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
