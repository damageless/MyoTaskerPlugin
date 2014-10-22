package com.damageddev.myotaskerplugin;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.damageddev.myotaskerplugin.fragments.SettingsFragment;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        SettingsFragment settingsFragment = SettingsFragment.newInstance();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, settingsFragment)
                .commit();

    }
}
