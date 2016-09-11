package com.expensemanager.app.models;

/**
 * Created by Zhaolong Zhong on 9/10/16.
 */

public class EAction {
    private int titleId;
    private int imageId;

    public EAction(int titleId, int imageId) {
        this.titleId = titleId;
        this.imageId = imageId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}