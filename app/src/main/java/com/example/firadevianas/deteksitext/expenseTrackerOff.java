package com.example.firadevianas.deteksitext;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Firadevianas on 3/15/2019.
 */

public class expenseTrackerOff extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
