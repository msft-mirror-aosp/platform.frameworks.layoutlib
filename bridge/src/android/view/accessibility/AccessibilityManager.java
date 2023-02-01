/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.view.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.AccessibilityServiceInfo.FeedbackType;
import android.accessibilityservice.AccessibilityShortcutInfo;
import android.annotation.FloatRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.RemoteAction;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.view.IWindow;
import android.view.View;
import android.view.accessibility.AccessibilityEvent.EventType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * System level service that serves as an event dispatch for {@link AccessibilityEvent}s.
 * Such events are generated when something notable happens in the user interface,
 * for example an {@link android.app.Activity} starts, the focus or selection of a
 * {@link android.view.View} changes etc. Parties interested in handling accessibility
 * events implement and register an accessibility service which extends
 * {@code android.accessibilityservice.AccessibilityService}.
 *
 * @see AccessibilityEvent
 * @see android.content.Context#getSystemService
 */
@SuppressWarnings("UnusedDeclaration")
public final class AccessibilityManager {
    public static final int STATE_FLAG_ACCESSIBILITY_ENABLED = 0x00000001;
    public static final int STATE_FLAG_TOUCH_EXPLORATION_ENABLED = 0x00000002;
    public static final int STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED = 0x00000004;
    public static final int STATE_FLAG_DISPATCH_DOUBLE_TAP = 0x00000008;
    public static final int STATE_FLAG_REQUEST_MULTI_FINGER_GESTURES = 0x00000010;
    public static final int STATE_FLAG_TRACE_A11Y_INTERACTION_CONNECTION_ENABLED = 0x00000100;
    public static final int STATE_FLAG_TRACE_A11Y_INTERACTION_CONNECTION_CB_ENABLED = 0x00000200;
    public static final int STATE_FLAG_TRACE_A11Y_INTERACTION_CLIENT_ENABLED = 0x00000400;
    public static final int STATE_FLAG_TRACE_A11Y_SERVICE_ENABLED = 0x00000800;
    public static final int STATE_FLAG_AUDIO_DESCRIPTION_BY_DEFAULT_ENABLED = 0x00001000;
    public static final int DALTONIZER_DISABLED = -1;
    public static final int DALTONIZER_SIMULATE_MONOCHROMACY = 0;
    public static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    public static final int AUTOCLICK_DELAY_DEFAULT = 600;
    public static final String ACTION_CHOOSE_ACCESSIBILITY_BUTTON =
            "com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON";
    public static final int ACCESSIBILITY_BUTTON = 0;
    public static final int ACCESSIBILITY_SHORTCUT_KEY = 1;
    public static final int FLAG_CONTENT_ICONS = 1;
    public static final int FLAG_CONTENT_TEXT = 2;
    public static final int FLAG_CONTENT_CONTROLS = 4;

    /**
     * The contrast is defined as a float in [-1, 1], with a default value of 0.
     * @hide
     */
    public static final float CONTRAST_MIN_VALUE = -1f;

    /** @hide */
    public static final float CONTRAST_MAX_VALUE = 1f;

    /** @hide */
    public static final float CONTRAST_DEFAULT_VALUE = 0f;

    /** @hide */
    public static final float CONTRAST_NOT_SET = Float.MIN_VALUE;

    private static AccessibilityManager sInstance = new AccessibilityManager(null, null, 0);


    /**
     * Listener for the accessibility state.
     */
    public interface AccessibilityStateChangeListener {

        /**
         * Called back on change in the accessibility state.
         *
         * @param enabled Whether accessibility is enabled.
         */
        public void onAccessibilityStateChanged(boolean enabled);
    }

    /**
     * Listener for the system touch exploration state. To listen for changes to
     * the touch exploration state on the device, implement this interface and
     * register it with the system by calling
     * {@link #addTouchExplorationStateChangeListener}.
     */
    public interface TouchExplorationStateChangeListener {

