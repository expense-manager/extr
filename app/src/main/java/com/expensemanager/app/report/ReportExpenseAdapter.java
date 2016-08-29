package com.expensemanager.app.report;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.ExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportExpenseAdapter.class.getSimpleName();

    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;
    public static final int LEN_OF_WEEK = 7;
    public static final int LEN_OF_YEAR = 12;
    public static final int DAYS_OF_WEEK = 7;
    public static final int MONTHS_OF_YEAR = 12;

    private static final int VIEW_TYPE_DEFAULT = 0;
    private int timeSlotsLength;
    private double[] amounts;
    private Date[] startEnd;
    private int requestCode;
    private Context context;

    public ReportExpenseAdapter(Context context, Date[] startEnd, int requestCode) {
        this.context = context;
        amounts = new double[0];
        timeSlotsLength = 0;
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
        int pos = getItemCount() - position;
        String slotName = null;

        switch(requestCode) {
            case WEEKLY:
                slotName = Helpers.getDayOfWeekString(pos);
                break;
            case MONTHLY:
                slotName = Helpers.getDayOfMonthString(pos);
                break;
            case YEARLY:
                slotName = Helpers.getMonthOfYearString(pos);
                break;
        }

        if (slotName == null) {
            return;
        }

        viewHolder.timeSlotTextView.setText(slotName);
        viewHolder.amountTextView.setText("$" + new DecimalFormat("##.##").format(amount));

        // Item click listener
        viewHolder.itemView.setOnClickListener(v -> {
            Date[] clickedStartEnd = null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startEnd[0]);

            switch(requestCode) {
                case WEEKLY:
                    calendar.set(Calendar.DAY_OF_WEEK, pos);
                    clickedStartEnd = Helpers.getDayStartEndDate(calendar.getTime());
                    break;
                case MONTHLY:
                    calendar.set(Calendar.DAY_OF_MONTH, pos);
                    clickedStartEnd = Helpers.getDayStartEndDate(calendar.getTime());
                    break;
                case YEARLY:
                    calendar.set(Calendar.MONTH, pos - 1);
                    clickedStartEnd = Helpers.getMonthStartEndDate(calendar.getTime());
                    break;
            }

            ExpenseActivity.newInstance(getContext(), clickedStartEnd);
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

        timeSlotsLength = DAYS_OF_WEEK + 2;
        if (new Date().compareTo(startEnd[1]) >= 0) {
            amounts = new double[LEN_OF_WEEK];
        } else {
            // Show list items up to today
            int day = Helpers.getCurrentDayOfWeek();
            amounts = new double[day];
        }

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int day = Helpers.getDayOfWeek(e.getExpenseDate());
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
        timeSlotsLength = maxDays + 2;

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
            int day = Helpers.getDayOfMonth(e.getExpenseDate());
            String s = e.getCreatedAt().toString();
            amounts[day - 1] += e.getAmount();
        }
    }

    private void fetchCategoriesAndAmountsForYear(List<Expense> expenses) {
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        timeSlotsLength = MONTHS_OF_YEAR;
        if (new Date().compareTo(startEnd[1]) >= 0) {
            amounts = new double[LEN_OF_YEAR];
        } else {
            // Show list items up to today
            int month = Helpers.getCurrentMonthOfYear();
            amounts = new double[month + 1];
        }

        for (Expense e : expenses) {
            // todo: use expense time instead of created time
            int month = Helpers.getMonthOfYear(e.getExpenseDate());
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
