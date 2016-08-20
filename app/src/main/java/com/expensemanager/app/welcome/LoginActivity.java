package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private String email;
    private String password;

    @BindView(R.id.login_activity_login_button_id) Button loginButton;
    @BindView(R.id.login_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.login_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.login_activity_clear_email_image_view_id) ImageView clearEmailImageView;
    @BindView(R.id.login_activity_clear_password_image_view_id) ImageView clearPasswordImageView;
    @BindView(R.id.login_activity_error_relative_layout_id) RelativeLayout errorMessageRelativeLayout;
    @BindView(R.id.login_activity_error_text_view_id) TextView errorMessageTextView;

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

        emailEditText.addTextChangedListener(emailTextWatcher);
        passwordEditText.addTextChangedListener(passwordTextWatcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sign_up_activity_id:
                SignActivity.newInstance(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.login_activity_clear_email_image_view_id, R.id.login_activity_clear_password_image_view_id})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_activity_clear_email_image_view_id:
                emailEditText.setText("");
                break;
            case R.id.login_activity_clear_password_image_view_id:
                passwordEditText.setText("");
                break;
        }
    }

    public void login(View v) {
        //todo: add progress bar

        SyncUser.login(email, password).onSuccess(onLoginSuccess, Task.UI_THREAD_EXECUTOR);
    }

    private Continuation<JSONObject, Void> onLoginSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            // todo: dismiss progress bar
            if (task.isFaulted()) {
                Log.e(TAG, "Error in login. ", task.getError());
                // display info message
            }

            if (task.getResult().has("error")) {
                errorMessageTextView.setText(task.getResult().getString("error"));
                errorMessageRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                MainActivity.newInstance(LoginActivity.this);
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
            setButton();
            clearEmailImageView.setVisibility(email != null && email.length() != 0 ? View.VISIBLE : View.GONE);
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
            setButton();
            clearPasswordImageView.setVisibility(password != null && password.length() != 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setButton() {
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
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
    }
}
