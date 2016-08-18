package com.expensemanager.app.models;

import android.support.annotation.Nullable;

import java.util.Date;

import io.realm.Realm;
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

    // JSON Key, columns' name in server
    public static final String OBJECT_ID_JSON_KEY = "objectId";
    public static final String AMOUNT_JSON_KEY = "amount";
    public static final String NOTE_JSON_KEY = "note";

    // Property name key
    public static final String ID_KEY = "id";
    public static final String CREATED_AT_KEY = "createdAt";

    // Property
    @PrimaryKey
    private String id;
    private String imageUrl;
    private String note;
    private double amount;
    private Date createdAt;
    private boolean isSynced;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    // Query

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
}
