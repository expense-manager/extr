package com.expensemanager.app.service.font;

/**
 * Created by Zhaolong Zhong on 8/30/16.
 */

public enum Font {
    DEFAULT("SF_UI_Display_Regular.otf"),
    LIGHT("SF_UI_Display_Light.otf"),
    REGULAR("SF_UI_Display_Regular.otf"),
    TEXT_LIGHT("SF_UI_Text_Light.otf"),
    TEXT_REGULAR("SF_UI_Text_Light.otf"),
    BOLD("SF_UI_Text_Bold.otf");

    private String name;

    Font(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
