package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;
    private CallbackManager callbackManager;

    @BindView(R.id.login_activity_login_button_id) Button loginButton;
    @BindView(R.id.login_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.login_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.login_activity_forget_login_details_linear_layout_id) LinearLayout forgetLoginDetailsLinearLayout;
    @BindView(R.id.login_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.login_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.login_activity_sign_up_linear_layout_id) LinearLayout signUpLinearLayout;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;
    @BindView(R.id.login_activity_facebook_login_button_id) LoginButton facebookLoginButton;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        AppEventsLogger.activateApp(this);

        ButterKnife.bind(this);

        Log.d(TAG, "isFacebookLogin:" + isLoggedIn());

        callbackManager = CallbackManager.Factory.create();

        facebookLoginButton.setReadPermissions("email");

        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "facebook accessToken: " + loginResult.getAccessToken().getToken());
                Log.d(TAG, "facebook id:" + loginResult.getAccessToken().getUserId());
                Log.d(TAG, "facebook expirationDate:" + loginResult.getAccessToken().getExpires());
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.e(TAG, "onError: " + exception.toString());
            }
        });

        invalidateViews();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + resultCode);
    }

    private void invalidateViews() {
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(this::login);
        setupButton();

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        // todo: add help
        forgetLoginDetailsLinearLayout.setOnClickListener(v -> Log.d(TAG, "Forget login details clicked"));
        signUpLinearLayout.setOnClickListener(v -> {
            SignUpActivity.newInstance(this);
            finish();
        });
    }

    public void login(View v) {
        progressBar.setVisibility(View.VISIBLE);
        Helpers.closeSoftKeyboard(this);
        SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private Continuation<JSONObject, Void> onLoginSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
            }

            if (task.getResult().has("error")) {
                // Clear password
                passwordEditText.setText("");
                // Show error messasge
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
                Helpers.closeSoftKeyboard(LoginActivity.this);
            } else {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // Make sure main activity at the top on stack, no other activity in the backstack.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            return null;
        }
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setupButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            setupButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setupButton() {
        loginButton.setEnabled(isValidUserInfo());
        if (loginButton.isEnabled()) {
            int color = ContextCompat.getColor(this, R.color.white);
            loginButton.setTextColor(color);
        } else {
            int color = ContextCompat.getColor(this, R.color.cyan);
            loginButton.setTextColor(color);
        }
    }

    private boolean isValidUserInfo() {
        getLoginInfo();

        if (email == null || email.length() == 0) {
            return false;
        }

        if (password == null || password.length() < 6) {
            return false;
        }

        return true;
    }

    private void getLoginInfo() {
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}
