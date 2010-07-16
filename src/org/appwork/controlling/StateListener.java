package org.appwork.controlling;

public abstract class StateListener implements StateEventListener {

    private State state;

    /**
     * @param rfsInitComplete
     */
    public StateListener(State rfsInitComplete) {
        state = rfsInitComplete;

    }

    public void onStateChange(StateEvent event) {
        if (event.getNewState() == state) onStateReached(event);
    }

    /**
     * @param event
     */
    public abstract void onStateReached(StateEvent event);

    public void onStateUpdate(StateEvent event) {

    }

}
