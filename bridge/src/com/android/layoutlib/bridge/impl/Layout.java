/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.layoutlib.bridge.impl;

import com.android.ide.common.rendering.api.HardwareConfig;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.android.RenderParamsFlags;
import com.android.layoutlib.bridge.bars.AppCompatActionBar;
import com.android.layoutlib.bridge.bars.BridgeActionBar;
import com.android.layoutlib.bridge.bars.Config;
import com.android.layoutlib.bridge.bars.FrameworkActionBar;
import com.android.layoutlib.bridge.bars.NavigationBar;
import com.android.layoutlib.bridge.bars.NavigationHandle;
import com.android.layoutlib.bridge.bars.StatusBar;
import com.android.layoutlib.bridge.bars.TitleBar;
import com.android.resources.Density;
import com.android.resources.ResourceType;
import com.android.resources.ScreenOrientation;

import android.R.id;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.AttachInfo_Accessor;
import android.view.InsetsFrameProvider;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.ViewRootImpl_Accessor;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os._Original_Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import static com.android.layoutlib.bridge.android.RenderParamsFlags.FLAG_KEY_EDGE_TO_EDGE;
import static com.android.layoutlib.bridge.android.RenderParamsFlags.FLAG_KEY_USE_GESTURE_NAV;
import static com.android.layoutlib.bridge.bars.Config.isGreaterOrEqual;
import static com.android.layoutlib.bridge.impl.ResourceHelper.getBooleanThemeFrameworkAttrValue;
import static com.android.layoutlib.bridge.impl.ResourceHelper.getBooleanThemeValue;
import static com.android.layoutlib.bridge.util.InsetUtil.getNavBarLayoutParamsForRotation;

/**
 * The Layout used to create the system decor.
 * <p>
 * The layout inflated will contain a content frame where the user's layout can be inflated.
 * <pre>
 *  +-------------------------------------------------+---+
 *  | Status bar                                      | N |
 *  +-------------------------------------------------+ a |
 *  | Title/Framework Action bar (optional)           | v |
 *  +-------------------------------------------------+   |
 *  | AppCompat Action bar (optional)                 |   |
 *  +-------------------------------------------------+   |
 *  | Content, vertical extending                     | b |
 *  |                                                 | a |
 *  |                                                 | r |
 *  +-------------------------------------------------+---+
 * </pre>
 * or
 * <pre>
 *  +--------------------------------------+
 *  | Status bar                           |
 *  +--------------------------------------+
 *  | Title/Framework Action bar (optional)|
 *  +--------------------------------------+
 *  | AppCompat Action bar (optional)      |
 *  +--------------------------------------+
 *  | Content, vertical extending          |
 *  |                                      |
 *  |                                      |
 *  +--------------------------------------+
 *  | Nav bar                              |
 *  +--------------------------------------+
 * </pre>
 */
public class Layout extends FrameLayout {

    // Theme attributes used for configuring appearance of the system decor.
    private static final String ATTR_WINDOW_FLOATING = "windowIsFloating";
    private static final String ATTR_WINDOW_BACKGROUND = "windowBackground";
    private static final String ATTR_WINDOW_FULL_SCREEN = "windowFullscreen";
    private static final String ATTR_NAV_BAR_HEIGHT = "navigation_bar_height";
    private static final String ATTR_NAV_BAR_WIDTH = "navigation_bar_width";
    private static final String ATTR_STATUS_BAR_HEIGHT = "status_bar_height";
    private static final String ATTR_WINDOW_ACTION_BAR = "windowActionBar";
    private static final String ATTR_ACTION_BAR_SIZE = "actionBarSize";
    private static final String ATTR_WINDOW_NO_TITLE = "windowNoTitle";
    private static final String ATTR_WINDOW_TITLE_SIZE = "windowTitleSize";
    private static final String ATTR_WINDOW_TRANSLUCENT_STATUS = StatusBar.ATTR_TRANSLUCENT;
    private static final String ATTR_WINDOW_TRANSLUCENT_NAV = NavigationBar.ATTR_TRANSLUCENT;

