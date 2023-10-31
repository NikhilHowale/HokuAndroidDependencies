package com.hokuapps.startvideocall.displayer;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Displays image with "fade in" animation
 *
 */
public class FadeInBitmapDisplayer {
    private final int durationMillis;
    private final boolean animateFromNetwork;
    private final boolean animateFromDisk;
    private final boolean animateFromMemory;

    /**
     * @param durationMillis Duration of "fade-in" animation (in milliseconds)
     */
    public FadeInBitmapDisplayer(int durationMillis) {
        this(durationMillis, true, true, true);
    }

    /**
     * @param durationMillis     Duration of "fade-in" animation (in milliseconds)
     * @param animateFromNetwork Whether animation should be played if image is loaded from network
     * @param animateFromDisk    Whether animation should be played if image is loaded from disk cache
     * @param animateFromMemory  Whether animation should be played if image is loaded from memory cache
     */
    public FadeInBitmapDisplayer(int durationMillis, boolean animateFromNetwork, boolean animateFromDisk,
                                 boolean animateFromMemory) {
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisk = animateFromDisk;
        this.animateFromMemory = animateFromMemory;
    }

    /**
     * Animates {@link ImageView} with "fade-in" effect
     *
     * @param imageView      {@link ImageView} which display image in
     * @param durationMillis The length of the animation in milliseconds
     */
    public static void animate(View imageView, int durationMillis) {
        if (imageView != null) {
            AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
            fadeImage.setDuration(durationMillis);
            fadeImage.setInterpolator(new DecelerateInterpolator());
            imageView.startAnimation(fadeImage);
        }
    }
}
