package com.expensemanager.app.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Zhaolong Zhong on 8/19/16.
 */

public class Helpers {
    public static final String TAG = Helpers.class.getSimpleName();

    /**
     * Return a readable date format. For example,
     * Today at 10:50 AM
     * Yesterday at 07:13 PM
     * Monday at 06:07 PM
     * 8/16/16 at 10:35 AM
     *
     * @param createdAt
     * @return
     */
    public static String formatCreateAt(Date createdAt) {
        StringBuilder readableDate = new StringBuilder();

        // Current calendar
        Calendar currentCalendar = Calendar.getInstance();
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_WEEK);

        // Created At calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdAt);
        int createdAtWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        int createdAtYear = calendar.get(Calendar.YEAR);

        // Use Today, Yesterday and Weekday if createdAt date is in current week.
        if (currentWeek == createdAtWeek && currentYear == createdAtYear) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (currentDay == dayOfWeek) {
                readableDate.append("Today");
            } else if (currentDay == dayOfWeek + 1 || currentDay == dayOfWeek - 7) {
                readableDate.append("Yesterday");
            } else {
                SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.US);
                readableDate.append(dayOfWeekFormat.format(createdAt));
            }
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
            readableDate.append(dateFormat.format(createdAt));
        }

        readableDate.append(" at ");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        readableDate.append(timeFormat.format(createdAt));

        return readableDate.toString();
    }
}
