package org.appwork.shutdown;

public abstract class ShutdownEvent {
    private int  hookPriority = 10000;
    private long maxDuration  = 30000l;

    public boolean forceRunonFailingAddShutDownEvent() {
        return false;
    }

    public int getHookPriority() {
        return hookPriority;
    }

    /**
     * Waits at most <code>millis</code> milliseconds for this event to die. A
     * timeout of <code>0</code> means to wait forever
     */
    public long getMaxDuration() {

        return maxDuration;
    }

    abstract public void onShutdown(ShutdownRequest shutdownRequest);

    /**
     * The higher the priority, the earlier the hook will be called.
     * 
     * @param priority
     */

    public void setHookPriority(final int priority) {
        hookPriority = priority;
    }

    /**
     * Waits at most <code>millis</code> milliseconds for this event to die. A
     * timeout of <code>0</code> means to wait forever
     */
    public void setMaxDuration(final long maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " Priority: " + getHookPriority();
    }

}
