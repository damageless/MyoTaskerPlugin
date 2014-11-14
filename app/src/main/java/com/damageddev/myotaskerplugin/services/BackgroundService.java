/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.damageddev.myotaskerplugin.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.damageddev.myotaskerplugin.EditActivity;
import com.damageddev.myotaskerplugin.R;
import com.damageddev.myotaskerplugin.utils.Constants;
import com.damageddev.myotaskerplugin.utils.TaskerPlugin;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;


public final class BackgroundService extends Service {
    public static final long DEFAULT_UNLOCK_TIME_MILLIS = 5000l;

    protected static final Intent INTENT_REQUEST_REQUERY =
            new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY)
                    .putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
                            EditActivity.class.getName());

    private Toast mToast;
    private SharedPreferences mDefaultPreferences;
    private Hub mHub;

    private long mLastUnlockTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mDefaultPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

        mHub = Hub.getInstance();
    }

    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast(getString(R.string.myo_connected));
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            mHub.pairWithAnyMyo();
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.POSE, pose.toString());

            boolean isUnlocked = (mLastUnlockTime + DEFAULT_UNLOCK_TIME_MILLIS) > timestamp;

            if (pose == Pose.THUMB_TO_PINKY && !isUnlocked) {
                mLastUnlockTime = timestamp;
                myo.vibrate(Myo.VibrationType.MEDIUM);
            }

            if (isUnlocked) {
                TaskerPlugin.Event.addPassThroughData(INTENT_REQUEST_REQUERY, bundle);
                BackgroundService.this.sendBroadcast(INTENT_REQUEST_REQUERY);
                myo.vibrate(Myo.VibrationType.SHORT);
                mLastUnlockTime = timestamp;
            }

            if (mDefaultPreferences.getBoolean("show_toasts", true)) {
                showToast(pose.toString());
            }
        }
    };

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (!mHub.init(this, getPackageName())) {
            stopSelf();
        }

        mHub.addListener(mListener);
        mHub.pairWithAnyMyo();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }

        mToast.show();
    }

}