/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.tools.layoutlib.create;

/**
 * Stores data needed for native JNI registration, and possibly the framework bytecode
 * instrumentation.
 */
public class NativeConfig {

    private NativeConfig() {}

    public final static String[] DEFERRED_STATIC_INITIALIZER_CLASSES = new String [] {
            "android.graphics.ColorSpace",
            "android.graphics.FontFamily",
            "android.graphics.Matrix",
            "android.graphics.Path",
            // Order is important! Fonts and FontFamily have to be initialized before Typeface
            "android.graphics.fonts.Font",
            "android.graphics.fonts.FontFamily$Builder",
            "android.graphics.Typeface",
            "android.graphics.text.PositionedGlyphs",
            "android.graphics.text.LineBreaker",
    };

    public final static String[] DELEGATE_CLASS_NATIVES = new String[] {
            "android.os.SystemClock",
            "android.view.Display",
            "libcore.icu.ICU",
    };

    /**
     * The list of core classes to register with JNI
     */
    public final static String[] CORE_CLASS_NATIVES = new String[] {
            "android.animation.PropertyValuesHolder",
            "android.content.res.StringBlock",
            "android.content.res.XmlBlock",
            "android.media.ImageReader",
            "android.os.SystemProperties",
            "android.os.Trace",
            "android.text.AndroidCharacter",
            "android.util.Log",
            "android.view.MotionEvent",
            "android.view.Surface",
            "com.android.internal.util.VirtualRefBasePtr",
            "libcore.util.NativeAllocationRegistry_Delegate",
    };

    /**
     * The list of graphics classes to register with JNI
     */
    public final static String[] GRAPHICS_CLASS_NATIVES = new String[] {
            "android.graphics.Bitmap",
            "android.graphics.BitmapFactory",
            "android.graphics.ByteBufferStreamAdaptor",
            "android.graphics.Camera",
            "android.graphics.Canvas",
            "android.graphics.CanvasProperty",
            "android.graphics.ColorFilter",
            "android.graphics.ColorSpace",
            "android.graphics.CreateJavaOutputStreamAdaptor",
            "android.graphics.DrawFilter",
            "android.graphics.FontFamily",
            "android.graphics.Graphics",
            "android.graphics.HardwareRenderer",
            "android.graphics.ImageDecoder",
            "android.graphics.Interpolator",
            "android.graphics.MaskFilter",
            "android.graphics.Matrix",
            "android.graphics.NinePatch",
            "android.graphics.Paint",
            "android.graphics.Path",
            "android.graphics.PathEffect",
            "android.graphics.PathMeasure",
            "android.graphics.Picture",
            "android.graphics.RecordingCanvas",
            "android.graphics.Region",
            "android.graphics.RenderEffect",
            "android.graphics.RenderNode",
            "android.graphics.Shader",
            "android.graphics.Typeface",
            "android.graphics.YuvImage",
            "android.graphics.animation.NativeInterpolatorFactory",
            "android.graphics.animation.RenderNodeAnimator",
            "android.graphics.drawable.AnimatedVectorDrawable",
            "android.graphics.drawable.VectorDrawable",
            "android.graphics.fonts.Font",
            "android.graphics.fonts.FontFamily",
            "android.graphics.text.LineBreaker",
            "android.graphics.text.MeasuredText",
            "android.graphics.text.TextRunShaper",
            "android.util.PathParser",
    };
}
