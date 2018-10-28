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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View.AccessibilityDelegate;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherStateManager.StateHandler;
import com.android.launcher3.R;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.graphics.BitmapRenderer;
import com.android.launcher3.util.TouchController;
import com.android.launcher3.widget.WidgetsFullSheet;
import com.android.systemui.shared.recents.view.RecentsTransition;

public class UiFactory {

    public static final boolean USE_HARDWARE_BITMAP = false; // FeatureFlags.IS_DOGFOOD_BUILD;

    public static TouchController[] createTouchControllers(Launcher launcher) {
        return new TouchController[] {
                new TwoStepSwipeController(launcher),
                new OverviewSwipeUpController(launcher)};
    }

    public static AccessibilityDelegate newPageIndicatorAccessibilityDelegate() {
        return null;
    }

    public static StateHandler[] getStateHandler(Launcher launcher) {
        return new StateHandler[] {
                launcher.getAllAppsController(), launcher.getWorkspace(),
                new RecentsViewStateController(launcher)};
    }

    public static void onWorkspaceLongPress(Launcher launcher) {
        PopupMenu menu = new PopupMenu(launcher.getApplicationContext(),
                launcher.getWorkspace().getPageIndicator());

        menu.getMenu().add(R.string.wallpaper_button_text).setOnMenuItemClickListener((i) -> {
            launcher.onClickWallpaperPicker(null);
            return true;
        });
        menu.getMenu().add(R.string.widget_button_text).setOnMenuItemClickListener((i) -> {
            if (launcher.getPackageManager().isSafeMode()) {
                Toast.makeText(launcher, R.string.safemode_widget_error, Toast.LENGTH_SHORT).show();
            } else {
                WidgetsFullSheet.show(launcher, true /* animated */);
            }
            return true;
        });
        if (launcher.hasSettings()) {
            menu.getMenu().add(R.string.settings_button_text).setOnMenuItemClickListener((i) -> {
                launcher.startActivity(new Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                        .setPackage(launcher.getPackageName())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            });
        }
        menu.show();
    }

    public static Bitmap createFromRenderer(int width, int height, boolean forceSoftwareRenderer,
            BitmapRenderer renderer) {
        if (USE_HARDWARE_BITMAP && !forceSoftwareRenderer) {
            return RecentsTransition.createHardwareBitmap(width, height, renderer::render);
        } else {
            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            renderer.render(new Canvas(result));
            return result;
        }
    }
}
