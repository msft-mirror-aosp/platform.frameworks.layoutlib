/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.tools.layoutlib.annotations.LayoutlibDelegate;
import com.android.tools.layoutlib.java.LinkedHashMap_Delegate;
import com.android.tools.layoutlib.java.NioUtils_Delegate;
import com.android.tools.layoutlib.java.System_Delegate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Describes the work to be done by {@link AsmGenerator}.
 */
public final class CreateInfo implements ICreateInfo {

    @Override
    public MethodReplacer[] getMethodReplacers() {
        return METHOD_REPLACERS;
    }

    @Override
    public Class<?>[] getInjectedClasses() {
        return INJECTED_CLASSES;
    }

    @Override
    public String[] getDelegateMethods() {
        return DELEGATE_METHODS;
    }

    @Override
    public String[] getDelegateClassNatives() {
        return DELEGATE_CLASS_NATIVES;
    }

    @Override
    public String[] getDelegateClassNativesToNatives() {
        return DELEGATE_CLASS_NATIVES_TO_NATIVES;
    }

    @Override
    public boolean shouldKeepAllNativeClasses() {
        return false;
    }

    @Override
    public String[] getKeepClassNatives() {
        return KEEP_CLASS_NATIVES;
    }

    @Override
    public String[] getRenamedClasses() {
        return RENAMED_CLASSES;
    }

    @Override
    public String[] getDeleteReturns() {
        return DELETE_RETURNS;
    }

    @Override
    public String[] getJavaPkgClasses() {
      return JAVA_PKG_CLASSES;
    }

    @Override
    public String[] getRefactoredClasses() {
        return REFACTOR_CLASSES;
    }

    @Override
    public String[] getExcludedClasses() {
        String[] refactoredClasses = getJavaPkgClasses();
        int count = refactoredClasses.length / 2 + EXCLUDED_CLASSES.length;
        Set<String> excludedClasses = new HashSet<>(count);
        for (int i = 0; i < refactoredClasses.length; i+=2) {
            excludedClasses.add(refactoredClasses[i]);
        }
        excludedClasses.addAll(Arrays.asList(EXCLUDED_CLASSES));
        return excludedClasses.toArray(new String[0]);
    }

    @Override
    public String[] getPromotedFields() {
        return PROMOTED_FIELDS;
    }

    @Override
    public String[] getPromotedMethods() {
        return PROMOTED_METHODS;
    }

    @Override
    public String[] getPromotedClasses() {
        return PROMOTED_CLASSES;
    }

    @Override
    public Map<String, InjectMethodRunnable> getInjectedMethodsMap() {
        return INJECTED_METHODS;
    }

    @Override
    public String[] getDeferredStaticInitializerClasses() {
        return DEFERRED_STATIC_INITIALIZER_CLASSES;
    }

    //-----

    private static final MethodReplacer[] METHOD_REPLACERS = new MethodReplacer[] {
        new SystemLoadLibraryReplacer(),
        new SystemArrayCopyReplacer(),
        new LocaleGetDefaultReplacer(),
        new LocaleAdjustLanguageCodeReplacer(),
        new SystemLogReplacer(),
        new SystemNanoTimeReplacer(),
        new SystemCurrentTimeMillisReplacer(),
        new LinkedHashMapEldestReplacer(),
        new ContextGetClassLoaderReplacer(),
        new ImageReaderNativeInitReplacer(),
        new NioUtilsFreeBufferReplacer(),
        new ProcessInitializerInitSchedReplacer(),
    };

    /**
     * The list of class from layoutlib_create to inject in layoutlib.
     */
    private final static Class<?>[] INJECTED_CLASSES = new Class<?>[] {
            OverrideMethod.class,
            MethodListener.class,
            MethodAdapter.class,
            ICreateInfo.class,
            CreateInfo.class,
            LayoutlibDelegate.class,
            InjectMethodRunnable.class,
            InjectMethodRunnables.class,
            /* Java package classes */
            System_Delegate.class,
            LinkedHashMap_Delegate.class,
            NioUtils_Delegate.class,
        };

