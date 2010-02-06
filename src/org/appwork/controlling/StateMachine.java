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
    public synchronized void setStatus(State newState) {
        if (currentState == newState) return;
        if (!currentState.getChildren().contains(newState)) { throw new StateConflictException("Cannot change state from " + currentState + " to " + newState); }

        this.forceState(newState);
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
        if (currentState == initState) return;
        if (finalState != currentState) throw new StateConflictException("Cannot reset from state " + currentState);
        StateEvent event = new StateEvent(this, StateEvent.CHANGED, currentState, initState);
        this.currentState = this.initState;
        eventSender.fireEvent(event);
        path.clear();
        path.add(initState);
    }

    /**
     * @return
     */
    public boolean isFinal() {
        // TODO Auto-generated method stub
        return finalState != currentState;
    }

    /**
     * @param parseInt
     */
    public void forceState(int id) {
        State newState = getStateById(this.initState, id, null);
        if (newState == null) throw new StateConflictException("No State with ID " + id);
        forceState(newState);
    }

    /**
     * @param newState
     */
    private synchronized void forceState(State newState) {
        if (currentState == newState) return;
        StateEvent event = new StateEvent(this, StateEvent.CHANGED, currentState, newState);
        path.add(newState);
        Log.L.finest(owner + " State changed " + currentState + " -> " + newState);
        currentState = newState;
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
