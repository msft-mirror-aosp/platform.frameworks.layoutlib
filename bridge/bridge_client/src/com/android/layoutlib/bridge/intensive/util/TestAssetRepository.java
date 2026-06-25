/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.layoutlib.bridge.intensive.util;

import com.android.ide.common.rendering.api.AssetRepository;

import android.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.io.ByteStreams;

/**
 * {@link AssetRepository} used for render tests.
 */
public class TestAssetRepository extends AssetRepository {
    private final String mAssetPath;
    private final Map<String, JarFile> mJarCache = new HashMap<>();

    public TestAssetRepository(@NonNull String assetPath) {
        if (assetPath.endsWith(".jar")) {
            mAssetPath = "jar:" + assetPath + "!/";
        } else {
            mAssetPath = assetPath;
        }
    }

    private InputStream open(String path) throws FileNotFoundException {
        try {
            if (path.startsWith("jar:")) {
                int index = path.indexOf("!/");
                if (index != -1) {
                    String jarPath = path.substring(4, index);
                    String entryPath = path.substring(index + 2);
                    JarFile jarFile = mJarCache.get(jarPath);
                    if (jarFile == null) {
                        jarFile = new JarFile(jarPath);
                        mJarCache.put(jarPath, jarFile);
                    }
                    JarEntry entry = jarFile.getJarEntry(entryPath);
                    if (entry != null) {
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            byte[] data = ByteStreams.toByteArray(is);
                            return new ByteArrayInputStream(data);
                        }
                    }
                }
            } else {
                File asset = new File(path);
                if (asset.isFile()) {
                    return new FileInputStream(asset);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public InputStream openAsset(String path, int mode) throws IOException {
        return open(mAssetPath + path);
    }

    @Override
    public InputStream openNonAsset(int cookie, String path, int mode) throws IOException {
        return open(path);
    }

    @Override
    public boolean isFileResource(String path) {
        if (path.startsWith("jar:")) {
            int index = path.indexOf("!/");
            if (index != -1) {
                String jarPath = path.substring(4, index);
                String entryPath = path.substring(index + 2);
                try {
                    JarFile jarFile = mJarCache.get(jarPath);
                    if (jarFile == null) {
                        jarFile = new JarFile(jarPath);
                        mJarCache.put(jarPath, jarFile);
                    }
                    return jarFile.getJarEntry(entryPath) != null;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return super.isFileResource(path);
    }

    public void close() {
        for (JarFile jarFile : mJarCache.values()) {
            try {
                jarFile.close();
            } catch (IOException ignore) {
            }
        }
        mJarCache.clear();
    }
}
