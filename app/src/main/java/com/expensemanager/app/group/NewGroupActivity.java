package com.expensemanager.app.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncGroup;

import org.json.JSONObject;

import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NewGroupActivity extends AppCompatActivity {
    private static final String TAG = NewGroupActivity.class.getSimpleName();

    private Group group;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.new_group_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.new_group_activity_group_edit_text_id) EditText groupEditText;
    @BindView(R.id.new_group_activity_about_edit_text_id) EditText aboutEditText;
    @BindView(R.id.progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewGroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_activity);
        ButterKnife.bind(this);

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);

        group = new Group();
        group.setUserId(loginUserId);
        // Get a random unused color

        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_new_group));
        saveTextView.setVisibility(View.VISIBLE);
        backImageView.setImageResource(R.drawable.ic_window_close);
        saveTextView.setText(R.string.create);

        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
        saveTextView.setOnClickListener(v -> save());
    }

    private void save() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        if (loginUserId == null) {
            Log.i(TAG, "Error getting login user id.");
            return;
        }
        group.setUserId(loginUserId);
        String uuid = UUID.randomUUID().toString();
        group.setId(uuid);
        group.setName(nameEditText.getText().toString());
        group.setGroupname(groupEditText.getText().toString().toLowerCase());
        group.setAbout(aboutEditText.getText().toString());

        progressBar.setVisibility(View.VISIBLE);
        SyncGroup.create(group).continueWith(onCreateSuccess, Task.UI_THREAD_EXECUTOR);
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onCreateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in creating new category.", task.getError());
            }

            JSONObject jsonObject = task.getResult();

            close();

            return null;
        }
    };

    public void closeSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this.getCurrentFocus();
        if (inputMethodManager != null && view != null){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void close() {
        finish();
        overridePendingTransition(0, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}