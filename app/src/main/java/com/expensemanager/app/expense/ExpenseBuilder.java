package com.expensemanager.app.expense;

import com.expensemanager.app.models.Expense;

import java.util.ArrayList;

/**
 * Created by Zhaolong Zhong on 8/21/16.
 */

public class ExpenseBuilder {
    private Expense expense;
    private String categoryId;
    private String createdBy;
    private ArrayList<byte[]> photoList;

    public ExpenseBuilder() {

    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public ArrayList<byte[]> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(ArrayList<byte[]> photoList) {
        this.photoList = photoList;
    }
}
