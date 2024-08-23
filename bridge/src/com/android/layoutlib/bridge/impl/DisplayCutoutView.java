/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.layoutlib.bridge.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.DisplayCutout;
import android.view.DisplayCutout.BoundsPosition;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.DisplayCutout.BOUNDS_POSITION_BOTTOM;
import static android.view.DisplayCutout.BOUNDS_POSITION_LEFT;
import static android.view.DisplayCutout.BOUNDS_POSITION_RIGHT;
import static android.view.DisplayCutout.BOUNDS_POSITION_TOP;

class DisplayCutoutView extends View {
    private final DisplayInfo mInfo = new DisplayInfo();
    private final Paint mPaint = new Paint();
    private final List<Rect> mBounds = new ArrayList<>();
    private final Rect mBoundingRect = new Rect();
    private final Path cutoutPath = new Path();
    private final int[] mLocation = new int[2];
    private final int mRotation;

    private int mColor = Color.BLACK;
    @BoundsPosition
    private final int mInitialPosition;
    private int mPosition;

    public DisplayCutoutView(Context context, @BoundsPosition int pos) {
        super(context);
        mInitialPosition = pos;
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        mRotation = mInfo.rotation;
    }

    public void setColor(int color) {
        if (color == mColor) {
            return;
        }
        mColor = color;
        mPaint.setColor(mColor);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateCutout();
    }

    private void updateCutout() {
        if (!isAttachedToWindow()) {
            return;
        }
        mPosition = getBoundPositionFromRotation(mInitialPosition, mRotation);
        requestLayout();
        getDisplay().getDisplayInfo(mInfo);
        mBounds.clear();
        mBoundingRect.setEmpty();
        cutoutPath.reset();
        int newVisible;
        if (hasCutout()) {
            mBounds.addAll(mInfo.displayCutout.getBoundingRects());
            localBounds(mBoundingRect);
            updateGravity();
            updateBoundingPath();
            invalidate();
            newVisible = VISIBLE;
        } else {
            newVisible = GONE;
        }
        if (newVisible != getVisibility()) {
            setVisibility(newVisible);
        }
    }

    private static int getBoundPositionFromRotation(@BoundsPosition int pos, int rotation) {
        return (pos - rotation) < 0
                ? pos - rotation + DisplayCutout.BOUNDS_POSITION_LENGTH
                : pos - rotation;
    }

    private void updateBoundingPath() {
        final Path path = mInfo.displayCutout.getCutoutPath();
        if (path != null) {
            cutoutPath.set(path);
        } else {
            cutoutPath.reset();
        }
    }

    private void updateGravity() {
        LayoutParams lp = getLayoutParams();
        if (lp instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) lp;
            int newGravity = getGravity(mInfo.displayCutout);
            if (flp.gravity != newGravity) {
                flp.gravity = newGravity;
                setLayoutParams(flp);
            }
        }
    }

    private boolean hasCutout() {
        final DisplayCutout displayCutout = mInfo.displayCutout;
        if (displayCutout == null) {
            return false;
        }

        if (mPosition == BOUNDS_POSITION_LEFT) {
            return !displayCutout.getBoundingRectLeft().isEmpty();
        } else if (mPosition == BOUNDS_POSITION_TOP) {
            return !displayCutout.getBoundingRectTop().isEmpty();
        } else if (mPosition == BOUNDS_POSITION_BOTTOM) {
            return !displayCutout.getBoundingRectBottom().isEmpty();
        } else if (mPosition == BOUNDS_POSITION_RIGHT) {
            return !displayCutout.getBoundingRectRight().isEmpty();
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mBounds.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setMeasuredDimension(
                resolveSizeAndState(mBoundingRect.width(), widthMeasureSpec, 0),
                resolveSizeAndState(mBoundingRect.height(), heightMeasureSpec, 0));
    }

    private static void boundsFromDirection(DisplayCutout displayCutout, int gravity, Rect out) {
        switch (gravity) {
            case Gravity.TOP:
                out.set(displayCutout.getBoundingRectTop());
                break;
            case Gravity.LEFT:
                out.set(displayCutout.getBoundingRectLeft());
                break;
            case Gravity.BOTTOM:
                out.set(displayCutout.getBoundingRectBottom());
                break;
            case Gravity.RIGHT:
                out.set(displayCutout.getBoundingRectRight());
                break;
            default:
                out.setEmpty();
        }
    }

    private void localBounds(Rect out) {
        DisplayCutout displayCutout = mInfo.displayCutout;
        boundsFromDirection(displayCutout, getGravity(displayCutout), out);
    }

    private int getGravity(DisplayCutout displayCutout) {
        if (mPosition == BOUNDS_POSITION_LEFT) {
            if (!displayCutout.getBoundingRectLeft().isEmpty()) {
                return Gravity.LEFT;
            }
        } else if (mPosition == BOUNDS_POSITION_TOP) {
            if (!displayCutout.getBoundingRectTop().isEmpty()) {
                return Gravity.TOP;
            }
        } else if (mPosition == BOUNDS_POSITION_BOTTOM) {
            if (!displayCutout.getBoundingRectBottom().isEmpty()) {
                return Gravity.BOTTOM;
            }
        } else if (mPosition == BOUNDS_POSITION_RIGHT) {
            if (!displayCutout.getBoundingRectRight().isEmpty()) {
                return Gravity.RIGHT;
            }
        }
        return Gravity.NO_GRAVITY;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        getLocationOnScreen(mLocation);
        canvas.translate(-mLocation[0], -mLocation[1]);
        canvas.drawPath(cutoutPath, mPaint);
        canvas.restore();
    }
}