package com.google.samples.apps.topeka;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

public final class MDexApp extends Application {
    public MDexApp() {
        // 1
    }

    @Override
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Throwable any) {
            any.printStackTrace(System.out);
        }
    }
}
