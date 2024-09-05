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
import com.android.systemui.monet.TonalPalette;
import com.android.systemui.monet.dynamiccolor.DynamicColor;
import com.android.systemui.monet.dynamiccolor.MaterialDynamicColors;
import com.android.systemui.monet.hct.Hct;
import com.android.systemui.monet.scheme.DynamicScheme;
import com.android.systemui.monet.scheme.SchemeTonalSpot;
import com.android.tools.layoutlib.annotations.VisibleForTesting;

import android.app.WallpaperColors;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Pair;

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
            extractPalette("accent1", dynamicColorMap, scheme.getAccent1());
            extractPalette("accent2", dynamicColorMap, scheme.getAccent2());
            extractPalette("accent3", dynamicColorMap, scheme.getAccent3());
            extractPalette("neutral1", dynamicColorMap, scheme.getNeutral1());
            extractPalette("neutral2", dynamicColorMap, scheme.getNeutral2());

            Hct sourceColorHct = Hct.fromInt(seed);
            DynamicScheme lightScheme = new SchemeTonalSpot(sourceColorHct, false, 0.0);
            DynamicScheme darkScheme = new SchemeTonalSpot(sourceColorHct, true, 0.0);
            //Themed Colors

            extractDynamicColors(dynamicColorMap, lightScheme, darkScheme, true /* isDark */);
            extractDynamicColors(dynamicColorMap, lightScheme, darkScheme, false /* isDark */);
            extractFixedColors(dynamicColorMap, lightScheme);

            return dynamicColorMap;
        } catch (IllegalArgumentException | IOException ignore) {
            return null;
        }
    }

    /**
     * Builds the dynamic theme from the {@link ColorScheme} copying what is done
     * in {@link ThemeOverlayController#getOverlay}
     */
    private static void extractPalette(String name,
            Map<String, Integer> colorMap, TonalPalette tonalPalette) {
        String resourcePrefix = "system_" + name;
        tonalPalette.getAllShadesMapped().forEach((key, value) -> {
            String resourceName = resourcePrefix + "_" + key;
            int colorValue = ColorUtils.setAlphaComponent(value, 0xFF);
            colorMap.put(resourceName, colorValue);
        });
        colorMap.put(resourcePrefix + "_0", Color.WHITE);
    }

    /**
     * Builds the dynamic theme corresponding to the Material colors, copying what is done
     * in {@link ThemeOverlayController#assignDynamicPaletteToOverlay}
     */
    private static void extractDynamicColors(Map<String, Integer> colorMap,
            DynamicScheme lightScheme, DynamicScheme darkScheme, boolean isDark) {
        String suffix = isDark ? "dark" : "light";
        DynamicScheme scheme = isDark ? darkScheme : lightScheme;
        DynamicColors.ALL_DYNAMIC_COLORS_MAPPED.forEach(p -> {
            String resourceName = "system_" + p.first + "_" + suffix;
            int colorValue = p.second.getArgb(scheme);
            colorMap.put(resourceName, colorValue);
        });
    }

    /**
     * Builds the dynamic theme corresponding to the Material colors, copying what is done
     * in {@link ThemeOverlayController#assignFixedColorsToOverlay}
     */
    private static void extractFixedColors(Map<String, Integer> colorMap,
            DynamicScheme lightScheme) {
        DynamicColors.FIXED_COLORS_MAPPED.forEach(p -> {
            String resourceName = "system_" + p.first;
            int colorValue = p.second.getArgb(lightScheme);
            colorMap.put(resourceName, colorValue);
        });
    }

    private boolean isDynamicColor(ResourceValue resourceValue) {
        if (!resourceValue.isFramework() || resourceValue.getResourceType() != ResourceType.COLOR) {
            return false;
        }
        return mDynamicColorMap.containsKey(resourceValue.getName());
    }

    public boolean hasDynamicColors() {
        return mDynamicColorMap != null;
    }

    // Copied from frameworks/base/packages/SystemUI/src/com/android/systemui/theme/DynamicColors.kt
    private static class DynamicColors {
        private static final MaterialDynamicColors MDC = new MaterialDynamicColors();
        private static final List<Pair<String, DynamicColor>> ALL_DYNAMIC_COLORS_MAPPED = List.of(
            Pair.create("primary_container", MDC.primaryContainer()),
            Pair.create("on_primary_container", MDC.onPrimaryContainer()),
            Pair.create("primary", MDC.primary()),
            Pair.create("on_primary", MDC.onPrimary()),
            Pair.create("secondary_container", MDC.secondaryContainer()),
            Pair.create("on_secondary_container", MDC.onSecondaryContainer()),
            Pair.create("secondary", MDC.secondary()),
            Pair.create("on_secondary", MDC.onSecondary()),
            Pair.create("tertiary_container", MDC.tertiaryContainer()),
            Pair.create("on_tertiary_container", MDC.onTertiaryContainer()),
            Pair.create("tertiary", MDC.tertiary()),
            Pair.create("on_tertiary", MDC.onTertiary()),
            Pair.create("background", MDC.background()),
            Pair.create("on_background", MDC.onBackground()),
            Pair.create("surface", MDC.surface()),
            Pair.create("on_surface", MDC.onSurface()),
            Pair.create("surface_container_low", MDC.surfaceContainerLow()),
            Pair.create("surface_container_lowest", MDC.surfaceContainerLowest()),
            Pair.create("surface_container", MDC.surfaceContainer()),
            Pair.create("surface_container_high", MDC.surfaceContainerHigh()),
            Pair.create("surface_container_highest", MDC.surfaceContainerHighest()),
            Pair.create("surface_bright", MDC.surfaceBright()),
            Pair.create("surface_dim", MDC.surfaceDim()),
            Pair.create("surface_variant", MDC.surfaceVariant()),
            Pair.create("on_surface_variant", MDC.onSurfaceVariant()),
            Pair.create("outline", MDC.outline()),
            Pair.create("outline_variant", MDC.outlineVariant()),
            Pair.create("error", MDC.error()),
            Pair.create("on_error", MDC.onError()),
            Pair.create("error_container", MDC.errorContainer()),
            Pair.create("on_error_container", MDC.onErrorContainer()),
            Pair.create("control_activated", MDC.controlActivated()),
            Pair.create("control_normal", MDC.controlNormal()),
            Pair.create("control_highlight", MDC.controlHighlight()),
            Pair.create("text_primary_inverse", MDC.textPrimaryInverse()),
            Pair.create("text_secondary_and_tertiary_inverse",
                    MDC.textSecondaryAndTertiaryInverse()),
            Pair.create("text_primary_inverse_disable_only",
                    MDC.textPrimaryInverseDisableOnly()),
            Pair.create("text_secondary_and_tertiary_inverse_disabled",
                    MDC.textSecondaryAndTertiaryInverseDisabled()),
            Pair.create("text_hint_inverse", MDC.textHintInverse()),
            Pair.create("palette_key_color_primary", MDC.primaryPaletteKeyColor()),
            Pair.create("palette_key_color_secondary", MDC.secondaryPaletteKeyColor()),
            Pair.create("palette_key_color_tertiary", MDC.tertiaryPaletteKeyColor()),
            Pair.create("palette_key_color_neutral", MDC.neutralPaletteKeyColor()),
            Pair.create("palette_key_color_neutral_variant",
                    MDC.neutralVariantPaletteKeyColor())
        );

        private static final List<Pair<String, DynamicColor>> FIXED_COLORS_MAPPED = List.of(
            Pair.create("primary_fixed", MDC.primaryFixed()),
            Pair.create("primary_fixed_dim", MDC.primaryFixedDim()),
            Pair.create("on_primary_fixed", MDC.onPrimaryFixed()),
            Pair.create("on_primary_fixed_variant", MDC.onPrimaryFixedVariant()),
            Pair.create("secondary_fixed", MDC.secondaryFixed()),
            Pair.create("secondary_fixed_dim", MDC.secondaryFixedDim()),
            Pair.create("on_secondary_fixed", MDC.onSecondaryFixed()),
            Pair.create("on_secondary_fixed_variant", MDC.onSecondaryFixedVariant()),
            Pair.create("tertiary_fixed", MDC.tertiaryFixed()),
            Pair.create("tertiary_fixed_dim", MDC.tertiaryFixedDim()),
            Pair.create("on_tertiary_fixed", MDC.onTertiaryFixed()),
            Pair.create("on_tertiary_fixed_variant", MDC.onTertiaryFixedVariant())
        );
    }
}
