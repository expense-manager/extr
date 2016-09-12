package com.expensemanager.app.expense.filter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.expensemanager.app.R;
import com.expensemanager.app.helpers.DatePickerFragment;
import com.expensemanager.app.helpers.TimePickerFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DateFilterFragment extends DialogFragment {
    private static final String TAG= DateFilterFragment.class.getSimpleName();

    public static final int START_DATE_PICKER = 0;
    public static final int END_DATE_PICKER = 1;
    public static final int START_TIME_PICKER = 2;
    public static final int END_TIME_PICKER = 3;
    public static final String DATE_PICKER = "date_picker";
    public static final String TIME_PICKER = "time_picker";

    private Unbinder unbinder;
    private DateFilterListener dateFilterListener;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private Date startDate;
    private Date endDate;

    @BindView(R.id.date_filter_fragment_start_date_switch_id) Switch startDateSwitch;
    @BindView(R.id.date_filter_fragment_start_date_text_view_id) TextView startDateTextView;
    @BindView(R.id.date_filter_fragment_start_time_text_view_id) TextView startTimeTextView;
    @BindView(R.id.date_filter_fragment_end_date_switch_id) Switch endDateSwitch;
    @BindView(R.id.date_filter_fragment_end_date_text_view_id) TextView endDateTextView;
    @BindView(R.id.date_filter_fragment_end_time_text_view_id) TextView endTimeTextView;
    @BindView(R.id.date_filter_fragment_save_button_id) Button saveButton;
    @BindView(R.id.date_filter_fragment_cancel_button_id) Button cancelButton;


    public DateFilterFragment() {}

    public static DateFilterFragment newInstance() {
        return new DateFilterFragment();
    }

    public void setListener(DateFilterListener dateFilterListener) {
        this.dateFilterListener = dateFilterListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expense_date_filter_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        invalidateViews();
    }

    private void invalidateViews() {
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        if (startDate != null) {
            startDateSwitch.setChecked(true);
            startCalendar.setTime(startDate);
        } else {
            startDateSwitch.setChecked(false);
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
        }

        if (endDate != null) {
            endDateSwitch.setChecked(true);
            endCalendar.setTime(endDate);
        } else {
            endDateSwitch.setChecked(false);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
        }

        // Initialize date and time
        setupDateAndTime(startDateSwitch.isChecked(), startDateTextView, startTimeTextView);
        setupDateAndTime(endDateSwitch.isChecked(), endDateTextView, endTimeTextView);

        // Start expense date listener
        startDateTextView.setOnClickListener(v -> {setupDatePicker(startCalendar, true);});
        startTimeTextView.setOnClickListener(v -> {setupTimePicker(startCalendar, true);});
        // End expense date listener
        endDateTextView.setOnClickListener(v -> {setupDatePicker(endCalendar, false);});
        endTimeTextView.setOnClickListener(v -> {setupTimePicker(endCalendar, false);});

        cancelButton.setOnClickListener(v -> close());
        saveButton.setOnClickListener(v -> save());

        startDateSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            setupDateAndTime(b, startDateTextView, startTimeTextView);
        });

        endDateSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            setupDateAndTime(b, endDateTextView, endTimeTextView);
        });


        formatDateAndTime(startCalendar.getTime(), startDateTextView, startTimeTextView);
        formatDateAndTime(endCalendar.getTime(), endDateTextView, endTimeTextView);
    }

    private void setupDateAndTime(boolean isCheck, TextView dateTextView, TextView timeTextView) {
        if (isCheck) {
            int textColor = ContextCompat.getColor(getContext(), R.color.black);
            dateTextView.setTextColor(textColor);
            timeTextView.setTextColor(textColor);
        } else {
            int textColor = ContextCompat.getColor(getContext(), R.color.gray_light);
            dateTextView.setTextColor(textColor);
            timeTextView.setTextColor(textColor);
        }
    }

    private void formatDateAndTime(Date date, TextView dateTextView, TextView timeTextView) {
        // Create format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        // Parse date and set text
        dateTextView.setText(dateFormat.format(date));
        timeTextView.setText(timeFormat.format(date));
    }

    private void setupDatePicker(Calendar calendar, boolean isStartDate) {
        DatePickerFragment.DATE_PICKER = isStartDate ? START_DATE_PICKER : END_DATE_PICKER;

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerFragment datePickerFragment = DatePickerFragment
            .newInstance(year, month, day);
        datePickerFragment.setListener(onDateSetListener);
        datePickerFragment.show(getFragmentManager(), DATE_PICKER);
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            if (DatePickerFragment.DATE_PICKER == START_DATE_PICKER) {
                startDateSwitch.setChecked(true);

                startCalendar.set(year, monthOfYear, dayOfMonth);
                formatDateAndTime(startCalendar.getTime(), startDateTextView, startTimeTextView);
            } else {
                endDateSwitch.setChecked(true);

                endCalendar.set(year, monthOfYear, dayOfMonth);
                formatDateAndTime(endCalendar.getTime(), endDateTextView, endTimeTextView);
            }
        }
    };

    private void setupTimePicker(Calendar calendar, boolean isStartDate) {
        TimePickerFragment.TIME_PICKER = isStartDate ? START_TIME_PICKER : END_TIME_PICKER;

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerFragment timePickerFragment = TimePickerFragment
            .newInstance(hour, minute);
        timePickerFragment.setListener(onTimeSetListener);
        timePickerFragment.show(getFragmentManager(), TIME_PICKER);
    }

    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            if (TimePickerFragment.TIME_PICKER == START_TIME_PICKER) {
                startDateSwitch.setChecked(true);

                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startCalendar.set(Calendar.MINUTE, minute);
                formatDateAndTime(startCalendar.getTime(), startDateTextView, startTimeTextView);
            } else {
                endDateSwitch.setChecked(true);

                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endCalendar.set(Calendar.MINUTE, minute);
                formatDateAndTime(endCalendar.getTime(), endDateTextView, endTimeTextView);
            }
        }
    };

    public void setFilterParams(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private  void save() {
        startDate = startDateSwitch.isChecked() ? startCalendar.getTime() : null;
        endDate = endDateSwitch.isChecked() ? endCalendar.getTime() : null;

        if (startDate != null && endDate != null && startDate.compareTo(endDate) > 0) {
            Toast.makeText(getContext(), "Invalid date selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        dateFilterListener.onFinishDateFilterDialog(startDate, endDate);
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

    public interface DateFilterListener {
        void onFinishDateFilterDialog(Date startDate, Date endDate);
    }
}
