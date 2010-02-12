package org.appwork.controlling;

import java.util.ArrayList;

import org.appwork.utils.logging.Log;

public class StateMachine {

    private State initState;
    private State currentState;
    private StateEventsender eventSender;
    private State finalState;
    private ArrayList<State> path;
    private StateMachineInterface owner;
    private Object lock = new Object();

    /**
     * @param interfac
     *            TODO
     * @param initState2
     * @param stoppedState
     */
    public StateMachine(StateMachineInterface interfac, State startState, State endState) {
        owner = interfac;
        initState = startState;
        currentState = startState;
        finalState = endState;
        this.eventSender = new StateEventsender();
        this.path = new ArrayList<State>();
        path.add(initState);
    }

    /**
     * @return the owner
     */
    public StateMachineInterface getOwner() {
        return owner;
    }

    /**
     * validates a statechain and checks if all states can be reached, and if
     * all chans result in one common finalstate
     * 
     * @param initState2
     * @throws StateConflictException
     */
    public static void validateStateChain(State initState) {
        if (initState.getParents().size() > 0) throw new StateConflictException("initState must not have a parent");
        checkState(initState);

    }

    /**
     * @param s
     * @return
     * @throws StateConflictException
     */
    private static State checkState(State state) {
        State finalState = null;
        for (State s : state.getChildren()) {
            State ret = checkState(s);
            if (finalState == null) finalState = ret;
            if (finalState != ret) throw new StateConflictException("States do not all result in one common final state");
        }
        if (finalState == null) { throw new StateConflictException(state + " is a blind state (has no children)"); }
        return finalState;
    }

    /**
     * @param addLinkState
     */
    public void setStatus(State newState) {
        synchronized (lock) {
            if (currentState == newState) return;
            if (!currentState.getChildren().contains(newState)) {

                StateConflictException e = new StateConflictException("Cannot change state from " + currentState + " to " + newState);
                e.printStackTrace();
                throw e;
            }

        }
        this.forceState(newState);
    }

    public void fireUpdate(State currentState) {
        if (currentState != null) {
            if (this.currentState != currentState) throw new StateConflictException("Cannot update state " + currentState + " because current state is " + this.currentState);
        }
        StateEvent event = new StateEvent(this, StateEvent.UPDATED, currentState, currentState);
        eventSender.fireEvent(event);
    }

    /**
     * @param canceledState
     * @param progressUpdate
     * @return
     */
    public boolean isState(State... states) {
        // TODO Auto-generated method stub
        for (State s : states) {
            if (s == currentState) return true;
        }
        return false;
    }

    /**
     * @param remoteUpload
     */
    public void addListener(StateEventListener listener) {
        eventSender.addListener(listener);
    }

    /**
     * @param transferController
     */
    public void removeListener(StateEventListener listener) {
        eventSender.removeListener(listener);
    }

    /**
     * 
     */
    public void reset() {
        StateEvent event;
        synchronized (lock) {
            if (currentState == initState) return;
            if (finalState != currentState) throw new StateConflictException("Cannot reset from state " + currentState);
            event = new StateEvent(this, StateEvent.CHANGED, currentState, initState);
            Log.L.finest(owner + " State changed (reset) " + currentState + " -> " + initState);
            this.currentState = this.initState;

            path.clear();
            path.add(initState);
        }
        eventSender.fireEvent(event);
    }

    /**
     * @return
     */
    public boolean isFinal() {
        // TODO Auto-generated method stub
        return finalState == currentState;
    }

    /**
     * @param parseInt
     */
    public void forceState(int id) {

        State newState;
        synchronized (lock) {
            newState = getStateById(this.initState, id, null);
            if (newState == null) throw new StateConflictException("No State with ID " + id);
        }
        forceState(newState);
    }

    /**
     * @param newState
     */
    public void forceState(State newState) {
        StateEvent event;
        synchronized (lock) {
            if (currentState == newState) return;
            event = new StateEvent(this, StateEvent.CHANGED, currentState, newState);
            path.add(newState);
            Log.L.finest(owner + " State changed " + currentState + " -> " + newState);
            currentState = newState;
        }
        eventSender.fireEvent(event);
    }

    /**
     * @param id
     * @return
     */
    private State getStateById(State startState, int id, ArrayList<State> foundStates) {

        if (foundStates == null) foundStates = new ArrayList<State>();
        if (foundStates.contains(startState)) return null;
        foundStates.add(startState);
        State ret = null;
        for (State s : startState.getChildren()) {

            if (s.getID() == id) return s;
            ret = getStateById(s, id, foundStates);
            if (ret != null) return ret;
        }
        return null;
    }

    /**
     * @param statusErrorUploadUnknown
     * @param statusErrorFilesizeToBig
     * @param statusErrorDownloadUnknown
     * @param statusFinished
     * @return
     */
    public boolean hasPassed(State... states) {
        for (State s : states) {
            if (path.contains(s)) return true;
        }
        return false;

    }

    /**
     * returns if the statemachine is in startstate currently
     * 
     * @return
     */
    public boolean isStartState() {
        // TODO Auto-generated method stub
        return currentState == this.initState;
    }

    /**
     * @return
     */
    public State getState() {
        // TODO Auto-generated method stub
        return currentState;
    }

}
