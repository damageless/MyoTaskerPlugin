package com.damageddev.myotaskerplugin;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.damageddev.myotaskerplugin.utils.Constants;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        addPreferencesFromResource(R.xml.settings);
    }
}
