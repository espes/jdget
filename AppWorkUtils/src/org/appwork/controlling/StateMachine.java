package org.appwork.controlling;

import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.utils.logging.Log;

public class StateMachine {

    private static State checkState(final State state) {
        State finalState = null;
        for (final State s : state.getChildren()) {
            final State ret = StateMachine.checkState(s);
            if (finalState == null) {
                finalState = ret;
            }
            if (finalState != ret) { throw new StateConflictException("States do not all result in one common final state"); }
        }
        if (finalState == null) { throw new StateConflictException(state + " is a blind state (has no children)"); }
        return finalState;
    }

    /**
     * validates a statechain and checks if all states can be reached, and if
     * all chans result in one common finalstate
     * 
     * @param initState
     * @throws StateConflictException
     */
    public static void validateStateChain(final State initState) {
        if (initState.getParents().size() > 0) { throw new StateConflictException("initState must not have a parent"); }
        StateMachine.checkState(initState);
    }

    private final State                          initState;
    private volatile State                       currentState;
    private final StateEventsender               eventSender;

    private final State                          finalState;
    private final java.util.List<StatePathEntry> path;
    private final StateMachineInterface          owner;
    private final Object                         lock  = new Object();

    private final Object                         lock2 = new Object();

    private final HashMap<State, Throwable>      exceptionMap;

    public StateMachine(final StateMachineInterface interfac, final State startState, final State endState) {
        this.owner = interfac;
        this.initState = startState;
        this.currentState = startState;
        this.finalState = endState;
        this.exceptionMap = new HashMap<State, Throwable>();
        this.eventSender = new StateEventsender();
        this.path = new ArrayList<StatePathEntry>();
        this.path.add(new StatePathEntry(this.initState));
    }

    public void addListener(final StateEventListener listener) {
        this.eventSender.addListener(listener);
    }

    /**
     * synchronized execution of a runnable if statemachine is currently in a
     * given state
     * 
     * @param run
     * @param state
     * @return
     */
    public boolean executeIfOnState(final Runnable run, final State state) {
        if (run == null || state == null) { return false; }
        synchronized (this.lock) {
            if (this.isState(state)) {
                run.run();
                return true;
            }
        }
        return false;
    }

    /*
     * synchronized hasPassed/addListener to start run when state has
     * reached/passed
     */
    public void executeOnceOnState(final Runnable run, final State state) {
        if (run == null || state == null) { return; }
        boolean reached = false;
        synchronized (this.lock) {
            if (this.hasPassed(state)) {
                reached = true;
            } else {
                this.addListener(new StateListener(state) {
                    @Override
                    public void onStateReached(final StateEvent event) {
                        StateMachine.this.removeListener(this);
                        run.run();
                    }
                });
            }
            if (reached) {
                new Thread(run, "AsyncOnStateWorker").start();
            }
        }
    }

    public void fireUpdate(final State currentState) {
        if (currentState != null) {
            synchronized (this.lock) {
                if (this.currentState != currentState) { throw new StateConflictException("Cannot update state " + currentState + " because current state is " + this.currentState); }
            }
        }
        final StateEvent event = new StateEvent(this, StateEvent.Types.UPDATED, currentState, currentState);
        this.eventSender.fireEvent(event);
    }

    public void forceState(final State newState) {
        StateEvent event;
        synchronized (this.lock) {
            if (this.currentState == newState) { return; }
            event = new StateEvent(this, StateEvent.Types.CHANGED, this.currentState, newState);
            synchronized (this.lock2) {
                this.path.add(new StatePathEntry(newState));
            }
            Log.L.finest(this.owner + " State changed " + this.currentState + " -> " + newState);
            this.currentState = newState;
        }
        this.eventSender.fireEvent(event);
    }

    public Throwable getCause(final State newState) {
        return this.exceptionMap.get(newState);
    }

    /**
     * TODO: not synchronized
     * 
     * @param failedState
     * @return
     */
    public StatePathEntry getLatestStateEntry(final State failedState) {
        try {
            StatePathEntry entry = null;
            synchronized (this.lock2) {
                for (int i = this.path.size() - 1; i >= 0; i--) {
                    entry = this.path.get(i);
                    if (entry.getState() == failedState) { return entry; }
                }
            }
        } catch (final Exception e) {
        }
        return null;
    }

    public StateMachineInterface getOwner() {
        return this.owner;
    }

    /**
     * @return the path
     */
    public java.util.List<StatePathEntry> getPath() {
        return this.path;
    }

    public State getState() {
        return this.currentState;
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

    public boolean hasPassed(final State... states) {
        synchronized (this.lock2) {
            for (final State s : states) {
                for (final StatePathEntry e : this.path) {
                    if (e.getState() == s) { return true; }
                }
            }
        }
        return false;
    }

    // private State getStateById(State startState, int id,
    // java.util.List<State>
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

    public boolean isFinal() {
        synchronized (this.lock) {
            return this.finalState == this.currentState;
        }
    }

    /**
     * returns if the statemachine is in startstate currently
     */
    public boolean isStartState() {
        synchronized (this.lock) {
            return this.currentState == this.initState;
        }
    }

    public boolean isState(final State... states) {
        synchronized (this.lock) {
            for (final State s : states) {
                if (s == this.currentState) { return true; }
            }
        }
        return false;
    }

    public void removeListener(final StateEventListener listener) {
        this.eventSender.removeListener(listener);
    }

    public void reset() {
        this.reset(false);
    }

    /**
     * set force to true of you want to reset in any case. else reset is only
     * possible inf inal state
     * 
     * @param force
     */
    public void reset(final boolean force) {
        StateEvent event;
        synchronized (this.lock) {
            if (this.currentState == this.initState) { return; }
            if (!force && this.finalState != this.currentState) { throw new StateConflictException("Cannot reset from state " + this.currentState); }
            event = new StateEvent(this, StateEvent.Types.CHANGED, this.currentState, this.initState);
            Log.L.finest(this.owner + " State changed (reset) " + this.currentState + " -> " + this.initState);
            this.currentState = this.initState;
            synchronized (this.lock2) {
                this.path.clear();
                this.path.add(new StatePathEntry(this.initState));
            }
        }
        this.eventSender.fireEvent(event);
    }

    public void setCause(final State failedState, final Throwable e) {
        this.exceptionMap.put(failedState, e);
    }

    public void setStatus(final State newState) {
        synchronized (this.lock) {
            if (this.currentState == newState) { return; }
            if (!this.currentState.getChildren().contains(newState)) { throw new StateConflictException("Cannot change state from " + this.currentState + " to " + newState); }
        }
        this.forceState(newState);
    }

    /**
     * Throws a StateViolationException if the current state is not state
     * 
     * @param downloadBranchlist
     */
    public void validateState(final State state) {
        if (!this.isState(state)) { throw new StateViolationException(state); }
    }
}
