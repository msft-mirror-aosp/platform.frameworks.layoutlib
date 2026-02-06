/*
 * Copyright (C) 2014 The Android Open Source Project
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

import org.jetbrains.annotations.NotNull;
import org.junit.Before;

import java.util.Locale;

import com.android.layoutlib.bridge.intensive.BridgeClient;

public class RenderTestBase extends BridgeClient {
    private static final String RESOURCE_JAR_PROPERTY = "test_res.jar";
    private static final String ASSET_JAR_PROPERTY = "test_asset.jar";
    private static final String S_PACKAGE_NAME = "com.android.layoutlib.test.myapplication";

    public String getAppTestDir() {
        return "testApp/MyApplication";
    }

    public String getAppTestRes() {
        String appTestRes = System.getProperty(RESOURCE_JAR_PROPERTY);
        if (appTestRes == null) {
            // Fallback for IDE execution where system properties might not be set.
            // Attempts to locate the jar in the standard build output structure relative to platform res dir.
            appTestRes = getTestBuildOutput() +
                    "/layoutlib-test-res/linux_glibc_common/gen/layoutlib-test-res.jar";
        }
        return appTestRes;
    }

    public String getAppResources() {
        return getAppTestDir() + "/src/main/res";
    }

    public String getAppTestAsset() {
        String appTestAsset = System.getProperty(ASSET_JAR_PROPERTY);
        if (appTestAsset == null) {
            // Fallback for IDE execution where system properties might not be set.
            appTestAsset = getTestBuildOutput() + "layoutlib-test-asset/linux_glibc_common/gen" +
                    "/layoutlib-test-asset.jar";
        }
        return appTestAsset;
    }

    public String getAppClassesLocation() {
        return getAppTestDir() +
                "/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/";
    }

    public String getAppGoldenDir() {
        String goldenImagePath = getAppTestDir();
        boolean isMac = System.getProperty("os.name").toLowerCase(Locale.US).contains("mac");
        if (isMac) {
            goldenImagePath += "/golden-mac/";
        } else {
            goldenImagePath += "/golden/";
        }
        return goldenImagePath;
    }

    @NotNull
    private static String getTestBuildOutput() {
        return PLATFORM_RES_DIR +
                "/../../../../../soong/.intermediates/frameworks/layoutlib/bridge/tests/";
    }

    @Before
    public void initPackageName() {
        setPackageName(S_PACKAGE_NAME);
    }

}
