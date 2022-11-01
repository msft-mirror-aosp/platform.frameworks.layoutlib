/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.layoutlib.bridge.android;

import com.android.ide.common.rendering.api.ILayoutLog;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.ResourceValueImpl;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.internal.graphics.ColorUtils;
import com.android.resources.ResourceType;
import com.android.systemui.monet.ColorScheme;
import com.android.systemui.monet.Style;
import com.android.tools.layoutlib.annotations.VisibleForTesting;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for RenderResources that allows overriding default system colors
 * when using dynamic theming.
 */
public class DynamicRenderResources extends RenderResources {
    private final RenderResources mBaseResources;
    private Map<String, Integer> mDynamicColorMap;

    public DynamicRenderResources(RenderResources baseResources) {
        mBaseResources = baseResources;
    }

    @Override
    public void setLogger(ILayoutLog logger) {
        mBaseResources.setLogger(logger);
    }

    @Override
    public StyleResourceValue getDefaultTheme() {
        return mBaseResources.getDefaultTheme();
    }

    @Override
    public void applyStyle(StyleResourceValue theme, boolean useAsPrimary) {
        mBaseResources.applyStyle(theme, useAsPrimary);
    }

    @Override
    public void clearStyles() {
        mBaseResources.clearStyles();
    }

    @Override
    public List<StyleResourceValue> getAllThemes() {
        return mBaseResources.getAllThemes();
    }

    @Override
    public ResourceValue findItemInTheme(ResourceReference attr) {
        ResourceValue baseValue = mBaseResources.findItemInTheme(attr);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue findItemInStyle(StyleResourceValue style, ResourceReference attr) {
        ResourceValue baseValue = mBaseResources.findItemInStyle(style, attr);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue findResValue(String reference, boolean forceFrameworkOnly) {
        ResourceValue baseValue = mBaseResources.findResValue(reference, forceFrameworkOnly);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue dereference(ResourceValue resourceValue) {
        ResourceValue baseValue = mBaseResources.dereference(resourceValue);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue getUnresolvedResource(ResourceReference reference) {
        ResourceValue baseValue = mBaseResources.getUnresolvedResource(reference);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue getResolvedResource(ResourceReference reference) {
        ResourceValue baseValue = mBaseResources.getResolvedResource(reference);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public ResourceValue resolveResValue(ResourceValue value) {
        ResourceValue baseValue = mBaseResources.resolveResValue(value);
        return resolveDynamicColors(baseValue);
    }

    @Override
    public StyleResourceValue getParent(StyleResourceValue style) {
        return mBaseResources.getParent(style);
    }

    @Override
    public StyleResourceValue getStyle(ResourceReference reference) {
        return mBaseResources.getStyle(reference);
    }

    private ResourceValue resolveDynamicColors(ResourceValue baseValue) {
        if (hasDynamicColors() && baseValue != null && isDynamicColor(baseValue)) {
            int dynamicColor = mDynamicColorMap.get(baseValue.getName());
            String colorHex = "#" + Integer.toHexString(dynamicColor).substring(2);
            return new ResourceValueImpl(baseValue.getNamespace(), baseValue.getResourceType(),
                    baseValue.getName(), colorHex);
        }
        return baseValue;
    }

    public void setWallpaper(String wallpaperPath, boolean isNightMode) {
        if (wallpaperPath == null) {
            mDynamicColorMap = null;
            return;
        }
        mDynamicColorMap = createDynamicColorMap(wallpaperPath, isNightMode);
    }

    /**
     * Extracts colors from the wallpaper and creates the corresponding dynamic theme.
     * It uses the main wallpaper color and the {@link Style#TONAL_SPOT} style.
     *
     * @param wallpaperPath path of the wallpaper resource to use
     * @param isNightMode whether to use night mode or not
     *
     * @return map of system color names to their dynamic values
     */
    @VisibleForTesting
    static Map<String, Integer> createDynamicColorMap(String wallpaperPath, boolean isNightMode) {
        try (InputStream stream = DynamicRenderResources.class.getResourceAsStream(wallpaperPath)) {
            Bitmap wallpaper = BitmapFactory.decodeStream(stream);
            if (wallpaper == null) {
                return null;
            }
            WallpaperColors wallpaperColors = WallpaperColors.fromBitmap(wallpaper);
            int seed = ColorScheme.getSeedColor(wallpaperColors);
            ColorScheme scheme = new ColorScheme(seed, isNightMode);
            Map<String, Integer> dynamicColorMap = new HashMap<>();
            int paletteSize = scheme.getAccent1().size();
            extractPalette(scheme.getAllAccentColors(), "accent", paletteSize, dynamicColorMap);
            extractPalette(scheme.getAllNeutralColors(), "neutral", paletteSize, dynamicColorMap);
            return dynamicColorMap;
        } catch (IllegalArgumentException | IOException ignore) {
            return null;
        }
    }

    /**
     * Builds the dynamic theme from the {@link ColorScheme} copying what is done
     * in {@link ThemeOverlayController#getOverlay}
     */
    private static void extractPalette(List<Integer> shades, String name, int paletteSize,
            Map<String, Integer> colorMap) {
        for (int i = 0; i < shades.size(); i++) {
            int luminosity = i % paletteSize;
            int paletteIndex = i / paletteSize + 1;
            String resourceName;
            String baseResourceName = "system_" + name + paletteIndex;
            switch (luminosity) {
                case 0:
                    resourceName = baseResourceName + "_0";
                    colorMap.put(resourceName, Color.WHITE);
                    resourceName = baseResourceName + "_10";
                    break;
                case 1:
                    resourceName = baseResourceName + "_50";
                    break;
                default:
                    resourceName = baseResourceName + "_" + (luminosity - 1) + "00";
            }
            colorMap.put(resourceName, ColorUtils.setAlphaComponent(shades.get(i), 0xFF));
        }
    }

    private static boolean isDynamicColor(ResourceValue resourceValue) {
        if (!resourceValue.isFramework() || resourceValue.getResourceType() != ResourceType.COLOR) {
            return false;
        }
        return resourceValue.getName().startsWith("system_accent")
                || resourceValue.getName().startsWith("system_neutral");
    }

    public boolean hasDynamicColors() {
        return mDynamicColorMap != null;
    }
}