    /**
     * The list of methods to rewrite as delegates.
     */
    public final static String[] DELEGATE_METHODS = new String[] {
        "android.app.Fragment#instantiate", //(Landroid/content/Context;Ljava/lang/String;Landroid/os/Bundle;)Landroid/app/Fragment;",
        "android.content.res.AssetManager#getAssignedPackageIdentifiers",
        "android.content.res.AssetManager#nativeCreate",
        "android.content.res.AssetManager#nativeCreateIdmapsForStaticOverlaysTargetingAndroid",
        "android.content.res.AssetManager#nativeDestroy",
        "android.content.res.AssetManager#nativeThemeCreate",
        "android.content.res.AssetManager#nativeThemeDestroy",
        "android.content.res.AssetManager#open",
        "android.content.res.Resources#getAnimation",
        "android.content.res.Resources#getAttributeSetSourceResId",
        "android.content.res.Resources#getBoolean",
        "android.content.res.Resources#getColor",
        "android.content.res.Resources#getColorStateList",
        "android.content.res.Resources#getDimension",
        "android.content.res.Resources#getDimensionPixelOffset",
        "android.content.res.Resources#getDimensionPixelSize",
        "android.content.res.Resources#getDrawable",
        "android.content.res.Resources#getFloat",
        "android.content.res.Resources#getFont",
        "android.content.res.Resources#getIdentifier",
        "android.content.res.Resources#getIntArray",
        "android.content.res.Resources#getInteger",
        "android.content.res.Resources#getLayout",
        "android.content.res.Resources#getQuantityString",
        "android.content.res.Resources#getQuantityText",
        "android.content.res.Resources#getResourceEntryName",
        "android.content.res.Resources#getResourceName",
        "android.content.res.Resources#getResourcePackageName",
        "android.content.res.Resources#getResourceTypeName",
        "android.content.res.Resources#getString",
        "android.content.res.Resources#getStringArray",
        "android.content.res.Resources#getText",
        "android.content.res.Resources#getTextArray",
        "android.content.res.Resources#getValue",
        "android.content.res.Resources#getValueForDensity",
        "android.content.res.Resources#getXml",
        "android.content.res.Resources#loadXmlResourceParser",
        "android.content.res.Resources#obtainAttributes",
        "android.content.res.Resources#openRawResource",
        "android.content.res.Resources#openRawResourceFd",
        "android.content.res.Resources#obtainTypedArray",
        "android.content.res.Resources$Theme#obtainStyledAttributes",
        "android.content.res.Resources$Theme#resolveAttribute",
        "android.content.res.Resources$Theme#resolveAttributes",
        "android.content.res.TypedArray#getValueAt",
        "android.content.res.TypedArray#obtain",
        "android.graphics.FontFamily#addFontFromAssetManager",
        "android.graphics.Typeface#create",
        "android.graphics.Typeface$Builder#createAssetUid",
        "android.graphics.drawable.AdaptiveIconDrawable#<init>",
        "android.graphics.drawable.AnimatedVectorDrawable$VectorDrawableAnimatorUI#onDraw",
        "android.graphics.drawable.AnimatedVectorDrawable#draw",
        "android.graphics.fonts.Font$Builder#nGetAssetBuffer",
        "android.graphics.fonts.Font$Builder#nGetNativeAsset",
        "android.graphics.fonts.Font$Builder#nGetReleaseNativeAssetFunc",
        "android.graphics.fonts.SystemFonts#buildSystemFallback",
        "android.os.Binder#getNativeBBinderHolder",
        "android.os.Binder#getNativeFinalizer",
        "android.os.Handler#sendMessageAtTime",
        "android.os.HandlerThread#run",
        "android.preference.Preference#getView",
        "android.provider.DeviceConfig#getBoolean",
        "android.provider.DeviceConfig#getFloat",
        "android.provider.DeviceConfig#getInt",
        "android.provider.DeviceConfig#getLong",
        "android.provider.DeviceConfig#getString",
        "android.text.format.DateFormat#is24HourFormat",
        "android.util.Xml#newPullParser",
        "android.view.Choreographer#getRefreshRate",
        "android.view.Display#getWindowManager",
        "android.view.Display#updateDisplayInfoLocked",
        "android.view.HandlerActionQueue#postDelayed",
        "android.view.LayoutInflater#initPrecompiledViews",
        "android.view.LayoutInflater#parseInclude",
        "android.view.LayoutInflater#rInflate",
        "android.view.MenuInflater#registerMenu",
        "android.view.PointerIcon#loadResource",
        "android.view.PointerIcon#registerDisplayListener",
        "android.view.SurfaceControl#nativeCreateTransaction",
        "android.view.TextureView#getTextureLayer",
        "android.view.View#draw",
        "android.view.View#dispatchDetachedFromWindow",
        "android.view.View#getWindowToken",
        "android.view.View#isInEditMode",
        "android.view.View#layout",
        "android.view.View#measure",
        "android.view.ViewRootImpl#isInTouchMode",
        "android.view.WindowManagerGlobal#getWindowManagerService",
        "android.view.inputmethod.InputMethodManager#isInEditMode",
        "com.android.internal.util.XmlUtils#convertValueToInt",
        "com.android.internal.view.menu.MenuBuilder#createNewMenuItem",
        "dalvik.system.VMRuntime#getNotifyNativeInterval",
        "dalvik.system.VMRuntime#newUnpaddedArray",
        "libcore.io.MemoryMappedFile#bigEndianIterator",
        "libcore.io.MemoryMappedFile#close",
        "libcore.io.MemoryMappedFile#mmapRO",
        "libcore.util.NativeAllocationRegistry#applyFreeFunction",
        "libcore.util.NativeAllocationRegistry#registerNativeAllocation",
    };

