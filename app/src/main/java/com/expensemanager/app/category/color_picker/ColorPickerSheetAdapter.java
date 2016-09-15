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
import com.expensemanager.app.expense.ActionSheetAdapter;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Zhaolong Zhong on 9/15/16.
 */

public class ColorPickerSheetAdapter extends RecyclerView.Adapter<ColorPickerSheetAdapter.ViewHolder> {
    private static final String TAG = ActionSheetAdapter.class.getSimpleName();

    private List<String> colors;
    private Set<String> usedColors;
    private String currentColor;
    private Context context;

    private ColorPickerSheetAdapter.OnItemClickListener onItemClickListener;

    public ColorPickerSheetAdapter(Context context, String currentColor, List<String> colors, Set<String> usedColors) {
        this.context = context;
        this.currentColor = currentColor;
        this.colors = colors;
        this.usedColors = usedColors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(colors.get(position), usedColors,currentColor, context);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public void setOnItemClickListener(ColorPickerSheetAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ColorPickerSheetAdapter.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder viewHolder, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ColorPickerSheetAdapter adapter;

        @BindView(R.id.color_item_outer_color_image_view_id) CircleImageView outerColorImageView;
        @BindView(R.id.color_item_padding_color_image_view_id) CircleImageView paddingColorImageView;
        @BindView(R.id.color_item_inner_color_image_view_id) CircleImageView innerColorImageView;

        public ViewHolder(View itemView, ColorPickerSheetAdapter parent) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            this.adapter = parent;
        }

        public void bind(String colorString, Set<String> usedColors, String currentColor, Context context) {
            String color = colorString;

            int background = ContextCompat.getColor(context, R.color.white);
            ColorDrawable colorDrawable = new ColorDrawable(background);
            paddingColorImageView.setImageDrawable(colorDrawable);

            // Regular color
            outerColorImageView.setVisibility(View.INVISIBLE);
            paddingColorImageView.setVisibility(View.INVISIBLE);

            if (color.equals(currentColor)) {
                // Regular color
                outerColorImageView.setVisibility(View.VISIBLE);
                paddingColorImageView.setVisibility(View.VISIBLE);
                // Currently selected
                colorDrawable = new ColorDrawable(Color.parseColor(color));
                outerColorImageView.setImageDrawable(colorDrawable);
            } else if (usedColors.contains(color)){
                // Regular color
                outerColorImageView.setVisibility(View.VISIBLE);
                paddingColorImageView.setVisibility(View.VISIBLE);
                // Selected by other categories
                background = ContextCompat.getColor(context, R.color.gray_light);
                colorDrawable = new ColorDrawable(background);
                outerColorImageView.setImageDrawable(colorDrawable);
            }

            // Show color
            colorDrawable = new ColorDrawable(Color.parseColor(color));
            innerColorImageView.setImageDrawable(colorDrawable);
        }

        @Override
        public void onClick(View v) {
            final ColorPickerSheetAdapter.OnItemClickListener onItemClickListener = adapter.getOnItemClickListener();
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(this, getAdapterPosition());
            }
        }
    }
}
