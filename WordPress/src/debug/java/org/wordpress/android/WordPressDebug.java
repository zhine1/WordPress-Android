package org.wordpress.android;

import android.os.StrictMode;
import android.util.Log;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.yarolegovich.wellsql.WellSql;

import org.wordpress.android.modules.DaggerAppComponentDebug;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;
import org.wordpress.android.util.UploadWorker;

public class WordPressDebug extends WordPress {
    @Override
    public void onCreate() {
        super.onCreate();

        // enableStrictMode()

        SoLoader.init(this, true);

        // Init Flipper
        if (FlipperUtils.shouldEnableFlipper(this)) {
            FlipperClient client = AndroidFlipperClient.getInstance(this);
            client.addPlugin(new InspectorFlipperPlugin(getApplicationContext(), DescriptorMapping.withDefaults()));
            client.addPlugin(mNetworkFlipperPlugin);
            client.addPlugin(new InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()));
            client.start();
        }
    }

    @Override
    protected void initWorkManager() {
        Configuration config = (new Configuration.Builder())
                .setMinimumLoggingLevel(Log.DEBUG)
                .setWorkerFactory(new UploadWorker.Factory(mUploadStarter, mSiteStore))
                .build();
        WorkManager.initialize(this, config);
    }

    @Override
    protected void initWellSql() {
        WellSql.init(new WPWellSqlConfig(getApplicationContext()));
    }

    @Override
    protected void initDaggerComponent() {
        mAppComponent = DaggerAppComponentDebug.builder()
                                               .application(this)
                                               .build();
    }

    /**
     * enables "strict mode" for testing - should NEVER be used in release builds
     */
    private void enableStrictMode() {
        // return if the build is not a debug build
        if (!BuildConfig.DEBUG) {
            AppLog.e(T.UTILS, "You should not call enableStrictMode() on a non debug build");
            return;
        }

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                           .detectDiskReads()
                                           .detectDiskWrites()
                                           .detectNetwork()
                                           .penaltyLog()
                                           .penaltyFlashScreen()
                                           .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                                       .detectActivityLeaks()
                                       .detectLeakedSqlLiteObjects()
                                       .detectLeakedClosableObjects()
                                       .detectLeakedRegistrationObjects() // <-- requires Jelly Bean
                                       .penaltyLog()
                                       .build());

        AppLog.w(T.UTILS, "Strict mode enabled");
    }
}
