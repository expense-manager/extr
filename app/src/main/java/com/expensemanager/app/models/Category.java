package com.expensemanager.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Category implements RealmModel {
    private static final String TAG = Category.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String NAME_JSON_KEY = "name";
    public static final String COLOR_JSON_KEY = "color";
    public static final String USER_JSON_KEY = "userId";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";

    // Property
    @PrimaryKey
    private String id;
    private String name;
    private String color;
    private String userId;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.name = jsonObject.getString(NAME_JSON_KEY);
            // Hex String to Hex int
            this.color = jsonObject.getString(COLOR_JSON_KEY);
            if (jsonObject.has(USER_JSON_KEY)) {
                // {"__type":"Pointer","className":"_User","objectId":"2ZutGFhpA3"}
                this.userId = jsonObject.getJSONObject(USER_JSON_KEY).getString(OBJECT_ID_JSON_KEY);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing category.", e);
        }
    }

    public static void mapFromJSONArray(JSONArray jsonArray) {
        RealmList<Category> categories = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject categoryJson = jsonArray.getJSONObject(i);
                Category category = new Category();
                category.mapFromJSON(categoryJson);
                categories.add(category);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing category.", e);
            }
        }

        realm.copyToRealmOrUpdate(categories);
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @return map of all categories
     */
    public static Map<String, Category> getAllCategoriesMap() {
        Map<String, Category> map = new HashMap<>();

        for (Category c : getAllCategories()) {
            map.put(c.getId(), c);
        }

        return map;
    }

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

    public static void delete(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Category> categories = realm.where(Category.class).equalTo(ID_KEY, id).findAll();
        categories.deleteFromRealm(0);
        realm.commitTransaction();
        realm.close();
    }
}
