package com.expensemanager.app.category.icon_picker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.service.enums.EColor;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Zhaolong Zhong on 9/19/16.
 */

public class IconPickerFragment extends DialogFragment {
    private static final String TAG= IconPickerFragment.class.getSimpleName();
    private static final String CURRENT_COLOR = "current_color";
    private final int COLUMNS = 4;

    Unbinder unbinder;
    private IconPickerListener listener;
    private IconPickerAdapter colorPickerAdapter;
    private Set<String> usedColors;
    private String currentColor;
    private String groupId;
    private List<String> colors;

    @BindView(R.id.color_picker_fragment_recycler_view_id) RecyclerView categoryColorRecyclerView;

    public IconPickerFragment() {}

    public static IconPickerFragment newInstance(String currentColor) {
        // pass setting to fragment
        IconPickerFragment colorPickerFragment = new IconPickerFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_COLOR, currentColor);

        colorPickerFragment.setArguments(args);
        return colorPickerFragment;
    }

    public void setListener(IconPickerFragment.IconPickerListener listener) {
        this.listener = listener;
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

        groupId = Helpers.getCurrentGroupId();

        colors = EColor.getAllColors();
        currentColor = getArguments().getString(CURRENT_COLOR);
        usedColors = Helpers.getUsedColorSet(groupId);

        colorPickerAdapter = new IconPickerAdapter(getActivity(), listener, this, currentColor, colors, usedColors);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        categoryColorRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), COLUMNS,
            GridLayoutManager.VERTICAL, false));
        categoryColorRecyclerView.setAdapter(colorPickerAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // define listener to pass setting to activity
    public interface IconPickerListener {
        void onFinishCategoryColorDialog(String color);
    }
}
