package com.android.layoutlib.bridge;

import com.android.layoutlib.bridge.util.ChoreographerCallbacks;
import com.android.layoutlib.bridge.util.HandlerMessageQueue;
import com.android.tools.layoutlib.annotations.NotNull;

import android.view.Choreographer_Delegate;

import java.util.concurrent.atomic.AtomicLong;

public class SessionInteractiveData {
    private final HandlerMessageQueue mHandlerMessageQueue = new HandlerMessageQueue();
    private final ChoreographerCallbacks mChoreographerCallbacks = new ChoreographerCallbacks();
    // Current system time
    private final AtomicLong mNanosTime = new AtomicLong(System.nanoTime());
    private final AtomicLong mPreviousNanosTime = new AtomicLong(System.nanoTime());
    // Time that the system booted up in nanos
    private final AtomicLong mBootNanosTime = new AtomicLong(System.nanoTime());

    @NotNull
    public HandlerMessageQueue getHandlerMessageQueue() {
        return mHandlerMessageQueue;
    }

    @NotNull
    public ChoreographerCallbacks getChoreographerCallbacks() { return mChoreographerCallbacks; }

    public void setNanosTime(long nanos) {
        mPreviousNanosTime.set(mNanosTime.get());
        mNanosTime.set(nanos);
    }

    public long getNanosTime() {
        return mNanosTime.get() - mPreviousNanosTime.get()
                + Choreographer_Delegate.sChoreographerTime;
    }

    public void setBootNanosTime(long nanos) {
        mBootNanosTime.set(nanos);
    }

    public long getBootNanosTime() {
        return mBootNanosTime.get();
    }

    public void dispose() {
        mHandlerMessageQueue.clear();
        mChoreographerCallbacks.clear();
    }
}
