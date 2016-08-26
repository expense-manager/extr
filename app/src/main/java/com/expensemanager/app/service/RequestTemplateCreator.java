package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.expense.ExpenseBuilder;
import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class RequestTemplateCreator {
    private static final String TAG = RequestTemplateCreator.class.getSimpleName();

    private static final String BASE_URL = "https://e-manager.herokuapp.com/parse/";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String CONTENT = "CONTENT";

    public static RequestTemplate login(String username, String password) {
        String url = BASE_URL + "login";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate signUp(String username, String password, String fullname) {
        String url = BASE_URL + "users";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);
        params.put(User.FULLNAME_JSON_KEY, fullname);

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate logout() {
        String url = BASE_URL + "logout";

        return new RequestTemplate(POST, url, null);
    }

    public static RequestTemplate getAllExpenses() {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();
        //todo: getAllExpensesByUserId

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getExpenseById(String id) {
        String url = BASE_URL + "classes/Expense" + "/" + id;

        return new RequestTemplate(GET, url, null);
    }

    public static RequestTemplate createExpense(ExpenseBuilder expenseBuilder) {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();

        Expense expense = expenseBuilder.getExpense();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());
        // todo: able to post selected category category

        try {
            // Category pointer
            JSONObject categoryIdObj=new JSONObject();
            categoryIdObj.put("__type", "Pointer");
            categoryIdObj.put("className", "Category");
            categoryIdObj.put("objectId", expense.getCategoryId());
            params.put("categoryId", categoryIdObj.toString());

            // Date pointer
            // "spentAt" -> "{"__type":"Date","iso":"2016-08-04T21:48:00.000Z"}"
            SimpleDateFormat timezoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.000'Z'");
            timezoneFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String time = timezoneFormat.format(expense.getExpenseDate());

            JSONObject dateObj=new JSONObject();
            dateObj.put("__type", "Date");
            dateObj.put(Expense.ISO_EXPENSE_DATE_JSON_KEY, time);
            params.put(Expense.EXPENSE_DATE_JSON_KEY, dateObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating category id pointer object for 'where' in createExpense", e);
        }

        // todo: build User Pointer
        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateExpense(Expense expense) {
        String url = BASE_URL + "classes/Expense/" + expense.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());
        // todo: able to update with categoryId
        try {
            // Category pointer
            JSONObject categoryIdObj=new JSONObject();
            categoryIdObj.put("__type", "Pointer");
            categoryIdObj.put("className", "Category");
            categoryIdObj.put("objectId", expense.getCategoryId());
            params.put("categoryId", categoryIdObj.toString());

            // Date pointer
            // "spentAt" -> "{"__type":"Date","iso":"2016-08-04T21:48:00.000Z"}"
            SimpleDateFormat timezoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.000'Z'");
            timezoneFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String time = timezoneFormat.format(expense.getExpenseDate());

            JSONObject dateObj=new JSONObject();
            dateObj.put("__type", "Date");
            dateObj.put(Expense.ISO_EXPENSE_DATE_JSON_KEY, time);
            params.put(Expense.EXPENSE_DATE_JSON_KEY, dateObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating category id pointer object for 'where' in createExpense", e);
        }

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteExpense(String expenseId) {
        String url = BASE_URL + "classes/Expense/" + expenseId;

        return new RequestTemplate(DELETE, url, null);
    }

    public static RequestTemplate getAllCategories() {
        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();
        //todo: getAllCategoriesByUserId

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate createCategory(Category category) {
        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());
        params.put(Category.COLOR_JSON_KEY, category.getColor());

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateCategory(Category category) {
        String url = BASE_URL + "classes/Category/" + category.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());
        params.put(Category.COLOR_JSON_KEY, category.getColor());

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteCategory(String categoryId) {
        String url = BASE_URL + "classes/Category/" + categoryId;

        return new RequestTemplate(DELETE, url, null);
    }

    /**
     * Upload photo to File table
     * @param photoName
     * @param content
     * @return
     */
    public static RequestTemplate uploadPhoto(String photoName, byte[] content) {
        String url = BASE_URL + "files/" + photoName;
        Map<String, byte[]> params = new HashMap<>();
        params.put(CONTENT, content);

        return new RequestTemplate(POST, url, null, params, false);
    }

    public static RequestTemplate getExpensePhotoByPhotoId(String photoId) {
        String url = BASE_URL + "classes/Photo/" + photoId;
        return new RequestTemplate(GET, url, null);
    }

    public static RequestTemplate getExpensePhotoByExpenseId(String expenseId) {
        String url = BASE_URL + "classes/Photo";
        Map<String, String> params = new HashMap<>();

        JSONObject expensePointerObj = new JSONObject();
        JSONObject expenseIdObj=new JSONObject();
        try {
            expensePointerObj.put("__type", "Pointer");
            expensePointerObj.put("className", "Expense");
            expensePointerObj.put("objectId", expenseId);
            expenseIdObj.put("expenseId", expensePointerObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating expense id pointer object for where in getExpensePhotoByExpenseId", e);
        }

        params.put("where", Helpers.encodeURIComponent(expenseIdObj.toString()));
        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate addExpensePhoto(String expenseId, String fileName) {
        String url = BASE_URL + "classes/Photo";
        Map<String, String> params = new HashMap<>();
        JSONObject expensePointerObject = new JSONObject();
        JSONObject photoObject = new JSONObject();

        try {
            // Build Expense pointer
            expensePointerObject.put("__type", "Pointer");
            expensePointerObject.put("className", "Expense");
            expensePointerObject.put("objectId", expenseId);

            Log.d(TAG, "expense pointer:" + expensePointerObject.toString());
            params.put("expenseId", expensePointerObject.toString());

            // Build File pointer
            photoObject.put("__type", "File");
            photoObject.put("name", fileName);

            Log.d(TAG, "photoObject:" + photoObject.toString());
            params.put("photo", photoObject.toString());

            return new RequestTemplate(POST, url, params);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating expense pointer or photo pointer for where clause in addExpensePhoto()", e);
        }

        return null;
    }

    public static RequestTemplate getLoginUser() {
        String url = BASE_URL + "users/me";
        return new RequestTemplate(GET, url, null, true);
    }

    public static RequestTemplate deleteFileByName(String fileName) {
        String url = BASE_URL + "files/" + fileName;

        return new RequestTemplate(DELETE, url, null);
    }

    public static RequestTemplate deleteExpensePhoto(String expensePhotoId) {
        String url = BASE_URL + "classes/Photo/" + expensePhotoId;

        return new RequestTemplate(DELETE, url, null);
    }

    public static RequestTemplate updateUser(User user) {
        String url = BASE_URL + "users/" + user.getId();
        Map<String, String> params = new HashMap<>();

        params.put(User.FULLNAME_JSON_KEY, user.getFullname());
        // todo: save more info

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate addUserPhoto(String userId, String fileName) {
        String url = BASE_URL + "users/" + userId;
        Map<String, String> params = new HashMap<>();
        JSONObject photoObject = new JSONObject();

        try {
            // Build File pointer
            photoObject.put("__type", "File");
            photoObject.put("name", fileName);

            Log.d(TAG, "photoObject:" + photoObject.toString());
            params.put("photo", photoObject.toString());

            return new RequestTemplate(PUT, url, params);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating photo pointer for where clause in addExpensePhoto()", e);
        }

        return null;
    }
}
