package com.expensemanager.app.category;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.SyncCategory;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class NewCategoryActivity extends AppCompatActivity {
    private static final String TAG = NewCategoryActivity.class.getSimpleName();

    private Category category;

    @BindView(R.id.new_category_activity_name_edit_text_id) EditText nameEditText;
    @BindView(R.id.new_category_activity_save_button_id) Button saveButton;
    @BindView(R.id.new_category_activity_progress_bar_id) ProgressBar progressBar;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, NewCategoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_category_activity);
        ButterKnife.bind(this);

        category = new Category();
        saveButton.setOnClickListener(v -> save());
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_ATOP);
    }

    private void save() {
        String uuid = UUID.randomUUID().toString();
        category.setId(uuid);
        category.setName(nameEditText.getText().toString());

        progressBar.setVisibility(View.VISIBLE);
        SyncCategory.create(category).continueWith(onCreateSuccess, Task.UI_THREAD_EXECUTOR);
        closeSoftKeyboard();
    }

    private Continuation<JSONObject, Void> onCreateSuccess = new Continuation<JSONObject, Void>() {
        @Override
        public Void then(Task<JSONObject> task) throws Exception {
            progressBar.setVisibility(View.GONE);
            if (task.isFaulted()) {
                Log.e(TAG, "Error in creating new category.", task.getError());
            }

            JSONObject result = task.getResult();
            String categoryId = result.optString(Category.OBJECT_ID_JSON_KEY);

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            category.setId(categoryId);
            realm.copyToRealmOrUpdate(category);
            realm.commitTransaction();
            realm.close();

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
