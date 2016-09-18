package com.expensemanager.app.overview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseDetailActivity;
import com.expensemanager.app.expense.ProfileExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.enums.EIcon;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= OverviewAdapter.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Expense> expenses;
    private Context context;
    private boolean showMember;

    public OverviewAdapter(Context context, ArrayList<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.expenses.size();
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
                View view = inflater.inflate(R.layout.expense_item_default, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.expense_item_default, parent, false);
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
        // Reset views
        viewHolder.iconImageView.setVisibility(View.INVISIBLE);
        viewHolder.userFullnameTextView.setVisibility(View.GONE);
        viewHolder.userPhotoImageView.setVisibility(View.GONE);
        viewHolder.dividerView.setVisibility(View.GONE);

        Expense expense = expenses.get(position);
        Category category = expense.getCategory();

        viewHolder.spentAtTextView.setText(Helpers.formatCreateAt(expense.getExpenseDate()));
        viewHolder.amountTextView.setText("$" + expense.getAmount());

        // Load category data or hide
        if (category != null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
            viewHolder.categoryColorImageView.setImageDrawable(colorDrawable);
            viewHolder.categoryNameTextView.setText(category.getName());

            EIcon eIcon = EIcon.instanceFromName(category.getIcon());
            if (eIcon != null) {
                viewHolder.iconImageView.setImageResource(eIcon.getValueRes());
                viewHolder.iconImageView.setVisibility(View.VISIBLE);
            }
        } else {
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(NO_CATEGORY_COLOR));
            viewHolder.categoryColorImageView.setImageDrawable(colorDrawable);
            viewHolder.categoryNameTextView.setText(NO_CATEGORY_ID);
        }

        User user = User.getUserById(expense.getUserId());
        if (showMember && user != null) {
            Helpers.loadIconPhoto(viewHolder.userPhotoImageView, user.getPhotoUrl());
            viewHolder.userPhotoImageView.setOnClickListener(v -> {
                ProfileExpenseActivity.newInstance(context, user.getId());
                ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
            });

            viewHolder.userFullnameTextView.setText(user.getFullname());
            viewHolder.userFullnameTextView.setVisibility(View.VISIBLE);
            viewHolder.userPhotoImageView.setVisibility(View.VISIBLE);
            viewHolder.dividerView.setVisibility(View.VISIBLE);
        }

        // Set item click listener
        viewHolder.cardView.setOnClickListener(v -> {
            ExpenseDetailActivity.newInstance(context, expenses.get(position).getId());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void setShowMember(boolean showMember) {
        this.showMember = showMember;
        notifyDataSetChanged();
    }

    public void clear() {
        expenses.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Expense> expenses) {
        if (expenses == null) {
            return;
        }

        this.expenses.addAll(expenses);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.expense_item_default_spent_at_text_view_id) TextView spentAtTextView;
        @BindView(R.id.expense_item_default_amount_text_view_id) TextView amountTextView;
        @BindView(R.id.expense_item_default_category_color_image_view_id) CircleImageView categoryColorImageView;
        @BindView(R.id.expense_item_default_icon_image_view_id) ImageView iconImageView;
        @BindView(R.id.expense_item_default_user_photo_image_view_id) ImageView userPhotoImageView;
        @BindView(R.id.expense_item_default_name_text_view_id) TextView userFullnameTextView;
        @BindView(R.id.expense_item_default_view_id) View dividerView;
        @BindView(R.id.expense_item_default_category_name_text_view_id) TextView categoryNameTextView;
        @BindView(R.id.expense_item_default_card_view_id) CardView cardView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