        /**
         * Called when the touch exploration enabled state changes.
         *
         * @param enabled Whether touch exploration is enabled.
         */
        public void onTouchExplorationStateChanged(boolean enabled);
    }

    /**
     * Listener for the system high text contrast state. To listen for changes to
     * the high text contrast state on the device, implement this interface and
     * register it with the system by calling
     * {@link #addHighTextContrastStateChangeListener}.
     */
    public interface HighTextContrastChangeListener {

        /**
         * Called when the high text contrast enabled state changes.
         *
         * @param enabled Whether high text contrast is enabled.
         */
        public void onHighTextContrastStateChanged(boolean enabled);
    }

    /**
     * Listener for the UI contrast. To listen for changes to
     * the UI contrast on the device, implement this interface and
     * register it with the system by calling {@link #addUiContrastChangeListener}.
     */
    public interface UiContrastChangeListener {

        /**
         * Called when the color contrast enabled state changes.
         *
         * @param uiContrast The color contrast as in {@link #getUiContrast}
         */
        void onUiContrastChanged(@FloatRange(from = -1.0f, to = 1.0f) float uiContrast);
    }

    /**
     * Listener for changes to the state of accessibility services.
     *
     * <p>
     * This refers to changes to {@link AccessibilityServiceInfo}, including:
     * <ul>
     *     <li>Whenever a service is enabled or disabled, or its info has been set or removed.</li>
     *     <li>Whenever a metadata attribute of any running service's info changes.</li>
     * </ul>
     *
     * @see #getEnabledAccessibilityServiceList for a list of infos of the enabled accessibility
     * services.
     * @see #addAccessibilityServicesStateChangeListener
     *
     */
    public interface AccessibilityServicesStateChangeListener {

        /**
         * Called when the state of accessibility services changes.
         *
         * @param manager The manager that is calling back
         */
        void onAccessibilityServicesStateChanged(@NonNull  AccessibilityManager manager);
    }

    /**
     * Listener for the audio description by default state. To listen for
     * changes to the audio description by default state on the device,
     * implement this interface and register it with the system by calling
     * {@link #addAudioDescriptionRequestedChangeListener}.
     */
    public interface AudioDescriptionRequestedChangeListener {
        /**
         * Called when the audio description enabled state changes.
         *
         * @param enabled Whether audio description by default is enabled.
         */
        void onAudioDescriptionRequestedChanged(boolean enabled);
    }

    /**
     * Policy to inject behavior into the accessibility manager.
     *
     * @hide
     */
    public interface AccessibilityPolicy {
        /**
         * Checks whether accessibility is enabled.
         *
         * @param accessibilityEnabled Whether the accessibility layer is enabled.
         * @return whether accessibility is enabled.
         */
        boolean isEnabled(boolean accessibilityEnabled);

        /**
         * Notifies the policy for an accessibility event.
         *
         * @param event The event.
         * @param accessibilityEnabled Whether the accessibility layer is enabled.
         * @param relevantEventTypes The events relevant events.
         * @return The event to dispatch or null.
         */
        @Nullable AccessibilityEvent onAccessibilityEvent(@NonNull AccessibilityEvent event,
                boolean accessibilityEnabled, @EventType int relevantEventTypes);

        /**
         * Gets the list of relevant events.
         *
         * @param relevantEventTypes The relevant events.
         * @return The relevant events to report.
         */
        @EventType int getRelevantEventTypes(@EventType int relevantEventTypes);

        /**
         * Gets the list of installed services to report.
         *
         * @param installedService The installed services.
         * @return The services to report.
         */
        @NonNull List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(
                @Nullable List<AccessibilityServiceInfo> installedService);

        /**
         * Gets the list of enabled accessibility services.
         *
         * @param feedbackTypeFlags The feedback type to query for.
         * @param enabledService The enabled services.
         * @return The services to report.
         */
        @Nullable List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(
                @FeedbackType int feedbackTypeFlags,
                @Nullable List<AccessibilityServiceInfo> enabledService);
    }