    // Default sizes
    private static final int DEFAULT_STATUS_BAR_HEIGHT = 25;
    private static final int DEFAULT_TITLE_BAR_HEIGHT = 25;
    private static final int DEFAULT_NAV_BAR_SIZE = 48;

    // Ids assigned to components created. This is so that we can refer to other components in
    // layout params.
    private static final String ID_NAV_BAR = "navBar";
    private static final String ID_STATUS_BAR = "statusBar";
    private static final String ID_APP_COMPAT_ACTION_BAR = "appCompatActionBar";
    private static final String ID_FRAMEWORK_BAR = "frameworkBar";
    // Prefix used with the above ids in order to make them unique in framework namespace.
    private static final String ID_PREFIX = "android_layoutlib_";

    private final List<InsetsFrameProvider> mInsetsFrameProviders = new ArrayList<>();

    /**
     * Temporarily store the builder so that it doesn't have to be passed to all methods used
     * during inflation.
     */
    private Builder mBuilder;

    /**
     * App UI layout
     */
    private final RelativeLayout mAppUiRoot;

    /**
     * This holds user's layout.
     */
    private FrameLayout mContentRoot;

    public Layout(@NonNull Builder builder) {
        super(builder.mContext);

        mBuilder = builder;
        View frameworkActionBar = null;
        View appCompatActionBar = null;
        TitleBar titleBar = null;
        StatusBar statusBar = null;
        View navBar = null;

        if (builder.mWindowBackground != null) {
            Drawable d = ResourceHelper.getDrawable(builder.mWindowBackground, builder.mContext,
                    builder.mContext.getTheme());
            setBackground(d);
        }

        int simulatedPlatformVersion = getParams().getSimulatedPlatformVersion();
        HardwareConfig hwConfig = getParams().getHardwareConfig();
        Density density = hwConfig.getDensity();
        boolean isRtl = Bridge.isLocaleRtl(getParams().getLocale());
        setLayoutDirection(isRtl ? LAYOUT_DIRECTION_RTL : LAYOUT_DIRECTION_LTR);

        if (mBuilder.hasNavBar()) {
            navBar = createNavBar(getContext(), mBuilder.useGestureNav(), density, isRtl,
                    getParams().isRtlSupported(), mBuilder.mIsEdgeToEdge, simulatedPlatformVersion,
                    false);
        }

        if (builder.hasStatusBar()) {
            statusBar = createStatusBar(getContext(), density, isRtl, getParams().isRtlSupported(),
                    mBuilder.mIsEdgeToEdge, simulatedPlatformVersion);
        }

        if (mBuilder.hasAppCompatActionBar()) {
            BridgeActionBar bar =
                    createActionBar(getContext(), getParams(), true, navBar, statusBar);
            mContentRoot = bar.getContentRoot();
            appCompatActionBar = bar.getRootView();
        }

        // Title bar must appear on top of the Action bar
        if (mBuilder.hasTitleBar()) {
            titleBar = createTitleBar(getContext(), getParams().getAppLabel(),
                    simulatedPlatformVersion, navBar, statusBar);
        } else if (mBuilder.hasFrameworkActionBar()) {
            BridgeActionBar bar =
                    createActionBar(getContext(), getParams(), false, navBar, statusBar);
            if (mContentRoot == null) {
                // We only set the content root if the AppCompat action bar did not already
                // provide it
                mContentRoot = bar.getContentRoot();
            }
            frameworkActionBar = bar.getRootView();
        }

        mAppUiRoot = new RelativeLayout(builder.mContext);
        addAppUiViews(titleBar,
                mContentRoot == null ? (mContentRoot = createContentFrame(navBar, statusBar)) :
                        frameworkActionBar, appCompatActionBar);
        addView(mAppUiRoot);
        //addView(createSysUiOverlay(mBuilder.mContext));

        LinearLayout sysUiRoot = buildSysUi(statusBar, navBar);
        if (sysUiRoot != null) {
            addView(sysUiRoot, MATCH_PARENT, MATCH_PARENT);
        }
        // Done with the builder. Don't hold a reference to it.
        mBuilder = null;
    }

