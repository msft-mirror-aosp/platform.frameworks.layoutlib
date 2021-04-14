/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.tools.idea.validator;

import com.android.tools.idea.validator.ValidatorResult.Metric;
import com.android.tools.layoutlib.annotations.NotNull;

import android.annotation.NonNull;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.Image;
import javax.imageio.ImageIO;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.io.File.separatorChar;

/**
 * Image implementation to be used in Accessibility Test Framework.
 */
public class AtfBufferedImage implements Image {

    // The source buffered image, expected to contain the full screen rendered image of the layout.
    @NotNull private final BufferedImage mBufferedImage;
    // Metrics to be returned
    @NotNull private final Metric mMetric;

    private final int mLeft;
    private final int mTop;
    private final int mWidth;
    private final int mHeight;

    AtfBufferedImage(@NotNull BufferedImage image, @NotNull Metric metric) {
        assert(image.getType() == TYPE_INT_ARGB);
        mBufferedImage = image;
        mMetric = metric;
        mWidth = mBufferedImage.getWidth();
        mHeight = mBufferedImage.getHeight();
        mLeft = 0;
        mTop = 0;
    }

    private AtfBufferedImage(
            @NotNull BufferedImage image,
            @NotNull Metric metric,
            int left,
            int top,
            int width,
            int height) {
        mBufferedImage = image;
        mMetric = metric;
        mLeft = left;
        mTop = top;
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    @NotNull
    public Image crop(int left, int top, int width, int height) {
        // Here do saving.
        return new AtfBufferedImage(mBufferedImage, mMetric, left, top, width, height);
    }

    @Override
    @NotNull
    public int[] getPixels() {
        // ATF unfortunately writes in-place on returned int[] for color analysis.
        // It must return copied list otherwise it won't work.
        BufferedImage cropped = mBufferedImage.getSubimage(mLeft, mTop, mWidth, mHeight);
        WritableRaster raster = cropped.copyData(
                cropped.getRaster().createCompatibleWritableRaster());
        int[] toReturn = ((DataBufferInt) raster.getDataBuffer()).getData();
        mMetric.mImageMemoryBytes += toReturn.length * 4;

        if (LayoutValidator.shouldSaveCroppedImages()) {
            saveImage(cropped);
        }

        return toReturn;
    }

    private void saveImage(BufferedImage image) {
        try {
            String name = "img_for_atf_bounds_WxH_" + image.getWidth() + "x" + image.getHeight();

            File output = new File(getDebugDir(), name);
            if (output.exists()) {
                output.delete();
            }
            ImageIO.write(image, "PNG", output);
        } catch (IOException ioe) {
            mMetric.mErrorMessage = ioe.getMessage();
        }
    }

    @NonNull
    private File getDebugDir() {
        File failureDir;
        String failureDirString = System.getProperty("debug.dir");
        if (failureDirString != null) {
            failureDir = new File(failureDirString);
        } else {
            String workingDirString = System.getProperty("user.dir");
            failureDir = new File(workingDirString, "out/debugs");
        }

        failureDir.mkdirs();
        return failureDir;
    }
}
