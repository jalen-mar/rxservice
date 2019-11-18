package com.gemini.jalen.rxservice.cache;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
    private Application application;

    public Storage(Application application) {
        this.application = application;
    }

    public SharedPreferences getGlobalStorage() {
        return application.getSharedPreferences("APP_STORAGE", Context.MODE_PRIVATE);
    }

    public String getCurrentUserId() {
        return getGlobalStorage().getString("current_user_id", "");
    }

    public SharedPreferences getUserStorage() {
        return application.getSharedPreferences(getCurrentUserId(), Context.MODE_PRIVATE);
    }

    public String getCurrentUserToken() {
        return getUserStorage().getString("current_user_authentication", null);
    }

    public String getUserStorage(String key) {
        return getUserStorage().getString(key, null);
    }

    public Storage saveUserStorage(String key, String value) {
        getUserStorage().edit().putString(key, value).apply();
        return this;
    }

    public Storage deleteUserStorage(String key) {
        getUserStorage().edit().putString(key, null).apply();
        return this;
    }

    public String getGlobalStorage(String key) {
        return getGlobalStorage().getString(key, null);
    }

    public Storage saveGlobalStorage(String key, String value) {
        getGlobalStorage().edit().putString(key, value).apply();
        return this;
    }

    public Storage deleteGlobalStorage(String key) {
        getGlobalStorage().edit().putString(key, null).apply();
        return this;
    }

    public void save(String userId, String token) {
        getGlobalStorage().edit().putString("current_user_id", userId).apply();
        getUserStorage().edit().putString("current_user_authentication", token).apply();
    }

    public void clear() {
        getUserStorage().edit().clear().apply();
        getGlobalStorage().edit().remove("current_user_id").apply();
    }
}
