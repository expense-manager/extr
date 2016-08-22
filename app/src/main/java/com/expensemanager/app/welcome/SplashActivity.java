package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.expensemanager.app.R;
import com.expensemanager.app.main.MainActivity;
import com.expensemanager.app.models.User;

/**
 * Created by Zhaolong Zhong on 8/18/16.
 */

public class SplashActivity extends AppCompatActivity {

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String sessionToken = sharedPreferences.getString(User.SESSION_TOKEN, null);

        if (TextUtils.isEmpty(sessionToken)) {
            WelcomeActivity.newInstance(this);
        } else {
            MainActivity.newInstance(this);
        }

        finish();
    }
}
