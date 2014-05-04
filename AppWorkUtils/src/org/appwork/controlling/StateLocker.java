package org.appwork.controlling;

public class StateLocker implements StateEventListener {

    private StateMachine[] stateMachines;
    private int counter;
    private State waitState;
    private State[] exceptions;
    private State interruptState = null;
    private StateMachine interruptStatemachine;

    /**
     * @param machines
     */
    public StateLocker(StateMachine... machines) {
        stateMachines = machines.clone();
        counter = 0;
    }

    /**
     * @param stoppedState
     * @throws InterruptedException
     * @throws StateExceptionException
     */
    public void lockUntilAllHavePassed(final State state, final State... exceptions) throws InterruptedException, StateExceptionException {
        waitState = state;
        this.exceptions = exceptions;
        if (stateMachines == null || stateMachines.length == 0) return;
        try {
            for (StateMachine st : stateMachines) {
                for (State e : exceptions) {
                    if (st.hasPassed(e)) throw new StateExceptionException(st, e);
                }

                if (st.hasPassed(state)) {
                    increaseCounter();

                } else {
                    st.addListener(this);
                }
            }

            main: while (counter < stateMachines.length) {
                synchronized (this) {
                    this.wait(2000);
                }
                if (interruptState != null) { throw new StateExceptionException(interruptStatemachine, interruptState); }
                for (StateMachine st : stateMachines) {
                    if (!st.hasPassed(state)) {
                        continue main;
                    }
                }

                break;
            }
        } finally {
            for (StateMachine st : stateMachines) {
                st.removeListener(this);

            }

        }
    }

    /**
     * 
     */
    private synchronized int increaseCounter() {
        return ++counter;
    }

    public void onStateChange(StateEvent event) {
        if (event.getNewState() == waitState) {
            if (increaseCounter() == stateMachines.length) {
                synchronized (this) {
                    this.notify();
                }
                event.getStateMachine().removeListener(this);
            }
        } else {
            for (State s : exceptions) {
                if (event.getNewState() == s) {
                    this.interruptState = s;
                    this.interruptStatemachine = event.getStateMachine();
                    synchronized (this) {
                        this.notify();
                    }
                    return;
                }
            }
        }
    }

    public void onStateUpdate(StateEvent event) {

    }

}
