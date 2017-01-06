package org.wordpress.aztec.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.wordpress.aztec.Html;

import java.util.Map;

public class PicassoImageLoader implements Html.ImageGetter {

    private Context context;
    private Map<String, Target> targets;

    public PicassoImageLoader(Context context, Map<String, Target> targets) {
        this.context = context;
        this.targets = targets;
    }

    @Override
    public void loadImage(String source, final Callbacks callbacks, int maxWidth) {
        Picasso picasso = Picasso.with(context);
        picasso.setLoggingEnabled(true);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callbacks.onImageLoaded(new BitmapDrawable(context.getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                callbacks.onImageLoadingFailed();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        targets.put(source, target);
        //noinspection SuspiciousNameCombination
        picasso.load(source).resize(maxWidth, maxWidth).centerInside().onlyScaleDown().into(target);
    }
}
