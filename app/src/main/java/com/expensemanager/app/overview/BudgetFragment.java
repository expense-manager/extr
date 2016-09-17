package com.expensemanager.app.overview;

import android.app.Fragment;
import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expensemanager.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 9/16/16.
 */

public class BudgetFragment extends Fragment {
    private static final String TAG = BudgetFragment.class.getSimpleName();

    private int levelStatus = 0;
    private int level = 8000;
    private int steps = 100;
    private int animationTime = 4000;

    private ClipDrawable clipDrawable;
    private Handler handler = new Handler();

    @BindView(R.id.circle_solid_view_id) View budgetView;

    public static Fragment newInstance() {
        return new BudgetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.budget_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        clipDrawable = (ClipDrawable) budgetView.getBackground();
        clipDrawable.setLevel(0);
        handler.post(animateRunnable);
    }

    private Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            animateClipDrawable(animationTime);
        }
    };

    private void animateClipDrawable(int milliseconds) {
        int stepTime = milliseconds / steps;
        levelStatus += level/steps;
        clipDrawable.setLevel(levelStatus);

        if (levelStatus <= level) {
            handler.postDelayed(animateRunnable, stepTime);
        } else {
            handler.removeCallbacks(animateRunnable);
        }
    }
}
