/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.graphics.drawable;

import com.android.internal.R;

import android.content.res.Resources;

public class AdaptiveIconDrawable_Delegate {
    public static String sPath;

    /**
     * Delegate that replaces a call to Resources.getString in
     * the constructor of AdaptiveIconDrawable.
     * This allows to pass a non-default value for the mask for adaptive icons.
     */
    @SuppressWarnings("unused")
    public static String getResourceString(int resId) {
        if (resId == R.string.config_icon_mask) {
            return sPath;
        }
        return Resources.getSystem().getString(resId);
    }
}
