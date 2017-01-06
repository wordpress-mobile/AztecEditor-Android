package org.wordpress.aztec.demo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

import org.wordpress.aztec.Html;

import java.util.Locale;

public class GlideImageLoader implements Html.ImageGetter {

    public class LoggingListener<T, R> implements RequestListener<T, R> {
        @Override public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                    "onException(%s, %s, %s, %s)", e, model, target, isFirstResource), e);
            return false;
        }
        @Override public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            android.util.Log.d("GLIDE", String.format(Locale.ROOT,
                    "onResourceReady(%s, %s, %s, %s, %s)", resource, model, target, isFromMemoryCache, isFirstResource));
            return false;
        }
    }

    private Context context;

    public GlideImageLoader(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String source, final Callbacks callbacks, final int maxWidth) {
        Glide.with(context).load(source).fitCenter().listener(new LoggingListener<String, GlideDrawable>()).into(new Target<GlideDrawable>() {
            @Override
            public void onLoadStarted(Drawable placeholder) {
                Resources r = context.getResources();
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                callbacks.onImageLoadingFailed();
            }

            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                callbacks.onImageLoaded(resource);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
            }

            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(maxWidth, Target.SIZE_ORIGINAL);
            }

            @Override
            public void setRequest(Request request) {

            }

            @Override
            public Request getRequest() {
                return null;
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onDestroy() {

            }
        });
    }
}
