package com.expensemanager.app.category;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.report.ReportFragment;
import com.expensemanager.app.service.SyncCategory;

import java.util.ArrayList;
import java.util.Calendar;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Zhaolong Zhong on 9/9/16.
 */

public class CategoryFragment extends Fragment {
    private static final String TAG = ReportFragment.class.getSimpleName();

    private ArrayList<Category> categories;
    private CategoryAdapter categoryAdapter;
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;

//    @BindView(R.id.toolbar_id)
//    Toolbar toolbar;
//    @BindView(R.id.toolbar_back_image_view_id)
//    ImageView backImageView;
//    @BindView(R.id.toolbar_title_text_view_id)
//    TextView titleTextView;
    @BindView(R.id.category_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static CategoryFragment newInstance() {
        return new CategoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.category_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

//        setupToolbar();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);

        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getActivity(), categories);
        setupRecyclerView();

        invalidateViews();
        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncCategory.getAllCategoriesByGroupId(groupId);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(getActivity(), syncTimeKey, syncTimeInMillis);
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncCategory.getAllCategoriesByGroupId(groupId).continueWith(onGetCategoryFinished, Task.UI_THREAD_EXECUTOR);
            }
        });
        // Configure the refreshing colors
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

    public void invalidateViews() {
        categoryAdapter.clear();
        categoryAdapter.addAll(Category.getAllCategoriesByGroupId(groupId));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(categoryAdapter);
    }

//    private void setupToolbar() {
//        toolbar.setContentInsetsAbsolute(0,0);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        }
//        titleTextView.setText(getString(R.string.category));
//        titleTextView.setOnClickListener(v -> close());
//        backImageView.setOnClickListener(v -> close());
//    }

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
    }
}