    @Nullable
    private LinearLayout buildSysUi(@Nullable StatusBar statusBar, @Nullable View navBar) {
        if (statusBar == null && navBar == null) {
            return null;
        }
        LinearLayout sysUiRoot = new LinearLayout(mBuilder.mContext);
        if (navBar == null) {
            sysUiRoot.addView(statusBar);
        } else if (statusBar == null) {
            sysUiRoot.addView(navBar);
        } else {
            ViewGroup statusBarContainer = new FrameLayout(mBuilder.mContext);
            statusBarContainer.addView(statusBar);
            sysUiRoot.addView(statusBarContainer,
                    new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f));

            if (!mBuilder.useGestureNav() && mBuilder.mNavBarOrientation == VERTICAL) {
                sysUiRoot.setOrientation(HORIZONTAL);
            } else {
                sysUiRoot.setOrientation(VERTICAL);
            }
            sysUiRoot.addView(navBar);
        }
        return sysUiRoot;
    }

    @Override
    public boolean getChildVisibleRect(View child, Rect r, Point offset, boolean forceParentCheck) {
        return r.intersect(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        int width = mRight - mLeft;
        int height = mBottom - mTop;
        if (width > 0 && height > 0) {
            r.set(0, 0, width, height);
            if (globalOffset != null) {
                globalOffset.set(-mScrollX, -mScrollY);
            }
            return true;
        }
        return false;
    }

    @NonNull
    private static View createSysUiOverlay(@NonNull BridgeContext context) {
        SysUiOverlay overlay =  new SysUiOverlay(context, 20, 10, 50, 40, 60);
        overlay.setNotchColor(Color.BLACK);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return overlay;
    }

    @NonNull
    private FrameLayout createContentFrame(@Nullable View navBar, @Nullable StatusBar statusBar) {
        FrameLayout contentRoot = new FrameLayout(getContext());
        RelativeLayout.LayoutParams params = createAppUiLayoutParams(MATCH_PARENT, MATCH_PARENT);
        if (navBar != null && mBuilder.hasSolidNavBar()) {
            if (mBuilder.isNavBarVertical()) {
                params.bottomMargin = navBar.getLayoutParams().height;
            } else {
                params.rightMargin = navBar.getLayoutParams().width;
            }
        }
        if (!mBuilder.mIsEdgeToEdge) {
            int below = -1;
            if (mBuilder.mAppCompatActionBarSize > 0) {
                below = getId(ID_APP_COMPAT_ACTION_BAR);
            } else if (mBuilder.hasFrameworkActionBar() || mBuilder.hasTitleBar()) {
                below = getId(ID_FRAMEWORK_BAR);
            } else if (statusBar != null && mBuilder.hasSolidStatusBar()) {
                params.topMargin = statusBar.getLayoutParams().height;
            }
            if (below != -1) {
                params.addRule(RelativeLayout.BELOW, below);
            }
        }
        contentRoot.setLayoutParams(params);
        contentRoot.setId(id.content);
        return contentRoot;
    }

    @NonNull
    private RelativeLayout.LayoutParams createAppUiLayoutParams(int width, int height) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (width > 0) {
            width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
        }
        if (height > 0) {
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);
        }
        return new RelativeLayout.LayoutParams(width, height);
    }

    @NonNull
    public FrameLayout getContentRoot() {
        return mContentRoot;
    }

    @NonNull
    private SessionParams getParams() {
        return mBuilder.mParams;
    }

    @NonNull
    @Override
    public BridgeContext getContext() {
        return (BridgeContext) super.getContext();
    }

    @NonNull
    public List<InsetsFrameProvider> getInsetsFrameProviders() {
        return mInsetsFrameProviders;
    }

    /**
     * @param isRtl whether the current locale is an RTL locale.
     * @param isRtlSupported whether the applications supports RTL (i.e. has supportsRtl=true in the
     * manifest and targetSdkVersion >= 17.
     */
    @NonNull
    private StatusBar createStatusBar(BridgeContext context, Density density, boolean isRtl,
            boolean isRtlSupported, boolean isEdgeToEdge, int simulatedPlatformVersion) {
        StatusBar statusBar = new StatusBar(context, density, isRtl, isRtlSupported, isEdgeToEdge,
                simulatedPlatformVersion);
        statusBar.setId(getId(ID_STATUS_BAR));
        WindowManager.LayoutParams layoutParams = statusBar.getBarLayoutParams();
        mInsetsFrameProviders.addAll(Arrays.asList(layoutParams.providedInsets));
        FrameLayout.LayoutParams lparams = new FrameLayout.LayoutParams(layoutParams);
        lparams.gravity = layoutParams.gravity;
        statusBar.setLayoutParams(lparams);
        return statusBar;
    }

    private BridgeActionBar createActionBar(@NonNull BridgeContext context,
            @NonNull SessionParams params, boolean appCompatActionBar, @Nullable View navBar,
            @Nullable StatusBar statusBar) {
        boolean isMenu = "menu".equals(params.getFlag(RenderParamsFlags.FLAG_KEY_ROOT_TAG));
        String id;

        // For the framework action bar, we set the height to MATCH_PARENT only if there is no
        // AppCompat ActionBar below it
        int heightRule = appCompatActionBar || !mBuilder.hasAppCompatActionBar() ? MATCH_PARENT :
                WRAP_CONTENT;
        RelativeLayout.LayoutParams layoutParams =
                createAppUiLayoutParams(MATCH_PARENT, heightRule);
        if (navBar != null && mBuilder.hasSolidNavBar()) {
            // If there
            if (mBuilder.isNavBarVertical()) {
                layoutParams.rightMargin = navBar.getLayoutParams().width;
            } else if (appCompatActionBar || !mBuilder.hasAppCompatActionBar()) {
                layoutParams.bottomMargin = navBar.getLayoutParams().height;
            }
        }


        BridgeActionBar actionBar;
        if (appCompatActionBar && !isMenu) {
            actionBar = new AppCompatActionBar(context, params);
            id = ID_APP_COMPAT_ACTION_BAR;

            if (mBuilder.hasTitleBar() || mBuilder.hasFrameworkActionBar()) {
                layoutParams.addRule(RelativeLayout.BELOW, getId(ID_FRAMEWORK_BAR));
            } else if (statusBar != null && mBuilder.hasSolidStatusBar()) {
                layoutParams.topMargin = statusBar.getLayoutParams().height;
            }
        } else {
            actionBar = new FrameworkActionBar(context, params);
            id = ID_FRAMEWORK_BAR;
            if (statusBar != null && mBuilder.hasSolidStatusBar()) {
                layoutParams.topMargin = statusBar.getLayoutParams().height;
            }
        }

        actionBar.getRootView().setLayoutParams(layoutParams);
        actionBar.getRootView().setId(getId(id));
        actionBar.createMenuPopup();
        return actionBar;
    }

    @NonNull
    private TitleBar createTitleBar(BridgeContext context, String title,
            int simulatedPlatformVersion, @Nullable View navBar, @Nullable StatusBar statusBar) {
        TitleBar titleBar = new TitleBar(context, title, simulatedPlatformVersion);
        RelativeLayout.LayoutParams params =
                createAppUiLayoutParams(MATCH_PARENT, mBuilder.mTitleBarSize);
        if (statusBar != null && mBuilder.hasSolidStatusBar()) {
            params.topMargin = statusBar.getLayoutParams().height;
        }
        if (navBar != null && mBuilder.isNavBarVertical() && mBuilder.hasSolidNavBar()) {
            params.rightMargin = navBar.getLayoutParams().width;
        }
        titleBar.setLayoutParams(params);
        titleBar.setId(getId(ID_FRAMEWORK_BAR));
        return titleBar;
    }

    /**
     * @param useGestureNav whether the system UI is using gesture navigation.
     * @param isRtl whether the current locale is an RTL locale.
     * @param isRtlSupported whether the applications supports RTL (i.e. has supportsRtl=true in the
     * manifest and targetSdkVersion >= 17.
     */
    @NonNull
    private View createNavBar(BridgeContext context, boolean useGestureNav, Density density,
            boolean isRtl, boolean isRtlSupported, boolean isEdgeToEdge,
            int simulatedPlatformVersion, boolean isQuickStepEnabled) {
        int rotation = Surface.ROTATION_0;
        // Only allow quickstep in the latest version or >= 28
        isQuickStepEnabled = isQuickStepEnabled &&
                (simulatedPlatformVersion == 0 || simulatedPlatformVersion >= 28);
        View navBar;
        if (useGestureNav) {
            navBar = new NavigationHandle(context);
        } else {
            navBar = new NavigationBar(context, density, mBuilder.mNavBarOrientation, isRtl,
                    isRtlSupported, isEdgeToEdge, simulatedPlatformVersion, isQuickStepEnabled);
            if (mBuilder.mNavBarOrientation == VERTICAL) {
                rotation = Surface.ROTATION_90;
            }
        }
        WindowManager.LayoutParams layoutParams =
                getNavBarLayoutParamsForRotation(mBuilder.mContext, navBar, rotation);
        mInsetsFrameProviders.addAll(Arrays.asList(layoutParams.providedInsets));
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(layoutParams);
        lparams.gravity = layoutParams.gravity;
        navBar.setLayoutParams(lparams);
        navBar.setId(getId(ID_NAV_BAR));
        return navBar;
    }

    private void addAppUiViews(@NonNull View... views) {
        for (View view : views) {
            if (view != null) {
                mAppUiRoot.addView(view);
            }
        }
    }

    private int getId(String name) {
        return Bridge.getResourceId(ResourceType.ID, ID_PREFIX + name);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void requestFitSystemWindows() {
        // The framework call would usually bubble up to ViewRootImpl but, in layoutlib, Layout will
        // act as view root for most purposes. That way, we can also save going through the Handler
        // to dispatch the new applied insets.
        ViewRootImpl root = AttachInfo_Accessor.getRootView(this);
        if (root != null) {
            ViewRootImpl_Accessor.dispatchApplyInsets(root, this);
        }
    }

    /**
     * A helper class to help initialize the Layout.
     */
    static class Builder {
        @NonNull
        private final SessionParams mParams;
        @NonNull
        private final BridgeContext mContext;
        private final RenderResources mResources;

        private final boolean mWindowIsFloating;
        private ResourceValue mWindowBackground;
        private int mStatusBarSize;
        private int mNavBarSize;
        private int mNavBarOrientation;
        private int mAppCompatActionBarSize;
        private int mFrameworkActionBarSize;
        private int mTitleBarSize;
        private boolean mTranslucentStatus;
        private boolean mTranslucentNav;
        private boolean mUseGestureNav;
        private boolean mIsEdgeToEdge;

        public Builder(@NonNull SessionParams params, @NonNull BridgeContext context) {
            mParams = params;
            mContext = context;
            mResources = mParams.getResources();
            mWindowIsFloating =
                    getBooleanThemeFrameworkAttrValue(mResources, ATTR_WINDOW_FLOATING, true);

            findBackground();

            if (!mParams.isForceNoDecor()) {
                mIsEdgeToEdge = isGreaterOrEqual(mParams.getSimulatedPlatformVersion(),
                        VANILLA_ICE_CREAM) ||
                        Boolean.TRUE.equals(mParams.getFlag(FLAG_KEY_EDGE_TO_EDGE));
                findStatusBar();
                findFrameworkBar();
                findAppCompatActionBar();
                findNavBar();
            }
        }

        private void findBackground() {
            if (!mParams.isTransparentBackground()) {
                mWindowBackground = mResources.findItemInTheme(
                        BridgeContext.createFrameworkAttrReference(ATTR_WINDOW_BACKGROUND));
                mWindowBackground = mResources.resolveResValue(mWindowBackground);
            }
        }

        private void findStatusBar() {
            boolean windowFullScreen =
                    getBooleanThemeFrameworkAttrValue(mResources, ATTR_WINDOW_FULL_SCREEN, false);
            if (!windowFullScreen && !mWindowIsFloating) {
                mStatusBarSize =
                        getFrameworkAttrDimension(ATTR_STATUS_BAR_HEIGHT, DEFAULT_STATUS_BAR_HEIGHT);
                mTranslucentStatus =
                        getBooleanThemeFrameworkAttrValue(
                                mResources, ATTR_WINDOW_TRANSLUCENT_STATUS, false);
            }
        }

        /**
         * The behavior is different whether the App is using AppCompat or not.
         * <h1>With App compat :</h1>
         * <li> framework ("android:") attributes have to effect
         * <li> windowNoTile=true hides the AppCompatActionBar
         * <li> windowActionBar=false throws an exception
         */
        private void findAppCompatActionBar() {
            if (mWindowIsFloating || !mContext.isAppCompatTheme()) {
                return;
            }

            boolean windowNoTitle =
                    getBooleanThemeValue(mResources,
                            mContext.createAppCompatAttrReference(ATTR_WINDOW_NO_TITLE), false);

            boolean windowActionBar =
                    getBooleanThemeValue(mResources,
                            mContext.createAppCompatAttrReference(ATTR_WINDOW_ACTION_BAR), true);

            if (!windowNoTitle && windowActionBar) {
                mAppCompatActionBarSize =
                        getDimension(mContext.createAppCompatAttrReference(ATTR_ACTION_BAR_SIZE),
                                DEFAULT_TITLE_BAR_HEIGHT);
            }
        }

        /**
         * Find if we should show either the titleBar or the framework ActionBar
         * <p>
         * <h1> Without App compat :</h1>
         * <li> windowNoTitle has no effect
         * <li> android:windowNoTile=true hides the <b>ActionBar</b>
         * <li> android:windowActionBar=true/false toggles between ActionBar/TitleBar
         * </ul>
         * <pre>
         * +------------------------------------------------------------+
         * |               |         android:windowNoTitle              |
         * |android:       |    TRUE             |      FALSE           |
         * |windowActionBar|---------------------+----------------------+
         * |    TRUE       | Nothing             | ActionBar (Default)  |
         * |    FALSE      | Nothing             | TitleBar             |
         * +---------------+--------------------------------------------+
         * </pre>
         *
         * @see #findAppCompatActionBar()
         */
        private void findFrameworkBar() {
            if (mWindowIsFloating) {
                return;
            }
            boolean frameworkWindowNoTitle =
                    getBooleanThemeFrameworkAttrValue(mResources, ATTR_WINDOW_NO_TITLE, false);

            // Check if an actionbar is needed
            boolean isMenu = "menu".equals(mParams.getFlag(RenderParamsFlags.FLAG_KEY_ROOT_TAG));

            boolean windowActionBar =
                    getBooleanThemeFrameworkAttrValue(mResources, ATTR_WINDOW_ACTION_BAR, true);

            if (!frameworkWindowNoTitle || isMenu) {
                if (isMenu || windowActionBar) {
                    mFrameworkActionBarSize =
                            getFrameworkAttrDimension(ATTR_ACTION_BAR_SIZE, DEFAULT_TITLE_BAR_HEIGHT);
                } else {
                    mTitleBarSize = getDimension(
                            mContext.createAppCompatAttrReference(ATTR_WINDOW_TITLE_SIZE),
                            DEFAULT_TITLE_BAR_HEIGHT);
                }
            }
        }

        private void findNavBar() {
            if (hasSoftwareButtons() && !mWindowIsFloating) {
                mUseGestureNav = Boolean.TRUE.equals(mParams.getFlag(FLAG_KEY_USE_GESTURE_NAV));
                // get orientation
                HardwareConfig hwConfig = mParams.getHardwareConfig();
                boolean barOnBottom = true;

                if (hwConfig.getOrientation() == ScreenOrientation.LANDSCAPE && !mUseGestureNav) {
                    int shortSize = hwConfig.getScreenHeight();
                    int shortSizeDp = shortSize * DisplayMetrics.DENSITY_DEFAULT /
                            hwConfig.getDensity().getDpiValue();

                    // 0-599dp: "phone" UI with bar on the side
                    // 600+dp: "tablet" UI with bar on the bottom
                    barOnBottom = shortSizeDp >= 600;
                }

                mNavBarOrientation = barOnBottom ? LinearLayout.HORIZONTAL : VERTICAL;
                mNavBarSize =
                        getFrameworkAttrDimension(
                                barOnBottom ? ATTR_NAV_BAR_HEIGHT : ATTR_NAV_BAR_WIDTH,
                                DEFAULT_NAV_BAR_SIZE);
                mTranslucentNav =
                        getBooleanThemeFrameworkAttrValue(mResources, ATTR_WINDOW_TRANSLUCENT_NAV,
                                false);
            }
        }

        private int getDimension(@NonNull ResourceReference attrRef, int defaultValue) {
            ResourceValue value = mResources.findItemInTheme(attrRef);
            value = mResources.resolveResValue(value);
            if (value != null) {
                TypedValue typedValue = ResourceHelper.getValue(attrRef.getName(), value.getValue(),
                        true);
                if (typedValue != null) {
                    return (int) typedValue.getDimension(mContext.getMetrics());
                }
            }
            return defaultValue;
        }

        private int getFrameworkAttrDimension(@NonNull String attr, int defaultValue) {
            return getDimension(BridgeContext.createFrameworkAttrReference(attr), defaultValue);
        }

        private boolean hasSoftwareButtons() {
            return mParams.getHardwareConfig().hasSoftwareButtons();
        }

        /**
         * Returns true if the nav bar is present and not translucent.
         */
        private boolean hasSolidNavBar() {
            return hasNavBar() && !mTranslucentNav && !mIsEdgeToEdge;
        }

        /**
         * Returns true if the status bar is present and not translucent.
         */
        private boolean hasSolidStatusBar() {
            return hasStatusBar() && !mTranslucentStatus && !mIsEdgeToEdge;
        }

        private boolean hasNavBar() {
            return Config.showOnScreenNavBar(mParams.getSimulatedPlatformVersion()) &&
                    hasSoftwareButtons() && mNavBarSize > 0;
        }

        private boolean useGestureNav() {
            return mUseGestureNav;
        }

        private boolean hasTitleBar() {
            return mTitleBarSize > 0;
        }

        private boolean hasStatusBar() {
            return mStatusBarSize > 0;
        }

        private boolean hasAppCompatActionBar() {
            return mAppCompatActionBarSize > 0;
        }

        /**
         * Return true if the nav bar is present and is vertical.
         */
        private boolean isNavBarVertical() {
            return hasNavBar() && mNavBarOrientation == VERTICAL;
        }

        private boolean hasFrameworkActionBar() {
            return mFrameworkActionBarSize > 0;
        }

        private boolean hasNotch() {
            return !mParams.isForceNoDecor();
        }
    }
}
