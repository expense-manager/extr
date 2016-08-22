package com.expensemanager.app.expense;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryPickerFragment extends DialogFragment {
    private static final String TAG= CategoryPickerFragment.class.getSimpleName();

    Unbinder unbinder;
    private ExpenseCategoryPickerListener listener;
    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private ExpenseCategoryAdapter adapter;
    private Set<String> usedColors;
    private String currentColor;

    @BindView(R.id.expense_category_fragment_recycler_view_id) RecyclerView categoryRecyclerView;

    public CategoryPickerFragment() {}

    public static CategoryPickerFragment newInstance() {
        // pass setting to fragment
        CategoryPickerFragment
            categoryPickerFragment = new CategoryPickerFragment();

        return categoryPickerFragment;
    }

    public void setListener(ExpenseCategoryPickerListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expense_category_picker_fragment, container);
        // bind fragment with ButterKnife
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categories = new ArrayList<>();
        amounts = new ArrayList<>();
        adapter = new ExpenseCategoryAdapter(getActivity(), listener, this, categories, amounts);

        setupRecyclerView();
        invalidateViews();
    }

    private void invalidateViews() {
        adapter.clear();
        adapter.addAll(new ArrayList<>(Expense.getAllExpenses()));
    }

    private void setupRecyclerView() {
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        categoryRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind fragment and ButterKnife
        unbinder.unbind();
    }

    // define listener to pass setting to activity
    public interface ExpenseCategoryPickerListener {
        void onFinishExpenseCategoryDialog(Category category, double amount);
    }
}
