package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;

import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;

    @BindView(R.id.login_activity_login_button_id) Button loginButton;
    @BindView(R.id.login_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.login_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.login_activity_forget_login_details_linear_layout_id) LinearLayout forgetLoginDetailsLinearLayout;
    @BindView(R.id.login_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.login_activity_error_text_view_id) TextView errorMessageTextView;
    @BindView(R.id.login_activity_sign_up_linear_layout_id) LinearLayout signUpLinearLayout;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        invalidateViews();
    }

    private void invalidateViews() {
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(this::login);

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
                closeSoftKeyboard();
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
            int color = ContextCompat.getColor(this, R.color.blue);
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

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
