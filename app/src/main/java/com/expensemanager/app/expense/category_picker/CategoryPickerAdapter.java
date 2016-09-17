package com.expensemanager.app.expense.category_picker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.service.enums.EIcon;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryPickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= CategoryPickerAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_NULL = 1;

    private ArrayList<Category> categories;
    private Context context;

    public CategoryPickerAdapter(Context context, ArrayList<Category> categories) {
        this.context = context;
        this.categories = categories;
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
                view = inflater.inflate(R.layout.category_picker_item, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            case VIEW_TYPE_NULL:
                view = inflater.inflate(R.layout.category_picker_item_null, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;

            default:
                View defaultView = inflater.inflate(R.layout.category_picker_item, parent, false);
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
            case VIEW_TYPE_NULL:
                ViewHolderDefault viewHolderNull = (ViewHolderDefault) viewHolder;
                viewHolderNull.colorImageView.setVisibility(View.INVISIBLE);
                viewHolderNull.iconImageView.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        // Reset views
        viewHolder.iconImageView.setVisibility(View.INVISIBLE);

        Category category = categories.get(position);
        EIcon eIcon = EIcon.instanceFromName(category.getIcon());

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        viewHolder.colorImageView.setImageDrawable(colorDrawable);
        viewHolder.colorImageView.setVisibility(View.VISIBLE);

        if (eIcon != null) {
            viewHolder.iconImageView.setImageResource(eIcon.getValueRes());
            viewHolder.iconImageView.setVisibility(View.VISIBLE);
        }
        viewHolder.nameTextView.setText(category.getName());
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
        @BindView(R.id.category_picker_item_color_image_view_id) CircleImageView colorImageView;
        @BindView(R.id.category_picker_item_icon_image_view_id) ImageView iconImageView;
        @BindView(R.id.category_picker_item_name_text_view_id) TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
