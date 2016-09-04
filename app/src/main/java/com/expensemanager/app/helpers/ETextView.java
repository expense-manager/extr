package com.expensemanager.app.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.service.font.Font;

/**
 * Created by Zhaolong Zhong on 9/4/16.
 */

public class ETextView extends TextView {

    public ETextView(Context context) {
        super(context);
        initialize();
    }

    public ETextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ETextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        if (!isInEditMode()) {
            setTypeface(EApplication.getInstance().getTypeface(Font.REGULAR));
        }
    }
}
