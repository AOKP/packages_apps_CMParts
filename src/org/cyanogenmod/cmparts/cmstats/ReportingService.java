/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cyanogenmod.cmparts.cmstats;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.util.Log;
import cyanogenmod.providers.CMSettings;

import java.util.List;

public class ReportingService extends IntentService {
    /* package */ static final String TAG = "CMStats";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public ReportingService() {
        super(ReportingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        String deviceId = Utilities.getUniqueID(getApplicationContext());
        String deviceName = Utilities.getDevice();
        String deviceVersion = Utilities.getModVersion();
        String deviceCountry = Utilities.getCountryCode(getApplicationContext());
        String deviceCarrier = Utilities.getCarrier(getApplicationContext());
        String deviceCarrierId = Utilities.getCarrierId(getApplicationContext());

        final int jobId = AnonymousStats.getNextJobId(getApplicationContext());

        if (DEBUG) Log.d(TAG, "scheduling job id: " + jobId);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName);
        bundle.putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId);
        bundle.putString(StatsUploadJobService.KEY_VERSION, deviceVersion);
        bundle.putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry);
        bundle.putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier);
        bundle.putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId);
        bundle.putLong(StatsUploadJobService.KEY_TIMESTAMP, System.currentTimeMillis());

        // schedule aokp stats upload
        js.schedule(new JobInfo.Builder(jobId, new ComponentName(getPackageName(),
                StatsUploadJobService.class.getName()))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1000)
                .setExtras(bundle)
                .setPersisted(true)
                .build());

        // reschedule
        AnonymousStats.updateLastSynced(this);
        ReportingServiceManager.setAlarm(this);
    }
}
