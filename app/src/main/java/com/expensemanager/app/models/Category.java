package com.expensemanager.app.models;

import android.support.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

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

    // Query

    /**
     * @return all categories
     */
    public static RealmResults<Category> getAllCategories() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> categories = realm.where(Category.class).findAll();
        realm.close();

        return categories;
    }

    /**
     * @param id
     * @return Category object if exist, otherwise return null.
     */
    public static @Nullable Category getCategoryById(String id) {
        Realm realm = Realm.getDefaultInstance();
        Category category = realm.where(Category.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return category;
    }
}
