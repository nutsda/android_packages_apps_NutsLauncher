/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.android.launcher3.CellLayout.ContainerType;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.badge.BadgeRenderer;
import com.android.launcher3.views.AllAppsScrim;

import java.util.ArrayList;

public class DeviceProfile {

    public interface LauncherLayoutChangeListener {
        void onLauncherLayoutChanged();
    }

    public final InvariantDeviceProfile inv;

    // Device properties
    public final boolean isTablet;
    public final boolean isLargeTablet;
    public final boolean isPhone;
    public final boolean transposeLayoutWithOrientation;

    // Device properties in current orientation
    public final boolean isLandscape;
    public final int widthPx;
    public final int heightPx;
    public final int availableWidthPx;
    public final int availableHeightPx;
    /**
     * The maximum amount of left/right workspace padding as a percentage of the screen width.
     * To be clear, this means that up to 7% of the screen width can be used as left padding, and
     * 7% of the screen width can be used as right padding.
     */
    private static final float MAX_HORIZONTAL_PADDING_PERCENT = 0.14f;

    private static final float TALL_DEVICE_ASPECT_RATIO_THRESHOLD = 2.0f;


    // Workspace
    private final int desiredWorkspaceLeftRightMarginPx;
    public final int cellLayoutPaddingLeftRightPx;
    public final int cellLayoutBottomPaddingPx;
    public final int edgeMarginPx;
    public final Rect defaultWidgetPadding;
    private final int defaultPageSpacingPx;
    private final int topWorkspacePadding;
    public float workspaceSpringLoadShrinkFactor;
    public final int workspaceSpringLoadedBottomSpace;

    // Page indicator
    private int pageIndicatorSizePx;

    // Workspace icons
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;
    public int iconDrawablePaddingOriginalPx;

    public int cellWidthPx;
    public int cellHeightPx;
    public int workspaceCellPaddingXPx;

    // Folder
    public int folderBackgroundOffset;
    public int folderIconSizePx;
    public int folderIconPreviewPadding;

    // Folder cell
    public int folderCellWidthPx;
    public int folderCellHeightPx;

    // Folder child
    public int folderChildIconSizePx;
    public int folderChildTextSizePx;
    public int folderChildDrawablePaddingPx;

    // Hotseat
    public int hotseatCellHeightPx;
    // In portrait: size = height, in landscape: size = width
    public int hotseatBarSizePx;
    public int hotseatBarTopPaddingPx;
    public int hotseatBarBottomPaddingPx;
    public int hotseatBarSidePaddingPx;

    // All apps
    public int allAppsCellHeightPx;
    public int allAppsNumCols;
    public int allAppsNumPredictiveCols;
    public int allAppsButtonVisualSize;
    public int allAppsIconSizePx;
    public int allAppsIconDrawablePaddingPx;
    public float allAppsIconTextSizePx;

    // Widgets
    public final PointF appWidgetScale = new PointF(1.0f, 1.0f);

    // Drop Target
    public int dropTargetBarSizePx;

    // Insets
    private Rect mInsets = new Rect();

    // Listeners
    private ArrayList<LauncherLayoutChangeListener> mListeners = new ArrayList<>();

    // Icon badges
    public BadgeRenderer mBadgeRenderer;

    public DeviceProfile(Context context, InvariantDeviceProfile inv,
            Point minSize, Point maxSize,
            int width, int height, boolean isLandscape) {

        this.inv = inv;
        this.isLandscape = isLandscape;

        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        // Constants from resources
        isTablet = res.getBoolean(R.bool.is_tablet);
        isLargeTablet = res.getBoolean(R.bool.is_large_tablet);
        isPhone = !isTablet && !isLargeTablet;

        // Some more constants
        transposeLayoutWithOrientation =
                res.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);

        context = getContext(context, isVerticalBarLayout()
                ? Configuration.ORIENTATION_LANDSCAPE
                : Configuration.ORIENTATION_PORTRAIT);
        res = context.getResources();


