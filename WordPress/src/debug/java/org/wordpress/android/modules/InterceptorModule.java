package org.wordpress.android.modules;

import androidx.annotation.NonNull;

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.Interceptor;

@Module
public class InterceptorModule {
    @Provides @IntoSet @Named("network-interceptors")
    public Interceptor provideFlipperInterceptor(@NonNull NetworkFlipperPlugin networkFlipperPlugin) {
        return new FlipperOkhttpInterceptor(networkFlipperPlugin);
    }
}
