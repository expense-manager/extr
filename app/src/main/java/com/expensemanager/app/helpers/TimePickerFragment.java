package com.expensemanager.app.helpers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import com.expensemanager.app.R;

public class TimePickerFragment extends DialogFragment {
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static int TIME_PICKER;

    TimePickerDialog.OnTimeSetListener listener;

    public static TimePickerFragment newInstance(int hour, int minute) {
        TimePickerFragment frag = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(HOUR, hour);
        args.putInt(MINUTE, minute);
        frag.setArguments(args);
        return frag;
    }

    public void setListener(TimePickerDialog.OnTimeSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.MyTimePickerDialogTheme);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = getArguments().getInt(HOUR);
        int minute = getArguments().getInt(MINUTE);

        // Activity has to implement this interface
        //TimePickerDialog.OnTimeSetListener listener = (TimePickerDialog.OnTimeSetListener) getActivity();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}
