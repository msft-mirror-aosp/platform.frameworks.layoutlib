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

package com.android.layoutlib.bridge;

import org.junit.Before;
import java.util.Locale;
import android.view.View;
import com.android.layoutlib.bridge.intensive.BridgeClient;

public class RenderTestBase extends BridgeClient {
    public static final String S_PACKAGE_NAME = "com.android.layoutlib.test.myapplication";

    public String getAppTestDir() {
        return "testApp/MyApplication";
    }

    public String getAppTestRes() {
        return getAppTestDir() + "/src/main/res";
    }

    public String getAppResources() {
        return getAppTestRes();
    }

    public String getAppTestAsset() {
        return getAppTestDir() + "/src/main/assets/";
    }

    public String getAppClassesLocation() {
        return getAppTestDir()
                + "/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes/";
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


    @Before
    public void initPackageName() {
        setPackageName(S_PACKAGE_NAME);
    }

}
