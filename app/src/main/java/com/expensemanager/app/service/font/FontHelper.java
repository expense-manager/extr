package com.expensemanager.app.service.font;

import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhaolong Zhong on 8/30/16.
 */

public class FontHelper {
    private final static String TAG = FontHelper.class.getSimpleName();

    private final static String SYSTEM_FONT_MAP = "sSystemFontMap";
    public final static String MONOSPACE = "MONOSPACE";
    public final static String SANS_SERIF = "sans-serif";

    public static void setDefaultFont(final Typeface newTypeface) {
        if (Build.VERSION.SDK_INT >= 21) {
            Map<String, Typeface> typefaceMap = new HashMap<>();
            typefaceMap.put(SANS_SERIF, newTypeface);

            try {
                final Field staticSystemFontMapField = Typeface.class
                        .getDeclaredField(SYSTEM_FONT_MAP);
                staticSystemFontMapField.setAccessible(true);
                staticSystemFontMapField.set(null, typefaceMap);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "No such typeface field", e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Illegal access, cannot set new typeface", e);
            }
        } else {
            try {
                final Field staticTypefaceField = Typeface.class
                        .getDeclaredField(MONOSPACE);
                staticTypefaceField.setAccessible(true);
                staticTypefaceField.set(null, newTypeface);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "No such typeface field", e);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Illegal access, cannot set new typeface", e);
            }
        }
    }
}
