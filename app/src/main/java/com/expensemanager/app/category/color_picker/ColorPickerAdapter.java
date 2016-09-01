package com.expensemanager.app.category.color_picker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ColorPickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ColorPickerAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;

    private List<String> colors;
    private Set<String> usedColors;
    private String currentColor;
    private Context context;
    private ColorPickerFragment.ColorPickerListener listener;
    private ColorPickerFragment fragment;

    public ColorPickerAdapter(Context context, ColorPickerFragment.ColorPickerListener listener,
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
                View view = inflater.inflate(R.layout.color_item, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.color_item, parent, false);
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

        int background = ContextCompat.getColor(context, R.color.white);
        ColorDrawable colorDrawable = new ColorDrawable(background);
        viewHolder.paddingColorImageView.setImageDrawable(colorDrawable);

        // Regular color
        viewHolder.outerColorImageView.setVisibility(View.INVISIBLE);
        viewHolder.paddingColorImageView.setVisibility(View.INVISIBLE);

        if (color.equals(currentColor)) {
            // Regular color
            viewHolder.outerColorImageView.setVisibility(View.VISIBLE);
            viewHolder.paddingColorImageView.setVisibility(View.VISIBLE);
            // Currently selected
            colorDrawable = new ColorDrawable(Color.parseColor(color));
            viewHolder.outerColorImageView.setImageDrawable(colorDrawable);
        } else if (usedColors.contains(color)){
            // Regular color
            viewHolder.outerColorImageView.setVisibility(View.VISIBLE);
            viewHolder.paddingColorImageView.setVisibility(View.VISIBLE);
            // Selected by other categories
            background = ContextCompat.getColor(context, R.color.gray_light);
            colorDrawable = new ColorDrawable(background);
            viewHolder.outerColorImageView.setImageDrawable(colorDrawable);
        }

        // Show color
        colorDrawable = new ColorDrawable(Color.parseColor(color));
        viewHolder.innerColorImageView.setImageDrawable(colorDrawable);

        viewHolder.itemView.setOnClickListener(v -> {
            selectColor(position);
        });
    }

    private void selectColor(int position) {
        String color = colors.get(position);

        // Available colors
        if (color.equals(currentColor)) {
            fragment.dismiss();
        } else {
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
        @BindView(R.id.color_item_outer_color_image_view_id) CircleImageView outerColorImageView;
        @BindView(R.id.color_item_padding_color_image_view_id) CircleImageView paddingColorImageView;
        @BindView(R.id.color_item_inner_color_image_view_id) CircleImageView innerColorImageView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}