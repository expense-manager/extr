package com.expensemanager.app.report;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.font.Font;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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
    private ArrayList<Double> sumLists;
    private ArrayList<List<Category> > categoryLists;
    private double total;
    private Context context;
    private int requestCode;
    private double averageExpense;
    private String groupId;

    public ReportAdapter(Context context, ArrayList<Date[]> dates, int requestCode) {
        this.context = context;
        this.dates = dates;
        this.requestCode = requestCode;
        sumLists = new ArrayList<>();
        categoryLists = new ArrayList<>();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
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
                String averageString = getContext().getString(R.string.report_average);
                viewHolderHeader.averageTextView.setText(averageString + Helpers.formatNumToDouble(averageExpense));
                viewHolderHeader.averageTextView.setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
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

        viewHolder.amountTextView.setText("$" + sumLists.get(position));
        viewHolder.categoryRecyclerView.setFocusable(false);
        viewHolder.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        viewHolder.categoryRecyclerView.setAdapter(new CategoryListAdapter(categoryLists.get(position)));
        if (categoryLists.get(position).size() == 0) {
            viewHolder.categoryRecyclerView.setVisibility(View.GONE);
        } else {
            viewHolder.categoryRecyclerView.setVisibility(View.VISIBLE);
        }

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

        viewHolder.categoryRecyclerView.setOnTouchListener((v, m) -> {
            if (m.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }
            ReportDetailActivity.newInstance(context, startEnd, requestCode);
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
            return true;
        });

        viewHolder.itemView.setOnClickListener(v -> {
            ReportDetailActivity.newInstance(context, startEnd, requestCode);
            ((Activity)getContext()).overridePendingTransition(R.anim.right_in, R.anim.stay);
        });
    }

    public void clear() {
        dates.clear();
        sumLists.clear();
        categoryLists.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Date[]> dates) {
        if (dates == null) {
            return;
        }
        this.dates.addAll(dates);

        loadDataFromDates(dates);
        refreshHeader();
        notifyDataSetChanged();
    }

    private void loadDataFromDates(List<Date[]> dates) {
        for (Date[] startEnd : dates) {
            double currentSum = 0;
            Map<Category, Double> categorySumMap = new HashMap<>();
            List<Expense> expenses = Expense.getAllExpensesByDateAndGroupId(startEnd[0], startEnd[1], groupId);
            for (Expense expense : expenses) {
                currentSum += expense.getAmount();
                Category category = expense.getCategory();
                Double categorySum = categorySumMap.get(category);

                if (categorySum == null) {
                    categorySumMap.put(category, expense.getAmount());
                } else {
                    categorySumMap.put(category, categorySum + expense.getAmount());
                }
            }

            if (categorySumMap.size() == 0) {
                sumLists.add(0D);
                categoryLists.add(new ArrayList<>());
                continue;
            }

            PriorityQueue<Map.Entry<Category, Double>> maxHeap = new PriorityQueue<>(
                categorySumMap.size(), new Comparator<Map.Entry<Category, Double>>() {
                @Override
                public int compare(Map.Entry<Category, Double> e1, Map.Entry<Category, Double> e2) {
                    return e2.getValue().compareTo(e1.getValue());
                }
            });

            for (Map.Entry<Category, Double> entry : categorySumMap.entrySet()) {
                maxHeap.offer(entry);
            }

            List<Category> currentCategoryList = new ArrayList<>();
            int boundary = Math.min(6, categorySumMap.size());
            for (int i = 0; i < boundary; i++) {
                Log.i(TAG, " " + maxHeap.peek().getValue());
                currentCategoryList.add(maxHeap.poll().getKey());
            }
            categoryLists.add(currentCategoryList);
            sumLists.add(Helpers.formatNumToDouble(currentSum));
            total += currentSum;
        }
    }

    private void refreshHeader() {
        if (dates.size() > 1) {
            averageExpense = total / dates.size();
            notifyItemChanged(0);
        }
    }

    public static class ViewHolderDefault extends RecyclerView.ViewHolder {
        // Header
        public TextView averageTextView;

        // Default item
        public TextView nameTextView;
        public TextView amountTextView;
        public RecyclerView categoryRecyclerView;

        private View itemView;

        public ViewHolderDefault(View view, int viewType) {
            super(view);

            if (viewType == VIEW_TYPE_HEADER) {
                averageTextView = (TextView) view
                    .findViewById(R.id.report_item_header_average_text_view_id);
            } else if (viewType == VIEW_TYPE_DEFAULT) {
                nameTextView = (TextView) view.findViewById(R.id.report_item_default_name_text_view_id);
                amountTextView = (TextView) view.findViewById(R.id.report_item_default_amount_text_view_id);
                categoryRecyclerView = (RecyclerView) view.findViewById(R.id.report_item_default_recycler_view_id);
            }

            itemView = view;
        }
    }
}
