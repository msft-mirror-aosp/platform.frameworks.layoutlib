/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.layoutlib.bridge.bars;

import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.ResourceHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * Navigation bar for gesture navigation.
 * <p>
 * This was copied/adapted from:
 * - packages/SystemUI/src/com/android/systemui/navigationbar/gestural/NavigationHandle.java
 * - packages/SystemUI/src/com/android/systemui/navigationbar/gestural/QuickswitchOrientedNavHandle.java
 */
public class NavigationHandle extends View {
    // Matches navigation_handle_radius in packages/SystemUI/res/values/dimens.xml
    private static final int RADIUS_DP = 2;
    // Matches navigation_handle_bottom in packages/SystemUI/res/values/dimens.xml
    private static final int BOTTOM_DP = 10;
    // Matches navigation_home_handle_width in packages/SystemUI/res/values/dimens.xml
    private static final int WIDTH_DP = 108;
    // Matches navigation_bar_home_handle_light_color in packages/SystemUI/res/values/colors.xml
    private static final String LIGHT_COLOR = "#EBffffff";
    // Matches navigation_bar_home_handle_dark_color in packages/SystemUI/res/values/colors.xml
    private static final String DARK_COLOR = "#99000000";

    private final Paint mPaint = new Paint();
    private final float mRadius;
    private final float mBottom;
    private boolean mRequiresInvalidate;
    private final int mWidth;
    private final RectF mTmpBoundsRectF = new RectF();

    public NavigationHandle(Context context) {
        super(context);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, RADIUS_DP,
                displayMetrics);
        mBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOTTOM_DP,
                displayMetrics);

        mPaint.setAntiAlias(true);
        boolean isLightTheme = ResourceHelper.getBooleanThemeFrameworkAttrValue(
                ((BridgeContext) context).getRenderResources(), "isLightTheme", false);
        if (isLightTheme) {
            mPaint.setColor(ResourceHelper.getColor(DARK_COLOR));
        } else {
            mPaint.setColor(ResourceHelper.getColor(LIGHT_COLOR));
        }
        setFocusable(false);
        mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH_DP,
                displayMetrics);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (alpha > 0f && mRequiresInvalidate) {
            mRequiresInvalidate = false;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(computeHomeHandleBounds(), mRadius, mRadius, mPaint);
    }

    private RectF computeHomeHandleBounds() {
        float height = mRadius * 2;
        float left = getWidth() / 2f - mWidth / 2f;
        float top = (getHeight() - mBottom - height);
        float right = getWidth() / 2f + mWidth / 2f;
        float bottom = top + height;

        mTmpBoundsRectF.set(left, top, right, bottom);
        return mTmpBoundsRectF;
    }
}
