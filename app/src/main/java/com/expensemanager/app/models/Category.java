package com.expensemanager.app.models;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/16/16.
 */

@RealmClass
public class Category implements RealmModel {
    private static final String TAG = Category.class.getSimpleName();

    // Property name key
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";

    // Property
    @PrimaryKey
    private String id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
