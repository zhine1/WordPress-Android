package org.wordpress.android.modules;

import android.content.Context;

import androidx.annotation.NonNull;

import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;

import org.wordpress.android.BuildConfig;
import org.wordpress.android.WordPress;
import org.wordpress.android.fluxc.network.UserAgent;
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppConfigModule {
    @Singleton
    @Provides
    @NonNull
    NetworkFlipperPlugin provideNetworkFlipperPlugin() {
        return new NetworkFlipperPlugin();
    }

    @Provides
    public AppSecrets provideAppSecrets() {
        return new AppSecrets(BuildConfig.OAUTH_APP_ID, BuildConfig.OAUTH_APP_SECRET);
    }

    @Provides
    public UserAgent provideUserAgent(Context appContext) {
        return new UserAgent(appContext, WordPress.USER_AGENT_APPNAME);
    }
}
