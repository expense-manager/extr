package com.expensemanager.app.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.expensemanager.app.R;

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

        WelcomeActivity.newInstance(this);
        finish();
    }
}
