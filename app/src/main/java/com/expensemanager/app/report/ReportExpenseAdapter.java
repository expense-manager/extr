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
import java.util.Calendar;
import java.util.Date;
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
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;
    public static final int LEN_OF_WEEK = 7;
    public static final int LEN_OF_YEAR = 12;

    private static final int VIEW_TYPE_DEFAULT = 0;
    private String[] timeSlots;
    private double[] amounts;
    private Date[] startEnd;
    private int requestCode;
    private Context context;

    public ReportExpenseAdapter(Context context, Date[] startEnd, int requestCode) {
        this.context = context;
        amounts = new double[0];
        timeSlots = new String[0];
        this.startEnd = startEnd;
        this.requestCode = requestCode;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return this.amounts.length;
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
        double amount = amounts[getItemCount() - position - 1];

        if (requestCode == MONTHLY) {
            int pos = getItemCount() - position;
            int lastDig = pos % 10;
            String timeSlot = String.valueOf(pos);
            switch(lastDig) {
                case 1:
                    timeSlot += "st";
                    break;
                case 2:
                    timeSlot += "nd";
                    break;
                case 3:
                    timeSlot += "rd";
                    break;
                default:
                    timeSlot += "th";

            }
            viewHolder.timeSlotTextView.setText(timeSlot);
        } else {
            String timeSlot = timeSlots[getItemCount() - position];
            viewHolder.timeSlotTextView.setText(timeSlot);
        }
        viewHolder.amountTextView.setText("$" + amount);
        viewHolder.itemView.setOnClickListener(v -> {
            // todo:jump to expense list to view expenses
        });
    }

    public void clear() {
        notifyDataSetChanged();
    }

    public void addAll(List<Expense> expenses, int requestCode) {
        // Initialize bar chart data set
        if (requestCode == WEEKLY) {
            fetchCategoriesAndAmountsForWeek(expenses);
        } else if (requestCode == MONTHLY) {
            fetchCategoriesAndAmountsForMonth(expenses);
        } else if (requestCode == YEARLY) {
            fetchCategoriesAndAmountsForYear(expenses);
        }

        notifyDataSetChanged();
    }

    private void fetchCategoriesAndAmountsForWeek(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            amounts = new double[0];
            return;
        }

        timeSlots = WEEK;
        if (new Date().compareTo(startEnd[1]) >= 0) {
            amounts = new double[LEN_OF_WEEK];
        } else {
            // Show list items up to today
            int day = Helpers.getCurrentDayOfWeek();
            amounts = new double[day];
        }

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int day = Helpers.getDayOfWeek(e.getCreatedAt());
            String s = e.getCreatedAt().toString();
            amounts[day - 1] += e.getAmount();
        }
    }

    private void fetchCategoriesAndAmountsForMonth(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        timeSlots = new String[maxDays + 2];

        if (new Date().compareTo(startEnd[1]) >= 0) {
            int day = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            amounts = new double[day];
        } else {
            // Show list items up to today
            int day = Helpers.getCurrentDayOfMonth();
            amounts = new double[day];
        }

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int day = Helpers.getDayOfMonth(e.getCreatedAt());
            String s = e.getCreatedAt().toString();
            amounts[day - 1] += e.getAmount();
        }
    }

    private void fetchCategoriesAndAmountsForYear(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        timeSlots = YEAR;
        if (new Date().compareTo(startEnd[1]) >= 0) {
            amounts = new double[LEN_OF_YEAR];
        } else {
            // Show list items up to today
            int month = Helpers.getCurrentMonthOfYear();
            amounts = new double[month + 1];
        }

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int month = Helpers.getMonthOfYear(e.getCreatedAt());
            amounts[month] += e.getAmount();
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
