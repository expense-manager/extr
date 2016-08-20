package com.expensemanager.app.service;

import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.User;

import java.util.HashMap;
import java.util.Map;

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

    public static RequestTemplate login(String username, String password) {
        String url = BASE_URL + "login";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate signUp(String username, String password) {
        String url = BASE_URL + "users";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate getAllExpenses() {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();
        //todo: getAllExpensesByUserId

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate createExpense(Expense expense) {
        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateExpense(Expense expense) {
        String url = BASE_URL + "classes/Expense/" + expense.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());

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

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateCategory(Category category) {
        String url = BASE_URL + "classes/Category/" + category.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteCategory(String categoryId) {
        String url = BASE_URL + "classes/Category/" + categoryId;

        return new RequestTemplate(DELETE, url, null);
    }
}
