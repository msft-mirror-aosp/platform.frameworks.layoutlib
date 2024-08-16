/*
 * Copyright 2024 The Android Open Source Project
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

#include <gui/BufferQueue.h>
#include <gui/IGraphicBufferConsumer.h>
#include <gui/IGraphicBufferProducer.h>
#include <ui/GraphicBuffer.h>

#include "android_runtime/android_view_Surface.h"
#include "core_jni_helpers.h"
#include "jni.h"

namespace android {

jfieldID gNativeContextFieldId;

/**
 * Class to store information needed by the Layoutlib renderer
 */
class JNILayoutlibRendererContext : public RefBase {
public:
    ~JNILayoutlibRendererContext() override {
        if (mBufferConsumer != nullptr) {
            mBufferConsumer.clear();
        }
    }

    void setBufferConsumer(const sp<IGraphicBufferConsumer>& consumer) {
        mBufferConsumer = consumer;
    }

    IGraphicBufferConsumer* getBufferConsumer() {
        return mBufferConsumer.get();
    }

private:
    sp<IGraphicBufferConsumer> mBufferConsumer;
};

static jobject android_view_LayoutlibRenderer_createSurface(JNIEnv* env, jobject thiz) {
    sp<IGraphicBufferProducer> gbProducer;
    sp<IGraphicBufferConsumer> gbConsumer;
    BufferQueue::createBufferQueue(&gbProducer, &gbConsumer);

    // Save the IGraphicBufferConsumer in the context so that it can be reused for buffer creation
    sp<JNILayoutlibRendererContext> newCtx = sp<JNILayoutlibRendererContext>::make();
    newCtx->setBufferConsumer(gbConsumer);
    auto* const currentCtx = reinterpret_cast<JNILayoutlibRendererContext*>(
            env->GetLongField(thiz, gNativeContextFieldId));
    if (newCtx != nullptr) {
        // Create a strong reference to the new context to avoid it being destroyed
        newCtx->incStrong((void*)android_view_LayoutlibRenderer_createSurface);
    }
    if (currentCtx != nullptr) {
        // Delete the reference to the previous context as it is not needed and can be destroyed
        currentCtx->decStrong((void*)android_view_LayoutlibRenderer_createSurface);
    }
    env->SetLongField(thiz, gNativeContextFieldId, reinterpret_cast<jlong>(newCtx.get()));

    return android_view_Surface_createFromIGraphicBufferProducer(env, gbProducer);
}

static jobject android_view_LayoutlibRenderer_createBuffer(JNIEnv* env, jobject thiz, jint width,
                                                           jint height) {
    auto* ctx = reinterpret_cast<JNILayoutlibRendererContext*>(
            env->GetLongField(thiz, gNativeContextFieldId));
    if (ctx == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException", "No surface has been created");
        return nullptr;
    }

    IGraphicBufferConsumer* bufferConsumer = ctx->getBufferConsumer();
    bufferConsumer->setDefaultBufferSize(width, height);
    auto* bufferItem = new BufferItem();
    bufferConsumer->acquireBuffer(bufferItem, 0);
    sp<GraphicBuffer> buffer = bufferItem->mGraphicBuffer;

    int bytesPerPixel = 4;
    uint32_t dataSize = buffer->getStride() * buffer->getHeight() * bytesPerPixel;

    void* pData = nullptr;
    buffer->lockAsync(0, Rect::EMPTY_RECT, &pData, 0);

    jobject byteBuffer = env->NewDirectByteBuffer(pData, dataSize);
    return byteBuffer;
}

static const JNINativeMethod gMethods[] = {
        {"nativeCreateSurface", "()Landroid/view/Surface;",
         (void*)android_view_LayoutlibRenderer_createSurface},
        {"nativeCreateBuffer", "(II)Ljava/nio/ByteBuffer;",
         (void*)android_view_LayoutlibRenderer_createBuffer},
};

int register_android_view_LayoutlibRenderer(JNIEnv* env) {
    jclass layoutlibRendererClass = FindClassOrDie(env, "android/view/LayoutlibRenderer");
    gNativeContextFieldId = GetFieldIDOrDie(env, layoutlibRendererClass, "mNativeContext", "J");

    return RegisterMethodsOrDie(env, "android/view/LayoutlibRenderer", gMethods, NELEM(gMethods));
}

} // namespace android