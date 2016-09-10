package com.expensemanager.app.models;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class PhotoSource {
    private static final String TAG = PhotoSource.class.getSimpleName();

    private int iconResId;
    private String title;

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
