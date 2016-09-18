package com.expensemanager.app.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.Helpers;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SettingsBudgetFragment extends DialogFragment {
    private static final String TAG= SettingsBudgetFragment.class.getSimpleName();

    public static final int WEEKLY = 0;
    public static final int MONTHLY = 1;

    private Unbinder unbinder;
    private SettingBudgetListener settingBudgetListener;
    private int requestCode;
    private double amount;

    @BindView(R.id.setting_budget_fragment_label_text_view_id) TextView budgetLabelTextView;
    @BindView(R.id.setting_budget_fragment_amount_edit_text_id) EditText budgetAmountEditText;
    @BindView(R.id.setting_budget_fragment_save_button_id) Button saveButton;
    @BindView(R.id.setting_budget_fragment_cancel_button_id) Button cancelButton;

    public SettingsBudgetFragment() {}

    public static SettingsBudgetFragment newInstance() {
        return new SettingsBudgetFragment();
    }

    public void setListener(SettingBudgetListener settingBudgetListener) {
        this.settingBudgetListener = settingBudgetListener;
    }

    public void setParams(int requestCode, double amount) {
        this.requestCode = requestCode;
        this.amount = amount;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_budget_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        invalidateViews();
    }

    private void invalidateViews() {
        if (requestCode == SettingsFragment.WEEKLY) {
            budgetLabelTextView.setText(getContext().getString(R.string.weekly_budget));
        } else {
            budgetLabelTextView.setText(getContext().getString(R.string.monthly_budget));
        }

        budgetAmountEditText.setText(String.valueOf(amount));
        budgetAmountEditText.requestFocus();
        budgetAmountEditText.setSelection(String.valueOf(amount).length());

        cancelButton.setOnClickListener(v -> close());
        saveButton.setOnClickListener(v -> save());
    }

    private  void save() {
        Helpers.closeSoftKeyboard(getContext());

        try {
            amount = Double.valueOf(budgetAmountEditText.getText().toString());
            amount = Helpers.formatNumToDouble(amount);
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount cannot be zero.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot convert amount to double.", e);
            Toast.makeText(getContext(), "Incorrect amount format.", Toast.LENGTH_SHORT).show();
            return;
        }

        settingBudgetListener.onFinishSettingBudgetDialog(requestCode, amount);
        close();
    }

    protected void close() {
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface SettingBudgetListener {
        void onFinishSettingBudgetDialog(int requestCode, double amount);
    }
}
