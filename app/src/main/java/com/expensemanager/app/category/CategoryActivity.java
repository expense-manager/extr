package com.expensemanager.app.category;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.SyncCategory;

import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class CategoryActivity extends BaseActivity {
    private static final String TAG = CategoryActivity.class.getSimpleName();

    private ArrayList<Category> categories;
    private CategoryAdapter categoryAdapter;
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;
    private Handler handler = new Handler();

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.category_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.category_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, CategoryActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity);
        ButterKnife.bind(this);

        groupId = Helpers.getCurrentGroupId();
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = Helpers.getSyncTimeInMillis(syncTimeKey);

        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categories);

        setupToolbar();
        setupRecyclerView();
        setupSwipeToRefresh();

        fab.setOnClickListener(v -> {
            NewCategoryActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private void invalidateViews() {
        handler.removeCallbacks(animateRunnable);
        handler.postDelayed(animateRunnable, 300);

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncCategory.getAllCategoriesByGroupId(groupId);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(this, syncTimeKey, syncTimeInMillis);
        }
    }

    private Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            categoryAdapter.clear();
            categoryAdapter.addAll(Category.getAllCategoriesByGroupId(groupId));
        }
    };

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.category));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);
    }

    private void setupSwipeToRefresh() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncCategory.getAllCategoriesByGroupId(groupId).continueWith(onGetCategoryFinished, Task.UI_THREAD_EXECUTOR);
            }
        });

        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private Continuation<Void, Void> onGetCategoryFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }

            return null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();

        if (animateRunnable != null) {
            handler.removeCallbacks(animateRunnable);
        }
    }
}
