/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.layoutlib.bridge.util

import com.android.ide.common.rendering.api.ILayoutLog

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import android.view.Choreographer.FrameCallback
import kotlin.test.assertContentEquals

class ChoreographerCallbacksTest {

    private class ValidatingLogger : ILayoutLog {

        override fun error(tag: String?, message: String, viewCookie: Any?, data: Any?) {
            errorMessages.add(message)
        }

        override fun error(tag: String?, message: String, throwable: Throwable?, viewCookie: Any?, data: Any?) {
            errorMessages.add(message)
        }

        val errorMessages = mutableListOf<String>()
    }

    private val logger = ValidatingLogger()

    @Before
    fun setUp() {
        logger.errorMessages.clear()
    }

    @Test
    fun testAddAndExecuteInOrder() {
        val callbacks = ChoreographerCallbacks()
        val order = mutableListOf<Int>()

        callbacks.add(Runnable { order.add(2) }, null, 200)
        callbacks.add(FrameCallback { order.add(1) }, null, 100)
        callbacks.execute(200, logger)

        assertContentEquals(listOf(1, 2), order)
        Assert.assertTrue(logger.errorMessages.isEmpty())
    }

    @Test
    fun testAddAndExecuteOnlyDue() {
        val callbacks = ChoreographerCallbacks()
        val order = mutableListOf<Int>()

        callbacks.add(Runnable { order.add(2) }, null, 200)
        callbacks.add(FrameCallback { order.add(1) }, null, 100)
        callbacks.execute(100, logger)

        assertContentEquals(listOf(1), order)
        Assert.assertTrue(logger.errorMessages.isEmpty())
    }

    @Test
    fun testRemove() {
        val callbacks = ChoreographerCallbacks()
        val order = mutableListOf<Int>()

        val runnable = Runnable { order.add(2) }
        callbacks.add(runnable, null, 200)
        callbacks.add(FrameCallback { order.add(1) }, null, 100)
        callbacks.remove(runnable, null)
        callbacks.execute(200, logger)

        assertContentEquals(order, listOf(1))
        Assert.assertTrue(logger.errorMessages.isEmpty())
    }

    @Test
    fun testErrorIfUnknownCallbackType() {
        val callbacks = ChoreographerCallbacks()

        callbacks.add(Any(), null, 100)
        callbacks.execute(200, logger)

        Assert.assertFalse(logger.errorMessages.isEmpty())
        Assert.assertEquals(logger.errorMessages[0], "Unexpected action as Choreographer callback")
    }

    @Test
    fun testRemoveNullAction() {
        val callbacks = ChoreographerCallbacks()
        val order = mutableListOf<Int>()

        val token1 = Any()
        val token2 = Any()
        callbacks.add(Runnable { order.add(2) }, token1, 200)
        callbacks.add(FrameCallback { order.add(1) }, token1, 100)
        callbacks.add(Runnable { order.add(3) }, token2, 100)
        callbacks.add(Runnable { order.add(4) }, null, 200)
        callbacks.remove(null, token1)
        callbacks.execute(200, logger)

        assertContentEquals(listOf(3, 4), order)
        Assert.assertTrue(logger.errorMessages.isEmpty())
    }
}
