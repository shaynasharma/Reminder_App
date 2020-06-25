package com.example.bizhawkzreminderdemo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

public enum MainContext {
    INSTANCE;

    private MainActivity mainActivity;

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Context getContext() {
        return mainActivity.getApplicationContext();
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public LayoutInflater getLayoutInflater() {
        return mainActivity.getLayoutInflater();
    }

    void initialize(@NonNull MainActivity mainActivity) {
        setMainActivity(mainActivity);
    }

}
