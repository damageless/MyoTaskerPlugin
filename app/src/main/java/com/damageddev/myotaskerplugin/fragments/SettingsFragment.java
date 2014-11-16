package com.damageddev.myotaskerplugin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.damageddev.myotaskerplugin.R;
import com.damageddev.myotaskerplugin.utils.Constants;

/**
 * Created by agessel on 10/21/14.
 */
public class SettingsFragment extends PreferenceFragment {
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(Constants.SHARED_PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);

        addPreferencesFromResource(R.xml.settings);
    }


}
