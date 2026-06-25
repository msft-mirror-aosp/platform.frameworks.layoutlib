/*
 * Copyright (C) 2026 The Android Open Source Project
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

import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.ViewInfo;
import com.android.layoutlib.bridge.intensive.setup.LayoutPullParser;
import com.android.layoutlib.bridge.intensive.setup.LayoutlibBridgeClientCallback;


import org.junit.After;
import org.junit.Test;

import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for interactive rendering capabilities in Layoutlib.
 */
public class InteractiveRenderTests extends InteractiveTestBase {

    private boolean mButtonClicked = false;

    @After
    public void cleanup() {
        endSession();
    }

    @Test
    public void testButtonClickInteraction() throws ClassNotFoundException {
        // 1. Create layout with a Button
        String layoutStr = "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "              android:padding=\"16dp\"\n" +
                "              android:orientation=\"vertical\"\n" +
                "              android:layout_width=\"match_parent\"\n" +
                "              android:layout_height=\"match_parent\">\n" +
                "    <Button\n" +
                "        android:id=\"@+id/click_button\"\n" +
                "        android:text=\"Click Me\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"/>\n" +
                "</LinearLayout>";

        LayoutPullParser parser = LayoutPullParser.createFromString(layoutStr);

        LayoutlibBridgeClientCallback layoutLibCallback =
                new LayoutlibBridgeClientCallback(getLogger(), mDefaultClassLoader,
                        "com.android.layoutlib.test.myapplication");
        layoutLibCallback.initResources();

        SessionParams params = getSessionParamsBuilder()
                .setParser(parser)
                .setCallback(layoutLibCallback)
                .build();

        // 2. Start the interactive session
        startSession(params);

        // 3. Add click listener from within session context
        mSession.execute(() -> {
            List<ViewInfo> roots = mSession.getRootViews();
            assertFalse("No root views found", roots.isEmpty());

            ViewInfo rootInfo = roots.getFirst();
            assertFalse("Root view has no children", rootInfo.getChildren().isEmpty());

            ViewInfo buttonInfo = rootInfo.getChildren().getFirst(); // Button is the first child
            assertNotNull("ButtonInfo has no view object", buttonInfo.getViewObject());

            Button button = (Button) buttonInfo.getViewObject();
            button.setOnClickListener(v -> mButtonClicked = true);
        });

        // 4. Simulate a click event
        // coordinates (180, 288) hit the center of the button
        dispatchTouchEvent(RenderSession.TouchEventType.PRESS, 180, 288);
        advanceTime(TimeUnit.MILLISECONDS.toNanos(100));
        dispatchTouchEvent(RenderSession.TouchEventType.RELEASE, 180, 288);
        advanceTime(TimeUnit.MILLISECONDS.toNanos(100));

        // 5. Verify listener was called
        assertTrue("Button was not clicked", mButtonClicked);
    }
}

