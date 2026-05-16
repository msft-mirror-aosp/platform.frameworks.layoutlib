/*
 * Copyright (C) 2025 The Android Open Source Project
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


package android.provider;

import com.android.ide.common.rendering.api.SessionParams;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContentResolver;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.android.RenderTestBase;
import com.android.layoutlib.bridge.impl.RenderAction;
import com.android.layoutlib.bridge.intensive.LayoutLibTestCallback;
import com.android.layoutlib.bridge.intensive.setup.LayoutPullParser;

import org.junit.BeforeClass;
import org.junit.Test;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;

import static org.junit.Assert.assertEquals;

public class Settings_System_DelegateTest extends RenderTestBase {
    private String name = "Setting Name";
    private String value = "value";
    private int intValue = 5;

    @BeforeClass
    public static void setUp() {
        Bridge.prepareThread();
    }

    @Test
    public void fullCircleString() {
        // Setup
        // Create the layout pull parser for our resources (empty.xml can not be part of the test
        // app as it won't compile).
        LayoutPullParser parser = LayoutPullParser.createFromPath("/empty.xml");
        // Create LayoutLibCallback.
        LayoutLibTestCallback layoutLibCallback =
                new LayoutLibTestCallback(getLogger(), mDefaultClassLoader);
        SessionParams params =
                getSessionParamsBuilder().setParser(parser).setCallback(layoutLibCallback).setTheme(
                        "Theme.Material", false).build();
        DisplayMetrics metrics = new DisplayMetrics();
        Configuration configuration = RenderAction.getConfiguration(params);
        BridgeContext context =
                new BridgeContext(params.getProjectKey(), metrics, params.getResources(),
                        params.getAssets(), params.getLayoutlibCallback(), configuration,
                        params.getTargetSdkVersion(), params.isRtlSupported());
        context.initResources(params.getAssets());
        ContentResolver cr = new BridgeContentResolver(context);

        //String round trip
        Settings_System_Delegate.putStringForUser(cr, name, value, "", false, 0, false);
        String retrievedValue = Settings_System_Delegate.getStringForUser(cr, name, 5);
        assertEquals(retrievedValue, value);

        // int round trip
        Settings.Global.putInt(cr, name, intValue);
        try {
            assertEquals(intValue, Settings.System.getInt(cr, name));
        } catch (SettingNotFoundException e) {
            throw new RuntimeException(e);
        }

        // long round trip
        Settings.Global.putLong(cr, name, intValue);
        try {
            assertEquals(intValue, Settings.System.getLong(cr, name));
        } catch (SettingNotFoundException e) {
            throw new RuntimeException(e);
        }

        // float round trip
        Settings.Global.putFloat(cr, name, (float) intValue);
        try {
            assertEquals((float) intValue, Settings.System.getFloat(cr, name), 0);
        } catch (SettingNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            context.disposeResources();
        }

    }
}

