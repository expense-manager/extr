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

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private String email;
    private String password;
    private String confirmPassword;
    private String fullname;

    @BindView(R.id.sign_up_activity_sign_up_button_id) Button signUpButton;
    @BindView(R.id.sign_up_activity_login_text_view_id) TextView loginTextView;
    @BindView(R.id.sign_up_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.sign_up_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.sign_up_activity_confirm_password_edit_text_id) EditText confirmPasswordEditText;
    @BindView(R.id.sign_up_activity_name_edit_text_id) EditText nameEditText;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        ButterKnife.bind(this);

        signUpButton.setEnabled(false);
        signUpButton.setOnClickListener(this::signUp);
        loginTextView.setOnClickListener(v -> {
            LoginActivity.newInstance(this);
        });

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
        confirmPasswordEditText.addTextChangedListener(confirmPasswordTextWatcher);
    }

    public void signUp(View v) {
        //todo: add progress bar

        SyncUser.signUp(email, password).continueWith(onSignUpSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private Continuation<Void, Void> onSignUpSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error in sign up.", task.getError());
                // todo: dismiss progress bar
                // display info message
            }

            SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
            return null;
        }
    };

    private Continuation<Void, Void> onLoginSuccess = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            // todo: dismiss progress bar
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
                // display info message
            }

            MainActivity.newInstance(SignUpActivity.this);
            return null;
        }
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            signUpButton.setEnabled(isValidUserInfo());
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
            signUpButton.setEnabled(isValidUserInfo());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher confirmPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            signUpButton.setEnabled(isValidUserInfo());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private boolean isValidUserInfo() {
        getSignUpInfo();

        if (email == null || email.length() == 0) {
            return false;
        }

        if (password == null || password.length() < 6) {
            return false;
        }

        if (!password.equals(confirmPassword)) {
            return false;
        }

        return true;
    }

    private void getSignUpInfo() {
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        confirmPassword = confirmPasswordEditText.getText().toString();
        fullname = nameEditText.getText().toString();
    }
}
