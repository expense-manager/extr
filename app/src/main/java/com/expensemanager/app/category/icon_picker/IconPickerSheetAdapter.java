package com.expensemanager.app.category.icon_picker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ActionSheetAdapter;
import com.expensemanager.app.service.enums.EIcon;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Zhaolong Zhong on 9/19/16.
 */

public class IconPickerSheetAdapter extends RecyclerView.Adapter<IconPickerSheetAdapter.ViewHolder> {
    private static final String TAG = ActionSheetAdapter.class.getSimpleName();

    private List<String> icons;
    private Set<String> usedIcons;
    private String currentIcon;
    private Context context;
    private String currentColor;

    private IconPickerSheetAdapter.OnItemClickListener onItemClickListener;

    public IconPickerSheetAdapter(Context context, String currentIcon, List<String> icons, Set<String> usedIcons, String currentColor) {
        this.context = context;
        this.currentIcon = currentIcon;
        this.icons = icons;
        this.usedIcons = usedIcons;
        this.currentColor = currentColor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_item, parent, false);
        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(icons.get(position), usedIcons,currentIcon, context, currentColor);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    public void setOnItemClickListener(IconPickerSheetAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public IconPickerSheetAdapter.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ViewHolder viewHolder, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private IconPickerSheetAdapter adapter;

        @BindView(R.id.icon_item_border_view_id) CircleImageView backgroundView;
        @BindView(R.id.icon_item_image_view_id) ImageView iconImageView;

        public ViewHolder(View itemView, IconPickerSheetAdapter parent) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            this.adapter = parent;
        }

        public void bind(String iconString, Set<String> usedIcons, String currentIcon, Context context, String currentColor) {
            String iconName = iconString;

            int background = ContextCompat.getColor(context, R.color.white);
            ColorDrawable colorDrawable = new ColorDrawable(background);
//            paddingColorImageView.setImageDrawable(colorDrawable);
//
//            // Regular color
//            outerColorImageView.setVisibility(View.INVISIBLE);
//            paddingColorImageView.setVisibility(View.INVISIBLE);

            if (iconName.equals(currentIcon)) {
//                // Regular color
//                outerColorImageView.setVisibility(View.VISIBLE);
//                paddingColorImageView.setVisibility(View.VISIBLE);
//                // Currently selected
//                colorDrawable = new ColorDrawable(Color.parseColor(color));
//                outerColorImageView.setImageDrawable(colorDrawable);
            } else if (usedIcons.contains(iconName)){
                // Regular color
//                outerColorImageView.setVisibility(View.VISIBLE);
//                paddingColorImageView.setVisibility(View.VISIBLE);
//                // Selected by other categories
//                background = ContextCompat.getColor(context, R.color.gray_light);
//                colorDrawable = new ColorDrawable(background);
//                outerColorImageView.setImageDrawable(colorDrawable);
            }

//            // Show color
//            colorDrawable = new ColorDrawable(Color.parseColor(color));
//            innerColorImageView.setImageDrawable(colorDrawable);

            backgroundView.setImageDrawable(new ColorDrawable(Color.parseColor(currentColor)));
            iconImageView.setImageResource(EIcon.instanceFromName(iconName).getValueRes());
        }

        @Override
        public void onClick(View v) {
            final IconPickerSheetAdapter.OnItemClickListener onItemClickListener = adapter.getOnItemClickListener();
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(this, getAdapterPosition());
            }
        }
    }
}
