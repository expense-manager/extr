package com.expensemanager.app.category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class CategoryDetailActivity extends AppCompatActivity {
    private static final String TAG = CategoryDetailActivity.class.getSimpleName();

    private static final String CATEGORY_ID = "CATEGORY_ID";

    private Category category;

    @BindView(R.id.category_detail_activity_name_text_view_id) TextView nameTextView;

    public static void newInstance(Context context, String id) {
        Intent intent = new Intent(context, CategoryDetailActivity.class);
        intent.putExtra(CATEGORY_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_detail_activity);
        ButterKnife.bind(this);

        String categoryId = getIntent().getStringExtra(CATEGORY_ID);
        category = Category.getCategoryById(categoryId);

        invalidateViews();
    }

    private void invalidateViews() {
        nameTextView.setText(category.getName());

        nameTextView.setOnClickListener(v -> updateName());
    }

    private void updateName() {
        Log.d(TAG, "category clicked");
        // todo: implement logic
        // steps:
        // 1. popup a dialog
        // 2. set default amount in dialog
        // 3. click save button in dialog to save

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
//        category.setName(NEW_VALUE);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
        realm.close();
    }

}
