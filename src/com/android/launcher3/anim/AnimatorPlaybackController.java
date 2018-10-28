/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.launcher3.anim;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to control the playback of an {@link AnimatorSet}, with custom interpolators
 * and durations.
 *
 * Note: The implementation does not support start delays on child animations or
 * sequential playbacks.
 */
public abstract class AnimatorPlaybackController implements ValueAnimator.AnimatorUpdateListener {

    public static AnimatorPlaybackController wrap(AnimatorSet anim, long duration) {

        /**
         * TODO: use {@link AnimatorSet#setCurrentPlayTime(long)} once b/68382377 is fixed.
         */
        return new AnimatorPlaybackControllerVL(anim, duration);
    }

    private final ValueAnimator mAnimationPlayer;
    private final long mDuration;

    protected final AnimatorSet mAnim;
    private AnimatorSet mOriginalTarget;

    protected float mCurrentFraction;
    private Runnable mEndAction;

    protected AnimatorPlaybackController(AnimatorSet anim, long duration) {
        mAnim = anim;
        mOriginalTarget = mAnim;
        mDuration = duration;

        mAnimationPlayer = ValueAnimator.ofFloat(0, 1);
        mAnimationPlayer.setInterpolator(Interpolators.LINEAR);
        mAnimationPlayer.addListener(new OnAnimationEndDispatcher());
        mAnimationPlayer.addUpdateListener(this);
    }

    public AnimatorSet getTarget() {
        return mAnim;
    }

    public void setOriginalTarget(AnimatorSet anim) {
        mOriginalTarget = anim;
    }

    public AnimatorSet getOriginalTarget() {
        return mOriginalTarget;
    }

    public long getDuration() {
        return mDuration;
    }

    public AnimatorPlaybackController cloneFor(AnimatorSet anim) {
        AnimatorPlaybackController controller = AnimatorPlaybackController.wrap(anim, mDuration);
        controller.setOriginalTarget(mOriginalTarget);
        controller.setPlayFraction(mCurrentFraction);
        return controller;
    }

    /**
     * Starts playing the animation forward from current position.
     */
    public void start() {
        mAnimationPlayer.setFloatValues(mCurrentFraction, 1);
        mAnimationPlayer.setDuration(clampDuration(1 - mCurrentFraction));
        mAnimationPlayer.start();
    }

    /**
     * Starts playing the animation backwards from current position
     */
    public void reverse() {
        mAnimationPlayer.setFloatValues(mCurrentFraction, 0);
        mAnimationPlayer.setDuration(clampDuration(mCurrentFraction));
        mAnimationPlayer.start();
    }

    /**
     * Pauses the currently playing animation.
     */
    public void pause() {
        mAnimationPlayer.cancel();
    }

    /**
     * Returns the underlying animation used for controlling the set.
     */
    public ValueAnimator getAnimationPlayer() {
        return mAnimationPlayer;
    }

    /**
     * Sets the current animation position and updates all the child animators accordingly.
     */
    public abstract void setPlayFraction(float fraction);

    public float getProgressFraction() {
        return mCurrentFraction;
    }

    /**
     * Sets the action to be called when the animation is completed. Also clears any
     * previously set action.
     */
    public void setEndAction(Runnable runnable) {
        mEndAction = runnable;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        setPlayFraction((float) valueAnimator.getAnimatedValue());
    }

    protected long clampDuration(float fraction) {
        float playPos = mDuration * fraction;
        if (playPos <= 0) {
            return 0;
        } else {
            return Math.min((long) playPos, mDuration);
        }
    }

    public void dispatchOnStart() {
        dispatchOnStartRecursively(mAnim);
    }

    private void dispatchOnStartRecursively(Animator animator) {
        for (AnimatorListener l : nonNullList(animator.getListeners())) {
            l.onAnimationStart(animator);
        }

        if (animator instanceof AnimatorSet) {
            for (Animator anim : nonNullList(((AnimatorSet) animator).getChildAnimations())) {
                dispatchOnStartRecursively(anim);
            }
        }
    }

    public static class AnimatorPlaybackControllerVL extends AnimatorPlaybackController {

        private final ValueAnimator[] mChildAnimations;

        private AnimatorPlaybackControllerVL(AnimatorSet anim, long duration) {
            super(anim, duration);

            // Build animation list
            ArrayList<ValueAnimator> childAnims = new ArrayList<>();
            getAnimationsRecur(mAnim, childAnims);
            mChildAnimations = childAnims.toArray(new ValueAnimator[childAnims.size()]);
        }

        private void getAnimationsRecur(AnimatorSet anim, ArrayList<ValueAnimator> out) {
            long forceDuration = anim.getDuration();
            for (Animator child : anim.getChildAnimations()) {
                if (forceDuration > 0) {
                    child.setDuration(forceDuration);
                }
                if (child instanceof ValueAnimator) {
                    out.add((ValueAnimator) child);
                } else if (child instanceof AnimatorSet) {
                    getAnimationsRecur((AnimatorSet) child, out);
                } else {
                    throw new RuntimeException("Unknown animation type " + child);
                }
            }
        }

        @Override
        public void setPlayFraction(float fraction) {
            mCurrentFraction = fraction;
            long playPos = clampDuration(fraction);
            for (ValueAnimator anim : mChildAnimations) {
                anim.setCurrentPlayTime(Math.min(playPos, anim.getDuration()));
            }
        }

    }

    private class OnAnimationEndDispatcher extends AnimationSuccessListener {

        @Override
        public void onAnimationStart(Animator animation) {
            mCancelled = false;
        }

        @Override
        public void onAnimationSuccess(Animator animator) {
            dispatchOnEndRecursively(mAnim);
            if (mEndAction != null) {
                mEndAction.run();
            }
        }

        private void dispatchOnEndRecursively(Animator animator) {
            for (AnimatorListener l : nonNullList(animator.getListeners())) {
                l.onAnimationEnd(animator);
            }

            if (animator instanceof AnimatorSet) {
                for (Animator anim : nonNullList(((AnimatorSet) animator).getChildAnimations())) {
                    dispatchOnEndRecursively(anim);
                }
            }
        }
    }

    private static <T> List<T> nonNullList(ArrayList<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