    private final IAccessibilityManagerClient.Stub mClient =
            new IAccessibilityManagerClient.Stub() {
                public void setState(int state) {
                }

                public void notifyServicesStateChanged(long updatedUiTimeout) {
                }

                public void setRelevantEventTypes(int eventTypes) {
                }

                public void setFocusAppearance(int strokeWidth, int color) {
                }

                public void setUiContrast(float contrast) {

                }
            };

    /**
     * Get an AccessibilityManager instance (create one if necessary).
     *
     */
    public static AccessibilityManager getInstance(Context context) {
        return sInstance;
    }

    /**
     * Create an instance.
     *
     * @param context A {@link Context}.
     */
    public AccessibilityManager(Context context, IAccessibilityManager service, int userId) {
    }

    public IAccessibilityManagerClient getClient() {
        return mClient;
    }

    /**
     * Returns if the {@link AccessibilityManager} is enabled.
     *
     * @return True if this {@link AccessibilityManager} is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return false;
    }

    /**
     * Returns if the touch exploration in the system is enabled.
     *
     * @return True if touch exploration is enabled, false otherwise.
     */
    public boolean isTouchExplorationEnabled() {
        return true;
    }

    /**
     * Returns if the high text contrast in the system is enabled.
     * <p>
     * <strong>Note:</strong> You need to query this only if your application is
     * doing its own rendering and does not rely on the platform rendering pipeline.
     * </p>
     *
     */
    public boolean isHighTextContrastEnabled() {
        return false;
    }

    /**
     * Returns the color contrast for the user.
     * <p>
     * <strong>Note:</strong> You need to query this only if your application is
     * doing its own rendering and does not rely on the platform rendering pipeline.
     * </p>
     * @return The color contrast, float in [-1, 1] where
     *          0 corresponds to the default contrast
     *         -1 corresponds to the minimum contrast that the user can set
     *          1 corresponds to the maximum contrast that the user can set
     */
    @FloatRange(from = -1.0f, to = 1.0f)
    public float getUiContrast() {
        return 0;
    }

    /**
     * Sends an {@link AccessibilityEvent}.
     */
    public void sendAccessibilityEvent(AccessibilityEvent event) {
    }

    /**
     * Returns whether there are observers registered for this event type. If
     * this method returns false you shuold not generate events of this type
     * to conserve resources.
     *
     * @param type The event type.
     * @return Whether the event is being observed.
     */
    public boolean isObservedEventType(@AccessibilityEvent.EventType int type) {
        return false;
    }

    /**
     * Requests interruption of the accessibility feedback from all accessibility services.
     */
    public void interrupt() {
    }

