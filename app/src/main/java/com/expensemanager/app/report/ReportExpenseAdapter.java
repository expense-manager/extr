package com.expensemanager.app.report;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportExpenseAdapter.class.getSimpleName();

    public static final String[] WEEK = {"", "Sun","Mon", "Tue", "Wed", "Thu", "Fri","Sat", ""};
    public static final String[] YEAR = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sept","Oct","Nov","Dec",""};
    public static final int WEEKLY = 0;
    public static final int MONTYLY = 1;
    public static final int YEARLY = 2;

    private static final int VIEW_TYPE_DEFAULT = 0;
    private String[] timeSlots;
    private double[] amounts;
    private Context context;

    public ReportExpenseAdapter(Context context, int requestCode) {
        this.context = context;
        if (requestCode == WEEKLY) {
            timeSlots = WEEK;
        }
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.timeSlots.length - 2;
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
                View view = inflater.inflate(R.layout.expense_item_report, parent, false);
                viewHolder = new ViewHolderDefault(view);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.expense_item_report, parent, false);
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
        // todo: reverse order base on current date
        String timeSlot = timeSlots[getItemCount() - position];
        double amount = amounts[getItemCount() - position - 1];

        viewHolder.timeSlotTextView.setText(timeSlot);
        viewHolder.amountTextView.setText("$" + amount);
        viewHolder.itemView.setOnClickListener(v -> {
            // todo:jump to expense list to view expenses
        });
    }

    public void clear() {
        amounts = new double[timeSlots.length - 2];
        notifyDataSetChanged();
    }

    public void addAllForWeek(List<Expense> expenses) {
        fetchCategoriesAndAmountsForWeek(expenses);
        notifyDataSetChanged();
    }

    private void fetchCategoriesAndAmountsForWeek(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        timeSlots = WEEK;
        amounts = new double[timeSlots.length - 2];

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int day = Helpers.getDayOfWeek(e.getCreatedAt());
            amounts[day] += e.getAmount();
        }
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        @BindView(R.id.expense_item_report_expense_time_text_view_id) TextView timeSlotTextView;
        @BindView(R.id.expense_item_report_amount_text_view_id) TextView amountTextView;

        private View itemView;

        public ViewHolderDefault(View view) {
            super(view);
            ButterKnife.bind(this, view);
            itemView = view;
        }
    }
}
