package com.expensemanager.app.main;

import android.app.Application;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.expensemanager.app.BuildConfig;
import com.expensemanager.app.R;
import com.expensemanager.app.service.font.Font;
import com.expensemanager.app.service.font.FontHelper;
import com.instabug.library.Feature;
import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;
import com.squareup.leakcanary.LeakCanary;

import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Zhaolong Zhong on 8/16/16.
 */

public class EApplication extends Application {
    private static final String TAG = EApplication.class.getSimpleName();

    private static final String FONTS_DIR = "fonts/";

    private static EApplication application;
    private static Map<String, Typeface> typefaceMap;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        application = this;

        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }

        // Configure Realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm.getDefaultInstance();

        // Push notification
//        OneSignal.startInit(this).init();

        // Configure Font
        typefaceMap = new HashMap<>();
        Typeface defaultTypeface = loadTypeFace(Font.DEFAULT.getName());
        FontHelper.setDefaultFont(defaultTypeface);

        // Instabug
        new Instabug.Builder(this, BuildConfig.INSTABUG_KEY)
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .setTrackingUserStepsState(!BuildConfig.DEBUG ? Feature.State.ENABLED : Feature.State.DISABLED)
                .setCrashReportingState(!BuildConfig.DEBUG ? Feature.State.ENABLED : Feature.State.DISABLED)
                .setDebugEnabled(true)
                .build();

        Instabug.setPrimaryColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    }

    public Typeface getTypeface(Font font) {
        if (!typefaceMap.keySet().contains(font.getName())) {
            loadTypeFace(font.getName());
        }
        return typefaceMap.get(font.getName());
    }

    private Typeface loadTypeFace(String name) {
        try {
            Typeface typeface = Typeface.createFromAsset(this.getAssets(), FONTS_DIR + name);
            typefaceMap.put(name, typeface);

            return typeface;
        } catch (Exception e) {
            Log.e(TAG, "Cannot find font with path assets/fonts/" + name);
        }

        return typefaceMap.get(Font.DEFAULT.getName());
    }

    public static EApplication getInstance() {
        return application;
    }
}
