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

import android.graphics.BlendMode;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.view.ThreadedRenderer.DrawCallbacks;

import java.nio.ByteBuffer;
import java.util.List;

public class LayoutlibRenderer {

    private final ThreadedRenderer mDelegateRenderer;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private ImageReader mImageReader;
    private Image mNativeImage;

    LayoutlibRenderer(ThreadedRenderer renderer) {
        mDelegateRenderer = renderer;
    }

    public void draw(List<View> views) {
        if (views.isEmpty()) {
            return;
        }
        View firstView = views.getFirst();
        ViewRootImpl rootView = firstView.getViewRootImpl();
        if (rootView == null) {
            return;
        }
        // Animations require mDrawingTime to be set to animate
        mDelegateRenderer.draw(firstView, rootView.mAttachInfo,
                new DrawCallbacks() {
                    @Override
                    public void onPreDraw(RecordingCanvas canvas) {
                        canvas.scale(scaleX, scaleY);
                        // This way we clear the native image buffer before drawing
                        canvas.drawColor(0, BlendMode.CLEAR);
                    }

                    @Override
                    public void onPostDraw(RecordingCanvas canvas) {
                        for (int i = 1; i < views.size(); i++) {
                            View view = views.get(i);

                            ViewRootImpl root = view.getViewRootImpl();
                            if (root != null) {
                                WindowManager.LayoutParams attrs = root.mWindowAttributes;
                                if ((attrs.flags & WindowManager.LayoutParams.FLAG_DIM_BEHIND) != 0) {
                                    int argb = android.graphics.Color.toArgb(attrs.dimColor);
                                    int alpha = (int) (255 * attrs.dimAmount);
                                    int color = android.graphics.Color.argb(alpha,
                                            android.graphics.Color.red(argb),
                                            android.graphics.Color.green(argb),
                                            android.graphics.Color.blue(argb));
                                    canvas.drawColor(color);
                                }
                            }

                            canvas.save();
                            if (root != null) {
                                canvas.translate(root.mWinFrame.left, root.mWinFrame.top);
                            }
                            canvas.enableZ();
                            canvas.drawRenderNode(view.updateDisplayListIfDirty());
                            canvas.disableZ();
                            canvas.restore();
                        }
                    }
                });
        // Wait for render thread to finish rendering
        mDelegateRenderer.fence();
    }

    public void invalidateRoot() {
        mDelegateRenderer.invalidateRoot();
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        invalidateRoot();
    }

    /**
     * Prepares the renderer for drawing
     */
    public void setup(int width, int height, View rootView) {
        ViewRootImpl viewRoot =  rootView.mAttachInfo.mViewRootImpl;
        if (viewRoot == null) {
            return;
        }

        if (mImageReader == null || mImageReader.getWidth() != width || mImageReader.getHeight() != height) {
            if (mImageReader != null) {
                mImageReader.close();
            }
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
            mDelegateRenderer.setSurface(mImageReader.getSurface());
        }

        mDelegateRenderer.setup(width, height, rootView.mAttachInfo,
                viewRoot.mWindowAttributes.surfaceInsets);
    }

    public ByteBuffer getBuffer() {
        mNativeImage = mImageReader.acquireNextImage();
        if (mNativeImage == null) {
            return null;
        }
        Plane[] planes = mNativeImage.getPlanes();
        return planes[0].getBuffer();
    }

    public int getRowStride() {
        if (mNativeImage == null) {
            return 0;
        }
        return mNativeImage.getPlanes()[0].getRowStride();
    }

    public void releaseBuffer() {
        if (mNativeImage != null) {
            mNativeImage.close();
            mNativeImage = null;
        }
    }

    public void reset() {
        releaseBuffer();
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    public void destroy() {
        mDelegateRenderer.destroy();
    }
}
