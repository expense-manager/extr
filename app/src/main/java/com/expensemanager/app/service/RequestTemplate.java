package com.expensemanager.app.service;

import java.util.Map;

/**
 * Created by Zhaolong Zhong on 8/17/16.
 */

public class RequestTemplate {
    private static final String TAG = RequestTemplate.class.getSimpleName();

    private String method;
    private String url;
    private Map<String, String> params;
    private Map<String, byte[]> paramsByte;

    public RequestTemplate(String method, String url, Map<String, String> params) {
        this.method = method;
        this.url = url;
        this.params = params;
    }

    public RequestTemplate(String method, String url, Map<String, String> params, Map<String, byte[]> paramsByte) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.paramsByte = paramsByte;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, byte[]> getParamsByte() {
        return paramsByte;
    }
}
