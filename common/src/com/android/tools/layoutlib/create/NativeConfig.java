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

    public final static String[] DELEGATE_CLASS_NATIVES_TO_NATIVES = new String [] {
            "android.graphics.ColorSpace$Rgb",
            "android.graphics.FontFamily",
            "android.graphics.ImageDecoder",
            "android.graphics.Matrix",
            "android.graphics.Path",
            "android.graphics.Typeface",
            "android.graphics.fonts.Font$Builder",
            "android.graphics.fonts.FontFamily$Builder",
            "android.graphics.text.LineBreaker",
    };

    public final static String[] DELEGATE_CLASS_NATIVES = new String[] {
            "android.os.SystemClock",
            "android.os.SystemProperties",
            "android.view.Display",
            "libcore.icu.ICU",
    };

    /**
     * The list of classes to register with JNI
     */
    public final static String[] CLASS_NATIVES = new String[] {
            "android.animation.PropertyValuesHolder",
            "android.graphics.Bitmap",
            "android.graphics.BitmapFactory",
            "android.graphics.ByteBufferStreamAdaptor",
            "android.graphics.Canvas",
            "android.graphics.ColorFilter",
            "android.graphics.ColorSpace",
            "android.graphics.CreateJavaOutputStreamAdaptor",
            "android.graphics.DrawFilter",
            "android.graphics.FontFamily",
            "android.graphics.Graphics",
            "android.graphics.ImageDecoder",
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
            "android.graphics.RenderNode",
            "android.graphics.Shader",
            "android.graphics.Typeface",
            "android.graphics.drawable.AnimatedVectorDrawable",
            "android.graphics.drawable.VectorDrawable",
            "android.graphics.fonts.Font",
            "android.graphics.fonts.FontFamily",
            "android.graphics.text.LineBreaker",
            "android.graphics.text.MeasuredText",
            "android.util.PathParser",
            "com.android.internal.util.VirtualRefBasePtr",
            "com.android.internal.view.animation.NativeInterpolatorFactoryHelper",
    };
}
