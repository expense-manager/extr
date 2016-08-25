package com.expensemanager.app.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.expensemanager.app.R;

public class GroupActivity extends AppCompatActivity {
    private static final String TAG = GroupActivity.class.getSimpleName();

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, GroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);
    }
}
