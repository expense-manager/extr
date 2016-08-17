package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.expensemanager.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();

    @BindView(R.id.welcome_activity_signup_button_id) Button signupButton;
    @BindView(R.id.welcome_activity_login_button_id) Button loginButton;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);
        ButterKnife.bind(this);

        signupButton.setOnClickListener(v -> {
            SignupActivity.newInstance(this);
        });

        loginButton.setOnClickListener(v -> {
            LoginActivity.newInstance(this);
        });
    }

}
