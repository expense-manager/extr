package com.expensemanager.app.expense;

import android.app.Activity;
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
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ExpenseAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Expense> expenses;
    private Map<String, Category> categoriesMap;
    private Context context;

    public ExpenseAdapter(Context context, ArrayList<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
        // Get map of all category
        categoriesMap = Category.getAllCategoriesMap();
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
        Expense expense = expenses.get(position);
        Category category = categoriesMap.get(expense.getCategoryId());

        viewHolder.spentAtTextView.setText(Helpers.formatCreateAt(expense.getExpenseDate()));
        viewHolder.amountTextView.setText("$" + expense.getAmount());

        // Load category data or hide
        if (category != null) {
            viewHolder.categoryColorImageView.setVisibility(View.VISIBLE);
            viewHolder.categoryNameTextView.setVisibility(View.VISIBLE);

            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
            viewHolder.categoryColorImageView.setImageDrawable(colorDrawable);
            viewHolder.categoryNameTextView.setText(category.getName());
        } else {
            viewHolder.categoryColorImageView.setVisibility(View.INVISIBLE);
            viewHolder.categoryNameTextView.setVisibility(View.INVISIBLE);
        }

        // Set item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            ExpenseDetailActivity.newInstance(context, expenses.get(position).getId());
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void clear() {
        expenses.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Expense> expenses) {
        this.expenses.addAll(expenses);
        notifyDataSetChanged();
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.expense_item_default_spent_at_text_view_id) TextView spentAtTextView;
        @BindView(R.id.expense_item_default_amount_text_view_id) TextView amountTextView;
        @BindView(R.id.expense_item_default_category_color_image_view_id) CircleImageView categoryColorImageView;
        @BindView(R.id.expense_item_default_category_name_text_view_id) TextView categoryNameTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