    /**
     * The list of classes on which to delegate all native methods.
     */
    public final static String[] DELEGATE_CLASS_NATIVES = NativeConfig.DELEGATE_CLASS_NATIVES;

    public final static String[] DELEGATE_CLASS_NATIVES_TO_NATIVES =
            NativeConfig.DELEGATE_CLASS_NATIVES_TO_NATIVES;

    /**
     * The list of classes on which NOT to delegate any native method.
     */
    public final static String[] KEEP_CLASS_NATIVES = new String[] {
        "android.animation.PropertyValuesHolder",
        "android.graphics.BaseCanvas",
        "android.graphics.BaseRecordingCanvas",
        "android.graphics.Bitmap",
        "android.graphics.BitmapFactory",
        "android.graphics.BitmapShader",
        "android.graphics.BlendModeColorFilter",
        "android.graphics.BlurMaskFilter",
        "android.graphics.Canvas",
        "android.graphics.Color",
        "android.graphics.ColorFilter",
        "android.graphics.ColorMatrixColorFilter",
        "android.graphics.ColorSpace$Rgb",
        "android.graphics.ComposePathEffect",
        "android.graphics.ComposeShader",
        "android.graphics.CornerPathEffect",
        "android.graphics.DashPathEffect",
        "android.graphics.DiscretePathEffect",
        "android.graphics.DrawFilter",
        "android.graphics.EmbossMaskFilter",
        "android.graphics.HardwareRenderer",
        "android.graphics.Interpolator",
        "android.graphics.LightingColorFilter",
        "android.graphics.LinearGradient",
        "android.graphics.MaskFilter",
        "android.graphics.NinePatch",
        "android.graphics.Paint",
        "android.graphics.PaintFlagsDrawFilter",
        "android.graphics.PathDashPathEffect",
        "android.graphics.PathEffect",
        "android.graphics.PathMeasure",
        "android.graphics.Picture",
        "android.graphics.PorterDuffColorFilter",
        "android.graphics.RadialGradient",
        "android.graphics.RecordingCanvas",
        "android.graphics.Region",
        "android.graphics.RegionIterator",
        "android.graphics.RenderNode",
        "android.graphics.Shader",
        "android.graphics.SumPathEffect",
        "android.graphics.SweepGradient",
        "android.graphics.TableMaskFilter",
        "android.graphics.drawable.AnimatedVectorDrawable",
        "android.graphics.drawable.VectorDrawable",
        "android.graphics.fonts.SystemFonts",
        "android.graphics.text.MeasuredText",
        "android.graphics.text.MeasuredText$Builder",
        "android.media.ImageReader",
        "android.media.ImageReader$SurfaceImage",
        "android.os.SystemProperties",
        "android.util.Log",
        "android.util.PathParser",
        "android.view.Surface",
        "com.android.internal.view.animation.NativeInterpolatorFactoryHelper",
        "com.android.internal.util.VirtualRefBasePtr",
    };

