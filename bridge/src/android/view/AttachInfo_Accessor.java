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

import android.content.Context;
import android.graphics.Insets;
import android.util.Pair;
import android.view.View.AttachInfo;
import android.view.Window.OnContentApplyWindowInsetsListener;

import static android.view.View.SYSTEM_UI_LAYOUT_FLAGS;

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

    public static LayoutlibRenderer setAttachInfo(ViewGroup view) {
        Context context = view.getContext();
        WindowManagerImpl wm = (WindowManagerImpl)context.getSystemService(Context.WINDOW_SERVICE);
        wm.setBaseRootView(view);
        Display display = wm.getDefaultDisplay();
        ViewRootImpl root = new ViewRootImpl(context, display, new IWindowSession.Default(),
                new WindowLayout());
        root.setOnContentApplyWindowInsetsListener(sDefaultContentInsetsApplier);
        LayoutlibRenderer renderer = new LayoutlibRenderer(context, false, "layoutlib-renderer");
        AttachInfo info = root.mAttachInfo;
        info.mThreadedRenderer = renderer;
        info.mHasWindowFocus = true;
        info.mWindowVisibility = View.VISIBLE;
        info.mInTouchMode = false; // this is so that we can display selections.
        info.mHardwareAccelerated = true;
        info.mApplicationScale = 1.0f;
        ViewRootImpl_Accessor.setChild(root, view);
        view.assignParent(root);
        view.dispatchAttachedToWindow(info, 0);
        return renderer;
    }

    public static void dispatchOnPreDraw(View view) {
        view.mAttachInfo.mTreeObserver.dispatchOnPreDraw();
    }

    public static void detachFromWindow(final View view) {
        if (view != null) {
            final View.AttachInfo attachInfo = view.mAttachInfo;
            view.dispatchDetachedFromWindow();
            if (attachInfo != null) {
                ViewRootImpl_Accessor.detachFromWindow(attachInfo.mViewRootImpl);
                final ThreadedRenderer threadedRenderer = attachInfo.mThreadedRenderer;
                if(threadedRenderer != null) {
                    threadedRenderer.destroy();
                }
                ThreadedRenderer rootRenderer =
                        attachInfo.mViewRootImpl.mAttachInfo.mThreadedRenderer;
                if (rootRenderer != null) {
                    rootRenderer.destroy();
                }
            }
        }
    }

    public static ViewRootImpl getRootView(View view) {
        return view.mAttachInfo != null ? view.mAttachInfo.mViewRootImpl : null;
    }
}
