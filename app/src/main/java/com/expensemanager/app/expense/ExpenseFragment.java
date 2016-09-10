package com.expensemanager.app.expense;

import com.expensemanager.app.R;
import com.expensemanager.app.expense.filter.CategoryFilterFragment;
import com.expensemanager.app.expense.filter.DateFilterFragment;
import com.expensemanager.app.expense.filter.MemberFilterFragment;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.main.Analytics;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.RNotification;
import com.expensemanager.app.notifications.NotificationAdapter;
import com.expensemanager.app.service.SyncExpense;
import com.twotoasters.jazzylistview.effects.SlideInEffect;
import com.twotoasters.jazzylistview.recyclerview.JazzyRecyclerViewScrollListener;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ExpenseFragment extends Fragment {
    private static final String TAG = ExpenseFragment.class.getSimpleName();

    public static final String CATEGORY_ID = "category_id";
    public static final String START_END_DATE = "startEnd";
    public static final String IS_CATEGORY_FILTERED = "is_category_filtered";
    public static final String CATEGORY_FRAGMENT = "Category_Fragment";
    public static final String DATE_FRAGMENT = "Date_Fragment";
    public static final String USER_FRAGMENT = "User_Fragment";

    private ArrayList<Expense> expenses;
    private Member member;
    private boolean isMemberFiltered;
    private Category category;
    private boolean isCategoryFiltered;
    private Date startDate;
    private Date endDate;
    private boolean isDateFiltered;
    private String groupId;
    private long syncTimeInMillis;
    private String syncTimeKey;

    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.expense_fragment_recycler_view_id) RecyclerView recyclerView;
    @BindView(R.id.swipeContainer_id) SwipeRefreshLayout swipeContainer;

    public static ExpenseFragment newInstance() {
        return new ExpenseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.expense_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        groupId = sharedPreferences.getString(Group.ID_KEY, null);
        syncTimeKey = Helpers.getSyncTimeKey(TAG, groupId);
        syncTimeInMillis = sharedPreferences.getLong(syncTimeKey, 0);

        expenses = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(getActivity(), expenses);
        setupRecyclerView();

        invalidateViews();

        if (Helpers.needToSync(syncTimeInMillis)) {
            SyncExpense.getAllExpensesByGroupId(groupId);
            syncTimeInMillis = Calendar.getInstance().getTimeInMillis();
            Helpers.saveSyncTime(getActivity(), syncTimeKey, syncTimeInMillis);
        }

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncExpense.getAllExpensesByGroupId(groupId).continueWith(onGetExpenseFinished, Task.UI_THREAD_EXECUTOR);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    public void loadDate(Date[] startEnd) {
        if (startEnd == null) {
            return;
        }

        isDateFiltered = true;
        startDate = startEnd[0];
        endDate = startEnd[1];

        //invalidateViews();
    }

    public void loadCategory(String categoryId) {
        isCategoryFiltered = true;
        category = Category.getCategoryById(categoryId);

        //invalidateViews();
    }

    private Continuation<Void, Void> onGetExpenseFinished = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, "Error:", task.getError());
            }

            if (swipeContainer != null) {
                swipeContainer.setRefreshing(false);
            }

            return null;
        }
    };

    public void invalidateViews() {
        expenseAdapter.clear();

        // Check size of group members
        if (Member.getAllAcceptedMembersByGroupId(groupId).size() > 1) {
            expenseAdapter.setShowMember(true);
        } else {
            expenseAdapter.setShowMember(false);
        }

        if (isMemberFiltered && isCategoryFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndDateAndCategoryAndGroupId(member, startDate, endDate, category, groupId));
        } else if (isMemberFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndDateAndGroupId(member, startDate, endDate, groupId));
        } else if (isMemberFiltered && isCategoryFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndCategoryAndGroupId(member, category, groupId));
        } else if (isCategoryFiltered && isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndCategoryAndGroupId(startDate, endDate, category, groupId));
        } else if (isMemberFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByMemberAndGroupId(member, groupId));
        } else if (isCategoryFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByCategoryAndGroupId(category, groupId));
        } else if (isDateFiltered) {
            expenseAdapter.addAll(Expense.getAllExpensesByDateAndGroupId(startDate, endDate, groupId));
        } else {
            expenseAdapter.addAll(Expense.getAllExpensesByGroupId(groupId));
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(expenseAdapter);
        // Add JazzyListView scroll effect
        JazzyRecyclerViewScrollListener jazzyScrollListener = new JazzyRecyclerViewScrollListener();
        recyclerView.addOnScrollListener(jazzyScrollListener);
        jazzyScrollListener.setTransitionEffect(new SlideInEffect());
    }

    @Override
    public void onPrepareOptionsMenu (Menu menu) {
        if (Member.getAllAcceptedMembersByGroupId(groupId).size() < 2) {
            Log.i(TAG, "accepted member count: " + Member.getAllAcceptedMembersByGroupId(groupId).size());
            menu.getItem(0).setVisible(false);
            menu.getItem(0).setEnabled(false);
            // You can also use something like:
            // menu.findItem(R.id.example_foobar).setEnabled(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_menu, menu);
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_user_fragment_id:
                setupMember();
                return true;
            case R.id.menu_item_category_fragment_id:
                setupCategory();
                return true;
            case R.id.menu_item_date_fragment_id:
                setupDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupMember() {
        MemberFilterFragment memberFilterFragment = MemberFilterFragment.newInstance();
        memberFilterFragment.setListener(memberFilterListener);
        memberFilterFragment.setFilterParams(isMemberFiltered, member);
        memberFilterFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), USER_FRAGMENT);

        Analytics.track(getString(R.string.event_expense_date_filter_clicked));
    }

    private void setupDate() {
        DateFilterFragment dateFilterFragment = DateFilterFragment.newInstance();
        dateFilterFragment.setListener(dateFilterListener);
        dateFilterFragment.setFilterParams(startDate, endDate);
        dateFilterFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), DATE_FRAGMENT);

        Analytics.track(getString(R.string.event_expense_date_filter_clicked));
    }

    private void setupCategory() {
        CategoryFilterFragment categoryFilterFragment = CategoryFilterFragment.newInstance();
        categoryFilterFragment.setListener(categoryFilterListener);
        categoryFilterFragment.setFilterParams(isCategoryFiltered, category);
        categoryFilterFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), CATEGORY_FRAGMENT);
    }

    private MemberFilterFragment.MemberFilterListener memberFilterListener = new MemberFilterFragment.MemberFilterListener() {
        @Override
        public void onFinishMemberFilterDialog(Member member) {
            if (!isMemberFiltered ||
                (ExpenseFragment.this.member != null && member != null && ExpenseFragment.this.member.getId().equals(member.getId()))) {
                isMemberFiltered = !isMemberFiltered;
            }
            ExpenseFragment.this.member = member;
            invalidateViews();
        }
    };

    private DateFilterFragment.DateFilterListener dateFilterListener = new DateFilterFragment.DateFilterListener() {
        @Override
        public void onFinishDateFilterDialog(Date startDate, Date endDate) {
            isDateFiltered = startDate != null || endDate != null;
            ExpenseFragment.this.startDate = startDate;
            ExpenseFragment.this.endDate = endDate;
            invalidateViews();
        }
    };

    private CategoryFilterFragment.CategoryFilterListener categoryFilterListener = new CategoryFilterFragment.CategoryFilterListener() {
        @Override
        public void onFinishCategoryFilterDialog(Category category) {
            if (!isCategoryFiltered ||
                ((ExpenseFragment.this.category == null && category == null) || (ExpenseFragment.this.category != null
                    && category != null && ExpenseFragment.this.category.getId().equals(category.getId())))) {
                isCategoryFiltered = !isCategoryFiltered;
            }
            ExpenseFragment.this.category = category;
            invalidateViews();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(v -> invalidateViews());

        invalidateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        Realm realm = Realm.getDefaultInstance();
        realm.removeAllChangeListeners();
    }
}
