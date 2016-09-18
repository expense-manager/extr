package com.expensemanager.app.category;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
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

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= CategoryAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Category> categories;
    private Context context;
    private int lastPosition = -1;

    public CategoryAdapter(Context context, ArrayList<Category> categories) {
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
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                View view = inflater.inflate(R.layout.category_item_default, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.category_item_default, parent, false);
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
//                setAnimation(viewHolderDefault.container, position);
                setScaleAnimation(viewHolderDefault.container);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        Category category = categories.get(position);
        EIcon eIcon = EIcon.instanceFromName(category.getIcon());

        viewHolder.nameTextView.setText(category.getName());
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        viewHolder.colorImageView.setImageDrawable(colorDrawable);
        if (eIcon != null) {
            viewHolder.iconImageView.setImageResource(eIcon.getValueRes());
        }

        viewHolder.itemView.setOnClickListener(v -> {
            CategoryDetailActivity.newInstance(context, categories.get(position).getId());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(1000);
        view.startAnimation(anim);
    }

    public void clear() {
        categories.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Category> categories) {
        this.categories.addAll(categories);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.category_item_default_layout_container) FrameLayout container;
        @BindView(R.id.category_item_default_name_text_view_id) TextView nameTextView;
        @BindView(R.id.category_item_default_color_image_view_id) CircleImageView colorImageView;
        @BindView(R.id.category_item_default_icon_image_view_id) ImageView iconImageView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
