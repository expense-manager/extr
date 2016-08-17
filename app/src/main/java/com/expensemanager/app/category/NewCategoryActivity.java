package com.expensemanager.app.category;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class NewCategoryActivity extends AppCompatActivity {
    private static final String TAG = NewCategoryActivity.class.getSimpleName();

    private Category category;

    @BindView(R.id.new_category_activity_name_text_view_id) TextView nameTextView;
    @BindView(R.id.new_category_activity_save_button_id) Button saveButton;

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
    }

    private void save() {
        String uuid = UUID.randomUUID().toString();
        category.setId(uuid);
        category.setName(nameTextView.getText().toString());

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
        realm.close();

        close();
    }

    private void close() {
        finish();
    }
}