    /**
     *  The list of classes to rename, must be an even list: the binary FQCN
     *  of class to replace followed by the new FQCN.
     */
    private final static String[] RENAMED_CLASSES =
        new String[] {
            "android.os.ServiceManager",                       "android.os._Original_ServiceManager",
            "android.view.textservice.TextServicesManager",    "android.view.textservice._Original_TextServicesManager",
            "android.view.SurfaceView",                        "android.view._Original_SurfaceView",
            "android.view.accessibility.AccessibilityManager", "android.view.accessibility._Original_AccessibilityManager",
            "android.view.accessibility.AccessibilityNodeIdManager", "android.view.accessibility._Original_AccessibilityNodeIdManager",
            "android.webkit.WebView",                          "android.webkit._Original_WebView",
        };

    /**
     * The list of class references to update, must be an even list: the binary
     * FQCN of class to replace followed by the new FQCN. The classes to
     * replace are to be excluded from the output.
     */
    private final static String[] JAVA_PKG_CLASSES =
        new String[] {
        };

    /**
     * List of classes to refactor. This is similar to combining {@link #getRenamedClasses()} and
     * {@link #getJavaPkgClasses()}.
     * Classes included here will be renamed and then all their references in any other classes
     * will be also modified.
     * FQCN of class to refactor followed by its new FQCN.
     */
    private final static String[] REFACTOR_CLASSES =
            new String[] {
                    "android.os.Build",                                "android.os._Original_Build",
            };

    private final static String[] EXCLUDED_CLASSES =
        new String[] {
            "android.preference.PreferenceActivity",
            "java.**",
            "org.kxml2.io.KXmlParser",
            "org.xmlpull.**",
            "sun.**",
        };

    /**
     * List of fields for which we will update the visibility to be public. This is sometimes
     * needed when access from the delegate classes is needed.
     */
    private final static String[] PROMOTED_FIELDS = new String[] {
        "android.animation.AnimatorSet#mLastFrameTime",
        "android.animation.PropertyValuesHolder#sSetterPropertyMap",
        "android.animation.PropertyValuesHolder#sGetterPropertyMap",
        "android.animation.PropertyValuesHolder$IntPropertyValuesHolder#sJNISetterPropertyMap",
        "android.animation.PropertyValuesHolder$FloatPropertyValuesHolder#sJNISetterPropertyMap",
        "android.animation.PropertyValuesHolder$MultiFloatValuesHolder#sJNISetterPropertyMap",
        "android.animation.PropertyValuesHolder$MultiIntValuesHolder#sJNISetterPropertyMap",
        "android.graphics.Typeface#DEFAULT_FAMILY",
        "android.graphics.Typeface#sDynamicTypefaceCache",
        "android.graphics.drawable.AnimatedVectorDrawable$VectorDrawableAnimatorUI#mSet",
        "android.graphics.drawable.AnimatedVectorDrawable$VectorDrawableAnimatorRT#mPendingAnimationActions",
        "android.graphics.drawable.AnimatedVectorDrawable#mAnimatorSet",
        "android.graphics.drawable.AdaptiveIconDrawable#sMask",
        "libcore.util.NativeAllocationRegistry#freeFunction",
        "libcore.util.NativeAllocationRegistry#size",
    };

