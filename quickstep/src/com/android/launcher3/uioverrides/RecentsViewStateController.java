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
package com.android.launcher3.uioverrides;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager.AnimationConfig;
import com.android.launcher3.LauncherStateManager.StateHandler;
import com.android.launcher3.PagedView;
import com.android.launcher3.anim.AnimationSuccessListener;
import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.launcher3.anim.Interpolators;
import com.android.quickstep.AnimatedFloat;
import com.android.quickstep.RecentsView;

import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.LauncherState.OVERVIEW;

public class RecentsViewStateController implements StateHandler {

    private final Launcher mLauncher;
    private final RecentsView mRecentsView;
    private final WorkspaceCard mWorkspaceCard;

    private final AnimatedFloat mTransitionProgress = new AnimatedFloat(this::applyProgress);
    // The fraction representing the visibility of the RecentsView. This allows delaying the
    // overall transition while the RecentsView is being shown or hidden.
    private final AnimatedFloat mVisibilityMultiplier = new AnimatedFloat(this::applyProgress);

    public RecentsViewStateController(Launcher launcher) {
        mLauncher = launcher;
        mRecentsView = launcher.getOverviewPanel();
        mRecentsView.setStateController(this);

        mWorkspaceCard = (WorkspaceCard) mRecentsView.getChildAt(0);
        mWorkspaceCard.setup(launcher);
    }

    @Override
    public void setState(LauncherState state) {
        mWorkspaceCard.setWorkspaceScrollingEnabled(state == OVERVIEW);
        setVisibility(state == OVERVIEW);
        setTransitionProgress(state == OVERVIEW ? 1 : 0);
    }

    @Override
    public void setStateWithAnimation(final LauncherState toState,
            AnimatorSetBuilder builder, AnimationConfig config) {
        // Scroll to the workspace card before changing to the NORMAL state.
        int currPage = mRecentsView.getCurrentPage();
        if (toState == NORMAL && currPage != 0 && !config.userControlled) {
            int maxSnapDuration = PagedView.SLOW_PAGE_SNAP_ANIMATION_DURATION;
            int durationPerPage = maxSnapDuration / 10;
            int snapDuration = Math.min(maxSnapDuration, durationPerPage * currPage);
            mRecentsView.snapToPage(0, snapDuration);
            builder.setStartDelay(snapDuration);
        }

        ObjectAnimator progressAnim =
                mTransitionProgress.animateToValue(toState == OVERVIEW ? 1 : 0);
        progressAnim.setDuration(config.duration);
        progressAnim.setInterpolator(Interpolators.LINEAR);
        progressAnim.addListener(new AnimationSuccessListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mWorkspaceCard.setWorkspaceScrollingEnabled(false);
            }

            @Override
            public void onAnimationSuccess(Animator animator) {
                mWorkspaceCard.setWorkspaceScrollingEnabled(toState == OVERVIEW);
            }
        });
        builder.play(progressAnim);

        ObjectAnimator visibilityAnim = animateVisibility(toState == OVERVIEW);
        visibilityAnim.setDuration(config.duration);
        visibilityAnim.setInterpolator(Interpolators.LINEAR);
        builder.play(visibilityAnim);
    }

    public void setVisibility(boolean isVisible) {
        mVisibilityMultiplier.cancelAnimation();
        mRecentsView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mVisibilityMultiplier.updateValue(isVisible ? 1 : 0);
    }

    public ObjectAnimator animateVisibility(boolean isVisible) {
        ObjectAnimator anim = mVisibilityMultiplier.animateToValue(isVisible ? 1 : 0);
        if (isVisible) {
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mRecentsView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            anim.addListener(new AnimationSuccessListener() {
                @Override
                public void onAnimationSuccess(Animator animator) {
                    mRecentsView.setVisibility(View.GONE);
                }
            });
        }
        return anim;
    }

    public void setTransitionProgress(float progress) {
        mTransitionProgress.cancelAnimation();
        mTransitionProgress.updateValue(progress);
    }

    private void applyProgress() {
        mRecentsView.setAlpha(mTransitionProgress.value * mVisibilityMultiplier.value);
    }
}
