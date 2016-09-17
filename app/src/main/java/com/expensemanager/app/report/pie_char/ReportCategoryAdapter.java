package com.expensemanager.app.report.pie_char;

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
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.enums.EIcon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReportCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportCategoryAdapter.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";

    private static final int VIEW_TYPE_DEFAULT = 0;
    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private Map<String, Integer> categoryPositionMap;
    private Date[] startEnd;
    private Context context;

    public ReportCategoryAdapter(Context context, ArrayList<Category> categories, ArrayList<Double> amounts) {
        this.context = context;
        this.categories = categories;
        this.amounts = amounts;
        categoryPositionMap = new HashMap<>();
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
                View view = inflater.inflate(R.layout.category_item_report, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.category_item_report, parent, false);
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
        Category category = categories.get(position);
        double amount = amounts.get(position);
        EIcon eIcon = null;
        if (category != null) {
            eIcon = EIcon.instanceFromName(category.getIcon());
        }

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(category.getColor()));
        viewHolder.colorImageView.setImageDrawable(colorDrawable);
        if (eIcon != null) {
            viewHolder.iconImageView.setImageResource(eIcon.getValueRes());
            viewHolder.iconImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iconImageView.setVisibility(View.INVISIBLE);
        }
        viewHolder.nameTextView.setText(category.getName());
        viewHolder.amountTextView.setText(Helpers.doubleToCurrency(amount));

        // Item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            String categoryId = category != null ? category.getId() : null;
            ExpenseActivity.newInstance(getContext(), categoryId, startEnd);
        });
    }

    public void setStartEnd(Date[] startEnd) {
        this.startEnd = startEnd;
    }

    public void clear() {
        categories.clear();
        amounts.clear();
        categoryPositionMap.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Expense> expenses) {
        fetchCategoriesAndAmounts(expenses);
        notifyDataSetChanged();
    }

    private void fetchCategoriesAndAmounts(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        for (Expense expense : expenses) {
            String categoryId = expense.getCategoryId();

            if (categoryId == null) {
                categoryId = NO_CATEGORY_ID;
            }

            Integer position = categoryPositionMap.get(categoryId);

            if (position == null) {
                Category category = null;
                // Get new category
                if (!categoryId.equals(NO_CATEGORY_ID)) {
                    category = Category.getCategoryById(categoryId);
                }

                if (category == null) {
                    // Get temp category for no category
                    category = new Category();
                    category.setColor(NO_CATEGORY_COLOR);
                    category.setName(NO_CATEGORY_ID);
                }

                // Store position of new category into map
                categoryPositionMap.put(categoryId, categories.size());
                // Add new category to list
                categories.add(category);
                // Add first amount to list
                amounts.add(expense.getAmount());
            } else {
                double amount = amounts.get(position);
                amounts.set(position, amount + expense.getAmount());
            }
        }
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.category_item_report_color_image_view_id) CircleImageView colorImageView;
        @BindView(R.id.category_item_icon_image_view_id) ImageView iconImageView;
        @BindView(R.id.category_item_report_name_text_view_id) TextView nameTextView;
        @BindView(R.id.category_item_report_amount_text_view_id) TextView amountTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
