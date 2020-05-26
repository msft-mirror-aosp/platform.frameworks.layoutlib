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

import com.android.ide.common.rendering.api.SessionParams;
import com.android.layoutlib.bridge.intensive.RenderTestBase;
import com.android.layoutlib.bridge.intensive.setup.ConfigGenerator;
import com.android.layoutlib.bridge.intensive.setup.LayoutLibTestCallback;
import com.android.layoutlib.bridge.intensive.setup.LayoutPullParser;
import com.android.tools.idea.validator.ValidatorData.Issue;
import com.android.tools.idea.validator.ValidatorData.Level;
import com.android.tools.idea.validator.ValidatorData.Type;

import org.junit.Test;

import android.view.View;

import static org.junit.Assert.assertEquals;

public class LayoutValidatorTests extends RenderTestBase {

    @Test
    public void testRenderAndVerify() throws Exception {
        LayoutPullParser parser = createParserFromPath("a11y_test1.xml");
        LayoutLibTestCallback layoutLibCallback =
                new LayoutLibTestCallback(getLogger(), mDefaultClassLoader);
        layoutLibCallback.initResources();
        SessionParams params = getSessionParamsBuilder()
                .setParser(parser)
                .setConfigGenerator(ConfigGenerator.NEXUS_5)
                .setCallback(layoutLibCallback)
                .disableDecoration()
                .enableLayoutValidation()
                .build();

        renderAndVerify(params, "a11y_test1.png");
    }

    @Test
    public void testValidation() throws Exception {
        LayoutPullParser parser = createParserFromPath("a11y_test1.xml");
        LayoutLibTestCallback layoutLibCallback =
                new LayoutLibTestCallback(getLogger(), mDefaultClassLoader);
        layoutLibCallback.initResources();
        SessionParams params = getSessionParamsBuilder()
                .setParser(parser)
                .setConfigGenerator(ConfigGenerator.NEXUS_5)
                .setCallback(layoutLibCallback)
                .disableDecoration()
                .enableLayoutValidation()
                .build();

        render(sBridge, params, -1, session -> {
            ValidatorResult result = LayoutValidator
                    .validate(((View) session.getRootViews().get(0).getViewObject()));
            assertEquals(3, result.getIssues().size());
            for (Issue issue : result.getIssues()) {
                assertEquals(Type.ACCESSIBILITY, issue.mType);
                assertEquals(Level.ERROR, issue.mLevel);
            }

            assertEquals("This item may not have a label readable by screen readers.",
                    result.getIssues().get(0).mMsg);
            assertEquals("This item's size is 10dp x 10dp. Consider making this touch target " +
                            "48dp wide and 48dp high or larger.",
                    result.getIssues().get(1).mMsg);
            assertEquals("The item's text contrast ratio is 1.00. This ratio is based on a text color " +
                            "of #000000 and background color of #000000. Consider increasing this item's" +
                            " text contrast ratio to 4.50 or greater.",
                    result.getIssues().get(2).mMsg);
            // TODO: It should recognize 10dp x 10dp button. Investigate why it's not.
        });
    }
}
