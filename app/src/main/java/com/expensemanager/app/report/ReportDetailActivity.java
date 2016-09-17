package com.expensemanager.app.report;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.report.bar_char.ReportBarChartFragment;
import com.expensemanager.app.report.bar_char.ReportExpenseAdapter;
import com.expensemanager.app.report.main.ReportPagerAdapter;
import com.expensemanager.app.report.pie_char.ReportPieChartFragment;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
import com.expensemanager.app.service.font.Font;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportDetailActivity extends BaseActivity {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#BDBDBD";
    public static final String START_END_DATE = "startEnd";
    public static final String REQUEST_CODE = "request_code";
    public static final int DAYS_OF_WEEK = 7;
    public static final int MONTHS_OF_YEAR = 12;
    public static final int PIE_CHART_VALUE_THRESHOLD = 5;

    public static final int ANIMATION_TIME_MILLISECOND = 1200;
    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;

    private ReportPagerAdapter reportPagerAdapter;
    private Date[] startEnd;
    private ArrayList<Category> categories;
    private ArrayList<Double> amountsCategory;
    private List<Expense> expenses;
    private int timeSlotsLength;
    private double[] amountsTime;
    private int latestPosition;
    private int requestCode;
    private String loginUserId;
    private String groupId;
    private Map<String, Integer> categoryPositionMap;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.toolbar_edit_text_view_id) TextView editTextView;
    @BindView(R.id.toolbar_save_text_view_id) TextView saveTextView;
    @BindView(R.id.report_detail_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.report_detail_activity_tabs_id) TabLayout tabStrip;
    @BindView(R.id.report_detail_activity_viewpager_id) ViewPager viewPager;
    @BindView(R.id.report_detail_activity_pie_chart_id) PieChart pieChart;
    @BindView(R.id.report_detail_activity_bar_chart_id) BarChart barChart;
    @BindView(R.id.report_detail_activity_no_expense_hint_id) TextView noExpenseHintTextView;

    public static void newInstance(Context context, Date[] startEnd, int requestCode) {
        Intent intent = new Intent(context, ReportDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(REQUEST_CODE, requestCode);
        bundle.putSerializable(START_END_DATE, startEnd);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail_activity);
        ButterKnife.bind(this);



        loginUserId = Helpers.getLoginUserId();
        groupId = Helpers.getCurrentGroupId();

        categories = new ArrayList<>();
        amountsCategory = new ArrayList<>();
        categoryPositionMap = new HashMap<>();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            requestCode = bundle.getInt(REQUEST_CODE);
        }

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        setupToolbar();
        invalidateViews();

        setUpPieChart();
        setUpBarChart();

        updatePieChart();
        updateBarChart();

        SyncCategory.getAllCategoriesByGroupId(groupId);
        SyncExpense.getAllExpensesByGroupId(groupId);
    }

    private void invalidateViews() {
        if (startEnd != null) {
            Log.d(TAG, "Start: " + startEnd[0].getTime());
            Log.d(TAG, "End: " + startEnd[1].getTime());
            int[] startEndDay = Helpers.getStartEndDay(startEnd);
            if (startEndDay == null) {
                Log.i(TAG, "Invalid start end day.");
                return;
            }
            // Query data from date range
            expenses = Expense.getExpensesByRangeAndGroupId(startEnd, groupId);
        }

        if (expenses.size() == 0) {
            pieChart.setVisibility(View.INVISIBLE);
            barChart.setVisibility(View.INVISIBLE);
            noExpenseHintTextView.setVisibility(View.VISIBLE);
        }

        // Initialize bar chart data set
        if (requestCode == WEEKLY) {
            timeSlotsLength = DAYS_OF_WEEK + 2;
        } else if (requestCode == MONTHLY) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startEnd[0]);
            int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            timeSlotsLength = maxDays + 2;
        } else if (requestCode == YEARLY) {
            timeSlotsLength = MONTHS_OF_YEAR + 2;
        }

        amountsTime = new double[timeSlotsLength];

        // Fetch data
        fetchCategoriesAndAmounts(expenses);
        fetchTimeAndAmounts(expenses);

        // Create new Adapter
        reportPagerAdapter = new ReportPagerAdapter(getSupportFragmentManager(), startEnd, requestCode);
        viewPager.setAdapter(reportPagerAdapter);
        tabStrip.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.title_activity_report_detail));
        titleTextView.setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    private void setUpPieChart() {
        // Animate chart
        pieChart.animateY(ANIMATION_TIME_MILLISECOND, Easing.EasingOption.EaseInCirc);
        // Show description on bottom right corner
        pieChart.setDescription("");
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
        // Set legends
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

    private void setUpBarChart() {
        // Animate chart
        barChart.animateY(ANIMATION_TIME_MILLISECOND, Easing.EasingOption.EaseInCubic);
        // Show description on bottom right corner
        barChart.setDescription("");
        // Set min value for x axis
        if (expenses.size() == 0) {
            barChart.getXAxis().setEnabled(false);
        } else {
            barChart.getXAxis().setAxisMinValue(0);
            // Set max value for x axis
            barChart.getXAxis().setAxisMaxValue(timeSlotsLength - 1);
            // Hide grid line of x axis
            barChart.getXAxis().setDrawGridLines(false);
            // Place x axis to bottom
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        }
        // Hide grid lines from left y axis
        barChart.getAxisLeft().setDrawGridLines(false);
        // Hide grid lines from right y axis
        barChart.getAxisRight().setDrawGridLines(false);
        // Hide right y axis
        barChart.getAxisRight().setEnabled(false);
        // Hide left y axis
        barChart.getAxisLeft().setEnabled(false);
    }

    private void fetchCategoriesAndAmounts(List<Expense> expenses) {
        for (Expense expense : expenses) {
            String categoryId = expense.getCategoryId();
            if (categoryId == null) {
                categoryId = NO_CATEGORY_ID;
            }

            Integer position = categoryPositionMap.get(categoryId);

            if (position == null) {
                Category category = null;
                if (!categoryId.equals(NO_CATEGORY_ID)) {
                    category = Category.getCategoryById(categoryId);
                }

                if (category == null){
                    category = new Category();
                    category.setColor(NO_CATEGORY_COLOR);
                    category.setName(NO_CATEGORY_ID);
                }

                // Store position of new category into categoryPositionMap
                categoryPositionMap.put(categoryId, categories.size());
                // Add new category to list
                categories.add(category);
                // Add first amount to list
                amountsCategory.add(expense.getAmount());
            } else {
                // Get current amount
                double amount = amountsCategory.get(position);
                // Store new amount
                amountsCategory.set(position, amount + expense.getAmount());
            }
        }
    }

    private void fetchTimeAndAmounts(List<Expense> expenses) {
        for (Expense expense : expenses) {
            if (requestCode == WEEKLY) {
                int dateNum = Helpers.getDayOfWeek(expense.getExpenseDate());
                amountsTime[dateNum] += expense.getAmount();
            } else if (requestCode == MONTHLY) {
                int dateNum = Helpers.getDayOfMonth(expense.getExpenseDate());
                amountsTime[dateNum] += expense.getAmount();
            } else {
                int monthNum = Helpers.getMonthOfYear(expense.getExpenseDate());
                amountsTime[monthNum + 1] += expense.getAmount();
            }
        }
    }

    private void updatePieChart() {
        // Get colors from categories
        List<Integer> colors = new ArrayList<>();
        for (Category category : categories) {
            colors.add(Color.parseColor(category.getColor()));
        }

        // Calculate total expense
        float totalAmount = 0;
        for (int i = 0; i < categories.size(); i++) {
            totalAmount += amountsCategory.get(i);
        }

        // Update entries
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            // Calculate percentage
            float percentage = (float) (10000 * amountsCategory.get(i) / totalAmount);
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
        data.setValueTextColor(Color.WHITE);
        // Set data value format
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                ViewPortHandler viewPortHandler) {
                if (value <= PIE_CHART_VALUE_THRESHOLD) {
                    return "";
                }
                return value + "%";
            }
        });
        pieChart.setData(data);
    }

    private void updateBarChart() {
        barChart.getXAxis().setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 0 || value % 1 != 0 || value > timeSlotsLength - 2) {
                    return "";
                }
                int pos = (int) value;

                switch(requestCode) {
                    case WEEKLY:
                        return Helpers.getDayOfWeekString(pos);
                    case MONTHLY:
                        return Helpers.getDayOfMonthString(pos);
                    case YEARLY:
                        return Helpers.getMonthOfYearString(pos);
                }
                return "";
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        barChart.animateY(1000, Easing.EasingOption.EaseInCubic);

        List<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.COLORFUL_COLORS ) {
            colors.add(c);
        }
        for (int c : ColorTemplate.JOYFUL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.PASTEL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.LIBERTY_COLORS ) {
            colors.add(c);
        }

        List<BarEntry> entries = new ArrayList<>();

        for (int i = 1; i < timeSlotsLength - 1; i++) {
            entries.add(new BarEntry(i, (float) amountsTime[i], ""));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);
        // Default x axis width is 1, bar width is 0.9, spacing is 0.1
        data.setBarWidth(0.8f);
        data.setValueTextSize(10f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                ViewPortHandler viewPortHandler) {
                return value != 0 ? Helpers.doubleToCurrency(value) : "";
            }
        });

        barChart.setData(data);
        // Disable scalable
        barChart.setScaleEnabled(false);
        // Disable tab to highlight
        barChart.setHighlightPerTapEnabled(false);
        // Disable highlight during drag
        barChart.setHighlightPerDragEnabled(false);

        boolean isCurrentFrame = new Date().compareTo(startEnd[1]) <= 0;
        switch(requestCode) {
            case WEEKLY:
                // Limit view port to smaller data set
                barChart.setVisibleXRangeMaximum(9);
                break;
            case MONTHLY:
                // Limit view port to smaller data set
                barChart.setVisibleXRangeMaximum(7);
                if (isCurrentFrame) {
                    latestPosition = Helpers.getCurrentDayOfMonth();
                } else {
                    latestPosition = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                barChart.moveViewToX(Math.max(latestPosition - 5, 0));
                break;
            case YEARLY:
                // Limit view port to smaller data set
                barChart.setVisibleXRangeMaximum(7);
                if (isCurrentFrame) {
                    latestPosition = Helpers.getCurrentMonthOfYear() + 1;
                } else {
                    latestPosition = ReportExpenseAdapter.LEN_OF_YEAR;
                }
                barChart.moveViewToX(Math.max(latestPosition - 6, 0));
        }
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int newPosition) {
            if (expenses.size() == 0) {
                pieChart.setVisibility(View.INVISIBLE);
                barChart.setVisibility(View.INVISIBLE);
                noExpenseHintTextView.setVisibility(View.VISIBLE);
            }

            Fragment fragment = reportPagerAdapter.getFragmentByPosition(newPosition);
            if (fragment instanceof ReportPieChartFragment) {
                // Hide bar chart
                barChart.setVisibility(View.INVISIBLE);
                // Update data
                pieChart.invalidate();
                // Animate chart
                pieChart.animateY(ANIMATION_TIME_MILLISECOND, Easing.EasingOption.EaseInCirc);
                // Show pie chart
                pieChart.setVisibility(View.VISIBLE);
            } else if (fragment instanceof ReportBarChartFragment) {
                // Hide pie chart
                pieChart.setVisibility(View.INVISIBLE);
                // Show bar chart
                barChart.setVisibility(View.VISIBLE);
                // Move to default position
                switch(requestCode) {
                    case MONTHLY:
                        barChart.moveViewToX(Math.max(latestPosition - 5, 0));
                        break;
                    case YEARLY:
                        barChart.moveViewToX(Math.max(latestPosition - 6, 0));
                        break;
                }
                // Animate chart
                barChart.animateY(ANIMATION_TIME_MILLISECOND, Easing.EasingOption.EaseInCubic);
                // Update data
                barChart.invalidate();
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) { }

        @Override
        public void onPageScrollStateChanged(int arg0) { }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(viewPager != null) {
            viewPager.addOnPageChangeListener(pageChangeListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(pageChangeListener);
        }
    }
}
