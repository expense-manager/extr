package com.expensemanager.app.service;

/**
 * Created by Zhaolong Zhong on 9/3/16.
 */

import android.content.Context;
import android.support.annotation.Nullable;

import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;

public enum EIcon {
    DRINK(R.string.category_icon_local_drink, R.drawable.ic_local_drink_black_24dp),
    LOCAL_BAR(R.string.category_icon_local_bar, R.drawable.ic_local_bar_black_24dp),
    LOCAL_RESTAURANT(R.string.category_icon_restaurant, R.drawable.ic_restaurant_black_24dp),
    LOCAL_GAS_STATION(R.string.category_icon_local_gas_station, R.drawable.ic_local_gas_station_black_24dp),
    LOCAL_GROCERY(R.string.category_icon_local_grocery, R.drawable.ic_local_grocery_store_black_24dp),
    LOCAL_CAFE(R.string.category_icon_local_cafe, R.drawable.ic_local_cafe_black_24dp),
    LOCAL_TAXI(R.string.category_icon_local_taxi, R.drawable.ic_local_taxi_black_24dp),
    DIRECTIONS_BUS(R.string.category_icon_directions_bus, R.drawable.ic_directions_bus_black_24dp),
    FLIGHT(R.string.category_icon_flight, R.drawable.ic_flight_black_24dp),
    FAVORITE(R.string.category_icon_favorite, R.drawable.ic_favorite_black_24dp),
    STAR(R.string.category_icon_star, R.drawable.ic_star_black_24dp),
    BUBBLE_CHAR(R.string.category_icon_bubble_chart, R.drawable.ic_bubble_chart_black_24dp),
    FITNESS(R.string.category_icon_fitness_center, R.drawable.ic_fitness_center_black_24dp),
    MUSIC_NOTE(R.string.category_icon_music_note, R.drawable.ic_music_note_black_24dp),
    STORE(R.string.category_icon_store, R.drawable.ic_store_black_24dp),
    PETS(R.string.category_icon_pets, R.drawable.ic_pets_black_24dp),
    LOCAL_PRINTSHOP(R.string.category_icon_printshop, R.drawable.ic_local_printshop_black_24dp),
    BUSINESS_CENTER(R.string.category_icon_business_center, R.drawable.ic_business_center_black_24dp);

    private int nameRes;
    private int valueRes;

    EIcon(int nameRes, int valueRes) {
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

    public String getName() {
        Context context = EApplication.getInstance();
        return context.getResources().getString(this.nameRes);
    }

    public String getValue() {
        Context context = EApplication.getInstance();
        return context.getResources().getString(this.valueRes);
    }

    public static @Nullable EIcon instanceFromName(String name) {
        Context context = EApplication.getInstance();
        for (EIcon icon : values()) {
            String iconName = context.getResources().getString(icon.nameRes);

            if (name.equals(iconName)) {
                return icon;
            }
        }

        return null;
    }
}

