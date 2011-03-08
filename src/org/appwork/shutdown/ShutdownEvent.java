package org.appwork.shutdown;

public abstract class ShutdownEvent extends Thread implements Comparable<ShutdownEvent> {
    private int  hookPriority = 10000;
    private long maxDuration  = 30000l;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ShutdownEvent o) {

        return getHookPriority() < o.getHookPriority() ? 1 : getHookPriority() == o.getHookPriority() ? 0 : -1;
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

    @Override
    abstract public void run();

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
        return getClass().getSimpleName() + " Priority: " + getHookPriority();
    }

}
