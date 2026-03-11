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
import android.graphics.Insets;
import android.util.Pair;
import android.view.View.AttachInfo;
import android.view.Window.OnContentApplyWindowInsetsListener;
import android.view.WindowManager.LayoutParams;

import static android.view.View.SYSTEM_UI_LAYOUT_FLAGS;
import static android.view.View.VISIBLE;

/**
 * Class allowing access to package-protected methods/fields.
 */
public class AttachInfo_Accessor {
    // Copied from PhoneWindow.java
    private static final OnContentApplyWindowInsetsListener sDefaultContentInsetsApplier =
            (view, insets) -> {
                if ((view.getWindowSystemUiVisibility() & SYSTEM_UI_LAYOUT_FLAGS) != 0) {
                    return new Pair<>(Insets.NONE, insets);
                }
                Insets insetsToApply = insets.getSystemWindowInsets();
                return new Pair<>(insetsToApply,
                        insets.inset(insetsToApply).consumeSystemWindowInsets());
            };

    public static LayoutlibRenderer setAttachInfo(ViewGroup view, Layout sysUiLayout) {
        Context context = view.getContext();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(view, new LayoutParams());
        ViewRootImpl root = (ViewRootImpl) view.getParent();
        root.setOnContentApplyWindowInsetsListener(sDefaultContentInsetsApplier);
        LayoutlibRenderer renderer = new LayoutlibRenderer(context, false, "layoutlib-renderer");
        AttachInfo info = root.mAttachInfo;
        info.mThreadedRenderer = renderer.getThreadedRenderer();
        info.mHasWindowFocus = true;
        info.mWindowVisibility = View.VISIBLE;
        info.mInTouchMode = false; // this is so that we can display selections.
        info.mHardwareAccelerated = true;
        info.mApplicationScale = 1.0f;
        ViewRootImpl_Accessor.setChild(root, view);
        if (sysUiLayout != null) {
            InsetsController insetsController = root.getInsetsController();
            ((BridgeContext)context).createOrUpdateDisplayFrames(insetsController.getState());
            InsetUtil.setupSysUiInsets(context, insetsController,
                    sysUiLayout.getInsetsFrameProviders());
        }
        view.dispatchAttachedToWindow(info, 0);
        root.mTmpFrames.displayFrame.set(wm.getCurrentWindowMetrics().getBounds());
        root.mTmpFrames.frame.set(wm.getCurrentWindowMetrics().getBounds());
        root.mWinFrame.set(wm.getCurrentWindowMetrics().getBounds());
        return renderer;
    }

    public static void dispatchOnPreDraw(View view) {
        view.mAttachInfo.mTreeObserver.dispatchOnPreDraw();
    }

    public static void dispatchOnPreDraw(View view, ThreadedRenderer renderer) {
        view.mAttachInfo.mThreadedRenderer = renderer;
        dispatchOnPreDraw(view);
    }

    public static void dispatchOnGlobalLayout(View view) {
        view.dispatchAttachedToWindow(((ViewRootImpl)view.getParent()).mAttachInfo, VISIBLE);
        view.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
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



    public static ViewRootImpl getRootView(View view) {
        return view.mAttachInfo != null ? view.mAttachInfo.mViewRootImpl : null;
    }
}
