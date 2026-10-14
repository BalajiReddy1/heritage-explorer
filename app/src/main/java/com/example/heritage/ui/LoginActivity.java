package com.example.heritage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.heritage.R;
import com.example.heritage.data.SessionManager;
import com.example.heritage.util.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Sign in and register on one screen (Practical 1B and 4B).
 * Three wrong passwords disable the button for 30 seconds.
 */
public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private boolean registerMode;
    private CountDownTimer lockTimer;

    private TextView tvTitle, tvSubtitle;
    private TextInputLayout tilName, tilEmail, tilPassword;
    private TextInputEditText etName, etEmail, etPassword;
    private CheckBox cbRemember;
    private MaterialButton btnSubmit, btnSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (session.isSignedIn()) {
            openHome();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Ui.padForSystemBars(findViewById(R.id.root), true, true);

        tvTitle = findViewById(R.id.tv_form_title);
        tvSubtitle = findViewById(R.id.tv_form_subtitle);
        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSwitch = findViewById(R.id.btn_switch);

        registerMode = !session.hasAccount();
        if (session.hasAccount()) etEmail.setText(session.getEmail());
        updateMode();

        btnSubmit.setOnClickListener(v -> submit());
        btnSwitch.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateMode();
        });
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && btnSubmit.isEnabled()) {
                submit();
                return true;
            }
            return false;
        });

        startLockCountdownIfNeeded();
    }

    private void updateMode() {
        tvTitle.setText(registerMode ? R.string.create_account : R.string.sign_in);
        tvSubtitle.setText(registerMode ? R.string.register_subtitle : R.string.sign_in_subtitle);
        tilName.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        btnSwitch.setText(registerMode ? R.string.switch_to_sign_in : R.string.switch_to_register);
        if (lockTimer == null) {
            btnSubmit.setText(registerMode ? R.string.create_account : R.string.sign_in);
        }
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void submit() {
        String name = text(etName);
        String email = text(etEmail);
        String password = text(etPassword);

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean valid = true;
        if (registerMode && TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name));
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_empty));
            valid = false;
        } else if (registerMode && password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!valid) return;

        if (registerMode) {
            register(name, email, password);
        } else {
            signIn(email, password);
        }
    }

    private void register(String name, String email, String password) {
        if (session.hasAccount() && !email.equalsIgnoreCase(session.getEmail())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.replace_account_title)
                    .setMessage(getString(R.string.replace_account_message, session.getEmail()))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.replace, (d, w) -> completeRegister(name, email, password))
                    .show();
        } else {
            completeRegister(name, email, password);
        }
    }

    private void completeRegister(String name, String email, String password) {
        session.register(name, email, password);
        session.startSession(cbRemember.isChecked());
        Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void signIn(String email, String password) {
        if (!session.hasAccount()) {
            tilEmail.setError(getString(R.string.error_no_account));
            return;
        }
        if (session.checkCredentials(email, password)) {
            session.startSession(cbRemember.isChecked());
            openHome();
            return;
        }

        int attempt = session.recordFailedAttempt();
        etPassword.setText(null);
        if (attempt >= SessionManager.MAX_ATTEMPTS) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_locked_title)
                    .setMessage(getString(R.string.login_locked_message, SessionManager.MAX_ATTEMPTS))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            startLockCountdownIfNeeded();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.login_failed_title)
                    .setMessage(getString(R.string.login_failed_message, attempt, SessionManager.MAX_ATTEMPTS))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    /** Keeps the button disabled while locked. Survives closing and reopening the app. */
    private void startLockCountdownIfNeeded() {
        long remaining = session.lockRemainingMillis();
        if (remaining <= 0) return;

        btnSubmit.setEnabled(false);
        btnSwitch.setEnabled(false);
        lockTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) Math.ceil(millisUntilFinished / 1000.0);
                btnSubmit.setText(getString(R.string.login_try_again_in, seconds));
            }

            @Override
            public void onFinish() {
                lockTimer = null;
                session.clearLock();
                btnSubmit.setEnabled(true);
                btnSwitch.setEnabled(true);
                btnSubmit.setText(registerMode ? R.string.create_account : R.string.sign_in);
            }
        }.start();
    }

    private void openHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        if (lockTimer != null) lockTimer.cancel();
        super.onDestroy();
    }
}
