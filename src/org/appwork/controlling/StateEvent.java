/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.controlling
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.controlling;

import org.appwork.utils.event.Event;

/**
 * @author thomas
 * 
 */
public class StateEvent extends Event {

    private State oldState;
    private State newState;
    private StateMachine stateMachine;

    /**
     * @param stateMachine
     * @param changed2
     * @param currentState
     * @param newState
     */
    public StateEvent(StateMachine stateMachine, int id, State currentState, State newState) {
        super(stateMachine, id, newState);
        this.stateMachine = stateMachine;
        this.oldState = currentState;
        this.newState = newState;
    }

    /**
     * @return the oldState
     */
    public State getOldState() {
        return oldState;
    }

    /**
     * @return the newState
     */
    public State getNewState() {
        return newState;
    }

    public static final int CHANGED = 0;
    public static final int UPDATED = 1;

    /**
     * @return
     */
    public StateMachine getStateMachine() {
        // TODO Auto-generated method stub
        return stateMachine;
    }

    /**
     * @return
     */
    public Throwable getCause() {
        // TODO Auto-generated method stub
        return getStateMachine().getCause(getNewState());
    }

}
