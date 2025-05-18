package com.example.mobilalkosinstagram;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import android.util.Log;

public class HomeActivityLifecycleObserver implements LifecycleObserver {
    private final HomeActivity activity;

    public HomeActivityLifecycleObserver(HomeActivity activity) {
        this.activity = activity;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (activity != null) {
            activity.refreshFeed();
        } else {
            Log.e("LifecycleObserver", "Activity is null");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        if (activity != null) {
            activity.saveUserProgress();
        }
    }
}