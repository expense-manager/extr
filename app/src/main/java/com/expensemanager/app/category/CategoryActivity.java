package com.expensemanager.app.category;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryActivity extends AppCompatActivity {
    private static final String TAG = CategoryActivity.class.getSimpleName();

    private ArrayList<Category> categories;
    private CategoryAdapter categoryAdapter;

    @BindView(R.id.category_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.category_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, CategoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity);
        ButterKnife.bind(this);

        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categories);
        setupRecyclerView();

        invalidateViews();

        fab.setOnClickListener(v -> {
            NewCategoryActivity.newInstance(this);
        });
    }

    private void invalidateViews() {
        categoryAdapter.clear();
        categoryAdapter.addAll(new ArrayList<>(Category.getAllCategories()));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateViews();
    }
}
