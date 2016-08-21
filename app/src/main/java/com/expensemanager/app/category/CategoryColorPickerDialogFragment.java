package com.expensemanager.app.category;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.helpers.SpacesItemDecoration;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryColorPickerDialogFragment extends DialogFragment {
    private static final String TAG= CategoryColorPickerDialogFragment.class.getSimpleName();
    private final int COLUMNS = 3;
    public static final List<String> COLORS = Arrays.asList("#F44336","#3F51B5","#4CAF50","#FF9800","#E91E63","#2196F3","#8BC34A",
        "#FF5722","#03A9F4","#CDDC39","#795548","#9C27B0","#00BCD4","#FFEB3B","#9E9E9E","#673AB7","#757575",
        "#009688","#FFC107","#607D8B","#D32F2F","#303F9F","#388E3C","#F57C00","#C2185B","#1976D2","#689F38",
        "#E64A19","#0288D1","#AFB42B","#5D4037","#7B1FA2","#0097A7","#FBC02D","#616161","#512DA8","#212121",
        "#00796B","#FFA000","#455A64");

    private static CategoryColorDialogListener listener;

    Unbinder unbinder;
    @BindView(R.id.category_color_fragment_recycler_view_id) RecyclerView categoryColorRecyclerView;
    private CategoryColorAdapter adapter;
    private Set<String> usedColors;
    private String currentColor;

    // define listener to pass setting to activity
    public interface CategoryColorDialogListener {
        void onFinishCategoryColorDialog(String color);
    }

    public CategoryColorPickerDialogFragment() {}

    public static CategoryColorPickerDialogFragment newInstance(CategoryColorDialogListener listener,
            String currentColor, HashSet<String> usedColors) {
        CategoryColorPickerDialogFragment.listener = listener;
        // pass setting to fragment
        CategoryColorPickerDialogFragment categoryColorPickerDialogFragment = new CategoryColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("current_color", currentColor);
        args.putSerializable("used_colors", usedColors);

        categoryColorPickerDialogFragment.setArguments(args);
        return categoryColorPickerDialogFragment;
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

        usedColors = (Set<String>) getArguments().getSerializable("used_colors");
        currentColor = getArguments().getString("current_color");

        adapter = new CategoryColorAdapter(getActivity(), listener, this, currentColor, COLORS, usedColors);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        categoryColorRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), COLUMNS,
            LinearLayoutManager.VERTICAL, false));
        categoryColorRecyclerView.setAdapter(adapter);

        // set RecycleView item divider width
        int spacing = (int) Helpers.dpToPx(getActivity(), 4);
        SpacesItemDecoration decoration = new SpacesItemDecoration(spacing);
        categoryColorRecyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }
}
