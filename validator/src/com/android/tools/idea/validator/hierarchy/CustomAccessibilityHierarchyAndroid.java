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

package com.android.tools.idea.validator.hierarchy;

import com.android.tools.layoutlib.annotations.Nullable;

import android.view.View;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DeviceStateAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElementAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElementAndroid;
import java.util.List;

/**
 * {@link AccessibilityHierarchyAndroid} which is customized for Android Studio integration.
 * It supports access to support library which is not directly available in layoutlib.
 */
public class CustomAccessibilityHierarchyAndroid extends AccessibilityHierarchyAndroid {

    protected CustomAccessibilityHierarchyAndroid(
            DeviceStateAndroid deviceState,
            List<WindowHierarchyElementAndroid> windowHierarchyElements,
            WindowHierarchyElementAndroid activeWindow,
            ViewElementClassNamesAndroid viewElementClassNames) {
        super(deviceState, windowHierarchyElements, activeWindow, viewElementClassNames);
    }

    /**
     * A {@link AccessibilityHierarchyAndroid.BuilderAndroid} that builds a customized {@link
     * AccessibilityHierarchyAndroid} from a {@link View}.
     */
    public static class CustomBuilderAndroid extends BuilderAndroid {

        public CustomBuilderAndroid(View fromView) {
            this.fromRootView = fromView;
        }

        @Override
        protected
        WindowHierarchyElementAndroid.BuilderAndroid createWindowHierarchyElementAndroidBuilder(
                int id, View fromRootView) {
            return new CustomWindowHierarchyElementAndroid.CustomBuilderAndroid(0, fromRootView);
        }

        @Override
        protected ViewElementClassNamesAndroid createViewElementClassNamesAndroid(
                List<WindowHierarchyElementAndroid> windowHierarchyElements) {
            return new CustomViewElementClassNamesAndroid(windowHierarchyElements);
        }
    }

    /** {@link ViewElementClassNamesAndroid} which is customized for Android Studio integration. */
    protected static class CustomViewElementClassNamesAndroid extends ViewElementClassNamesAndroid {

        public CustomViewElementClassNamesAndroid(
                List<WindowHierarchyElementAndroid> windowHierarchyElements) {
            super(windowHierarchyElements);
        }

        @Override
        protected @Nullable Class<?> getClassByName(
                ViewHierarchyElementAndroid view,
                String className) {

            Class<?> toReturn = super.getClassByName(view, className);
            if (toReturn == null) {
                return CustomHierarchyHelper.getClassByName(className);
            }
            return toReturn;
        }
    }
}