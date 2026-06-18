/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.view;

import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.Layout;
import com.android.layoutlib.bridge.util.InsetUtil;

import android.content.Context;
import android.view.View.AttachInfo;
import android.view.WindowManager.LayoutParams;

/**
 * Class allowing access to package-protected methods/fields.
 */
public class AttachInfo_Accessor {
    public static LayoutlibRenderer setAttachInfo(ViewGroup view, Layout sysUiLayout) {
        Context context = view.getContext();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(view, new LayoutParams());
        ViewRootImpl root = (ViewRootImpl) view.getParent();
        if (sysUiLayout != null) {
            InsetsController insetsController = root.getInsetsController();
            ((BridgeContext)context).createOrUpdateDisplayFrames(insetsController.getState());
            InsetUtil.setupSysUiInsets(context, insetsController,
                    sysUiLayout.getInsetsFrameProviders());
        }

        AttachInfo info = root.mAttachInfo;
        view.dispatchAttachedToWindow(info, 0);
        return new LayoutlibRenderer(info.mThreadedRenderer);
    }

    public static void setHasWindowFocus(View view, boolean hasFocus) {
        View.AttachInfo info = view.mAttachInfo;
        if (info != null && info.mHasWindowFocus != hasFocus) {
            info.mHasWindowFocus = hasFocus;
            if (view instanceof ViewGroup) {
                view.dispatchWindowFocusChanged(hasFocus);
            }
        }
    }
}
