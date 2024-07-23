/*
 * Copyright (C) 2023 The Android Open Source Project
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
import android.graphics.BlendMode;
import android.graphics.RecordingCanvas;

public class LayoutlibRenderer extends ThreadedRenderer {

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    LayoutlibRenderer(Context context, boolean translucent, String name) {
        super(context, translucent, name);
    }

    public void draw(ViewGroup viewGroup) {
        ViewRootImpl rootView = AttachInfo_Accessor.getRootView(viewGroup);
        if (rootView == null) {
            return;
        }
        this.draw(viewGroup, rootView.mAttachInfo,
                new DrawCallbacks() {
                    @Override
                    public void onPreDraw(RecordingCanvas canvas) {
                        AttachInfo_Accessor.dispatchOnPreDraw(viewGroup);
                        canvas.scale(scaleX, scaleY);
                        // This way we clear the native image buffer before drawing
                        canvas.drawColor(0, BlendMode.CLEAR);
                    }

                    @Override
                    public void onPostDraw(RecordingCanvas canvas) {

                    }
                });
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
}
