package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.service.SyncUser;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;

    @BindView(R.id.login_activity_login_button_id) Button loginButton;
    @BindView(R.id.login_activity_sign_up_text_view_id) TextView signUpTextView;
    @BindView(R.id.login_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.login_activity_password_edit_text_id) EditText passwordEditText;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);


        loginButton.setEnabled(false);
        loginButton.setOnClickListener(this::login);
        signUpTextView.setOnClickListener(v -> {
            SignUpActivity.newInstance(this);
        });

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
    }

    public void login(View v) {
        //todo: add progress bar

        SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private Continuation<Void, Void> onLoginSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            // todo: dismiss progress bar
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
                // display info message
            }

            MainActivity.newInstance(LoginActivity.this);
            return null;
        }
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            loginButton.setEnabled(isValidUserInfo());
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
            loginButton.setEnabled(isValidUserInfo());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

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
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
    }
}