    /**
     * List of methods for which we will update the visibility to be public.
     */
    private final static String[] PROMOTED_METHODS = new String[] {
        "android.animation.AnimationHandler#doAnimationFrame",
        "android.media.ImageReader#nativeClassInit"
    };

    /**
     * List of classes to be promoted to public visibility. Prefer using PROMOTED_FIELDS to this
     * if possible.
     */
    private final static String[] PROMOTED_CLASSES = new String[] {
        "android.graphics.drawable.AnimatedVectorDrawable$VectorDrawableAnimatorUI",
        "android.graphics.drawable.AnimatedVectorDrawable$VectorDrawableAnimator",
        "libcore.util.NativeAllocationRegistry$CleanerRunner",
        "libcore.util.NativeAllocationRegistry$CleanerThunk",
    };

    /**
     * List of classes for which the methods returning them should be deleted.
     * The array contains a list of null terminated section starting with the name of the class
     * to rename in which the methods are deleted, followed by a list of return types identifying
     * the methods to delete.
     */
    private final static String[] DELETE_RETURNS =
        new String[] {
            null };                         // separator, for next class/methods list.



    private final static String[] DEFERRED_STATIC_INITIALIZER_CLASSES = new String[] {

    };

    private final static Map<String, InjectMethodRunnable> INJECTED_METHODS =
            new HashMap<String, InjectMethodRunnable>(1) {{
                put("android.content.Context",
                        InjectMethodRunnables.CONTEXT_GET_FRAMEWORK_CLASS_LOADER);
            }};

    public static class LinkedHashMapEldestReplacer implements MethodReplacer {

