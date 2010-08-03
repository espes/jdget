package org.appwork.controlling;

import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.utils.logging.Log;

public class StateMachine {

    private State initState;
    private volatile State currentState;
    private StateEventsender eventSender;
    private State finalState;
    private ArrayList<StatePathEntry> path;

    private StateMachineInterface owner;
    private Object lock = new Object();
    private Object lock2 = new Object();
    private HashMap<State, Throwable> exceptionMap;

    public StateMachine(StateMachineInterface interfac, State startState, State endState) {
        owner = interfac;
        initState = startState;
        currentState = startState;
        finalState = endState;
        exceptionMap = new HashMap<State, Throwable>();
        this.eventSender = new StateEventsender();
        this.path = new ArrayList<StatePathEntry>();
        path.add(new StatePathEntry(initState));
    }

    public StateMachineInterface getOwner() {
        return owner;
    }

    /**
     * validates a statechain and checks if all states can be reached, and if
     * all chans result in one common finalstate
     * 
     * @param initState
     * @throws StateConflictException
     */
    public static void validateStateChain(State initState) {
        if (initState.getParents().size() > 0) throw new StateConflictException("initState must not have a parent");
        checkState(initState);
    }

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

    public void setStatus(State newState) {
        synchronized (lock) {
            if (currentState == newState) return;
            if (!currentState.getChildren().contains(newState)) { throw new StateConflictException("Cannot change state from " + currentState + " to " + newState); }
        }
        this.forceState(newState);
    }

    public void fireUpdate(State currentState) {
        if (currentState != null) {
            synchronized (lock) {
                if (this.currentState != currentState) throw new StateConflictException("Cannot update state " + currentState + " because current state is " + this.currentState);
            }
        }
        StateEvent event = new StateEvent(this, StateEvent.UPDATED, currentState, currentState);
        eventSender.fireEvent(event);
    }

    public boolean isState(State... states) {
        synchronized (lock) {
            for (State s : states) {
                if (s == currentState) return true;
            }
        }
        return false;
    }

    public void addListener(StateEventListener listener) {
        eventSender.addListener(listener);
    }

    public void removeListener(StateEventListener listener) {
        eventSender.removeListener(listener);
    }

    public void reset() {
        StateEvent event;
        synchronized (lock) {
            if (currentState == initState) return;
            if (finalState != currentState) throw new StateConflictException("Cannot reset from state " + currentState);
            event = new StateEvent(this, StateEvent.CHANGED, currentState, initState);
            Log.L.finest(owner + " State changed (reset) " + currentState + " -> " + initState);
            this.currentState = this.initState;
            synchronized (lock2) {
                path.clear();
                path.add(new StatePathEntry(initState));
            }
        }
        eventSender.fireEvent(event);
    }

    /**
     * @return the path
     */
    public ArrayList<StatePathEntry> getPath() {
        return path;
    }

    public boolean isFinal() {
        synchronized (lock) {
            return finalState == currentState;
        }
    }

    // public void forceState(int id) {
    //
    // State newState;
    // synchronized (lock) {
    // newState = getStateById(this.initState, id, null);
    // if (newState == null) throw new
    // StateConflictException("No State with ID " + id);
    // }
    // forceState(newState);
    // }

    public void forceState(State newState) {
        StateEvent event;
        synchronized (lock) {
            if (currentState == newState) return;
            event = new StateEvent(this, StateEvent.CHANGED, currentState, newState);
            synchronized (lock2) {
                path.add(new StatePathEntry(newState));
            }
            Log.L.finest(owner + " State changed " + currentState + " -> " + newState);
            currentState = newState;
        }
        eventSender.fireEvent(event);
    }

    // private State getStateById(State startState, int id, ArrayList<State>
    // foundStates) {
    //
    // if (foundStates == null) foundStates = new ArrayList<State>();
    // if (foundStates.contains(startState)) return null;
    // foundStates.add(startState);
    // State ret = null;
    // for (State s : startState.getChildren()) {
    //
    // if (s.getID() == id) return s;
    // ret = getStateById(s, id, foundStates);
    // if (ret != null) return ret;
    // }
    // return null;
    // }

    public boolean hasPassed(State... states) {
        synchronized (lock2) {
            for (State s : states) {
                for (StatePathEntry e : path) {
                    if (e.getState() == s) return true;
                }
            }
        }
        return false;
    }

    /*
     * synchronized hasPassed/addListener to start run when state has
     * reached/passed
     */
    public void executeOnceOnState(final Runnable run, State state) {
        if (run == null || state == null) return;
        boolean reached = false;
        synchronized (lock) {
            if (hasPassed(state)) {
                reached = true;
            } else {
                addListener(new StateListener(state) {
                    @Override
                    public void onStateReached(StateEvent event) {
                        removeListener(this);
                        run.run();
                    }
                });
            }
        }
        if (reached) run.run();
    }

    /**
     * returns if the statemachine is in startstate currently
     */
    public boolean isStartState() {
        synchronized (lock) {
            return currentState == initState;
        }
    }

    public State getState() {
        return currentState;
    }

    public Throwable getCause(State newState) {
        return exceptionMap.get(newState);
    }

    public void setCause(State failedState, Throwable e) {
        exceptionMap.put(failedState, e);
    }

    /**
     * TODO: not synchronized
     * 
     * @param failedState
     * @return
     */
    public StatePathEntry getLatestStateEntry(State failedState) {
        try {
            StatePathEntry entry = null;
            synchronized (lock2) {
                for (int i = path.size() - 1; i >= 0; i--) {
                    entry = path.get(i);
                    if (entry.getState() == failedState) return entry;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
