package com.expensemanager.app.service.enums;

/**
 * Created by Zhaolong Zhong on 9/3/16.
 */

import android.content.Context;

import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;

import java.util.ArrayList;

public enum EColor {
//    DEFAULT(R.string.soft_default, R.color.soft_default),
    BLUE(R.string.soft_blue, R.color.blue),
    BLUE_GRAY(R.string.soft_blue_gray, R.color.soft_blue_gray),
    BROWN(R.string.soft_brown, R.color.soft_brown),
    CYAN(R.string.soft_cyan_like, R.color.soft_cyan_like),
    DEEP_PURPLE(R.string.soft_deep_purple, R.color.soft_deep_purple),
    GREEN(R.string.soft_green, R.color.soft_green),
    INDIGO(R.string.soft_indigo, R.color.soft_indigo),
    ORANGE(R.string.soft_orange, R.color.soft_orange),
    PURPLE(R.string.soft_purple, R.color.soft_purple),
    PURPLE_DEEP(R.string.soft_purple_deep, R.color.soft_purple_deep),
    PURPLE_DEEP_LIGHT(R.string.soft_purple_deep_light, R.color.soft_purple_deep_light),
    RED(R.string.soft_red, R.color.soft_red),
    YELLOW(R.string.soft_yellow, R.color.soft_yellow);

    private int nameRes;
    private int valueRes;

    EColor(int nameRes, int valueRes) {
        this.nameRes = nameRes;
        this.valueRes = valueRes;
    }

    public int getNameRes() {
        return nameRes;
    }

    public void setNameRes(int nameRes) {
        this.nameRes = nameRes;
    }

    public int getValueRes() {
        return valueRes;
    }

    public void setValueRes(int valueRes) {
        this.valueRes = valueRes;
    }

    public String getValue() {
        Context context = EApplication.getInstance();
        return context.getResources().getString(this.valueRes);
    }

    public static EColor instanceFromValue(String value) {
        Context context = EApplication.getInstance();
        for (EColor color : values()) {
            String colorValue = context.getResources().getString(color.valueRes);

            if (colorValue.equals(value)) {
                return color;
            }
        }

        return BLUE;
    }

    public static ArrayList<String> getAllColors() {
        ArrayList<String> colors = new ArrayList<>();
        Context context = EApplication.getInstance();

        for (EColor color : values()) {
            String colorValue = context.getResources().getString(color.valueRes);
            colors.add(colorValue);
        }

        return colors;
    }
}

