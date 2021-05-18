package com.example.contactutils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Hashtable;

public class SharedPref {

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void putSelectedSaveContact(Context context, String Key, Hashtable<String, Hashtable<String, Object>> value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString(Key, json);
        editor.apply();
    }

    public static Hashtable<String, Hashtable<String, Object>> getSelectedSaveContact(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Key, "");
        Type type = new TypeToken<Hashtable<String, Hashtable<String, Object>>>() {
        }.getType();
        Hashtable<String, Hashtable<String, Object>> arrayList = gson.fromJson(json, type);
        return arrayList;
    }

    public static void putString(Context context, String Key, String value) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(Key, value);
        editor.apply();
    }

    public static String getString(Context context, String Key) {
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Key, "");
    }
}
