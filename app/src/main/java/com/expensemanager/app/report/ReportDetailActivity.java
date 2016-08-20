package com.expensemanager.app.report;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ReportDetailActivity extends AppCompatActivity {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    private ArrayList<Category> categories;
    private ArrayList<Double> amounts;
    private ReportCategoryAdapter reportCategoryAdapter;

    @BindView(R.id.report_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.report_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.report_activity_pie_chart_id) PieChart pieChart;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ReportDetailActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail_activity);
        ButterKnife.bind(this);

        categories = new ArrayList<>();
        amounts = new ArrayList<>();
        reportCategoryAdapter = new ReportCategoryAdapter(this, categories, amounts);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        setUpChart();
        invalidateViews();
        SyncCategory.getAllCategories();
        SyncExpense.getAllExpenses();
    }

    private void invalidateViews() {
        reportCategoryAdapter.clear();
        reportCategoryAdapter.addAll(new ArrayList<>(Expense.getAllExpenses()));

        updateChart();
    }

    private void setUpChart() {
        // Show description on bottom right corner
        pieChart.setDescription("");
        // Animate chart
        pieChart.animateY(2000, Easing.EasingOption.EaseInOutCirc);
        // Disable label on pie chart
        pieChart.setDrawEntryLabels(false);
        // Slice select listener
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry en = (PieEntry) e;
                pieChart.setCenterText(en.getLabel() + '\n' + en.getValue() + '%');

            }

            @Override
            public void onNothingSelected() {
                pieChart.setCenterText("");
            }
        });
    }

    private void updateChart() {
        // todo: specific colors for categories
        List<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.JOYFUL_COLORS) {
            colors.add(c);
        }
        float totalAmount = 0;
        for (int i = 0; i < categories.size(); i++) {
            totalAmount += amounts.get(i);
        }
        // Update entries
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            // Calculate percentage
            float percentage = (float) (10000 * amounts.get(i) / totalAmount);
            float newValue = (int) percentage;
            entries.add(new PieEntry(newValue / 100, categories.get(i).getName()));
        }
        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        // Set data text size
        dataSet.setValueTextSize(12f);
        // Add colors list
        dataSet.setColors(colors);
        // Create data
        PieData data = new PieData(dataSet);
        // Set value color
        data.setValueTextColor(Color.BLACK);
        // Set legends
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // Set data value format
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                ViewPortHandler viewPortHandler) {
                return value + "%";
            }
        });
        pieChart.setData(data);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reportCategoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
