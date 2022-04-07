/*
 * Copyright (C) 2022 The Android Open Source Project
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

package android.hardware.input;


import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.lights.LightsManager;
import android.os.BlockUntrustedTouchesMode;
import android.os.Handler;
import android.os.IBinder;
import android.os.InputEventInjectionSync;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputMonitor;
import android.view.PointerIcon;
import android.view.VerifiedInputEvent;

import java.util.ArrayList;
import java.util.List;

public final class InputManager {
    public static final int[] BLOCK_UNTRUSTED_TOUCHES_MODES = {
            BlockUntrustedTouchesMode.DISABLED,
            BlockUntrustedTouchesMode.PERMISSIVE,
            BlockUntrustedTouchesMode.BLOCK
    };

    public static final String ACTION_QUERY_KEYBOARD_LAYOUTS =
            "android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS";

    public static final String META_DATA_KEYBOARD_LAYOUTS =
            "android.hardware.input.metadata.KEYBOARD_LAYOUTS";

    public static final int MIN_POINTER_SPEED = -7;

    public static final int MAX_POINTER_SPEED = 7;

    public static final int DEFAULT_POINTER_SPEED = 0;

    public static final float DEFAULT_MAXIMUM_OBSCURING_OPACITY_FOR_TOUCH = .8f;

    public static final int DEFAULT_BLOCK_UNTRUSTED_TOUCHES_MODE =
            BlockUntrustedTouchesMode.BLOCK;

    public static final long BLOCK_UNTRUSTED_TOUCHES = 158002302L;

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = InputEventInjectionSync.NONE;

    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT =
            InputEventInjectionSync.WAIT_FOR_RESULT;

    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH =
            InputEventInjectionSync.WAIT_FOR_FINISHED;

    public @interface SwitchState {}

    public static final int SWITCH_STATE_UNKNOWN = -1;

    public static final int SWITCH_STATE_OFF = 0;

    public static final int SWITCH_STATE_ON = 1;

    private static InputManager sInstance;

    public static InputManager resetInstance(IInputManager inputManagerService) {
        sInstance = new InputManager();
        return sInstance;
    }

    public static void clearInstance() {
        synchronized (InputManager.class) {
            sInstance = null;
        }
    }

    public static InputManager getInstance() {
        synchronized (InputManager.class) {
            if (sInstance == null) {
                sInstance = new InputManager();
            }
            return sInstance;
        }
    }

    public String getVelocityTrackerStrategy() {
        return null;
    }

    public InputDevice getInputDevice(int id) {
        return null;
    }

    public InputDevice getInputDeviceByDescriptor(String descriptor) {
        return null;
    }

    public int[] getInputDeviceIds() {
        return null;
    }

    public boolean isInputDeviceEnabled(int id) {
        return true;
    }

    public void enableInputDevice(int id) { }

    public void disableInputDevice(int id) { }

    public void registerInputDeviceListener(InputDeviceListener listener, Handler handler) { }

    public void unregisterInputDeviceListener(InputDeviceListener listener) { }

    public int isInTabletMode() {
        return SWITCH_STATE_UNKNOWN;
    }

    public void registerOnTabletModeChangedListener() { }

    public void unregisterOnTabletModeChangedListener(OnTabletModeChangedListener listener) { }

    private void initializeTabletModeListenerLocked() { }

    private int findOnTabletModeChangedListenerLocked(OnTabletModeChangedListener listener) {
        return -1;
    }

    public int isMicMuted() {
        return SWITCH_STATE_UNKNOWN;
    }

    public KeyboardLayout[] getKeyboardLayouts() {
        return null;
    }

    public List<String> getKeyboardLayoutDescriptorsForInputDevice(InputDevice device) {
        return new ArrayList<>();
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        return null;
    }

    public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) {
        return null;
    }

    public String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) {
        return null;
    }

    public void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor) { }

    public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        return null;
    }

    public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor) { }

    public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier,
            String keyboardLayoutDescriptor) { }

    public TouchCalibration getTouchCalibration(String inputDeviceDescriptor, int surfaceRotation) {
        return null;
    }

    public void setTouchCalibration(String inputDeviceDescriptor, int surfaceRotation,
            TouchCalibration calibration) { }

    public int getPointerSpeed(Context context) {
        return 0;
    }

    public void setPointerSpeed(Context context, int speed) { }

    public void tryPointerSpeed(int speed) { }

    public float getMaximumObscuringOpacityForTouch() {
        return 0f;
    }

    public void setMaximumObscuringOpacityForTouch(float opacity) { }

    public int getBlockUntrustedTouchesMode(Context context) {
        return 0;
    }

    public void setBlockUntrustedTouchesMode(Context context, int mode) { }

    public boolean[] deviceHasKeys(int[] keyCodes) {
        return null;
    }

    public boolean[] deviceHasKeys(int id, int[] keyCodes) {
        return null;
    }

    public int getKeyCodeForKeyLocation(int deviceId, int locationKeyCode) {
        return 0;
    }

    public boolean injectInputEvent(InputEvent event, int mode, int targetUid) {
        return false;
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return false;
    }

    public VerifiedInputEvent verifyInputEvent(InputEvent event) {
        return null;
    }

    public void setPointerIconType(int iconId) { }

    public void setCustomPointerIcon(PointerIcon icon) { }

    public void requestPointerCapture(IBinder windowToken, boolean enable) { }

    public InputMonitor monitorGestureInput(String name, int displayId) {
        return null;
    }

    public InputSensorInfo[] getSensorList(int deviceId) {
        return null;
    }

    public boolean enableSensor(int deviceId, int sensorType, int samplingPeriodUs,
            int maxBatchReportLatencyUs) {
        return false;
    }

    public void disableSensor(int deviceId, int sensorType) { }

    public boolean flushSensor(int deviceId, int sensorType) {
        return false;
    }

    public boolean registerSensorListener(IInputSensorEventListener listener) {
        return false;
    }

    public void unregisterSensorListener(IInputSensorEventListener listener) { }

    public int getBatteryStatus(int deviceId) {
        return 0;
    }

    public int getBatteryCapacity(int deviceId) {
        return 0;
    }

    public void addPortAssociation(String inputPort, int displayPort) { }

    public void removePortAssociation(String inputPort) { }

    public void addUniqueIdAssociation(String inputPort, String displayUniqueId) { }

    public void removeUniqueIdAssociation(String inputPort) { }

    public Vibrator getInputDeviceVibrator(int deviceId, int vibratorId) {
        return null;
    }

    public VibratorManager getInputDeviceVibratorManager(int deviceId) {
        return null;
    }

    public SensorManager getInputDeviceSensorManager(int deviceId) {
        return null;
    }

    public InputDeviceBatteryState getInputDeviceBatteryState(int deviceId, boolean hasBattery) {
        return null;
    }

    public LightsManager getInputDeviceLightsManager(int deviceId) {
        return null;
    }

    public void cancelCurrentTouch() { }

    public interface InputDeviceListener {
        void onInputDeviceAdded(int deviceId);

        void onInputDeviceRemoved(int deviceId);

        void onInputDeviceChanged(int deviceId);
    }

    public interface OnTabletModeChangedListener {
        void onTabletModeChanged(long whenNanos, boolean inTabletMode);
    }
}

