package com.expensemanager.app.expense.filter;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.enums.EIcon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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

public class CategoryFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= CategoryFilterAdapter.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_NULL = 1;

    private ArrayList<Category> categories;
    private Category category;
    private boolean isCategoryFiltered;
    private Context context;

    public CategoryFilterAdapter(Context context, ArrayList<Category> categories,
            boolean isCategoryFiltered, Category category) {
        this.context = context;
        this.categories = categories;
        this.isCategoryFiltered = isCategoryFiltered;
        this.category = category;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.categories.size();
    }

    @Override
    public int getItemViewType(int position) {
        return categories.get(position) != null ? VIEW_TYPE_DEFAULT : VIEW_TYPE_NULL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                view = inflater.inflate(R.layout.category_filter_item, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            case VIEW_TYPE_NULL:
                view = inflater.inflate(R.layout.category_filter_item_null, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;

            default:
                View defaultView = inflater.inflate(R.layout.category_filter_item, parent, false);
                viewHolder = new ViewHolderDefault(defaultView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        // Reset image views
        int background = ContextCompat.getColor(context, R.color.white);
        ColorDrawable colorDrawable = new ColorDrawable(background);
        ((ViewHolderDefault)viewHolder).paddingImageView.setImageDrawable(colorDrawable);
        ((ViewHolderDefault)viewHolder).outerImageView.setVisibility(View.INVISIBLE);
        ((ViewHolderDefault)viewHolder).paddingImageView.setVisibility(View.INVISIBLE);
        ((ViewHolderDefault) viewHolder).iconImageView.setVisibility(View.INVISIBLE);

        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_DEFAULT:
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            case VIEW_TYPE_NULL:
                ((ViewHolderDefault) viewHolder).frameOuterImageView.setBackgroundColor(Color.parseColor(NO_CATEGORY_COLOR));
                colorDrawable = new ColorDrawable(Color.parseColor(NO_CATEGORY_COLOR));
                if (isCategoryFiltered && category == null) {
                    ((ViewHolderDefault) viewHolder).outerImageView.setImageDrawable(colorDrawable);
                    ((ViewHolderDefault) viewHolder).outerImageView.setVisibility(View.VISIBLE);
                    ((ViewHolderDefault) viewHolder).paddingImageView.setVisibility(View.VISIBLE);
                }
                ((ViewHolderDefault) viewHolder).innerImageView.setImageDrawable(colorDrawable);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        Category c = categories.get(position);
        EIcon eIcon = EIcon.instanceFromName(c.getIcon());

        viewHolder.frameOuterImageView.setBackgroundColor(Color.parseColor(c.getColor()));
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(c.getColor()));

        if (isCategoryFiltered && category != null && category.getId().equals(c.getId())) {
            viewHolder.outerImageView.setVisibility(View.VISIBLE);
            viewHolder.paddingImageView.setVisibility(View.VISIBLE);
            viewHolder.outerImageView.setImageDrawable(colorDrawable);
        }

        viewHolder.innerImageView.setImageDrawable(colorDrawable);
        if (eIcon != null) {
            viewHolder.iconImageView.setImageResource(eIcon.getValueRes());
            viewHolder.iconImageView.setVisibility(View.VISIBLE);
        }
        viewHolder.nameTextView.setText(c.getName());
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
        @BindView(R.id.category_item_frame_outer_color_image_view_id) ImageView frameOuterImageView;
        @BindView(R.id.color_item_outer_color_image_view_id) CircleImageView outerImageView;
        @BindView(R.id.color_item_padding_color_image_view_id) CircleImageView paddingImageView;
        @BindView(R.id.color_item_inner_color_image_view_id) CircleImageView innerImageView;
        @BindView(R.id.color_item_icon_image_view_id) ImageView iconImageView;
        @BindView(R.id.category_item_name_text_view_id) TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
