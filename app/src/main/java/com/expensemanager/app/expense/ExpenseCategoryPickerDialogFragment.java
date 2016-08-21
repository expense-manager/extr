package com.expensemanager.app.expense;

import com.expensemanager.app.R;
import com.expensemanager.app.category.CategoryColorAdapter;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.SpacesItemDecoration;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.report.ReportCategoryAdapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ExpenseCategoryPickerDialogFragment extends DialogFragment {
    private static final String TAG= ExpenseCategoryPickerDialogFragment.class.getSimpleName();

    private static ExpenseCategoryDialogListener listener;

    Unbinder unbinder;
    @BindView(R.id.category_color_fragment_recycler_view_id) RecyclerView categoryRecyclerView;
    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private ExpenseCategoryAdapter adapter;
    private Set<String> usedColors;
    private String currentColor;

    // define listener to pass setting to activity
    public interface ExpenseCategoryDialogListener {
        void onFinishExpenseCategoryDialog(Category category, double amount);
    }

    public ExpenseCategoryPickerDialogFragment() {}

    public static ExpenseCategoryPickerDialogFragment newInstance(ExpenseCategoryDialogListener listener) {
        ExpenseCategoryPickerDialogFragment.listener = listener;
        // pass setting to fragment
        ExpenseCategoryPickerDialogFragment
            expenseCategoryPickerDialogFragment = new ExpenseCategoryPickerDialogFragment();

        return expenseCategoryPickerDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_color_picker_fragment, container);
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
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }
}
