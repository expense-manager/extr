package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.login_activity_login_button_id) Button loginButton;
    @BindView(R.id.login_activity_signup_text_view_id) TextView signupTextView;
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

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // todo:login to server
            // if failed
            // do something

            Log.i(TAG, "Login succeeded");
            MainActivity.newInstance(this);
        });

        signupTextView.setOnClickListener(v -> {
            SignupActivity.newInstance(this);
        });
    }

}
