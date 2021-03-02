/*
 * Copyright (C) 2021 The Android Open Source Project
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

package android.graphics.fonts;

import com.android.ide.common.rendering.api.ILayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

public class FontFamily_Delegate {

    @LayoutlibDelegate
    /*package*/ static int nGetFontSize(long family) {
        FontFamily_Builder_Delegate delegate = FontFamily_Builder_Delegate.getDelegate(family);
        if (delegate == null) {
            return 0;
        }
        return delegate.getSize();
    }

    @LayoutlibDelegate
    /*package*/ static long nGetFont(long family, int i) {
        FontFamily_Builder_Delegate delegate = FontFamily_Builder_Delegate.getDelegate(family);
        if (delegate == null) {
            return 0;
        }
        return delegate.mFontsList.get(i);
    }

    @LayoutlibDelegate
    /*package*/ static String nGetLangTags(long family) {
        Bridge.getLog().fidelityWarning(ILayoutLog.TAG_UNSUPPORTED,
                "Language tags are not supported", null, null, null /*data*/);
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static int nGetVariant(long family) {
        FontFamily_Builder_Delegate delegate = FontFamily_Builder_Delegate.getDelegate(family);
        if (delegate == null) {
            return 0;
        }
        return delegate.getVariant();
    }
}
