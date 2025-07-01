/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.launcher3.icons;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;

import static android.graphics.drawable.AdaptiveIconDrawable.getExtraInsetFraction;

public class MonochromeIconFactory_Accessor {
        // Adapted from com.android.launcher3.icons.ThemedIconDrawable
        public static Drawable getMonochromeIcon(AdaptiveIconDrawable adaptiveIcon,
            int foregroundColor) {
        MonochromeIconFactory monoFactory = new MonochromeIconFactory(adaptiveIcon.getBounds().width());
        monoFactory.setColorFilter(new BlendModeColorFilter(foregroundColor, BlendMode.SRC_IN));
        Drawable mono = monoFactory.wrap(adaptiveIcon);
        float inset = getExtraInsetFraction() / (1 + 2 * getExtraInsetFraction());
        return new InsetDrawable(mono, inset);
    }
}
