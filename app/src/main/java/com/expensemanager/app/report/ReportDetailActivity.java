package com.expensemanager.app.report;

import com.astuetz.PagerSlidingTabStrip;
import com.expensemanager.app.R;
import com.expensemanager.app.expense.NewExpenseActivity;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.service.SyncCategory;
import com.expensemanager.app.service.SyncExpense;
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ReportDetailActivity extends AppCompatActivity {
    private static final String TAG = ReportDetailActivity.class.getSimpleName();

    public static final String NO_CATEGORY_ID = "No Category";
    public static final String NO_CATEGORY_COLOR = "#F3F3F3";
    public static final String START_END_DATE = "startEnd";
    public static final String REQUEST_CODE = "request_code";

    public static final int ANIMATION_TIME_MILLISECOND = 1200;
    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;
    public static final int YEARLY = 2;

    ReportPagerAdapter reportPagerAdapter;
    private ReportCategoryAdapter reportCategoryAdapter;
    private Date[] startEnd;
    private ArrayList<Category> categories;
    private ArrayList<Double> amountsCategory;
    private List<Expense> expenses;
    private String[] timeSlots;
    private double[] amountsTime;
    private int latestPosition;
    private int requestCode;
    private Map<String, Integer> map;

    @BindView(R.id.report_activity_fab_id) FloatingActionButton fab;
    @BindView(R.id.report_activity_tabs_id) PagerSlidingTabStrip tabStrip;
    @BindView(R.id.report_activity_viewpager_id) ViewPager viewPager;
    @BindView(R.id.report_activity_pie_chart_id) PieChart pieChart;
    @BindView(R.id.report_activity_bar_chart_id) BarChart barChart;

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

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        setupViews();

        setUpPieChart();
        setUpBarChart();

        updatePieChart();
        updateBarChart();

        SyncCategory.getAllCategories();
        SyncExpense.getAllExpenses();
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
        barChart.getXAxis().setAxisMinValue(0);
        // Set max value for x axis
        barChart.getXAxis().setAxisMaxValue(timeSlots.length - 1);
        // Hide grid line of x axis
        barChart.getXAxis().setDrawGridLines(false);
        // Place x axis to bottom
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
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
        if (expenses == null || expenses.size() == 0) {
            return;
        }

        for (Expense e : expenses) {
            String cId = e.getCategoryId();
            if (cId == null) {
                cId = NO_CATEGORY_ID;
            }
            Integer pos = map.get(cId);
            if (pos == null) {
                Category c = null;
                // Get new category
                if (cId.equals(NO_CATEGORY_ID)) {
                    // Get temp category for no category
                    c = new Category();
                    c.setColor(NO_CATEGORY_COLOR);
                    c.setName(NO_CATEGORY_ID);
                } else {
                    c = Category.getCategoryById(cId);
                }
                // Store pos of new category into map
                map.put(cId, categories.size());
                // Add new category to list
                categories.add(c);
                // Add first amount to list
                amountsCategory.add(e.getAmount());
            } else {
                // Get current amount
                double am = amountsCategory.get(pos);
                // Store new amount
                amountsCategory.set(pos, am + e.getAmount());
            }
        }
    }

    private void fetchTimeAndAmounts(List<Expense> expenses) {
        if (expenses == null) {
            return;
        }
        // todo: fetcch bar chart data from expense date
        for (Expense e : expenses) {
            if (requestCode == WEEKLY) {
                int dateNum = Helpers.getDayOfWeek(e.getExpenseDate());
                amountsTime[dateNum] += e.getAmount();
            } else if (requestCode == MONTHLY) {
                int dateNum = Helpers.getDayOfMonth(e.getExpenseDate());
                amountsTime[dateNum] += e.getAmount();
            } else {
                int monthNum = Helpers.getMonthOfYear(e.getExpenseDate());
                amountsTime[monthNum] += e.getAmount();
            }
        }
    }

    private void updatePieChart() {
        // Get colors from categories
        List<Integer> colors = new ArrayList<>();
        for (Category c : categories) {
            colors.add(Color.parseColor(c.getColor()));
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
                return value + "%";
            }
        });
        pieChart.setData(data);
    }

    private void updateBarChart() {
        barChart.getXAxis().setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 0 || value % 1 != 0 || value > timeSlots.length - 2) {
                    return "";
                }
                int pos = (int) value;
                switch(requestCode) {
                    case WEEKLY:
                        return ReportExpenseAdapter.WEEK[pos];
                    case MONTHLY:
                        switch (pos % 10) {
                            case 1:
                                return String.valueOf(pos) + "st";
                            case 2:
                                return String.valueOf(pos) + "nd";
                            case 3:
                                return String.valueOf(pos) + "rd";
                            default:
                                return String.valueOf(pos) + "th";
                        }

                        //return String.valueOf(value);
                    case YEARLY:
                        return ReportExpenseAdapter.YEAR[pos];
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

        for (int i = 1; i < timeSlots.length - 1; i++) {
            if (requestCode == MONTHLY) {
                entries.add(new BarEntry(i, (float) amountsTime[i], ""));
            } else {
                entries.add(new BarEntry(i, (float) amountsTime[i], timeSlots[i]));
            }
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
                return value != 0 ? "$" + value : "";
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
                barChart.moveViewToX(latestPosition - 6);
                break;
            case YEARLY:
                // Limit view port to smaller data set
                // todo: test year data display
                barChart.setVisibleXRangeMaximum(9);
                if (isCurrentFrame) {
                    latestPosition = Helpers.getCurrentMonthOfYear();
                } else {
                    latestPosition = ReportExpenseAdapter.LEN_OF_YEAR;
                }
                barChart.moveViewToX(Math.max(latestPosition - 8, 0));
        }
    }

    private void setupViews() {
        categories = new ArrayList<>();
        amountsCategory = new ArrayList<>();
        map = new HashMap<>();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            requestCode = bundle.getInt(REQUEST_CODE);
        }

        if (startEnd != null) {
            Log.d(TAG, "Start: " + startEnd[0].getTime());
            Log.d(TAG, "End: " + startEnd[1].getTime());
            int[] startEndDay = Helpers.getStartEndDay(startEnd);
            if (startEndDay == null) {
                Log.i(TAG, "Invalid start end day.");
                return;
            }
            // Query data from date range
            expenses = Expense.getExpensesByRange(startEnd);
        }

        if (expenses == null) {
            return;
        }

        // Initialize bar chart data set
        if (requestCode == WEEKLY) {
            timeSlots = ReportExpenseAdapter.WEEK;
        } else if (requestCode == MONTHLY) {
            Calendar calendar = Calendar.getInstance();
            int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            timeSlots = new String[maxDays + 2];
        } else if (requestCode == YEARLY) {
            timeSlots = ReportExpenseAdapter.YEAR;
        }
        amountsTime = new double[timeSlots.length];

        // Fetch data
        fetchCategoriesAndAmounts(expenses);
        fetchTimeAndAmounts(expenses);

        // Create new Adapter
        reportPagerAdapter = new ReportPagerAdapter(getSupportFragmentManager(), startEnd, requestCode);
        // Set viewpager adapter for the pager
        viewPager.setAdapter(reportPagerAdapter);
        // Attach the pager tab to viewpager
        tabStrip.setViewPager(viewPager);
        // Listen to viewpager
        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        int currentPosition = 0;

        @Override
        public void onPageSelected(int newPosition) {

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
                // Update data
                barChart.invalidate();
                // Animate chart
                barChart.animateY(ANIMATION_TIME_MILLISECOND, Easing.EasingOption.EaseInCubic);
                // Show bar chart
                barChart.setVisibility(View.VISIBLE);
            }

            currentPosition = newPosition;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) { }

        @Override
        public void onPageScrollStateChanged(int arg0) { }
    };

    // Return the order of fragments in the view pager
    public class ReportPagerAdapter extends FragmentStatePagerAdapter {
        private String[] tabTitles = {"Categories", "Expenses"};
        private Map<Integer, Fragment> map = new HashMap<>();
        private Date[] startEnd;
        private int requestCode;

        // Adapter gets the manager insert or remove fragment from activity
        public ReportPagerAdapter(FragmentManager fragmentManager, Date[] startEnd, int requestCode) {
            super(fragmentManager);
            this.startEnd = startEnd;
            this.requestCode = requestCode;
        }

        // Decide fragment by tab position
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment =  ReportPieChartFragment.newInstance(startEnd, requestCode);
            } else if (position == 1) {
                fragment = ReportBarChartFragment.newInstance(startEnd, requestCode);
            }
            map.put(position, fragment);
            return fragment;
        }

        // Decide tab name by tab position
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        // get total count of fragments
        @Override
        public int getCount() {
            return tabTitles.length;
        }

        // Get fragment by position
        public Fragment getFragmentByPosition(int position) {
            return map.get(position);
        }

        // Update fragments
        public void updateFragments() {
            for (Fragment fragment : map.values()) {
                if (fragment instanceof ReportPieChartFragment) {
                    ((ReportPieChartFragment) fragment).invalidateViews();
                } else if (fragment instanceof  ReportBarChartFragment) {
                    ((ReportBarChartFragment) fragment).invalidateViews();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        // Listen to database updates
        realm.addChangeListener(v -> reportPagerAdapter.updateFragments());
        // Sync for updates
        SyncCategory.getAllCategories();
        SyncExpense.getAllExpenses();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
