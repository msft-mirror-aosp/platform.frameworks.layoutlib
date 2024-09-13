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

package android.graphics.drawable;

import com.android.layoutlib.bridge.impl.RenderAction;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.util.LruCache;
import android.view.InflateException;

import java.lang.reflect.Constructor;

public class DrawableInflater_Delegate {
    private static final LruCache<String, Constructor<? extends Drawable>> CONSTRUCTOR_MAP =
            new LruCache<>(20);

    /**
     * This is identical to the original method except that it uses LayoutlibCallback to
     * load the drawable class, which enables loading custom drawables from the project.
     */
    @LayoutlibDelegate
    /* package */ static Drawable inflateFromClass(DrawableInflater thisInflater,
            String className) {
        try {
            Constructor<? extends Drawable> constructor;
            synchronized (CONSTRUCTOR_MAP) {
                constructor = CONSTRUCTOR_MAP.get(className);
                if (constructor == null) {
                    final Class<? extends Drawable> clazz =
                            RenderAction.getCurrentContext().getLayoutlibCallback()
                                    .findClass(className).asSubclass(Drawable.class);
                    constructor = clazz.getConstructor();
                    CONSTRUCTOR_MAP.put(className, constructor);
                }
            }
            return constructor.newInstance();
        } catch (ClassCastException e) {
            // If loaded class is not a Drawable subclass.
            throw new InflateException("Class is not a Drawable " + className, e);
        } catch (ClassNotFoundException e) {
            // If loadClass fails, we should propagate the exception.
            throw new InflateException("Class not found " + className, e);
        } catch (Exception e) {
            throw new InflateException("Error inflating class " + className, e);
        }
    }

    public static void clearConstructorCache() {
        CONSTRUCTOR_MAP.evictAll();
    }
}