    /**
     * Returns the {@link ServiceInfo}s of the installed accessibility services.
     *
     * @return An unmodifiable list with {@link ServiceInfo}s.
     */
    @Deprecated
    public List<ServiceInfo> getAccessibilityServiceList() {
        return Collections.emptyList();
    }

    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
        return Collections.emptyList();
    }

    /**
     * Returns the {@link AccessibilityServiceInfo}s of the enabled accessibility services
     * for a given feedback type.
     *
     * @param feedbackTypeFlags The feedback type flags.
     * @return An unmodifiable list with {@link AccessibilityServiceInfo}s.
     *
     * @see AccessibilityServiceInfo#FEEDBACK_AUDIBLE
     * @see AccessibilityServiceInfo#FEEDBACK_GENERIC
     * @see AccessibilityServiceInfo#FEEDBACK_HAPTIC
     * @see AccessibilityServiceInfo#FEEDBACK_SPOKEN
     * @see AccessibilityServiceInfo#FEEDBACK_VISUAL
     */
    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(
            int feedbackTypeFlags) {
        return Collections.emptyList();
    }

    /**
     * Registers an {@link AccessibilityStateChangeListener} for changes in
     * the global accessibility state of the system.
     *
     * @param listener The listener.
     * @return True if successfully registered.
     */
    public boolean addAccessibilityStateChangeListener(
            AccessibilityStateChangeListener listener) {
        return true;
    }

    /**
     * Registers an {@link AccessibilityStateChangeListener} for changes in
     * the global accessibility state of the system. If the listener has already been registered,
     * the handler used to call it back is updated.
     *
     * @param listener The listener.
     * @param handler The handler on which the listener should be called back, or {@code null}
     *                for a callback on the process's main handler.
     */
    public void addAccessibilityStateChangeListener(
            @NonNull AccessibilityStateChangeListener listener, @Nullable Handler handler) {}

    public boolean removeAccessibilityStateChangeListener(
            AccessibilityStateChangeListener listener) {
        return true;
    }

    /**
     * Registers a {@link TouchExplorationStateChangeListener} for changes in
     * the global touch exploration state of the system.
     *
     * @param listener The listener.
     * @return True if successfully registered.
     */
    public boolean addTouchExplorationStateChangeListener(
            @NonNull TouchExplorationStateChangeListener listener) {
        return true;
    }

    /**
     * Registers an {@link TouchExplorationStateChangeListener} for changes in
     * the global touch exploration state of the system. If the listener has already been
     * registered, the handler used to call it back is updated.
     *
     * @param listener The listener.
     * @param handler The handler on which the listener should be called back, or {@code null}
     *                for a callback on the process's main handler.
     */
    public void addTouchExplorationStateChangeListener(
            @NonNull TouchExplorationStateChangeListener listener, @Nullable Handler handler) {}

    /**
     * Unregisters a {@link TouchExplorationStateChangeListener}.
     *
     * @param listener The listener.
     * @return True if successfully unregistered.
     */
    public boolean removeTouchExplorationStateChangeListener(
            @NonNull TouchExplorationStateChangeListener listener) {
        return true;
    }

    /**
     * Registers a {@link HighTextContrastChangeListener} for changes in
     * the global high text contrast state of the system.
     *
     * @param listener The listener.
     *
     * @hide
     */
    public void addHighTextContrastStateChangeListener(
            @NonNull HighTextContrastChangeListener listener, @Nullable Handler handler) {}

    /**
     * Unregisters a {@link HighTextContrastChangeListener}.
     *
     * @param listener The listener.
     *
     * @hide
     */
    public void removeHighTextContrastStateChangeListener(
            @NonNull HighTextContrastChangeListener listener) {}

    /**
     * Registers a {@link UiContrastChangeListener} for the current user.
     *
     * @param executor The executor on which the listener should be called back.
     * @param listener The listener.
     */
    public void addUiContrastChangeListener(
            @NonNull Executor executor,
            @NonNull UiContrastChangeListener listener) {}

    /**
     * Unregisters a {@link UiContrastChangeListener} for the current user.
     * If the listener was not registered, does nothing and returns.
     *
     * @param listener The listener to unregister.
     */
    public void removeUiContrastChangeListener(@NonNull UiContrastChangeListener listener) {}

    /**
     * Sets the current state and notifies listeners, if necessary.
     *
     * @param stateFlags The state flags.
     */
    private void setStateLocked(int stateFlags) {
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken,
            IAccessibilityInteractionConnection connection) {
        return View.NO_ID;
    }

    public void removeAccessibilityInteractionConnection(IWindow windowToken) {
    }

    /**
     * Unregisters the IAccessibilityManagerClient from the backing service
     * @hide
     */
    public boolean removeClient() {
        return false;
    }

    /**
     * Registers a {@link AccessibilityServicesStateChangeListener}.
     *
     * @param executor The executor.
     * @param listener The listener.
     */
    public void addAccessibilityServicesStateChangeListener(@NonNull Executor executor,
            @NonNull AccessibilityServicesStateChangeListener listener) {
    }

    /**
     * Registers a {@link AccessibilityServicesStateChangeListener}. This will execute a callback on
     * the process's main handler.
     *
     * @param listener The listener.
     */
    public void addAccessibilityServicesStateChangeListener(
            @NonNull AccessibilityServicesStateChangeListener listener) {
    }

    /**
     * Unregisters a {@link AccessibilityServicesStateChangeListener}.
     *
     * @param listener The listener.
     *
     * @return {@code true} if the listener was previously registered.
     */
    public boolean removeAccessibilityServicesStateChangeListener(
            @NonNull AccessibilityServicesStateChangeListener listener) {
        return false;
    }

    /**
     * Registers a {@link AccessibilityRequestPreparer}.
     */
    public void addAccessibilityRequestPreparer(AccessibilityRequestPreparer preparer) {
    }

    /**
     * Unregisters a {@link AccessibilityRequestPreparer}.
     */
    public void removeAccessibilityRequestPreparer(AccessibilityRequestPreparer preparer) {
    }

    public int getRecommendedTimeoutMillis(int originalTimeout, int uiContentFlags) {
        return originalTimeout;
    }

    /**
     * Gets the strokeWidth of the focus rectangle.
     *
     * @return The strokeWidth of the focus rectangle in pixels.
     */
    public int getAccessibilityFocusStrokeWidth() {
        return 0;
    }

    /**
     * Gets the color of the focus rectangle.
     *
     * @return The color of the focus rectangle.
     */
    public int getAccessibilityFocusColor() {
        return 0;
    }

    /**
     * Gets accessibility interaction connection tracing enabled state.
     *
     * @hide
     */
    public boolean isA11yInteractionConnectionTraceEnabled() {
        return false;
    }

    /**
     * Gets accessibility interaction connection callback tracing enabled state.
     *
     * @hide
     */
    public boolean isA11yInteractionConnectionCBTraceEnabled() {
        return false;
    }

    /**
     * Gets accessibility interaction client tracing enabled state.
     *
     * @hide
     */
    public boolean isA11yInteractionClientTraceEnabled() {
        return false;
    }

    /**
     * Gets accessibility service tracing enabled state.
     *
     * @hide
     */
    public boolean isA11yServiceTraceEnabled() {
        return false;
    }

    /**
     * Get the preparers that are registered for an accessibility ID
     *
     * @param id The ID of interest
     *
     * @return The list of preparers, or {@code null} if there are none.
     *
     * @hide
     */
    public List<AccessibilityRequestPreparer> getRequestPreparersForAccessibilityId(int id) {
        return null;
    }

    /**
     * Set the currently performing accessibility action in views.
     *
     * @param actionId the action id of {@link AccessibilityNodeInfo.AccessibilityAction}.
     *
     * @hide
     */
    public void notifyPerformingAction(int actionId) {
    }

    /**
     * Registers a {@link AudioDescriptionRequestedChangeListener}
     * for changes in the audio description by default state of the system.
     * The value could be read via {@link #isAudioDescriptionRequested}.
     *
     * @param executor The executor on which the listener should be called back.
     * @param listener The listener.
     */
    public void addAudioDescriptionRequestedChangeListener(@NonNull Executor executor,
            @NonNull AudioDescriptionRequestedChangeListener listener) {
    }

    /**
     * Unregisters a {@link AudioDescriptionRequestedChangeListener}.
     *
     * @param listener The listener.
     *
     * @return True if listener was previously registered.
     */
    public boolean removeAudioDescriptionRequestedChangeListener(
            @NonNull AudioDescriptionRequestedChangeListener listener) {
        return false;
    }

    /**
     * Sets the {@link android.view.accessibility.AccessibilityManager.AccessibilityPolicy}
     * controlling this manager.
     *
     * @param policy The policy.
     *
     * @hide
     */
    public void setAccessibilityPolicy(
            @Nullable android.view.accessibility.AccessibilityManager.AccessibilityPolicy policy) {
    }

    /**
     * Check if the accessibility volume stream is active.
     *
     * @return True if accessibility volume is active (i.e. some service has requested it). False
     * otherwise.
     *
     * @hide
     */
    public boolean isAccessibilityVolumeStreamActive() {
        return false;
    }

    /**
     * Report a fingerprint gesture to accessibility. Only available for the system process.
     *
     * @param keyCode The key code of the gesture
     *
     * @return {@code true} if accessibility consumes the event. {@code false} if not.
     *
     * @hide
     */
    public boolean sendFingerprintGesture(int keyCode) {
        return false;
    }

    /**
     * Returns accessibility window id from window token. Accessibility window id is the one
     * returned from AccessibilityWindowInfo.getId(). Only available for the system process.
     *
     * @param windowToken Window token to find accessibility window id.
     *
     * @return Accessibility window id for the window token.
     * AccessibilityWindowInfo.UNDEFINED_WINDOW_ID if accessibility window id not available for
     * the token.
     *
     * @hide
     */
    public int getAccessibilityWindowId(@Nullable IBinder windowToken) {
        return AccessibilityWindowInfo.UNDEFINED_WINDOW_ID;
    }

    /**
     * Associate the connection between the host View and the embedded SurfaceControlViewHost.
     *
     * @hide
     */
    public void associateEmbeddedHierarchy(@NonNull IBinder host, @NonNull IBinder embedded) {
    }

    /**
     * Disassociate the connection between the host View and the embedded SurfaceControlViewHost.
     * The given token could be either from host side or embedded side.
     *
     * @hide
     */
    public void disassociateEmbeddedHierarchy(@NonNull IBinder token) {
    }

    /**
     * Find an installed service with the specified {@link ComponentName}.
     *
     * @param componentName The name to match to the service.
     *
     * @return The info corresponding to the installed service, or {@code null} if no such service
     * is installed.
     *
     * @hide
     */
    public AccessibilityServiceInfo getInstalledServiceInfoWithComponentName(
            ComponentName componentName) {
        return null;
    }

    /**
     * Adds an accessibility interaction connection interface for a given window.
     *
     * @param windowToken The window token to which a connection is added.
     * @param leashToken The leash token to which a connection is added.
     * @param connection The connection.
     *
     * @hide
     */
    public int addAccessibilityInteractionConnection(IWindow windowToken, IBinder leashToken,
            String packageName, IAccessibilityInteractionConnection connection) {
        return View.NO_ID;
    }

    /**
     * Perform the accessibility shortcut if the caller has permission.
     *
     * @hide
     */
    public void performAccessibilityShortcut() {
    }

    /**
     * Perform the accessibility shortcut for the given target which is assigned to the shortcut.
     *
     * @param targetName The flattened {@link ComponentName} string or the class name of a system
     * class implementing a supported accessibility feature, or {@code null} if there's no
     * specified target.
     *
     * @hide
     */
    public void performAccessibilityShortcut(@Nullable String targetName) {
    }

    /**
     * Register the provided {@link RemoteAction} with the given actionId
     * <p>
     * To perform established system actions, an accessibility service uses the GLOBAL_ACTION
     * constants in AccessibilityService. To provide a
     * customized implementation for one of these actions, the id of the registered system action
     * must match that of the corresponding GLOBAL_ACTION constant.
     * </p>
     *
     * @param action The remote action to be registered with the given actionId as system action.
     * @param actionId The id uniquely identify the system action.
     *
     * @hide
     */
    public void registerSystemAction(@NonNull RemoteAction action, int actionId) {
    }

    /**
     * Unregister a system action with the given actionId
     *
     * @param actionId The id uniquely identify the system action to be unregistered.
     *
     * @hide
     */
    public void unregisterSystemAction(int actionId) {
    }

    /**
     * Notifies that the accessibility button in the system's navigation area has been clicked
     *
     * @param displayId The logical display id.
     *
     * @hide
     */
    public void notifyAccessibilityButtonClicked(int displayId) {
    }

    /**
     * Perform the accessibility button for the given target which is assigned to the button.
     *
     * @param displayId displayId The logical display id.
     * @param targetName The flattened {@link ComponentName} string or the class name of a system
     * class implementing a supported accessibility feature, or {@code null} if there's no
     * specified target.
     *
     * @hide
     */
    public void notifyAccessibilityButtonClicked(int displayId, @Nullable String targetName) {
    }

    /**
     * Notifies that the visibility of the accessibility button in the system's navigation area
     * has changed.
     *
     * @param shown {@code true} if the accessibility button is visible within the system
     * navigation area, {@code false} otherwise
     *
     * @hide
     */
    public void notifyAccessibilityButtonVisibilityChanged(boolean shown) {
    }

    /**
     * Set an IAccessibilityInteractionConnection to replace the actions of a picture-in-picture
     * window. Intended for use by the System UI only.
     *
     * @param connection The connection to handle the actions. Set to {@code null} to avoid
     * affecting the actions.
     *
     * @hide
     */
    public void setPictureInPictureActionReplacingConnection(
            @Nullable IAccessibilityInteractionConnection connection) {
    }

    /**
     * Returns the list of shortcut target names currently assigned to the given shortcut.
     *
     * @param shortcutType The shortcut type.
     *
     * @return The list of shortcut target names.
     *
     * @hide
     */
    public List<String> getAccessibilityShortcutTargets(int shortcutType) {
        return Collections.emptyList();
    }

    /**
     * Returns the {@link AccessibilityShortcutInfo}s of the installed accessibility shortcut
     * targets, for specific user.
     *
     * @param context The context of the application.
     * @param userId The user id.
     *
     * @return A list with {@link AccessibilityShortcutInfo}s.
     *
     * @hide
     */
    @NonNull
    public List<AccessibilityShortcutInfo> getInstalledAccessibilityShortcutListAsUser(
            @NonNull Context context, int userId) {
        return Collections.emptyList();
    }

    /**
     * Sets an {@link IWindowMagnificationConnection} that manipulates window magnification.
     *
     * @param connection The connection that manipulates window magnification.
     *
     * @hide
     */
    public void setWindowMagnificationConnection(
            @Nullable IWindowMagnificationConnection connection) {
    }

    /**
     * Determines if users want to select sound track with audio description by default.
     * <p>
     * Audio description, also referred to as a video description, described video, or
     * more precisely called a visual description, is a form of narration used to provide
     * information surrounding key visual elements in a media work for the benefit of
     * blind and visually impaired consumers.
     * </p>
     * <p>
     * The method provides the preference value to content provider apps to select the
     * default sound track during playing a video or movie.
     * </p>
     * <p>
     * Add listener to detect the state change via
     * {@link #addAudioDescriptionRequestedChangeListener}
     * </p>
     *
     * @return {@code true} if the audio description is enabled, {@code false} otherwise.
     */
    public boolean isAudioDescriptionRequested() {
        return false;
    }

    /**
     * Sets the system audio caption enabled state.
     *
     * @param isEnabled The system audio captioning enabled state.
     * @param userId The user Id.
     *
     * @hide
     */
    public void setSystemAudioCaptioningEnabled(boolean isEnabled, int userId) {
    }

    /**
     * Gets the system audio caption UI enabled state.
     *
     * @param userId The user Id.
     *
     * @return the system audio caption UI enabled state.
     *
     * @hide
     */
    public boolean isSystemAudioCaptioningUiEnabled(int userId) {
        return false;
    }

    /**
     * Sets the system audio caption UI enabled state.
     *
     * @param isEnabled The system audio captioning UI enabled state.
     * @param userId The user Id.
     *
     * @hide
     */
    public void setSystemAudioCaptioningUiEnabled(boolean isEnabled, int userId) {
    }

    /**
     * Determines if the accessibility button within the system navigation area is supported.
     *
     * @return {@code true} if the accessibility button is supported on this device,
     * {@code false} otherwise
     */
    public static boolean isAccessibilityButtonSupported() {
        return false;
    }
}
