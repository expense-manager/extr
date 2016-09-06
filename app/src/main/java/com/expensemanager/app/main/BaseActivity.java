package com.expensemanager.app.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.expensemanager.app.R;

/**
 * Created by Zhaolong Zhong on 8/20/16.
 */

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private ActivityCompat.OnRequestPermissionsResultCallback requestPermissionsResultCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setRequestPermissionsResultCallback(
            ActivityCompat.OnRequestPermissionsResultCallback requestPermissionsResultCallback) {
        this.requestPermissionsResultCallback = requestPermissionsResultCallback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestPermissionsResultCallback != null) {
            requestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                    grantResults);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close();
                break;
        }

        return true;
    }

    protected void close() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    public void onBackPressed() {
        close();
    }
}
