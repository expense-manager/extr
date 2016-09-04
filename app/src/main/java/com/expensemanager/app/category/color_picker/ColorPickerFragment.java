package com.expensemanager.app.category.color_picker;

import android.content.SharedPreferences;
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
import com.expensemanager.app.models.Group;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ColorPickerFragment extends DialogFragment {
    private static final String TAG= ColorPickerFragment.class.getSimpleName();
    private static final String CURRENT_COLOR = "current_color";
    private final int COLUMNS = 4;
    public static final List<String> COLORS = Arrays.asList("#F44336","#3F51B5","#4CAF50","#FF9800","#E91E63","#2196F3","#8BC34A",
        "#FF5722","#03A9F4","#CDDC39","#795548","#9C27B0","#00BCD4","#FFEB3B","#9E9E9E","#673AB7","#757575",
        "#009688","#FFC107","#607D8B","#D32F2F","#303F9F","#388E3C","#F57C00","#C2185B","#1976D2","#689F38",
        "#E64A19","#0288D1","#AFB42B","#5D4037","#7B1FA2","#0097A7","#FBC02D","#616161","#512DA8","#212121",
        "#00796B","#FFA000","#455A64");

    Unbinder unbinder;
    private ColorPickerListener listener;
    private ColorPickerAdapter adapter;
    private Set<String> usedColors;
    private String currentColor;
    private String groupId;

    @BindView(R.id.color_picker_fragment_recycler_view_id) RecyclerView categoryColorRecyclerView;

    public ColorPickerFragment() {}

    public static ColorPickerFragment newInstance(String currentColor) {
        // pass setting to fragment
        ColorPickerFragment colorPickerFragment = new ColorPickerFragment();
        Bundle args = new Bundle();
        args.putString(CURRENT_COLOR, currentColor);

        colorPickerFragment.setArguments(args);
        return colorPickerFragment;
    }

    public static String getRandomColor(Set<String> usedColors) {
        Random ran = new Random();
        int pos = ran.nextInt(COLORS.size());
        String color = COLORS.get(pos);
        while (usedColors != null && usedColors.contains(color)) {
            pos = ran.nextInt(COLORS.size());
            color = COLORS.get(pos);
        }
        return color;
    }

    public void setListener(ColorPickerFragment.ColorPickerListener listener) {
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

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        currentColor = getArguments().getString(CURRENT_COLOR);
        usedColors = Helpers.getUsedColorSet(groupId);

        adapter = new ColorPickerAdapter(getActivity(), listener, this, currentColor, COLORS, usedColors);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        categoryColorRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), COLUMNS,
            GridLayoutManager.VERTICAL, false));
        categoryColorRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbind frrament and ButterKnife
        unbinder.unbind();
    }

    // define listener to pass setting to activity
    public interface ColorPickerListener {
        void onFinishCategoryColorDialog(String color);
    }
}
