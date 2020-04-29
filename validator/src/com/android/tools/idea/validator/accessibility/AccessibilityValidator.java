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

package com.android.tools.idea.validator.accessibility;

import com.android.tools.idea.validator.ValidatorData;
import com.android.tools.idea.validator.ValidatorData.Fix;
import com.android.tools.idea.validator.ValidatorData.Issue;
import com.android.tools.idea.validator.ValidatorData.Level;
import com.android.tools.idea.validator.ValidatorData.Type;
import com.android.tools.idea.validator.ValidatorResult;
import com.android.tools.layoutlib.annotations.NotNull;
import com.android.tools.layoutlib.annotations.Nullable;

import android.view.View;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.common.collect.BiMap;

/**
 * Validator specific for running Accessibility specific issues.
 */
public class AccessibilityValidator {

    static {
        /**
         * Overriding default ResourceBundle ATF uses. ATF would use generic Java resources
         * instead of Android's .xml.
         *
         * By default ATF generates ResourceBundle to support Android specific env/ classloader,
         * which is quite different from Layoutlib, which supports multiple classloader depending
         * on env (testing vs in studio).
         *
         * To support ATF in Layoutlib, easiest way is to convert resources from Android xml to
         * generic Java resources (strings.properties), and have the default ResourceBundle ATF
         * uses be redirected.
         */
        StringManager.setResourceBundleProvider(locale -> ResourceBundle.getBundle("strings"));
    }

    /**
     * Run Accessibility specific validation test and receive results.
     * @param view the root view
     * @param filter list of levels to allow
     * @return results with all the accessibility issues and warnings.
     */
    @NotNull
    public static ValidatorResult validateAccessibility(
            @NotNull View view,
            @NotNull EnumSet<Level> filter) {
        ValidatorResult.Builder builder = new ValidatorResult.Builder();

        List<AccessibilityHierarchyCheckResult> results = getHierarchyCheckResults(view,
                builder.mSrcMap);

        for (AccessibilityHierarchyCheckResult result : results) {
            ValidatorData.Level level = convertLevel(result.getType());
            if (!filter.contains(level)) {
                continue;
            }

            ValidatorData.Fix fix = generateFix(result);
            Long srcId = null;
            if (result.getElement() != null) {
                srcId = result.getElement().getCondensedUniqueId();
            }
            Issue issue = new Issue(
                    Type.ACCESSIBILITY,
                    result.getMessage(Locale.ENGLISH).toString(),
                    level,
                    srcId,
                    fix);
            builder.mIssues.add(issue);
        }
        return builder.build();
    }

    @NotNull
    private static ValidatorData.Level convertLevel(@NotNull AccessibilityCheckResultType type) {
        switch (type) {
            case ERROR:
                return Level.ERROR;
            case WARNING:
                return Level.WARNING;
            case INFO:
                return Level.INFO;
            // TODO: Maybe useful later?
            case SUPPRESSED:
            case NOT_RUN:
            default:
                return Level.VERBOSE;
        }
    }

    @Nullable
    private static ValidatorData.Fix generateFix(@NotNull AccessibilityHierarchyCheckResult result) {
        // TODO: Once ATF is ready to return us with appropriate fix, build proper fix here.
        return new Fix("");
    }

    @NotNull
    private static List<AccessibilityHierarchyCheckResult> getHierarchyCheckResults(
            @NotNull View view,
            @NotNull BiMap<Long, View> originMap) {
        @NotNull Set<AccessibilityHierarchyCheck> checks = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                AccessibilityCheckPreset.LATEST);
        @NotNull AccessibilityHierarchyAndroid hierarchy = AccessibilityHierarchyAndroid.newBuilder(view).setViewOriginMap(originMap).build();
        ArrayList<AccessibilityHierarchyCheckResult> a11yResults = new ArrayList();

        for (AccessibilityHierarchyCheck check : checks) {
            a11yResults.addAll(check.runCheckOnHierarchy(hierarchy));
        }

        return a11yResults;
    }
}
