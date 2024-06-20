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

import com.android.layoutlib.bridge.impl.Layout;
import com.android.layoutlib.bridge.util.InsetUtil;

import android.content.Context;
import android.view.View.AttachInfo;

/**
 * Class allowing access to package-protected methods/fields.
 */
public class AttachInfo_Accessor {

    public static LayoutlibRenderer setAttachInfo(ViewGroup view) {
        Context context = view.getContext();
        WindowManagerImpl wm = (WindowManagerImpl)context.getSystemService(Context.WINDOW_SERVICE);
        wm.setBaseRootView(view);
        Display display = wm.getDefaultDisplay();
        ViewRootImpl root = new ViewRootImpl(context, display, new IWindowSession.Default(),
                new WindowLayout());
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
        if (view instanceof Layout) {
            InsetUtil.setupSysUiInsets(context, root.getInsetsController(),
                    ((Layout)view).getInsetsFrameProviders());
        }
        view.dispatchAttachedToWindow(info, 0);
        return renderer;
    }

    public static void dispatchOnPreDraw(View view) {
        view.mAttachInfo.mTreeObserver.dispatchOnPreDraw();
    }

    public static void dispatchOnGlobalLayout(View view) {
        view.mAttachInfo.mTreeObserver.dispatchOnGlobalLayout();
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
