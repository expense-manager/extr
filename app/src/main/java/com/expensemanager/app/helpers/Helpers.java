package com.expensemanager.app.helpers;

import com.expensemanager.app.models.Category;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static String dateToString (Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        return dateFormat.format(date);
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] inputData, int offset, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(inputData, 0, inputData.length, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getCenterCropDimensionForBitmap(Bitmap bitmap) {
        int dimension;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width >= height) {
            dimension = height;
        } else {
            dimension = width;
        }

        return dimension;
    }

    public static Bitmap padBitmap(Bitmap input, int paddingX, int paddingY) {
        Bitmap outputBitmap = Bitmap.createBitmap(input.getWidth() + 2 * paddingX, input.getHeight() + paddingY * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(input, paddingX, paddingY, null);

        return outputBitmap;
    }

    // dp to px
    public static double dpToPx(Context context, double dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        double px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    // Get used color set
    public static Set<String> getUsedColorSet() {
        Set<String> newUsedColors = new HashSet<>();

        for (Category c : Category.getAllCategories()) {
            if (c != null) {
                newUsedColors.add(c.getColor());
            }
        }

        return newUsedColors;
    }
}
