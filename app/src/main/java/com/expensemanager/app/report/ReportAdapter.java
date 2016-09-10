package com.expensemanager.app.report;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by Zhaolong Zhong on 8/24/16.
 */

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG= ReportAdapter.class.getSimpleName();

    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DEFAULT = 1;
    private ArrayList<Date[]> dates;
    private Context context;
    private int requestCode;
    private double averageExpense;

    public ReportAdapter(Context context, ArrayList<Date[]> dates, int requestCode) {
        this.context = context;
        this.dates = dates;
        this.requestCode = requestCode;
    }

    private Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return dates.size() + (dates.size() > 1 ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && dates.size() > 1) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                view = inflater.inflate(R.layout.report_item_header, parent, false);
                break;
            case VIEW_TYPE_DEFAULT:
                view = inflater.inflate(R.layout.report_item_default, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.report_item_default, parent, false);
                break;
        }

        viewHolder = new ViewHolderDefault(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                Log.i(TAG, "header");
                ViewHolderDefault viewHolderHeader = (ViewHolderDefault) viewHolder;
                String averageString = null;
                switch (requestCode) {
                    case WEEKLY:
                        averageString = "Weekly ave: ";
                        break;
                    case MONTHLY:
                        averageString = "Monthly ave: ";
                        break;
                    case YEARLY:
                        averageString = "Yearly ave: ";
                        break;
                }
                viewHolderHeader.averageTextView.setText(averageString + Helpers.formatNumToDouble(averageExpense));
                break;
            case VIEW_TYPE_DEFAULT:
                Log.i(TAG, "default");
                ViewHolderDefault viewHolderDefault = (ViewHolderDefault) viewHolder;
                configureViewHolderDefault(viewHolderDefault, position);
                break;
            default:
                break;
        }
    }

    private void configureViewHolderDefault(ViewHolderDefault viewHolder, int position) {
        if (dates.size() > 1) {
            position -= 1;
        }

        Date[] startEnd = dates.get(position);

        String name = null;
        switch(requestCode) {
            case WEEKLY:
                name = Helpers.getWeekStartEndString(startEnd[0]);
                break;
            case MONTHLY:
                name = Helpers.getMonthStringFromDate(startEnd[0]);
                break;
            case YEARLY:
                name = Helpers.getYearStringFromDate(startEnd[0]);
                break;
        }

        if (name == null) {
            return;
        }

        viewHolder.nameTextView.setText(name);

        viewHolder.itemView.setOnClickListener(v -> {
            ReportDetailActivity.newInstance(context, startEnd, requestCode);
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void clear() {
        dates.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Date[]> dates) {
        if (dates == null) {
            return;
        }
        this.dates.addAll(dates);
        notifyDataSetChanged();
        refreshHeader();
    }

    private void refreshHeader() {
        if (dates.size() > 1) {
            averageExpense = getTotalExpense() / dates.size();
            notifyItemChanged(0);
        }
    }

    private double  getTotalExpense() {
        double total = 0.0;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.shared_preferences_session_key), 0);
        String groupId = sharedPreferences.getString(Group.ID_KEY, null);

        for (Expense expense : Expense.getAllExpensesByGroupId(groupId)) {
            total += expense.getAmount();
        }

        return (double) Math.round(total * 100) / 100;
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        // Header
        public TextView averageTextView;

        // Default item
        public TextView nameTextView;

        private View itemView;

        public ViewHolderDefault(View view, int viewType) {
            super(view);

            if (viewType == VIEW_TYPE_HEADER) {
                averageTextView = (TextView) view
                    .findViewById(R.id.report_item_header_average_text_view_id);
            } else if (viewType == VIEW_TYPE_DEFAULT) {
                nameTextView = (TextView) view.findViewById(R.id.report_item_default_name_text_view_id);
            }

            itemView = view;
        }
    }
}
