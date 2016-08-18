package com.expensemanager.app.service;

import com.expensemanager.app.models.Expense;

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
}
