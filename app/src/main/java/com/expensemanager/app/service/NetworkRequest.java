package com.expensemanager.app.service;

import android.util.Log;

import com.expensemanager.app.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import bolts.Task;
import bolts.TaskCompletionSource;
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
                Map<String, String> parmsMap = requestTemplate.getParams();
                RequestBody requestBody = null;

                // Add params to url
                // Method - GET
                if (method.equals(RequestTemplateCreator.GET)) {
                    StringBuilder stringBuilder = new StringBuilder(url);
                    if (parmsMap != null) {
                        stringBuilder.append("?");
                        for (Map.Entry entry : parmsMap.entrySet()) {
                            stringBuilder.append(entry.getKey());
                            stringBuilder.append("=");
                            stringBuilder.append(entry.getValue());
                            stringBuilder.append(("&"));
                        }
                    }
                } else {
                    //todo: handle other method
                }

                // Add headers
                Request.Builder builder = getBasicBuilder(url, method, requestBody);

                // Send request
                try {
                    Response response = new OkHttpClient().newCall(builder.build()).execute();
                    String responseString = response.body().string();
                    Log.d(TAG, "Response: \n" + responseString);
                    // If response is JSONArray, we convert to JSONObject
                    if (responseString.charAt(0) == '[') {
                        responseString = "{\"results\":" + responseString + "}";
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
