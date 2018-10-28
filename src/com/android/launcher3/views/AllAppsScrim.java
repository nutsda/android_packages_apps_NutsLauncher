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
package com.android.launcher3.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.dynamicui.WallpaperColorInfo;
import com.android.launcher3.graphics.NinePatchDrawHelper;
import com.android.launcher3.graphics.ShadowGenerator;
import com.android.launcher3.util.Themes;

import static com.android.launcher3.graphics.NinePatchDrawHelper.EXTENSION_PX;

public class AllAppsScrim extends View implements WallpaperColorInfo.OnChangeListener {

    private static final int MAX_ALPHA = 235;
    private static final int MIN_ALPHA_PORTRAIT = 100;
    private static final int MIN_ALPHA_LANDSCAPE = MAX_ALPHA;

    protected final WallpaperColorInfo mWallpaperColorInfo;
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float mRadius;
    private final int mMinAlpha;
    private final int mAlphaRange;
    private final int mScrimColor;

    private final float mShadowBlur;
    private final Bitmap mShadowBitmap;

    private final NinePatchDrawHelper mShadowHelper = new NinePatchDrawHelper();

    private int mFillAlpha;

    private float mDrawHeight;
    private float mTranslateY;

    public AllAppsScrim(Context context) {
        this(context, null);
    }

    public AllAppsScrim(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsScrim(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mWallpaperColorInfo = WallpaperColorInfo.getInstance(context);
        mScrimColor = Themes.getAttrColor(context, R.attr.allAppsScrimColor);
        mRadius = getResources().getDimension(R.dimen.all_apps_scrim_radius);
        mShadowBlur = getResources().getDimension(R.dimen.all_apps_scrim_blur);

        Launcher launcher = Launcher.getLauncher(context);
        mFillAlpha = mMinAlpha = launcher.getDeviceProfile().isVerticalBarLayout()
                ? MIN_ALPHA_LANDSCAPE : MIN_ALPHA_PORTRAIT;
        mAlphaRange = MAX_ALPHA - mMinAlpha;
        mShadowBitmap = generateShadowBitmap();

        updateColors(mWallpaperColorInfo);
    }

    private Bitmap generateShadowBitmap() {
        float curveBot = mRadius + mShadowBlur;

        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(Color.TRANSPARENT);
        builder.radius = mRadius;
        builder.shadowBlur = mShadowBlur;

        // Create the bitmap such that only the top half is drawn in the bitmap.
        int bitmapHeight = Math.round(curveBot);
        int bitmapWidth = bitmapHeight + EXTENSION_PX;
        Bitmap result = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

        builder.bounds.set(0, mShadowBlur, bitmapWidth, 2 * curveBot + EXTENSION_PX);
        builder.drawShadow(new Canvas(result));
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWallpaperColorInfo.addOnChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWallpaperColorInfo.removeOnChangeListener(this);
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo info) {
        updateColors(info);
        invalidate();
    }

    private void updateColors(WallpaperColorInfo info) {
        mFillPaint.setColor(ColorUtils.compositeColors(mScrimColor,
                ColorUtils.compositeColors(mScrimColor, info.getMainColor())));
        mFillPaint.setAlpha(mFillAlpha);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float edgeTop = getHeight() + mTranslateY - mDrawHeight;

        mShadowHelper.draw(mShadowBitmap, canvas, 0, edgeTop - mShadowBlur, getWidth());
        canvas.drawRoundRect(0, edgeTop, getWidth(),
                getHeight() + mRadius, mRadius, mRadius, mFillPaint);
    }

    public void setProgress(float translateY, float alpha) {
        mFillAlpha = Math.round(alpha * mAlphaRange + mMinAlpha);
        mFillPaint.setAlpha(mFillAlpha);

        mTranslateY = translateY;

        invalidate();
    }

    public void setDrawRegion(float height) {
        mDrawHeight = height;
    }
}
