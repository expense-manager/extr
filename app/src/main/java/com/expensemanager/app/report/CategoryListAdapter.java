package com.expensemanager.app.report;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.enums.EIcon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= CategoryListAdapter.class.getSimpleName();

    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private static final int VIEW_TYPE_DEFAULT = 0;

    private List<Category> categories;

    public CategoryListAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public int getItemCount() {
        return this.categories.size();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                view = inflater.inflate(R.layout.category_report_list_item, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;

            default:
                View defaultView = inflater.inflate(R.layout.category_report_list_item, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Category category = categories.get(position);
        EIcon eIcon = null;
        if (category != null) {
            eIcon = EIcon.instanceFromName(category.getIcon());
        }

        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                if (category == null) {
                    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(NO_CATEGORY_COLOR));
                    viewHolderDefault.colorImageView.setImageDrawable(colorDrawable);
                    viewHolderDefault.iconImageView.setVisibility(View.INVISIBLE);
                } else {
                    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
                    viewHolderDefault.colorImageView.setImageDrawable(colorDrawable);
                    if (eIcon != null) {
                        viewHolderDefault.iconImageView.setImageResource(eIcon.getValueRes());
                        viewHolderDefault.iconImageView.setVisibility(View.VISIBLE);
                    } else {
                        viewHolderDefault.iconImageView.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void clear() {
        this.categories.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Category> categories) {
        this.categories.addAll(categories);
        notifyDataSetChanged();
    }

    public void add(Category category) {
        this.categories.add(category);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.category_report_list_item_color_image_view_id) CircleImageView colorImageView;
        @BindView(R.id.category_report_list_item_icon_image_view_id) ImageView iconImageView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
