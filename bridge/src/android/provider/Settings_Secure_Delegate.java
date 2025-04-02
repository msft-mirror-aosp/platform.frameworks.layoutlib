/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.provider;

import com.android.layoutlib.bridge.android.BridgeContentResolver;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.content.ContentResolver;

import java.util.Map;

public class Settings_Secure_Delegate {
    @LayoutlibDelegate
    public static String getStringForUser(ContentResolver cr, String name, int userHandle) {
        Map<String, String> settingsUserMap = getSettingsMap(cr);
        return settingsUserMap.get(name);
    }

    @LayoutlibDelegate
    public static boolean putStringForUser(ContentResolver resolver, String name, String value,
            int userHandle) {
        Map<String, String> settingsUserMap = getSettingsMap(resolver);
        settingsUserMap.put(name, value);
        return true;

    }

    @LayoutlibDelegate
    public static boolean putStringForUser(@NonNull ContentResolver resolver, @NonNull String name,
            @Nullable String value, @Nullable String tag, boolean makeDefault,
            @UserIdInt int userHandle, boolean overrideableByRestore) {
        Map<String, String> settingsUserMap = getSettingsMap(resolver);
        settingsUserMap.put(name, value);
        return true;
    }

    static Map<String, String> getSettingsMap(ContentResolver cr) {
        BridgeContentResolver bridgeContentResolver = (BridgeContentResolver) cr;
        return bridgeContentResolver.settingsUserMap;
    }

}
