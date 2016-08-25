package com.expensemanager.app.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/25/16.
 */

@RealmClass
public class ExpensePhoto implements RealmModel {
    private static final String TAG = ExpensePhoto.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String EXPENSE_ID_JSON_KEY = "expenseId";
    public static final String PHOTO_JSON_KEY = "photo";
    public static final String NAME_JSON_KEY = "name";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String EXPENSE_ID_KEY = "expenseId";
    public static final String FILE_NAME_KEY = "fileName";

    // Property
    @PrimaryKey
    private String id;
    private String expenseId;
    private String fileName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.expenseId = jsonObject.getJSONObject(EXPENSE_ID_JSON_KEY).getString(OBJECT_ID_JSON_KEY);
            this.fileName = jsonObject.getJSONObject(PHOTO_JSON_KEY).getString(NAME_JSON_KEY);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing expense.", e);
        }
    }

    public static void mapPhotoFromJSONArray(JSONArray jsonArray) {
        RealmList<ExpensePhoto> expensePhotos = new RealmList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject expenseJson = jsonArray.getJSONObject(i);
                ExpensePhoto expensePhoto = new ExpensePhoto();
                expensePhoto.mapFromJSON(expenseJson);
                expensePhotos.add(expensePhoto);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing expense.", e);
            }
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(expensePhotos);
        realm.commitTransaction();
        realm.close();
    }

    public static void delete(String expenseId, String fileName) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<ExpensePhoto> expensePhotos = realm
                .where(ExpensePhoto.class)
                .equalTo(EXPENSE_ID_KEY, expenseId)
                .equalTo(FILE_NAME_KEY, fileName)
                .findAll();
        expensePhotos.deleteFromRealm(0);
        realm.commitTransaction();
        realm.close();
    }

    public static RealmResults<ExpensePhoto> getExpensePhotoByExpenseId(String expenseId) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ExpensePhoto> expensePhotos = realm.where(ExpensePhoto.class).equalTo(EXPENSE_ID_KEY, expenseId).findAll();
        realm.close();

        return expensePhotos;
    }
}
