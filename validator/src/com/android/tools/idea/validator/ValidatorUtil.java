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

import com.android.tools.idea.validator.ValidatorData.Fix;
import com.android.tools.idea.validator.ValidatorData.Issue.IssueBuilder;
import com.android.tools.idea.validator.ValidatorData.Level;
import com.android.tools.idea.validator.ValidatorData.Type;
import com.android.tools.idea.validator.ValidatorResult.Builder;
import com.android.tools.layoutlib.annotations.NotNull;
import com.android.tools.layoutlib.annotations.Nullable;

import android.view.View;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.strings.StringManager;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;

public class ValidatorUtil {

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
     * @param policy policy to apply for the hierarchy
     * @param view root view to build hierarchy from
     * @param image screenshot image that matches the view
     * @return The hierarchical data required for running the ATF checks.
     */
    public static ValidatorHierarchy buildHierarchy(
            @NotNull ValidatorData.Policy policy,
            @NotNull View view,
            @Nullable BufferedImage image) {
        ValidatorHierarchy hierarchy = new ValidatorHierarchy();
        if (!policy.mTypes.contains(Type.ACCESSIBILITY)) {
            return hierarchy;
        }

        ValidatorResult.Builder builder = new ValidatorResult.Builder();
        @Nullable Parameters parameters = null;
        builder.mMetric.startTimer();

        hierarchy.mView = AccessibilityHierarchyAndroid
                .newBuilder(view)
                .setViewOriginMap(builder.mSrcMap)
                .build();
        if (image != null) {
            parameters = new Parameters();
            parameters.putScreenCapture(
                    new AtfBufferedImage(image, builder.mMetric));
        }
        builder.mMetric.recordHierarchyCreationTime();

        hierarchy.mBuilder = builder;
        hierarchy.mParameters = parameters;
        return hierarchy;
    }

    /**
     * @param hierarchy to build result from. If {@link ValidatorHierarchy#isHierarchyBuilt()}
     * is false, returns a result with an internal error.
     * @return Returns ValidatorResult with given hierarchical data.
     */
    public static ValidatorResult generateResults(
            @NotNull ValidatorData.Policy policy,
            @NotNull ValidatorHierarchy hierarchy) {
        ValidatorResult.Builder builder = hierarchy.mBuilder;

        if (!hierarchy.isHierarchyBuilt()) {
            // Unable to build.
            builder = new Builder();
            String errorMsg = hierarchy.mErrorMessage != null
                    ? hierarchy.mErrorMessage
                    : "Hierarchy is not built yet.";
            builder.mIssues.add(new IssueBuilder()
                    .setCategory("Accessibility")
                    .setType(Type.INTERNAL_ERROR)
                    .setMsg(errorMsg)
                    .setLevel(Level.ERROR)
                    .setSourceClass("ValidatorHierarchy").build());
            return builder.build();
        }

        AccessibilityHierarchyAndroid view = hierarchy.mView;
        Parameters parameters = hierarchy.mParameters;

        EnumSet<Level> filter = policy.mLevels;
        ArrayList<AccessibilityHierarchyCheckResult> a11yResults = new ArrayList<>();

        HashSet<AccessibilityHierarchyCheck> policyChecks = policy.mChecks;
        @NotNull Set<AccessibilityHierarchyCheck> checks = policyChecks.isEmpty()
                ? AccessibilityCheckPreset
                .getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
                : policyChecks;

        for (AccessibilityHierarchyCheck check : checks) {
            a11yResults.addAll(check.runCheckOnHierarchy(view, null, parameters));
        }

        for (AccessibilityHierarchyCheckResult result : a11yResults) {
            String category = ValidatorUtil.getCheckClassCategory(result.getSourceCheckClass());

            ValidatorData.Level level = ValidatorUtil.convertLevel(result.getType());
            if (!filter.contains(level)) {
                continue;
            }

            try {
                IssueBuilder issueBuilder = new IssueBuilder()
                        .setCategory(category)
                        .setMsg(result.getMessage(Locale.ENGLISH).toString())
                        .setLevel(level)
                        .setFix(ValidatorUtil.generateFix(result))
                        .setSourceClass(result.getSourceCheckClass().getSimpleName());
                if (result.getElement() != null) {
                    issueBuilder.setSrcId(result.getElement().getCondensedUniqueId());
                }
                AccessibilityHierarchyCheck subclass = AccessibilityCheckPreset
                        .getHierarchyCheckForClass(result
                                .getSourceCheckClass()
                                .asSubclass(AccessibilityHierarchyCheck.class));
                if (subclass != null) {
                    issueBuilder.setHelpfulUrl(subclass.getHelpUrl());
                }
                builder.mIssues.add(issueBuilder.build());
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                builder.mIssues.add(new IssueBuilder()
                        .setCategory(category)
                        .setType(Type.INTERNAL_ERROR)
                        .setMsg(sw.toString())
                        .setLevel(Level.ERROR)
                        .setSourceClass("ValidatorHierarchy").build());
            }
        }
        builder.mMetric.endTimer();
        return builder.build();
    }

    /**
     * @param checkClass classes expected to extend AccessibilityHierarchyCheck
     * @return {@link AccessibilityCheck.Category} of the class.
     */
    @NotNull
    private static String getCheckClassCategory(@NotNull Class<?> checkClass) {
        try {
            Class<? extends AccessibilityHierarchyCheck> subClass =
                    checkClass.asSubclass(AccessibilityHierarchyCheck.class);
            AccessibilityHierarchyCheck check =
                    AccessibilityCheckPreset.getHierarchyCheckForClass(subClass);
            return (check == null) ? "Accessibility" : check.getCategory().name();
        } catch (ClassCastException e) {
            return "Accessibility";
        }
    }

    /** Convert {@link AccessibilityCheckResultType} to {@link ValidatorData.Level} */
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
}