        private final String VOID_TO_MAP_ENTRY =
                Type.getMethodDescriptor(Type.getType(Map.Entry.class));
        private final String LINKED_HASH_MAP = Type.getInternalName(LinkedHashMap.class);

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return LINKED_HASH_MAP.equals(owner) &&
                    "eldest".equals(name) &&
                    VOID_TO_MAP_ENTRY.equals(desc);
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.opcode = Opcodes.INVOKESTATIC;
            mi.owner = Type.getInternalName(LinkedHashMap_Delegate.class);
            mi.desc = Type.getMethodDescriptor(
                    Type.getType(Map.Entry.class), Type.getType(LinkedHashMap.class));
        }
    }

    private static class ContextGetClassLoaderReplacer implements MethodReplacer {
        // When LayoutInflater asks for a class loader, we must return the class loader that
        // cannot return app's custom views/classes. This is so that in case of any failure
        // or exception when instantiating the views, the IDE can replace it with a mock view
        // and have proper error handling. However, if a custom view asks for the class
        // loader, we must return a class loader that can find app's custom views as well.
        // Thus, we rewrite the call to get class loader in LayoutInflater to
        // getFrameworkClassLoader and inject a new method in Context. This leaves the normal
        // method: Context.getClassLoader() free to be used by the apps.
        private final String VOID_TO_CLASS_LOADER =
                Type.getMethodDescriptor(Type.getType(ClassLoader.class));

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return owner.equals("android/content/Context") &&
                    sourceClass.equals("android/view/LayoutInflater") &&
                    name.equals("getClassLoader") &&
                    desc.equals(VOID_TO_CLASS_LOADER);
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.name = "getFrameworkClassLoader";
        }
    }

    private static class SystemCurrentTimeMillisReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(System.class).equals(owner) && name.equals("currentTimeMillis");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.name = "currentTimeMillis";
            mi.owner = Type.getInternalName(System_Delegate.class);
        }
    }

    private static class SystemNanoTimeReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(System.class).equals(owner) && name.equals("nanoTime");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.name = "nanoTime";
            mi.owner = Type.getInternalName(System_Delegate.class);
        }
    }

    public static class SystemLogReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(System.class).equals(owner) && name.length() == 4
                    && name.startsWith("log");
        }

        @Override
        public void replace(MethodInformation mi) {
            assert mi.desc.equals("(Ljava/lang/String;Ljava/lang/Throwable;)V")
                    || mi.desc.equals("(Ljava/lang/String;)V");
            mi.name = "log";
            mi.owner = Type.getInternalName(System_Delegate.class);
        }
    }

    /**
     * Platform code should not loadLibrary on its own. Layoutlib loading infrastructure takes case
     * of loading all the necessary native libraries (having the right paths etc.)
     */
    public static class SystemLoadLibraryReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(System.class).equals(owner) && name.equals("loadLibrary");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = Type.getInternalName(System_Delegate.class);
        }
    }

    /**
     * This is to replace a static call to a dummy, so that ImageReader can be loaded and accessed
     * during JNI loading
     */
    public static class ImageReaderNativeInitReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return "android/media/ImageReader".equals(owner) && name.equals("nativeClassInit");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = "android/media/ImageReader_Delegate";
            mi.opcode = Opcodes.INVOKESTATIC;
        }
    }

    private static class LocaleGetDefaultReplacer implements MethodReplacer {

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(Locale.class).equals(owner)
                    && "getDefault".equals(name)
                    && desc.equals(Type.getMethodDescriptor(Type.getType(Locale.class)));
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = "com/android/layoutlib/bridge/android/AndroidLocale";
        }
    }

    public static class LocaleAdjustLanguageCodeReplacer implements MethodReplacer {

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(java.util.Locale.class).equals(owner)
                    && ("adjustLanguageCode".equals(name)
                    && desc.equals(Type.getMethodDescriptor(Type.getType(String.class), Type.getType(String.class))));
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = "com/android/tools/layoutlib/java/util/LocaleAdjustLanguageCodeReplacement";
        }
    }

    private static class SystemArrayCopyReplacer implements MethodReplacer {
        /**
         * Descriptors for specialized versions {@link System#arraycopy} that are not present on the
         * Desktop VM.
         */
        private static Set<String> ARRAYCOPY_DESCRIPTORS = new HashSet<>(Arrays.asList(
                "([CI[CII)V", "([BI[BII)V", "([SI[SII)V", "([II[III)V",
                "([JI[JII)V", "([FI[FII)V", "([DI[DII)V", "([ZI[ZII)V"));

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(System.class).equals(owner) && "arraycopy".equals(name) &&
                    ARRAYCOPY_DESCRIPTORS.contains(desc);
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.desc = "(Ljava/lang/Object;ILjava/lang/Object;II)V";
        }
    }

    public static class DateFormatSet24HourTimePrefReplacer implements MethodReplacer {

        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return Type.getInternalName(DateFormat.class).equals(owner) &&
                    "set24HourTimePref".equals(name);
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = "com/android/tools/layoutlib/java/text/DateFormat_Delegate";
        }
    }

    public static class NioUtilsFreeBufferReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return "java/nio/NioUtils".equals(owner) && name.equals("freeDirectBuffer");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = Type.getInternalName(NioUtils_Delegate.class);
        }
    }

    public static class ProcessInitializerInitSchedReplacer implements MethodReplacer {
        @Override
        public boolean isNeeded(String owner, String name, String desc, String sourceClass) {
            return "android/graphics/HardwareRenderer$ProcessInitializer".equals(owner) &&
                    name.equals("initSched");
        }

        @Override
        public void replace(MethodInformation mi) {
            mi.owner = "android/graphics/HardwareRenderer_ProcessInitializer_Delegate";
            mi.opcode = Opcodes.INVOKESTATIC;
            mi.desc = "(J)V";
        }
    }
}
