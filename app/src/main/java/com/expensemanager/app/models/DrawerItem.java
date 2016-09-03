package com.expensemanager.app.models;

public class DrawerItem {

    private int icon;
    private String title;

    public int getIcon() {
        return icon;
    }

    public DrawerItem setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DrawerItem setTitle(String title) {
        this.title = title;
        return this;
    }
}