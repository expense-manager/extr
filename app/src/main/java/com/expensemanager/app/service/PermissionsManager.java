package com.expensemanager.app.service;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.expensemanager.app.main.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 */

public class PermissionsManager {
    private final static String TAG = PermissionsManager.class.getSimpleName();

    private final static int REQUEST_PERMISSIONS = 100;

    public static void verifyCameraPermissionGranted(BaseActivity baseActivity,
                                                       OnVerifyPermissionsGrantedCallback onVerifyPermissionsGrantedCallback) {
        verifyPermissionsGranted(baseActivity,
                onVerifyPermissionsGrantedCallback,
                Manifest.permission.CAMERA);
    }

    public static void verifyExternalStoragePermissionGranted(BaseActivity baseActivity,
                                                     OnVerifyPermissionsGrantedCallback onVerifyPermissionsGrantedCallback) {
        verifyPermissionsGranted(baseActivity,
                onVerifyPermissionsGrantedCallback,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void verifyPermissionsGranted(BaseActivity baseActivity,
                                                final OnVerifyPermissionsGrantedCallback onVerifyPermissionsGrantedCallback,
                                                String... permissions) {

        baseActivity.setRequestPermissionsResultCallback((int requestCode, String[] perms, int[] grantResults) -> {
            boolean allGranted = true;

            for(int grantResult : grantResults) {
                if(grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }

            onVerifyPermissionsGrantedCallback.onVerifyPermissionsGranted(allGranted);
        });

        List<String> permissionsToRequest = new ArrayList<>();

        for(String permission : permissions) {
            if(ContextCompat.checkSelfPermission(baseActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if(permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(baseActivity,
                    permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    REQUEST_PERMISSIONS);
        } else {
            onVerifyPermissionsGrantedCallback.onVerifyPermissionsGranted(true);
        }
    }

    public interface OnVerifyPermissionsGrantedCallback {
        void onVerifyPermissionsGranted(boolean isGranted);
    }
}
