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

import static com.android.launcher3.LauncherAnimUtils.OVERVIEW_TRANSITION_MS;

import android.graphics.Rect;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace;
import com.android.launcher3.userevent.nano.LauncherLogProto.ContainerType;
import com.android.quickstep.RecentsView;

/**
 * Definition for overview state
 */
public class OverviewState extends LauncherState {

    public static final float WORKSPACE_SCALE_ON_SCROLL = 0.9f;

    private static final int STATE_FLAGS = FLAG_SHOW_SCRIM | FLAG_WORKSPACE_ICONS_CAN_BE_DRAGGED;

    public OverviewState(int id) {
        super(id, ContainerType.WORKSPACE, OVERVIEW_TRANSITION_MS, 1f, STATE_FLAGS);
    }

    @Override
    public float[] getWorkspaceScaleAndTranslation(Launcher launcher) {
        Rect pageRect = new Rect();
        RecentsView.getPageRect(launcher, pageRect);
        if (launcher.getWorkspace().getNormalChildWidth() <= 0 || pageRect.isEmpty()) {
            return super.getWorkspaceScaleAndTranslation(launcher);
        }

        RecentsView rv = launcher.getOverviewPanel();
        float overlap = 0;
        if (rv.getCurrentPage() >= rv.getFirstTaskIndex()) {
            Utilities.scaleRectAboutCenter(pageRect, WORKSPACE_SCALE_ON_SCROLL);
            overlap = launcher.getResources().getDimension(R.dimen.workspace_overview_offset_x);
        }
        return getScaleAndTranslationForPageRect(launcher, overlap, pageRect);
    }

    @Override
    public float getHoseatAlpha(Launcher launcher) {
        return launcher.getDeviceProfile().isVerticalBarLayout() ? 0 : 1;
    }

    @Override
    public void onStateEnabled(Launcher launcher) {
        RecentsView rv = launcher.getOverviewPanel();
        rv.setOverviewStateEnabled(true);
    }

    @Override
    public void onStateDisabled(Launcher launcher) {
        RecentsView rv = launcher.getOverviewPanel();
        rv.setOverviewStateEnabled(false);
    }

    @Override
    public View getFinalFocus(Launcher launcher) {
        return launcher.getOverviewPanel();
    }

    public static float[] getScaleAndTranslationForPageRect(Launcher launcher, float offsetX,
            Rect pageRect) {
        Workspace ws = launcher.getWorkspace();
        float childWidth = ws.getNormalChildWidth();

        Rect insets = launcher.getDragLayer().getInsets();
        float scale = pageRect.width() / childWidth;

        float translationX = offsetX / scale;
        if (Utilities.isRtl(launcher.getResources())) {
            translationX = -translationX;
        }

        float halfHeight = ws.getHeight() / 2;
        float childTop = halfHeight - scale * (halfHeight - ws.getPaddingTop() - insets.top);
        float translationY = pageRect.top - childTop;

        return new float[] {scale, translationX, translationY};
    }
}
