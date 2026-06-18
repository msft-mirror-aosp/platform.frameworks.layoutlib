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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import android.os.Looper;
import android.os.Looper_Accessor;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamicRenderResourcesTest extends RenderTestBase {
    @BeforeClass
    public static void setUp() {
        Looper.prepareMainLooper();
    }

    @AfterClass
    public static void tearDown() {
        Looper_Accessor.cleanupThread();
    }

    @Test
    public void createDynamicTheme() {
        Map<String, Integer> dynamicColorMap = DynamicRenderResources.createDynamicColorMap(
                "/com/android/layoutlib/testdata/wallpaper1.webp");
        assertNotNull(dynamicColorMap);
        assertEquals(-1, (int)dynamicColorMap.get("system_accent1_0_light"));
        assertEquals(-4632, (int)dynamicColorMap.get("system_accent1_50_light"));
        assertEquals(-1795711, (int)dynamicColorMap.get("system_accent1_300_light"));
        assertEquals(-11394543, (int)dynamicColorMap.get("system_accent1_800_light"));
        assertEquals(-1, (int)dynamicColorMap.get("system_accent2_0_light"));
        assertEquals(-4632, (int)dynamicColorMap.get("system_accent2_50_light"));
        assertEquals(-3497321, (int)dynamicColorMap.get("system_accent2_300_light"));
        assertEquals(-12309982, (int)dynamicColorMap.get("system_accent2_800_light"));
        assertEquals(-1, (int)dynamicColorMap.get("system_accent3_0_light"));
        assertEquals(-4138, (int)dynamicColorMap.get("system_accent3_50_light"));
        assertEquals(-3692695, (int)dynamicColorMap.get("system_accent3_300_light"));
        assertEquals(-12571392, (int)dynamicColorMap.get("system_accent3_800_light"));
        assertEquals(-1, (int)dynamicColorMap.get("system_neutral1_0_light"));
        assertEquals(-135703, (int)dynamicColorMap.get("system_neutral1_50_light"));
        assertEquals(-4806492, (int)dynamicColorMap.get("system_neutral1_300_light"));
        assertEquals(-13160916, (int)dynamicColorMap.get("system_neutral1_800_light"));
        assertEquals(-1, (int)dynamicColorMap.get("system_neutral2_0_light"));
        assertEquals(-4632, (int)dynamicColorMap.get("system_neutral2_50_light"));
        assertEquals(-4413536, (int)dynamicColorMap.get("system_neutral2_300_light"));
        assertEquals(-12833495, (int)dynamicColorMap.get("system_neutral2_800_light"));

        assertEquals(-8890290, (int)dynamicColorMap.get("system_secondary_light"));
        assertEquals(-1589839, (int)dynamicColorMap.get("system_secondary_dark"));
        assertEquals(-12117750, (int)dynamicColorMap.get("system_on_primary_fixed"));
    }
}
