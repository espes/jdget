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

import org.appwork.utils.event.SimpleEvent;

/**
 * @author thomas
 * 
 */
public class StateEvent extends SimpleEvent<StateMachine, State, StateEvent.Types> {

    public static enum Types {
        CHANGED,
        UPDATED
    }

    /**
     * @param stateMachine
     * @param changed2
     * @param currentState
     * @param newState
     */
    public StateEvent(final StateMachine stateMachine, final Types id, final State currentState, final State newState) {
        super(stateMachine, id, currentState, newState);

    }

    /**
     * @return
     */
    public Throwable getCause() {

        return this.getStateMachine().getCause(this.getNewState());
    }

    /**
     * @return the newState
     */
    public State getNewState() {
        return this.getParameter(1);
    }

    /**
     * @return the oldState
     */
    public State getOldState() {
        return this.getParameter(0);
    }

    /**
     * @return
     */
    public StateMachine getStateMachine() {

        return this.getCaller();
    }

}
