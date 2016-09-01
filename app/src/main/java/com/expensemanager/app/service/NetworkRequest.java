package com.expensemanager.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.expensemanager.app.BuildConfig;
import com.expensemanager.app.R;
import com.expensemanager.app.main.EApplication;
import com.expensemanager.app.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import bolts.Task;
import bolts.TaskCompletionSource;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class NetworkRequest {
    private static final String TAG = NetworkRequest.class.getSimpleName();

    private static final Executor NETWORK_EXECUTOR = Executors.newCachedThreadPool();

    private RequestTemplate requestTemplate;
    private TaskCompletionSource<JSONObject> taskCompletionSource;

    public NetworkRequest(RequestTemplate requestTemplate, TaskCompletionSource<JSONObject> taskCompletionSource) {
        this.requestTemplate =requestTemplate;
        this.taskCompletionSource = taskCompletionSource;
    }

    public Task<JSONObject> send() {
        Task.call(new Callable<Void>() {
            public Void call() {
                String url = requestTemplate.getUrl();
                String method = requestTemplate.getMethod();
                Map<String, String> paramsMap = requestTemplate.getParams();
                Map<String, byte[]> paramsMapByte = requestTemplate.getParamsByte();

                RequestBody requestBody = null;

                // Add params to url
                // Method - GET
                if (method.equals(RequestTemplateCreator.GET)) {
                    StringBuilder stringBuilder = new StringBuilder(url);

                    if (paramsMap != null) {
                        stringBuilder.append("?");
                        for (Map.Entry entry : paramsMap.entrySet()) {
                            stringBuilder.append(entry.getKey());
                            stringBuilder.append("=");
                            stringBuilder.append(entry.getValue());
                            stringBuilder.append(("&"));
                        }
                    }

                    url = stringBuilder.toString();
                } else if (paramsMapByte != null) {
                    requestBody = RequestBody.create(MediaType.parse("image/jpeg"),
                            paramsMapByte.get(RequestTemplateCreator.CONTENT));
                } else {
                    // POST, PUT, DELETE
                    StringBuilder jsonBuilder = new StringBuilder();
                    jsonBuilder.append("{");
                    if (paramsMap != null) {
                        // Convert parasMap to JSON string.
                        int size = paramsMap.size();
                        int i = 0;

                        for (Map.Entry entry : paramsMap.entrySet()) {
                            String entryValue = entry.getValue().toString();
                            if(entryValue.charAt(0) == '{') {
                                jsonBuilder.append("\"");
                                jsonBuilder.append(entry.getKey());
                                jsonBuilder.append("\":");
                                jsonBuilder.append(entry.getValue());
                            } else {
                                jsonBuilder.append("\"");
                                jsonBuilder.append(entry.getKey());
                                jsonBuilder.append("\":\"");
                                jsonBuilder.append(entry.getValue());
                                jsonBuilder.append("\"");
                            }

                            if (i < size - 1) {
                                jsonBuilder.append(",");
                            }
                            i++;
                        }
                    }

                    jsonBuilder.append('}');
                    requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBuilder.toString());
                    Log.d(TAG, "request params: " + jsonBuilder.toString());
                }

                // Add headers
                Request.Builder builder = getBasicBuilder(url, method, requestBody);
                if (paramsMapByte != null) {
                    builder.addHeader("Content-Type", "image/jpeg");
                }

                if(requestTemplate.isUseToken()) {
                    Context context = EApplication.getInstance();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(
                            context.getString(R.string.shared_preferences_session_key), 0);
                    String sessionToken = sharedPreferences.getString(User.SESSION_TOKEN, null);

                    if (sessionToken == null) {
                        Log.e(TAG, "Error getting session token.");
                        return null;
                    }
                    builder.addHeader("X-Parse-Session-Token", sessionToken);
                }

                // Send request
                try {
                    Response response = new OkHttpClient().newCall(builder.build()).execute();
                    String responseString = response.body().string();
                    Log.d(TAG, "Response: \n" + responseString);
                    // If response is empty or JSONArray, we convert to JSONObject
                    if (responseString.isEmpty()) {
                        responseString = "{}";
                    } else if (responseString.charAt(0) != '{') {
                        responseString = "{\"results\":\"" + responseString + "\"}";
                    }

                    JSONObject result = new JSONObject(responseString);

                    // Set task result
                    taskCompletionSource.setResult(result);
                } catch (IOException e) {
                    Log.e(TAG, "Error sending request to url:" + requestTemplate.getUrl(), e);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response to JSONObject.", e);
                }

                return null;
            }
        }, NETWORK_EXECUTOR);

        return taskCompletionSource.getTask();
    }

    private Request.Builder getBasicBuilder(String url, String method, RequestBody requestBody) {
        return new Request.Builder()
                .url(url)
                .addHeader("X-Parse-Application-Id", BuildConfig.APP_ID)
                .addHeader("X-Parse-Master-Key", BuildConfig.MASTER_KEY)
                .addHeader("X-Parse-Revocable-Session", "1")
                .method(method, requestBody);
    }
}
