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

package android.view.shadow;

import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.tools.layoutlib.annotations.VisibleForTesting;

import android.graphics.Bitmap;
import android.view.math.Math3DHelper;

/**
 * Generate spot shadow bitmap.
 */
public class SpotShadowBitmapGenerator {

    private final ShadowConfig mShadowConfig;
    private final TriangleBuffer mTriangle;
    private float[] mStrips;
    private float[] mLightSources;

    public SpotShadowBitmapGenerator(ShadowConfig config) {
        mTriangle = new TriangleBuffer();
        mShadowConfig = config;
        // For now assume no change to the world size
        mTriangle.setSize(config.getWidth(), config.getHeight(), 0);
    }

    /**
     * Populate the shadow bitmap.
     */
    public void populateShadow() {
        try {
            mLightSources = SpotShadowVertexCalculator.calculateLight(
                    mShadowConfig.getLightRadius(),
                    mShadowConfig.getLightSourcePoints(),
                    mShadowConfig.getLightCoord()[0],
                    mShadowConfig.getLightCoord()[1],
                    mShadowConfig.getLightCoord()[2]);

            mStrips = new float[3 * SpotShadowVertexCalculator.getStripSize(
                    mShadowConfig.getRays(),
                    mShadowConfig.getLayers())];

            if (SpotShadowVertexCalculator.calculateShadow(
                    mLightSources,
                    mShadowConfig.getLightSourcePoints(),
                    mShadowConfig.getPoly(),
                    mShadowConfig.getPolyLength(),
                    mShadowConfig.getRays(),
                    mShadowConfig.getLayers(),
                    mShadowConfig.getShadowStrength(),
                    mStrips) != 1) {
                return;
            }

            mTriangle.drawTriangles(mStrips, mShadowConfig.getShadowStrength());

        } catch (IndexOutOfBoundsException|ArithmeticException mathError) {
            Bridge.getLog().warning(LayoutLog.TAG_INFO,  "Arithmetic error while drawing shadow",
                    mathError);
        } catch (Exception ex) {
            Bridge.getLog().warning(LayoutLog.TAG_INFO,  "Error while drawing shadow",
                    ex);
        }
    }

    /**
     * @return true if generated shadow poly is valid. False otherwise.
     */
    public boolean validate() {
        return mStrips != null && mStrips.length >= 9;
    }

    /**
     * @return the bitmap of shadow after it's populated
     */
    public Bitmap getBitmap() {
        return mTriangle.getImage();
    }

    @VisibleForTesting
    public float[] getStrips() {
        return mStrips;
    }

    @VisibleForTesting
    public void updateLightSource(float x, float y) {
        mShadowConfig.setLightCoord(x, y);
    }
}
