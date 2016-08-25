package com.expensemanager.app.models;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by Zhaolong Zhong on 8/16/16.
 */

@RealmClass
public class Expense implements RealmModel {
    private static final String TAG = Expense.class.getSimpleName();

    // Keys in JSON response
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String AMOUNT_JSON_KEY = "amount";
    public static final String NOTE_JSON_KEY = "note";
    public static final String CREATED_AT_JSON_KEY = "createdAt";
    public static final String CATEGORY_JSON_KEY = "categoryId";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";

    // Property
    @PrimaryKey
    private String id;
    private String photos;
    private String note;
    private double amount;
    private Date createdAt;
    private boolean isSynced;
    private String categoryId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void mapFromJSON(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString(OBJECT_ID_JSON_KEY);
            this.amount = jsonObject.getDouble(AMOUNT_JSON_KEY);
            this.note = jsonObject.optString(NOTE_JSON_KEY);
            if (jsonObject.has(CATEGORY_JSON_KEY)) {
                this.categoryId = jsonObject.getJSONObject(CATEGORY_JSON_KEY).getString(OBJECT_ID_JSON_KEY);
            }
            // Parse createdAt and convert UTC time to local time
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.createdAt = simpleDateFormat.parse(jsonObject.getString(CREATED_AT_JSON_KEY));
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing expense.", e);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing createdAt.", e);
        }
    }

    public static void mapFromJSONArray(JSONArray jsonArray) {
        RealmList<Expense> expenses = new RealmList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject expenseJson = jsonArray.getJSONObject(i);
                Expense expense = new Expense();
                expense.mapFromJSON(expenseJson);
                expenses.add(expense);
            } catch (JSONException e) {
                Log.e(TAG, "Error in parsing expense.", e);
            }
        }

        realm.copyToRealmOrUpdate(expenses);
        realm.commitTransaction();
        realm.close();
    }

    /**
     * @return all expenses
     */
    public static RealmResults<Expense> getAllExpenses() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Expense> expenses = realm.where(Expense.class).findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        return expenses;
    }

    /**
     * @param id
     * @return Expense object if exist, otherwise return null.
     */
    public static @Nullable Expense getExpenseById(String id) {
        Realm realm = Realm.getDefaultInstance();
        Expense expense = realm.where(Expense.class).equalTo(ID_KEY, id).findFirst();
        realm.close();

        return expense;
    }

    /**
     * @return expenses with date range
     */
    public static List<Expense> getRangeExpenses(Date startDate, Date endDate) {
        if (startDate.compareTo(endDate) > 0) {
            return new ArrayList<>();
        }

        Realm realm = Realm.getDefaultInstance();

        RealmResults<Expense> expenses = realm.where(Expense.class)
            .greaterThanOrEqualTo(CREATED_AT_KEY, startDate)
            .lessThanOrEqualTo(CREATED_AT_KEY, endDate)
            .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);

        realm.close();

        return expenses;
    }

    public static void delete(String id) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Expense> expenses = realm.where(Expense.class).equalTo(ID_KEY, id).findAll();
        expenses.deleteFromRealm(0);
        realm.commitTransaction();
        realm.close();
    }

    public static RealmResults<Expense> getExpensesByRange(Date[] startEnd) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Expense> expenses = realm.where(Expense.class)
                .greaterThan(CREATED_AT_KEY, startEnd[0])
                .lessThan(CREATED_AT_KEY, startEnd[1])
                .findAllSorted(CREATED_AT_KEY, Sort.DESCENDING);
        realm.close();

        return expenses;
    }
}
