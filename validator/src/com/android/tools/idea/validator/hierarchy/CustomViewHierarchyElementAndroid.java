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
import com.google.android.apps.common.testing.accessibility.framework.replacements.LayoutParams;
import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyActionAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElementAndroid;
import java.util.List;

/***
 * A {@link ViewHierarchyElementAndroid} which is customized for Android Studio integration.
 */
public class CustomViewHierarchyElementAndroid
        extends ViewHierarchyElementAndroid {

    protected CustomViewHierarchyElementAndroid(
            int id,
            @Nullable Integer parentId,
            List<Integer> childIds,
            @Nullable CharSequence packageName,
            @Nullable CharSequence className,
            @Nullable CharSequence accessibilityClassName,
            @Nullable String resourceName,
            @Nullable SpannableString contentDescription,
            @Nullable SpannableString text,
            boolean importantForAccessibility,
            @Nullable Boolean visibleToUser,
            boolean clickable,
            boolean longClickable,
            boolean focusable,
            @Nullable Boolean editable,
            @Nullable Boolean scrollable,
            @Nullable Boolean canScrollForward,
            @Nullable Boolean canScrollBackward,
            @Nullable Boolean checkable,
            @Nullable Boolean checked,
            @Nullable Boolean hasTouchDelegate,
            List<Rect> touchDelegateBounds,
            @Nullable Rect boundsInScreen,
            @Nullable Integer nonclippedHeight,
            @Nullable Integer nonclippedWidth,
            @Nullable Float textSize,
            @Nullable Integer textColor,
            @Nullable Integer backgroundDrawableColor,
            @Nullable Integer typefaceStyle,
            boolean enabled,
            @Nullable Long labeledById,
            @Nullable Long accessibilityTraversalBeforeId,
            @Nullable Long accessibilityTraversalAfterId,
            @Nullable Integer drawingOrder,
            List<Integer> superclassViews,
            List<ViewHierarchyActionAndroid> actionList,
            @Nullable LayoutParams layoutParams,
            @Nullable SpannableString hintText,
            @Nullable Integer hintTextColor,
            List<Rect> textCharacterLocations) {
        super(
                id,
                parentId,
                childIds,
                packageName,
                className,
                accessibilityClassName,
                resourceName,
                contentDescription,
                text,
                importantForAccessibility,
                visibleToUser,
                clickable,
                longClickable,
                focusable,
                editable,
                scrollable,
                canScrollForward,
                canScrollBackward,
                checkable,
                checked,
                hasTouchDelegate,
                touchDelegateBounds,
                boundsInScreen,
                nonclippedHeight,
                nonclippedWidth,
                textSize,
                textColor,
                backgroundDrawableColor,
                typefaceStyle,
                enabled,
                labeledById,
                accessibilityTraversalBeforeId,
                accessibilityTraversalAfterId,
                drawingOrder,
                superclassViews,
                actionList,
                layoutParams,
                hintText,
                hintTextColor,
                textCharacterLocations);
    }

    /**
     * A {@link ViewHierarchyElementAndroid.Builder} that builds a customized {@link
     * ViewHierarchyElementAndroid} from a {@link View}.
     */
    public static class CustomBuilder extends Builder {

        protected CustomBuilder(
                int id,
                @Nullable ViewHierarchyElementAndroid parent,
                View fromView) {
            super(id, parent, fromView);
        }

        @Override
        protected boolean isCheckable(View fromView) {
            return CustomHierarchyHelper.isCheckable(fromView);
        }
    }
}
