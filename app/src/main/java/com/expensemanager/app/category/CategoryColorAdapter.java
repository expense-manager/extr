package com.expensemanager.app.category;

import com.expensemanager.app.R;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= CategoryColorAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;

    private List<String> colors;
    private Set<String> usedColors;
    private String currentColor;
    private Context context;
    private ColorPickerFragment.ColorPickerListener listener;
    private ColorPickerFragment fragment;

    public CategoryColorAdapter(Context context, ColorPickerFragment.ColorPickerListener listener,
            ColorPickerFragment fragment, String currentColor, List<String> colors, Set<String> usedColors) {
        this.context = context;
        this.listener = listener;
        this.fragment = fragment;
        this.currentColor = currentColor;
        this.colors = colors;
        this.usedColors = usedColors;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.colors.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.category_item_color, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.category_item_color, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        String color = colors.get(position);
        viewHolder.colorImageView.setImageResource(0);

        // Check if is selected
        if (color.equals(currentColor)) {
            int background = ContextCompat.getColor(context, R.color.white_transparent);
            viewHolder.colorRelativeLayout.setBackgroundColor(background);
            viewHolder.colorImageView.setImageResource(R.drawable.ic_check_white_24dp);
        } else {
            viewHolder.colorRelativeLayout.setBackgroundColor(Color.parseColor(color));
            // Show availability
            if (usedColors.contains(color)) {
                viewHolder.colorImageView.setImageResource(R.drawable.ic_close_white_24dp);
            }
        }
        // Show color
        viewHolder.colorImageView.setBackgroundColor(Color.parseColor(color));

        viewHolder.itemView.setOnClickListener(v -> {
            selectColor(position);
        });
    }

    private void selectColor(int position) {
        String color = colors.get(position);

        // Available colors
        if (color.equals(currentColor)) {
            fragment.dismiss();
        } else if (!usedColors.contains(color)) {
            listener.onFinishCategoryColorDialog(color);
            fragment.dismiss();
        }
    }

    public void clear() {
        colors.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<String> colors) {
        this.colors.addAll(colors);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.category_item_color_relative_layout_id) RelativeLayout colorRelativeLayout;
        @BindView(R.id.category_item_color_image_view_id) ImageView colorImageView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}