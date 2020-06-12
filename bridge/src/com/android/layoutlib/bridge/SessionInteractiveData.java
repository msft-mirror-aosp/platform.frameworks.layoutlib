package com.android.layoutlib.bridge;

import com.android.layoutlib.bridge.util.HandlerMessageQueue;
import com.android.tools.layoutlib.annotations.NotNull;

public class SessionInteractiveData {
    private final HandlerMessageQueue mHandlerMessageQueue = new HandlerMessageQueue();

    @NotNull
    public HandlerMessageQueue getHandlerMessageQueue() {
        return mHandlerMessageQueue;
    }

    public void dispose() {
        mHandlerMessageQueue.clear();
    }
}
