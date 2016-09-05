package com.expensemanager.app.service;

import android.text.TextUtils;
import android.util.Log;

import com.expensemanager.app.helpers.Helpers;
import com.expensemanager.app.models.Category;
import com.expensemanager.app.models.Expense;
import com.expensemanager.app.models.Group;
import com.expensemanager.app.models.Member;
import com.expensemanager.app.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
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
    public static final String INCLUDE = "include";
    public static final String CONTENT = "CONTENT";
    public static final String WHERE = "where";

    public static RequestTemplate login(String username, String password) {
        String url = BASE_URL + "login";
        Map<String, String> params = new HashMap<>();

        params.put(User.USERNAME_JSON_KEY, username);
        params.put(User.PASSWORD_JSON_KEY, password);

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate signUp(String email, String password, String firstName, String lastName, String phone) {
        String url = BASE_URL + "users";
        Map<String, String> params = new HashMap<>();

        if (!TextUtils.isEmpty(phone)) {
            params.put(User.USERNAME_JSON_KEY, phone);
            params.put(User.PHONE_JSON_KEY, phone);
        } else {
            params.put(User.USERNAME_JSON_KEY, email);
            params.put(User.EMAIL_JSON_KEY, email);
        }

        params.put(User.PASSWORD_JSON_KEY, password);

        if (!TextUtils.isEmpty(firstName)) {
            params.put(User.FIRST_NAME_JSON_KEY, firstName);
        }

        if (!TextUtils.isEmpty(lastName)) {
            params.put(User.LAST_NAME_JSON_KEY, lastName);
        }

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

    public static RequestTemplate getAllExpensesByUserId(String userId) {
        if (userId == null) {
            return null;
        }

        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();
        JSONObject subUserIdObj = new JSONObject();
        JSONObject userIdObj=new JSONObject();

        try {
            subUserIdObj.put("__type", "Pointer");
            subUserIdObj.put("className", "_User");
            subUserIdObj.put("objectId", userId);
            userIdObj.put("userId", subUserIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getGroupUsersByUserId", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(userIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getAllExpensesByGroupId(String groupId) {
        if (groupId == null) {
            return null;
        }

        String url = BASE_URL + "classes/Expense";
        Map<String, String> params = new HashMap<>();
        JSONObject subGroupIdObj = new JSONObject();
        JSONObject groupIdObj=new JSONObject();

        try {
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", groupId);
            groupIdObj.put(Expense.GROUP_JSON_KEY, subGroupIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getGroupUsersByUserId", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(groupIdObj.toString()));
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

        String note = expense.getNote();

        if (note != null && !note.isEmpty() && note.length() > 0) {
            params.put(Expense.NOTE_JSON_KEY, expense.getNote());
        }

        try {
            // User pointer
            // {"__type":"Pointer","className":"_User","objectId":"2ZutGFhpA3"}
            JSONObject userIdObj=new JSONObject();
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", expense.getUserId());
            params.put(Expense.USER_JSON_KEY, userIdObj.toString());

            // Category pointer
            JSONObject categoryIdObj=new JSONObject();
            categoryIdObj.put("__type", "Pointer");
            categoryIdObj.put("className", "Category");
            categoryIdObj.put("objectId", expense.getCategoryId());
            params.put(Expense.CATEGORY_JSON_KEY, categoryIdObj.toString());

            // Group pointer
            JSONObject subGroupIdObj=new JSONObject();
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", expense.getGroupId());
            params.put(Expense.GROUP_JSON_KEY, subGroupIdObj.toString());

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
            Log.e(TAG, "Error pointer object for 'where' in createExpense", e);
        }

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateExpense(ExpenseBuilder expenseBuilder) {
        Expense expense = expenseBuilder.getExpense();

        String url = BASE_URL + "classes/Expense/" + expense.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Expense.AMOUNT_JSON_KEY, String.valueOf(expense.getAmount()));
        Log.i(TAG, String.valueOf(expense.getNote().length()));
        params.put(Expense.NOTE_JSON_KEY, expense.getNote());
        // todo: able to update with categoryId
        try {
            // User pointer
            // {"__type":"Pointer","className":"_User","objectId":"2ZutGFhpA3"}
            JSONObject userIdObj=new JSONObject();
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", expense.getUserId());
            params.put(Expense.USER_JSON_KEY, userIdObj.toString());

            // Category pointer
            JSONObject categoryIdObj=new JSONObject();
            categoryIdObj.put("__type", "Pointer");
            categoryIdObj.put("className", "Category");
            categoryIdObj.put("objectId", expense.getCategoryId());
            params.put(Expense.CATEGORY_JSON_KEY, categoryIdObj.toString());

            // Group pointer
            JSONObject subGroupIdObj=new JSONObject();
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", expense.getGroupId());
            params.put(Expense.GROUP_JSON_KEY, subGroupIdObj.toString());

            // Date pointer
            // "spentAt" -> "{"__type":"Date","iso":"2016-08-04T21:48:00.000Z"}"
            SimpleDateFormat timezoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00.000'Z'", Locale.US);
            timezoneFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String time = timezoneFormat.format(expense.getExpenseDate());

            JSONObject dateObj=new JSONObject();
            dateObj.put("__type", "Date");
            dateObj.put(Expense.ISO_EXPENSE_DATE_JSON_KEY, time);
            params.put(Expense.EXPENSE_DATE_JSON_KEY, dateObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error pointer object for 'where' in createExpense", e);
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

    public static RequestTemplate getAllCategoriesByUserId(String userId) {
        if (userId == null) {
            return null;
        }

        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();
        JSONObject subUserIdObj = new JSONObject();
        JSONObject userIdObj=new JSONObject();

        try {
            subUserIdObj.put("__type", "Pointer");
            subUserIdObj.put("className", "_User");
            subUserIdObj.put("objectId", userId);
            userIdObj.put("userId", subUserIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getGroupUsersByUserId", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(userIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getAllCategoriesByGroupId(String groupId) {
        if (groupId == null) {
            return null;
        }

        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();
        JSONObject subGroupIdObj = new JSONObject();
        JSONObject groupIdObj=new JSONObject();

        try {
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", groupId);
            groupIdObj.put(Category.GROUP_JSON_KEY, subGroupIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getGroupUsersByUserId", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(groupIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate createCategory(Category category) {
        String url = BASE_URL + "classes/Category";
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());
        params.put(Category.COLOR_JSON_KEY, category.getColor());

        try {
            // User pointer
            // {"__type":"Pointer","className":"_User","objectId":"2ZutGFhpA3"}
            JSONObject userIdObj=new JSONObject();
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", category.getUserId());
            params.put(Category.USER_JSON_KEY, userIdObj.toString());

            // Group pointer
            JSONObject subGroupIdObj=new JSONObject();
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", category.getGroupId());
            params.put(Category.GROUP_JSON_KEY, subGroupIdObj.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Error pointer object for 'where' in createExpense", e);
        }

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateCategory(Category category) {
        String url = BASE_URL + "classes/Category/" + category.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Category.NAME_JSON_KEY, category.getName());
        params.put(Category.COLOR_JSON_KEY, category.getColor());

        try {
            // User pointer
            // {"__type":"Pointer","className":"_User","objectId":"2ZutGFhpA3"}
            JSONObject userIdObj=new JSONObject();
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", category.getUserId());
            params.put(Category.USER_JSON_KEY, userIdObj.toString());

            // Group pointer
            JSONObject subGroupIdObj=new JSONObject();
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", category.getGroupId());
            params.put(Category.GROUP_JSON_KEY, subGroupIdObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error pointer object for 'where' in createExpense", e);
        }

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

        params.put(WHERE, Helpers.encodeURIComponent(expenseIdObj.toString()));
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

    public static RequestTemplate getAllUsersByUserFullName(String userFullName) {
        if (userFullName == null) {
            return null;
        }

        String url = BASE_URL + "users";
        Map<String, String> params = new HashMap<>();

        JSONObject userNameObj=new JSONObject();

        try {
            userNameObj.put(User.FULLNAME_JSON_KEY, userFullName);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user full name object for where in getAllUsersByUserFullName", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(userNameObj.toString()));

        return new RequestTemplate(GET, url, params);
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

        if (user.getEmail() != null && Helpers.isValidEmail(user.getEmail())) {
            params.put(User.EMAIL_JSON_KEY, user.getEmail());
            if (user.getUsername().contains("@") ) {
                params.put(User.USERNAME_JSON_KEY, user.getEmail());
            }
        }

        if (user.getFirstName() != null) {
            params.put(User.FIRST_NAME_JSON_KEY, user.getFirstName());
        }

        if (user.getLastName() != null) {
            params.put(User.LAST_NAME_JSON_KEY, user.getLastName());
        }

        if (user.getPhone() != null && user.getUsername().contains("@")) {
            params.put(User.PHONE_JSON_KEY, user.getPhone());
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Log.d(TAG, "entry key: " + entry.getKey() + ", value:" + entry.getValue());
        }

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

    public static RequestTemplate getAllGroupByUserId(String userId) {
        String url = BASE_URL + "classes/Group";
        Map<String, String> params = new HashMap<>();
        JSONObject subUserIdObj = new JSONObject();
        JSONObject userIdObj=new JSONObject();

        try {
            subUserIdObj.put("__type", "Pointer");
            subUserIdObj.put("className", "_User");
            subUserIdObj.put("objectId", userId);
            userIdObj.put("userId", subUserIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getGroupUsersByUserId", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(userIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }



    public static RequestTemplate getGroupByGroupname(String groupname) {
        String url = BASE_URL + "classes/Group";

        Map<String, String> params = new HashMap<>();
        JSONObject groupnameJSON = new JSONObject();

        try {
            groupnameJSON.put("groupname", groupname);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating expense id pointer object for where in getGroupByGroupname", e);
        }

        params.put(WHERE, Helpers.encodeURIComponent(groupnameJSON.toString()));

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getGroupById(String id) {
        String url = BASE_URL + "classes/Group" + "/" + id;

        return new RequestTemplate(GET, url, null);
    }

    public static RequestTemplate createGroup(Group group) {
        String url = BASE_URL + "classes/Group";
        Map<String, String> params = new HashMap<>();

        params.put(Group.GROUPNAME_JSON_KEY, group.getGroupname());

        if (!TextUtils.isEmpty(group.getName())) {
            params.put(Group.NAME_JSON_KEY, group.getName());
        }

        JSONObject userIdObj=new JSONObject();

        try {
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", group.getUserId());
            params.put("userId", userIdObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user pointer in createGroup");
        }

        return new RequestTemplate(POST, url, params);
    }

    public static RequestTemplate updateGroup(Group group) {
        String url = BASE_URL + "classes/Group/" + group.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Group.GROUPNAME_JSON_KEY, group.getGroupname());
        params.put(Group.NAME_JSON_KEY, group.getName());

        if (!TextUtils.isEmpty(group.getAbout())) {
            params.put(Group.ABOUT_JSON_KEY, group.getAbout());
        }

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteGroup(String groupId) {
        String url = BASE_URL + "classes/Group/" + groupId;

        return new RequestTemplate(DELETE, url, null);
    }

    public static RequestTemplate getMemberByMemberId(String memberId) {
        String url = BASE_URL + "classes/Member" + "/" + memberId;
        Map<String, String> params = new HashMap<>();

        params.put(INCLUDE, "groupId,userId,createdBy");

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getMembersByUserId(String userId) {
        String url = BASE_URL + "classes/Member";
        Map<String, String> params = new HashMap<>();
        JSONObject subUserIdObj = new JSONObject();
        JSONObject userIdObj=new JSONObject();

        try {
            subUserIdObj.put("__type", "Pointer");
            subUserIdObj.put("className", "_User");
            subUserIdObj.put("objectId", userId);
            userIdObj.put("userId", subUserIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getMembersByUserId", e);
        }

        params.put(INCLUDE, "groupId,userId,createdBy");
        params.put(WHERE, Helpers.encodeURIComponent(userIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }

    public static RequestTemplate getMembersByGroupId(String groupId) {
        String url = BASE_URL + "classes/Member";
        Map<String, String> params = new HashMap<>();
        JSONObject subGroupIdObj = new JSONObject();
        JSONObject groupIdObj=new JSONObject();

        try {
            subGroupIdObj.put("__type", "Pointer");
            subGroupIdObj.put("className", "Group");
            subGroupIdObj.put("objectId", groupId);
            groupIdObj.put("groupId", subGroupIdObj);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating user id pointer object for where in getMembersByGroupId", e);
        }

        params.put(INCLUDE, "groupId,userId,createdBy");
        params.put(WHERE, Helpers.encodeURIComponent(groupIdObj.toString()));

        return new RequestTemplate(GET, url, params);
    }

    // Join a group or invite a user
    public static RequestTemplate createMember(Member member) {
        String url = BASE_URL + "classes/Member";
        Map<String, String> params = new HashMap<>();
        JSONObject groupIdObject = new JSONObject();
        JSONObject userIdObj=new JSONObject();
        JSONObject createdByObj=new JSONObject();

        try {
            // Group pointer
            groupIdObject.put("__type", "Pointer");
            groupIdObject.put("className", "Group");
            groupIdObject.put("objectId", member.getGroupId());
            params.put(Member.GROUP_ID_KEY, groupIdObject.toString());

            // Reseiver User pointer
            userIdObj.put("__type", "Pointer");
            userIdObj.put("className", "_User");
            userIdObj.put("objectId", member.getUserId());
            params.put(Member.USER_ID_KEY, userIdObj.toString());

            // Reseiver User pointer
            createdByObj.put("__type", "Pointer");
            createdByObj.put("className", "_User");
            createdByObj.put("objectId", member.getCreatedBy().getId());
            params.put(Member.CREATED_BY_KEY, createdByObj.toString());

            params.put(Member.IS_ACCEPTED_KEY, String.valueOf(member.isAccepted()));

            return new RequestTemplate(POST, url, params);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating groupId or userId or createdBy pointer object for where in createMember()", e);
        }

        return null;
    }

    public static RequestTemplate updateMember(Member member) {
        String url = BASE_URL + "classes/Member/" + member.getId();
        Map<String, String> params = new HashMap<>();

        params.put(Member.IS_ACCEPTED_KEY, String.valueOf(member.isAccepted()));

        return new RequestTemplate(PUT, url, params);
    }

    public static RequestTemplate deleteMember(String memberId) {
        String url = BASE_URL + "classes/Member/" + memberId;

        return new RequestTemplate(DELETE, url, null);
    }
}
