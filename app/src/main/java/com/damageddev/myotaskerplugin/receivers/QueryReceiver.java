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

package com.damageddev.myotaskerplugin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.damageddev.myotaskerplugin.utils.BundleScrubber;
import com.damageddev.myotaskerplugin.utils.Constants;
import com.damageddev.myotaskerplugin.utils.TaskerPlugin;

public final class QueryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION.equals(intent.getAction())) {
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        Bundle myExtras = TaskerPlugin.Event.retrievePassThroughData(intent);

        if (myExtras != null && myExtras.getString(Constants.POSE).equals(intent.getExtras().get(Constants.POSE))) {
            setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
        }
    }
}