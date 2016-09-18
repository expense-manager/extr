package com.expensemanager.app.helpers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.expensemanager.app.R;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment {
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String DAY = "day";
    public static int DATE_PICKER;

    DatePickerDialog.OnDateSetListener listener;

    public static DatePickerFragment newInstance(int year, int month, int day) {
        DatePickerFragment frag = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR, year);
        args.putInt(MONTH, month);
        args.putInt(DAY, day);
        frag.setArguments(args);
        return frag;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.MyDatePickerDialogTheme);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = getArguments().getInt(YEAR);
        int month = getArguments().getInt(MONTH);
        int day = getArguments().getInt(DAY);

        if (year == 0) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }
        // Activity needs to implement this interface
        //DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getActivity();

        // Create a new instance of TimePickerDialog and return it
        return new DatePickerDialog(getActivity(), listener, year, month, day);  // month (0 - 11)
    }
}
