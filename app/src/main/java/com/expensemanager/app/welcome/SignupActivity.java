package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();

    @BindView(R.id.signup_activity_signup_button_id) Button signupButton;
    @BindView(R.id.signup_activity_login_text_view_id) TextView loginTextView;
    @BindView(R.id.signup_activity_email_edit_text_id) EditText emailEditText;
    @BindView(R.id.signup_activity_password_edit_text_id) EditText passwordEditText;
    @BindView(R.id.signup_activity_re_password_edit_text_id) EditText rePasswordEditText;
    @BindView(R.id.signup_activity_name_edit_text_id) EditText nameEditText;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SignupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        ButterKnife.bind(this);

        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String rePassword = rePasswordEditText.getText().toString();
            String name = nameEditText.getText().toString();

            // todo:signup to server
            // if failed
            // do something

            Log.i(TAG, "Signup succeeded");
            MainActivity.newInstance(this);
        });

        loginTextView.setOnClickListener(v -> {
            LoginActivity.newInstance(this);
        });
    }

}
