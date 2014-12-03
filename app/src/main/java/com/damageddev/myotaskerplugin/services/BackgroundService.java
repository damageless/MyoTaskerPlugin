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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
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
    public static final int NOTIFICATION_ID = 6592;

    public static final String ACTION_DISCONNECT = "com.damageddev.myotaskerplugin.ACTION_DISCONNECT";
    public static final String ACTION_CONNECT = " com.damageddev.myotaskerplugin.ACTION_CONNECT";

    private static final String SHOW_MYO_STATUS_NOTIFICATION = "show_myo_status_notification";
    private static final Intent INTENT_REQUEST_REQUERY =
            new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY)
                    .putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY,
                            EditActivity.class.getName());

    private Toast mToast;
    private Hub mHub;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private long mLastUnlockTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mHub = Hub.getInstance();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mHub.setLockingPolicy(Hub.LockingPolicy.NONE);
    }

    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            showToast(getString(R.string.myo_connected));

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                mNotificationBuilder = buildDisconnectNotification()
                        .setContentTitle(getString(R.string.myo_connected))
                        .setContentText(myo.getName())
                        .setSubText(myo.getMacAddress());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            mHub.attachToAdjacentMyo();

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                mNotificationBuilder = buildConnectNotification()
                        .setContentTitle(getString(R.string.disconnected_from_myo))
                        .setContentText(getString(R.string.last_connected_to) + " " + myo.getName());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            showToast(pose.toString());
            Bundle bundle = new Bundle();
            bundle.putString(Constants.POSE, pose.toString());

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            long unlockTime = Long.valueOf(sharedPreferences.getString("relock_time", "5")) * 1000;

            boolean isUnlocked = (mLastUnlockTime + unlockTime) > timestamp;

            if (pose == Pose.DOUBLE_TAP && !isUnlocked) {
                mLastUnlockTime = timestamp;
                myo.vibrate(Myo.VibrationType.SHORT);
            }

            if (isUnlocked && (pose != Pose.REST || pose != Pose.UNKNOWN)) {
                TaskerPlugin.Event.addPassThroughData(INTENT_REQUEST_REQUERY, bundle);
                BackgroundService.this.sendBroadcast(INTENT_REQUEST_REQUERY);
                mLastUnlockTime = timestamp;

                if (sharedPreferences.getBoolean("show_toasts", true)) {
                    showToast(pose.toString());
                }

                if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                    mNotificationBuilder
                            .setSubText(myo.getMacAddress())
                            .setContentTitle(myo.getName())
                            .setContentText(getString(R.string.last_gesture) + " " + pose.toString());
                    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                }
            }
        }

        @Override
        public void onAttach(Myo myo, long timestamp) {
            super.onAttach(myo, timestamp);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                mNotificationBuilder
                        .setContentTitle(getString(R.string.myo_attached))
                        .setContentText(myo.getName());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }

        @Override
        public void onDetach(Myo myo, long timestamp) {
            super.onDetach(myo, timestamp);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                mNotificationBuilder
                        .setContentTitle(getString(R.string.myo_detached))
                        .setContentText(myo.getName())
                        .setContentText(getString(R.string.detached_myo) + " " + myo.getName());
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }

        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            super.onArmUnsync(myo, timestamp);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);

            if (sharedPreferences.getBoolean(SHOW_MYO_STATUS_NOTIFICATION, true)) {
                mNotificationBuilder.setContentTitle(getString(R.string.myo_needs_sync));
                mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }
    };

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (TextUtils.isEmpty(action)) {
                init();
            } else {
                if (action.equals(ACTION_DISCONNECT)) {
                    mHub.shutdown();
                    mNotificationManager.cancelAll();
                } else if (action.equals(ACTION_CONNECT)) {
                    init();
                }
            }
        }

        return START_STICKY;
    }

    private void init() {
        if (!mHub.init(this, getPackageName())) {
            stopSelf();
        }

        mHub.removeListener(mListener);
        mHub.addListener(mListener);
        mHub.attachToAdjacentMyo();

        mNotificationBuilder = buildConnectNotification();
    }

    private NotificationCompat.Builder buildConnectNotification() {
        Intent connectIntent = new Intent(ACTION_DISCONNECT);
        PendingIntent connectPendingIntent = PendingIntent.getService(this, 0, connectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(BackgroundService.this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.myo_connected))
                .setOngoing(true)
                .addAction(R.drawable.ic_connect, getString(R.string.connect_to_myo), connectPendingIntent);
    }

    private NotificationCompat.Builder buildDisconnectNotification() {
        Intent disconnectIntent = new Intent(ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent = PendingIntent.getService(this, 0, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(BackgroundService.this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.myo_connected))
                .setOngoing(true)
                .addAction(R.drawable.ic_disconnect, getString(R.string.disconnect), disconnectPendingIntent);
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