package com.expensemanager.app.main;

import android.app.Application;

import com.onesignal.OneSignal;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhaolong Zhong on 8/16/16.
 */

public class EApplication extends Application {
    private static final String TAG = EApplication.class.getSimpleName();

    private static EApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // Configure Realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm.getDefaultInstance();

        // Push notification
        OneSignal.startInit(this).init();
    }

    public static EApplication getInstance() {
        return application;
    }
}