        ComponentName cn = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, cn, null);
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        desiredWorkspaceLeftRightMarginPx = isVerticalBarLayout() ? 0 : edgeMarginPx;
        cellLayoutPaddingLeftRightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_cell_layout_padding);
        cellLayoutBottomPaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_cell_layout_bottom_padding);
        pageIndicatorSizePx = res.getDimensionPixelSize(
                R.dimen.dynamic_grid_min_page_indicator_size);
        defaultPageSpacingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_page_spacing);
        topWorkspacePadding =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_top_padding);
        iconDrawablePaddingOriginalPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        dropTargetBarSizePx = res.getDimensionPixelSize(R.dimen.dynamic_grid_drop_target_size);
        workspaceSpringLoadedBottomSpace =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_min_spring_loaded_space);

        workspaceCellPaddingXPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_cell_padding_x);

        hotseatBarTopPaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_top_padding);
        hotseatBarBottomPaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_bottom_padding);
        hotseatBarSidePaddingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_side_padding);
        hotseatBarSizePx = isVerticalBarLayout()
                ? Utilities.pxFromDp(inv.iconSize, dm)
                : res.getDimensionPixelSize(R.dimen.dynamic_grid_hotseat_size)
                        + hotseatBarTopPaddingPx + hotseatBarBottomPaddingPx;

        // Determine sizes.
        widthPx = width;
        heightPx = height;
        if (isLandscape) {
            availableWidthPx = maxSize.x;
            availableHeightPx = minSize.y;
        } else {
            availableWidthPx = minSize.x;
            availableHeightPx = maxSize.y;
        }

        // Calculate all of the remaining variables.
        updateAvailableDimensions(dm, res);

        // Now that we have all of the variables calculated, we can tune certain sizes.
        float aspectRatio = ((float) Math.max(widthPx, heightPx)) / Math.min(widthPx, heightPx);
        boolean isTallDevice = Float.compare(aspectRatio, TALL_DEVICE_ASPECT_RATIO_THRESHOLD) >= 0;
        if (!isVerticalBarLayout() && isPhone && isTallDevice) {
            // We increase the hotseat size when there is extra space.
            // ie. For a display with a large aspect ratio, we can keep the icons on the workspace
            // in portrait mode closer together by adding more height to the hotseat.
            // Note: This calculation was created after noticing a pattern in the design spec.
            int extraSpace = getCellSize().y - iconSizePx - iconDrawablePaddingPx;
            hotseatBarSizePx += extraSpace - pageIndicatorSizePx;

            // Recalculate the available dimensions using the new hotseat size.
            updateAvailableDimensions(dm, res);
        }

        computeAllAppsButtonSize(context);

        // This is done last, after iconSizePx is calculated above.
        mBadgeRenderer = new BadgeRenderer(context, iconSizePx);
	mBadgeRenderer.setShowNotificationDotCount(Utilities.isShowNotificationDotCount(context));
    }

    DeviceProfile getMultiWindowProfile(Context context, Point mwSize) {
        // We take the minimum sizes of this profile and it's multi-window variant to ensure that
        // the system decor is always excluded.
        mwSize.set(Math.min(availableWidthPx, mwSize.x), Math.min(availableHeightPx, mwSize.y));

        // In multi-window mode, we can have widthPx = availableWidthPx
        // and heightPx = availableHeightPx because Launcher uses the InvariantDeviceProfiles'
        // widthPx and heightPx values where it's needed.
        DeviceProfile profile = new DeviceProfile(context, inv, mwSize, mwSize, mwSize.x, mwSize.y,
                isLandscape);

        // If there isn't enough vertical cell padding with the labels displayed, hide the labels.
        float workspaceCellPaddingY = profile.getCellSize().y - profile.iconSizePx
                - iconDrawablePaddingPx - profile.iconTextSizePx;
        if (workspaceCellPaddingY < profile.iconDrawablePaddingPx * 2) {
            profile.adjustToHideWorkspaceLabels();
        }

        // We use these scales to measure and layout the widgets using their full invariant profile
        // sizes and then draw them scaled and centered to fit in their multi-window mode cellspans.
        float appWidgetScaleX = (float) profile.getCellSize().x / getCellSize().x;
        float appWidgetScaleY = (float) profile.getCellSize().y / getCellSize().y;
        profile.appWidgetScale.set(appWidgetScaleX, appWidgetScaleY);

        return profile;
    }

    public void addLauncherLayoutChangedListener(LauncherLayoutChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeLauncherLayoutChangedListener(LauncherLayoutChangeListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    /**
     * Adjusts the profile so that the labels on the Workspace are hidden.
     * It is important to call this method after the All Apps variables have been set.
     */
    private void adjustToHideWorkspaceLabels() {
        iconTextSizePx = 0;
        iconDrawablePaddingPx = 0;
        cellHeightPx = iconSizePx;

        // In normal cases, All Apps cell height should equal the Workspace cell height.
        // Since we are removing labels from the Workspace, we need to manually compute the
        // All Apps cell height.
        int topBottomPadding = allAppsIconDrawablePaddingPx * (isVerticalBarLayout() ? 2 : 1);
        allAppsCellHeightPx = allAppsIconSizePx + allAppsIconDrawablePaddingPx
                + Utilities.calculateTextHeight(allAppsIconTextSizePx)
                + topBottomPadding * 2;
    }

    /**
     * Determine the exact visual footprint of the all apps button, taking into account scaling
     * and internal padding of the drawable.
     */
    private void computeAllAppsButtonSize(Context context) {
        Resources res = context.getResources();
        float padding = res.getInteger(R.integer.config_allAppsButtonPaddingPercent) / 100f;
        allAppsButtonVisualSize = (int) (iconSizePx * (1 - padding)) - context.getResources()
                        .getDimensionPixelSize(R.dimen.all_apps_button_scale_down);
    }

    private void updateAvailableDimensions(DisplayMetrics dm, Resources res) {
        updateIconSize(1f, res, dm);

        // Check to see if the icons fit within the available height.  If not, then scale down.
        float usedHeight = (cellHeightPx * inv.numRows);
        int maxHeight = (availableHeightPx - getTotalWorkspacePadding().y);
        if (usedHeight > maxHeight) {
            float scale = maxHeight / usedHeight;
            updateIconSize(scale, res, dm);
        }
        updateAvailableFolderCellDimensions(dm, res);
    }

    private void updateIconSize(float scale, Resources res, DisplayMetrics dm) {
        // Workspace
        float invIconSizePx = isVerticalBarLayout() ? inv.landscapeIconSize : inv.iconSize;
        iconSizePx = (int) (Utilities.pxFromDp(invIconSizePx, dm) * scale);
        iconTextSizePx = (int) (Utilities.pxFromSp(inv.iconTextSize, dm) * scale);
        iconDrawablePaddingPx = (int) (iconDrawablePaddingOriginalPx * scale);

        cellHeightPx = iconSizePx + iconDrawablePaddingPx
                + Utilities.calculateTextHeight(iconTextSizePx);
        int cellYPadding = (getCellSize().y - cellHeightPx) / 2;
        if (iconDrawablePaddingPx > cellYPadding && !isVerticalBarLayout()
                && !inMultiWindowMode()) {
            // Ensures that the label is closer to its corresponding icon. This is not an issue
            // with vertical bar layout or multi-window mode since the issue is handled separately
            // with their calls to {@link #adjustToHideWorkspaceLabels}.
            cellHeightPx -= (iconDrawablePaddingPx - cellYPadding);
            iconDrawablePaddingPx = cellYPadding;
        }
        cellWidthPx = iconSizePx + iconDrawablePaddingPx;

        // All apps
        allAppsIconTextSizePx = iconTextSizePx;
        allAppsIconSizePx = iconSizePx;
        allAppsIconDrawablePaddingPx = iconDrawablePaddingPx;
        allAppsCellHeightPx = getCellSize().y;

        if (isVerticalBarLayout()) {
            // Always hide the Workspace text with vertical bar layout.
            adjustToHideWorkspaceLabels();
        }

        // Hotseat
        if (isVerticalBarLayout()) {
            hotseatBarSizePx = iconSizePx;
        }
        hotseatCellHeightPx = iconSizePx;

        if (!isVerticalBarLayout()) {
            int expectedWorkspaceHeight = availableHeightPx - hotseatBarSizePx
                    - pageIndicatorSizePx - topWorkspacePadding;
            float minRequiredHeight = dropTargetBarSizePx + workspaceSpringLoadedBottomSpace;
            workspaceSpringLoadShrinkFactor = Math.min(
                    res.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f,
                    1 - (minRequiredHeight / expectedWorkspaceHeight));
        } else {
            workspaceSpringLoadShrinkFactor =
                    res.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f;
        }

        // Folder icon
        folderBackgroundOffset = -iconDrawablePaddingPx;
        folderIconSizePx = iconSizePx + 2 * -folderBackgroundOffset;
        folderIconPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
    }

    private void updateAvailableFolderCellDimensions(DisplayMetrics dm, Resources res) {
        int folderBottomPanelSize = res.getDimensionPixelSize(R.dimen.folder_label_padding_top)
                + res.getDimensionPixelSize(R.dimen.folder_label_padding_bottom)
                + Utilities.calculateTextHeight(res.getDimension(R.dimen.folder_label_text_size));

        updateFolderCellSize(1f, dm, res);

        // Don't let the folder get too close to the edges of the screen.
        int folderMargin = edgeMarginPx;

        // Check if the icons fit within the available height.
        float usedHeight = folderCellHeightPx * inv.numFolderRows + folderBottomPanelSize;
        int maxHeight = availableHeightPx - getTotalWorkspacePadding().y - folderMargin;
        float scaleY = maxHeight / usedHeight;

        // Check if the icons fit within the available width.
        float usedWidth = folderCellWidthPx * inv.numFolderColumns;
        int maxWidth = availableWidthPx - getTotalWorkspacePadding().x - folderMargin;
        float scaleX = maxWidth / usedWidth;

        float scale = Math.min(scaleX, scaleY);
        if (scale < 1f) {
            updateFolderCellSize(scale, dm, res);
        }
    }

    private void updateFolderCellSize(float scale, DisplayMetrics dm, Resources res) {
        folderChildIconSizePx = (int) (Utilities.pxFromDp(inv.iconSize, dm) * scale);
        folderChildTextSizePx =
                (int) (res.getDimensionPixelSize(R.dimen.folder_child_text_size) * scale);

        int textHeight = Utilities.calculateTextHeight(folderChildTextSizePx);
        int cellPaddingX = (int) (res.getDimensionPixelSize(R.dimen.folder_cell_x_padding) * scale);
        int cellPaddingY = (int) (res.getDimensionPixelSize(R.dimen.folder_cell_y_padding) * scale);

        folderCellWidthPx = folderChildIconSizePx + 2 * cellPaddingX;
        folderCellHeightPx = folderChildIconSizePx + 2 * cellPaddingY + textHeight;
        folderChildDrawablePaddingPx = Math.max(0,
                (folderCellHeightPx - folderChildIconSizePx - textHeight) / 3);
    }

    public void updateInsets(Rect insets) {
        mInsets.set(insets);
    }

    public void updateAppsViewNumCols() {
        allAppsNumCols = allAppsNumPredictiveCols = inv.numColumns;
    }

    /** Returns the width and height of the search bar, ignoring any padding. */
    public Point getSearchBarDimensForWidgetOpts() {
        if (isVerticalBarLayout()) {
            return new Point(dropTargetBarSizePx, availableHeightPx - 2 * edgeMarginPx);
        } else {
            int gap;
            if (isTablet) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                // XXX: If the icon size changes across orientations, we will have to take
                //      that into account here too.
                gap = ((widthPx - 2 * edgeMarginPx
                        - (inv.numColumns * cellWidthPx)) / (2 * (inv.numColumns + 1)))
                        + edgeMarginPx;
            } else {
                gap = desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right;
            }
            return new Point(availableWidthPx - 2 * gap, dropTargetBarSizePx);
        }
    }

    public Point getCellSize() {
        Point result = new Point();
        // Since we are only concerned with the overall padding, layout direction does
        // not matter.
        Point padding = getTotalWorkspacePadding();
        result.x = calculateCellWidth(availableWidthPx - padding.x
                - cellLayoutPaddingLeftRightPx * 2, inv.numColumns);
        result.y = calculateCellHeight(availableHeightPx - padding.y
                - cellLayoutBottomPaddingPx, inv.numRows);
        return result;
    }

    public Point getTotalWorkspacePadding() {
        Rect padding = getWorkspacePadding(null);
        return new Point(padding.left + padding.right, padding.top + padding.bottom);
    }

    /**
     * Returns the workspace padding in the specified orientation.
     */
    public Rect getWorkspacePadding(Rect recycle) {
        Rect padding = recycle == null ? new Rect() : recycle;
        if (isVerticalBarLayout()) {
            padding.top = 0;
            padding.bottom = edgeMarginPx;
            padding.left = hotseatBarSidePaddingPx;
            padding.right = hotseatBarSidePaddingPx;
            if (mInsets.left > mInsets.right) {
                padding.left += hotseatBarSizePx;
                padding.right += pageIndicatorSizePx;
            } else {
                padding.left += pageIndicatorSizePx;
                padding.right += hotseatBarSizePx;
            }
        } else {
            int paddingBottom = hotseatBarSizePx + pageIndicatorSizePx;
            if (isTablet) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                // The amount of screen space available for left/right padding.
                int availablePaddingX = Math.max(0, widthPx - ((inv.numColumns * cellWidthPx) +
                        ((inv.numColumns - 1) * cellWidthPx)));
                availablePaddingX = (int) Math.min(availablePaddingX,
                            widthPx * MAX_HORIZONTAL_PADDING_PERCENT);
                int availablePaddingY = Math.max(0, heightPx - topWorkspacePadding - paddingBottom
                        - (2 * inv.numRows * cellHeightPx) - hotseatBarTopPaddingPx
                        - hotseatBarBottomPaddingPx);
                padding.set(availablePaddingX / 2, topWorkspacePadding + availablePaddingY / 2,
                        availablePaddingX / 2, paddingBottom + availablePaddingY / 2);
            } else {
                // Pad the top and bottom of the workspace with search/hotseat bar sizes
                padding.set(desiredWorkspaceLeftRightMarginPx,
                        topWorkspacePadding,
                        desiredWorkspaceLeftRightMarginPx,
                        paddingBottom);
            }
        }
        return padding;
    }

    /**
     * @return the bounds for which the open folders should be contained within
     */
    public Rect getAbsoluteOpenFolderBounds() {
        if (isVerticalBarLayout()) {
            // Folders should only appear right of the drop target bar and left of the hotseat
            return new Rect(mInsets.left + dropTargetBarSizePx + edgeMarginPx,
                    mInsets.top,
                    mInsets.left + availableWidthPx - hotseatBarSizePx - edgeMarginPx,
                    mInsets.top + availableHeightPx);
        } else {
            // Folders should only appear below the drop target bar and above the hotseat
            return new Rect(mInsets.left,
                    mInsets.top + dropTargetBarSizePx + edgeMarginPx,
                    mInsets.left + availableWidthPx,
                    mInsets.top + availableHeightPx - hotseatBarSizePx
                            - pageIndicatorSizePx - edgeMarginPx);
        }
    }

    private int getWorkspacePageSpacing() {
        if (isVerticalBarLayout() || isLargeTablet) {
            // In landscape mode the page spacing is set to the default.
            return defaultPageSpacingPx;
        } else {
            // In portrait, we want the pages spaced such that there is no
            // overhang of the previous / next page into the current page viewport.
            // We assume symmetrical padding in portrait mode.
            return Math.max(defaultPageSpacingPx, getWorkspacePadding(null).left + 1);
        }
    }

    public static int calculateCellWidth(int width, int countX) {
        return width / countX;
    }
    public static int calculateCellHeight(int height, int countY) {
        return height / countY;
    }

    /**
     * When {@code true}, the device is in landscape mode and the hotseat is on the right column.
     * When {@code false}, either device is in portrait mode or the device is in landscape mode and
     * the hotseat is on the bottom row.
     */
    public boolean isVerticalBarLayout() {
        return isLandscape && transposeLayoutWithOrientation;
    }

    boolean shouldFadeAdjacentWorkspaceScreens() {
        return isVerticalBarLayout() || isLargeTablet;
    }

    private static void setLayoutGravity(View v, int gravity) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
        lp.gravity = gravity;
        v.setLayoutParams(lp);
    }

    public void layout(Launcher launcher, boolean notifyListeners) {
        FrameLayout.LayoutParams lp;
        boolean hasVerticalBarLayout = isVerticalBarLayout();

        // Layout the search bar space
        Point searchBarBounds = getSearchBarDimensForWidgetOpts();
        View searchBar = launcher.getDropTargetBar();
        lp = (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        lp.width = searchBarBounds.x;
        lp.height = searchBarBounds.y;
        lp.topMargin = mInsets.top + edgeMarginPx;
        searchBar.setLayoutParams(lp);

        // Layout the workspace
        PagedView workspace = launcher.getWorkspace();
        Rect workspacePadding = getWorkspacePadding(null);
        workspace.setPadding(workspacePadding.left, workspacePadding.top, workspacePadding.right,
                workspacePadding.bottom);
        workspace.setPageSpacing(getWorkspacePageSpacing());

        AllAppsScrim allAppsScrim = launcher.findViewById(R.id.all_apps_scrim);

        // Layout the hotseat
        Hotseat hotseat = launcher.getHotseat();
        lp = (FrameLayout.LayoutParams) hotseat.getLayoutParams();
        // We want the edges of the hotseat to line up with the edges of the workspace, but the
        // icons in the hotseat are a different size, and so don't line up perfectly. To account for
        // this, we pad the left and right of the hotseat with half of the difference of a workspace
        // cell vs a hotseat cell.
        float workspaceCellWidth = (float) widthPx / inv.numColumns;
        float hotseatCellWidth = (float) widthPx / inv.numHotseatIcons;
        int hotseatAdjustment = Math.round((workspaceCellWidth - hotseatCellWidth) / 2);
        float scrimMargin = launcher.getResources().getDimension(R.dimen.all_apps_scrim_margin);

        if (hasVerticalBarLayout) {
            // Vertical hotseat, on left or right based on the insets
            lp.height = LayoutParams.MATCH_PARENT;
            if (mInsets.left > mInsets.right) {
                lp.gravity = Gravity.LEFT;
                lp.width = hotseatBarSizePx + mInsets.left + hotseatBarSidePaddingPx;
                hotseat.getLayout().setPadding(
                        mInsets.left, mInsets.top, hotseatBarSidePaddingPx, mInsets.bottom);

                setLayoutGravity(launcher.getDropTargetBar(), Gravity.RIGHT);
            } else {
                lp.gravity = Gravity.RIGHT;
                lp.width = hotseatBarSizePx + mInsets.right + hotseatBarSidePaddingPx;
                hotseat.getLayout().setPadding(
                        hotseatBarSidePaddingPx, mInsets.top, mInsets.right, mInsets.bottom);

                setLayoutGravity(launcher.getDropTargetBar(), Gravity.LEFT);
            }

        } else if (isTablet) {
            // Pad the hotseat with the workspace padding calculated above
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarSizePx + mInsets.bottom;
            allAppsScrim.setDrawRegion(lp.height + scrimMargin);

            hotseat.getLayout().setPadding(hotseatAdjustment + workspacePadding.left
                            + cellLayoutPaddingLeftRightPx,
                    hotseatBarTopPaddingPx,
                    hotseatAdjustment + workspacePadding.right + cellLayoutPaddingLeftRightPx,
                    hotseatBarBottomPaddingPx + mInsets.bottom + cellLayoutBottomPaddingPx);
        } else {
            // For phones, layout the hotseat without any bottom margin
            // to ensure that we have space for the folders
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarSizePx + mInsets.bottom;
            allAppsScrim.setDrawRegion(lp.height + scrimMargin);

            hotseat.getLayout().setPadding(hotseatAdjustment + workspacePadding.left
                            + cellLayoutPaddingLeftRightPx,
                    hotseatBarTopPaddingPx,
                    hotseatAdjustment + workspacePadding.right + cellLayoutPaddingLeftRightPx,
                    hotseatBarBottomPaddingPx + mInsets.bottom + cellLayoutBottomPaddingPx);
        }
        hotseat.setLayoutParams(lp);

        // Layout the page indicators
        View pageIndicator = launcher.findViewById(R.id.page_indicator);
        if (pageIndicator != null) {
            lp = (FrameLayout.LayoutParams) pageIndicator.getLayoutParams();
            if (hasVerticalBarLayout) {
                if (mInsets.left > mInsets.right) {
                    lp.leftMargin = hotseatBarSidePaddingPx;
                    lp.rightMargin = mInsets.right;
                    lp.gravity =  Gravity.RIGHT | Gravity.BOTTOM;
                } else {
                    lp.leftMargin = mInsets.left;
                    lp.rightMargin = hotseatBarSidePaddingPx;
                    lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                }
                lp.bottomMargin = workspacePadding.bottom;
            } else {
                // Put the page indicators above the hotseat
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                lp.height = pageIndicatorSizePx;
                lp.bottomMargin = hotseatBarSizePx + mInsets.bottom;
            }
            pageIndicator.setLayoutParams(lp);
        }

        // Layout the AllAppsRecyclerView
        AllAppsContainerView appsView = launcher.findViewById(R.id.apps_view);
        int paddingLeftRight = desiredWorkspaceLeftRightMarginPx + cellLayoutPaddingLeftRightPx;
        appsView.setRecyclerViewSidePadding(paddingLeftRight, paddingLeftRight);

        if (notifyListeners) {
            for (int i = mListeners.size() - 1; i >= 0; i--) {
                mListeners.get(i).onLauncherLayoutChanged();
            }
        }
    }

    public int getCellHeight(@ContainerType int containerType) {
        switch (containerType) {
            case CellLayout.WORKSPACE:
                return cellHeightPx;
            case CellLayout.FOLDER:
                return folderCellHeightPx;
            case CellLayout.HOTSEAT:
                return hotseatCellHeightPx;
            default:
                // ??
                return 0;
        }
    }

    public boolean inMultiWindowMode() {
        return this != inv.landscapeProfile && this != inv.portraitProfile;
    }

    public boolean shouldIgnoreLongPressToOverview(float touchX) {
        boolean touchedLhsEdge = mInsets.left == 0 && touchX < edgeMarginPx;
        boolean touchedRhsEdge = mInsets.right == 0 && touchX > (widthPx - edgeMarginPx);
        return !inMultiWindowMode() && (touchedLhsEdge || touchedRhsEdge);
    }

    private static Context getContext(Context c, int orientation) {
        Configuration context = new Configuration(c.getResources().getConfiguration());
        context.orientation = orientation;
        return c.createConfigurationContext(context);
    }

    public void setShowNotificationDotCount(boolean value) {
	mBadgeRenderer.setShowNotificationDotCount(value);
    }
}
