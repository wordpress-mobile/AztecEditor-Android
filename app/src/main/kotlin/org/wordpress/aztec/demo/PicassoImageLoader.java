package org.wordpress.aztec.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.wordpress.aztec.AztecText;
import org.wordpress.aztec.Html;

import java.util.Map;

public class PicassoImageLoader implements Html.ImageGetter {

    private Context context;
    private Map<String, com.squareup.picasso.Target> targets;

    public PicassoImageLoader(Context context, AztecText aztec) {
        this.context = context;
        this.targets = new ArrayMap<>();

        // Picasso keeps a weak reference to targets so we need to attach them to AztecText
        aztec.setTag(targets);
    }

    @Override
    public void loadImage(final String source, final Callbacks callbacks, int maxWidth) {
        Picasso picasso = Picasso.with(context);
        picasso.setLoggingEnabled(true);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callbacks.onImageLoaded(new BitmapDrawable(context.getResources(), bitmap));
                targets.remove(source);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                callbacks.onImageLoadingFailed();
                targets.remove(source);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        // add a strong reference to the target until it's called or the view gets detroyed
        targets.put(source, target);

        //noinspection SuspiciousNameCombination
        picasso.load(source).resize(maxWidth, maxWidth).centerInside().onlyScaleDown().into(target);
    }
}
