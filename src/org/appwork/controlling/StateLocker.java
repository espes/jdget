package org.appwork.controlling;

public class StateLocker implements StateEventListener {

    private StateMachine[] stateMachines;
    private int counter;
    private State waitState;

    /**
     * @param machines
     */
    public StateLocker(StateMachine[] machines) {
        stateMachines = machines.clone();
        counter = 0;
    }

    /**
     * @param stoppedState
     * @throws InterruptedException
     */
    public void lockUntilAllHavePassed(final State state) throws InterruptedException {
        waitState = state;
        for (StateMachine st : stateMachines) {
            if (st.hasPassed(state)) {
                increaseCounter();

            } else {
                st.addListener(this);
            }
        }

        main: while (true) {
            synchronized (this) {
                this.wait(2000);
            }
            for (StateMachine st : stateMachines) {
                if (!st.hasPassed(state)) {
                    continue main;
                }
            }
            break;
        }
        for (StateMachine st : stateMachines) {
            st.removeListener(this);

        }

    }

    /**
     * 
     */
    private synchronized int increaseCounter() {
        return ++counter;
    }

    @Override
    public void onStateChange(StateEvent event) {
        if (event.getNewState() == waitState) {
            if (increaseCounter() == stateMachines.length) {
                synchronized (this) {
                    this.notify();
                }
                event.getStateMachine().removeListener(this);
            }
        }
    }

    @Override
    public void onStateUpdate(StateEvent event) {
        // TODO Auto-generated method stub

    }

}
