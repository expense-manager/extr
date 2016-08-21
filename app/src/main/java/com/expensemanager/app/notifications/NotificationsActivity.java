package com.expensemanager.app.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/20/16.
 */

public class NotificationsActivity extends BaseActivity {
    private static final String TAG = NotificationsActivity.class.getSimpleName();

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.notifications_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.notifications_activity_empty_state_text_view_id) TextView noNotificationTextView;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NotificationsActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);
        ButterKnife.bind(this);

        setupToolbar();

        invalidateViews();
    }

    private void invalidateViews() {
        noNotificationTextView.setVisibility(View.VISIBLE);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.notifications));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }
}