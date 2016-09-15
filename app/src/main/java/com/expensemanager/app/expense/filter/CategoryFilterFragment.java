package com.expensemanager.app.expense.filter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.ItemClickSupport;
import com.expensemanager.app.models.Category;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryFilterFragment extends DialogFragment {
    private static final String TAG = CategoryFilterFragment.class.getSimpleName();

    public static final int COLUMN = 3;
    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private Unbinder unbinder;
    private CategoryFilterListener categoryFilterListener;
    private ArrayList<Category> categories;
    private Category category;
    private boolean isCategoryFiltered;
    private CategoryFilterAdapter adapter;
    private String groupId;

    @BindView(R.id.expense_category_fragment_relative_layout_id) RelativeLayout categoryRelativeLayout;
    @BindView(R.id.expense_category_fragment_recycler_view_id) RecyclerView categoryRecyclerView;

    public CategoryFilterFragment() {}

    public static CategoryFilterFragment newInstance() {
        return new CategoryFilterFragment();
    }

    public void setListener(CategoryFilterListener categoryFilterListener) {
        this.categoryFilterListener = categoryFilterListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CategoryColorDialogStyle);

        groupId = Helpers.getCurrentGroupId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expense_category_filter_fragment, container);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Window window = getDialog().getWindow();

        if (window != null) {
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categories = new ArrayList<>();
        adapter = new CategoryFilterAdapter(getActivity(), categories, isCategoryFiltered, category);
        categoryRelativeLayout.setOnClickListener(v -> dismiss());

        setupRecyclerView();
        invalidateViews();
    }

    private void invalidateViews() {
        adapter.clear();
        // Add no category option
        adapter.add(null);
        // Add all categories
        adapter.addAll(Category.getAllCategoriesByGroupId(groupId));
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), COLUMN);
        categoryRecyclerView.setLayoutManager(gridLayoutManager);
        categoryRecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(categoryRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                categoryFilterListener.onFinishCategoryFilterDialog(categories.get(position));
                getDialog().dismiss();
            }
        });
    }

    public void setFilterParams(boolean isCategoryFiltered, Category category) {
        this.isCategoryFiltered = isCategoryFiltered;
        this.category = category;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface CategoryFilterListener {
        void onFinishCategoryFilterDialog(Category category);
    }
}
