package com.expensemanager.app.expense;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.filter.CategoryFilterFragment;
import com.expensemanager.app.expense.filter.DateFilterFragment;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncExpense;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ExpenseActivity extends BaseActivity
    implements CategoryFilterFragment.CategoryFilterListener, DateFilterFragment.DateFilterListener {

    private static final String TAG = ExpenseActivity.class.getSimpleName();

    public static final String CATEGORY_ID = "category_id";
    public static final String START_END_DATE = "startEnd";
    public static final String IS_CATEGORY_FILTERED = "is_category_filtered";
    public static final String CATEGORY_FRAGMENT = "Category_Fragment";
    public static final String DATE_FRAGMENT = "Dater_Fragment";

    private ArrayList<Expense> expenses;
    private Category category;
    private boolean isCategoryFiltered;
    private Date startDate;
    private Date endDate;
    private boolean isDateFiltered;
    private String groupId;

    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.expense_activity_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.expense_activity_fab_id) FloatingActionButton fab;

    public static void newInstance(Context context) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, String categoryId) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        intent.putExtra(CATEGORY_ID, categoryId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, Date[] startEnd) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(START_END_DATE, startEnd);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    public static void newInstance(Context context, String categoryId, Date[] startEnd) {
        Intent intent = new Intent(context, ExpenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(START_END_DATE, startEnd);
        intent.putExtras(bundle);
        intent.putExtra(CATEGORY_ID, categoryId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_activity);
        ButterKnife.bind(this);

        setupToolbar();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            isDateFiltered = true;
            Date[] startEnd = (Date[]) bundle.getSerializable(START_END_DATE);
            startDate = startEnd[0];
            endDate = startEnd[1];
        }
        if (getIntent().hasExtra(CATEGORY_ID)) {
            isCategoryFiltered = true;
            String categoryId = getIntent().getStringExtra(CATEGORY_ID);
            category = Category.getCategoryById(categoryId);
        }

        expenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenses);
        setupRecyclerView();

        fab.setOnClickListener(v -> {
            NewExpenseActivity.newInstance(this);
            overridePendingTransition(R.anim.right_in, R.anim.stay);
        });

        invalidateViews();
        SyncExpense.getAllExpensesByGroupId(groupId);
    }

    @Override
    public void onFinishCategoryFilterDialog(Category category) {
        if (!isCategoryFiltered ||
            ((this.category == null && category == null) || (this.category != null
                && category != null && this.category.getId().equals(category.getId())))) {
            isCategoryFiltered = !isCategoryFiltered;
        }
        this.category = category;
        invalidateViews();
    }

    @Override
    public void onFinishDateFilterDialog(Date startDate, Date endDate) {
        this.isDateFiltered = startDate != null || endDate != null;
        this.startDate = startDate;
        this.endDate = endDate;
        invalidateViews();
    }

    private void invalidateViews() {
        expenseAdapter.clear();

        // Check size of group members
        if (Member.getAllMembersByGroupId(groupId).size() > 1) {
            expenseAdapter.setShowMember(true);
        } else {
            expenseAdapter.setShowMember(false);
        }

        if (isCategoryFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndCategoryAndGrouopId(startDate, endDate, category, groupId));
        } else if (isCategoryFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByCategoryAndGroupId(category, groupId));
        } else if (isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndGroupId(startDate, endDate, groupId));
        } else {
            expenseAdapter.addAll(Expense.getAllExpensesByGroupId(groupId));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(expenseAdapter);
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        titleTextView.setText(getString(R.string.expense));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_category_fragment_id:
                setupCategory();
                break;
            case R.id.menu_item_date_fragment_id:
                setupDate();
                break;
        }

        return true;
    }

    private void setupDate() {
        DateFilterFragment dateFilterFragment = DateFilterFragment.newInstance();
        dateFilterFragment.setListener(this);
        dateFilterFragment.setFilterParams(startDate, endDate);
        dateFilterFragment.show(getSupportFragmentManager(), DATE_FRAGMENT);
    }

    private void setupCategory() {
        CategoryFilterFragment categoryFilterFragment = CategoryFilterFragment.newInstance();
        categoryFilterFragment.setListener(this);
        categoryFilterFragment.setFilterParams(isCategoryFiltered, category);
        categoryFilterFragment.show(getSupportFragmentManager(), CATEGORY_FRAGMENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());
        SyncExpense.getAllExpensesByGroupId(groupId);
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
