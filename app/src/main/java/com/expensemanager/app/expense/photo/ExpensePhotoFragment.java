package com.expensemanager.app.expense.photo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expensemanager.app.R;
import com.expensemanager.app.models.ExpensePhoto;
import com.expensemanager.app.overview.CustomPageIndicator;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ExpensePhotoFragment extends DialogFragment {
    private static final String TAG= ExpensePhotoFragment.class.getSimpleName();

    private static final String EXPENSE_ID = "expenseId";
    private static final String POSITION = "position";

    private String expenseId;
    private int position;
    private Unbinder unbinder;
    private ArrayList<ExpensePhoto> expensePhotos;
    private ExpensePhotoPagerAdapter expensePhotoPagerAdapter;

    @BindView(R.id.expense_photo_fragment_view_pager_id) ViewPager viewPager;
    @BindView(R.id.expense_photo_fragment_pager_indicator_container_id) LinearLayout mLinearLayout;
    @BindView(R.id.expense_photo_fragment_close_image_view_id) ImageView closeImageView;

    public ExpensePhotoFragment() {}

    public static ExpensePhotoFragment newInstance(String expenseId, int position) {
        ExpensePhotoFragment expensePhotoFragment = new ExpensePhotoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXPENSE_ID, expenseId);
        bundle.putInt(POSITION, position);
        expensePhotoFragment.setArguments(bundle);

        return expensePhotoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CategoryColorDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expense_photo_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        expenseId = bundle.getString(EXPENSE_ID);
        position = bundle.getInt(POSITION);
        expensePhotos = new ArrayList<>(ExpensePhoto.getExpensePhotoByExpenseId(expenseId));
        expensePhotoPagerAdapter = new ExpensePhotoPagerAdapter(getActivity(), expensePhotos);

        viewPager.setAdapter(expensePhotoPagerAdapter);
        viewPager.setCurrentItem(position);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setClipChildren(false);
        viewPager.setPageMarginDrawable(null);

        CustomPageIndicator viewPagerIndicator = new CustomPageIndicator(getActivity(), mLinearLayout, viewPager, R.drawable.indicator_circle_accent);
        viewPagerIndicator.setPageCount(expensePhotoPagerAdapter.getCount());
        viewPagerIndicator.setSpacingRes(R.dimen.space_medium);
        viewPagerIndicator.show();

        closeImageView.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
