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

package android.hardware;

import android.os.Handler;
import android.os.MemoryFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

public class EmptySensorManager extends SensorManager {
    private static EmptySensorManager sInstance;

    public static EmptySensorManager getInstance() {
        if (sInstance != null) {
            sInstance = new EmptySensorManager();
        }
        return sInstance;
    }

    @Override
    protected List<Sensor> getFullSensorList() {
        return Collections.emptyList();
    }

    @Override
    protected List<Sensor> getFullDynamicSensorList() {
        return Collections.emptyList();
    }

    @Override
    protected void unregisterListenerImpl(SensorEventListener listener, Sensor sensor) { }

    @Override
    protected boolean registerListenerImpl(SensorEventListener listener, Sensor sensor, int delayUs,
            Handler handler, int maxReportLatencyUs, int reservedFlags) {
        return false;
    }

    @Override
    protected boolean flushImpl(SensorEventListener listener) {
        return false;
    }

    @Override
    protected SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile,
            HardwareBuffer hardwareBuffer) {
        throw new UncheckedIOException(new IOException("Not supported by layoutlib"));
    }

    @Override
    protected void destroyDirectChannelImpl(SensorDirectChannel channel) { }

    @Override
    protected int configureDirectChannelImpl(SensorDirectChannel channel, Sensor s, int rate) {
        return 0;
    }

    @Override
    protected void registerDynamicSensorCallbackImpl(DynamicSensorCallback callback,
            Handler handler) { }

    @Override
    protected void unregisterDynamicSensorCallbackImpl(DynamicSensorCallback callback) { }

    @Override
    protected boolean requestTriggerSensorImpl(TriggerEventListener listener, Sensor sensor) {
        return false;
    }

    @Override
    protected boolean cancelTriggerSensorImpl(TriggerEventListener listener, Sensor sensor,
            boolean disable) {
        return false;
    }

    @Override
    protected boolean initDataInjectionImpl(boolean enable, int mode) {
        return false;
    }

    @Override
    protected boolean injectSensorDataImpl(Sensor sensor, float[] values, int accuracy,
            long timestamp) {
        return false;
    }

    @Override
    protected boolean setOperationParameterImpl(SensorAdditionalInfo parameter) {
        return false;
    }
}
