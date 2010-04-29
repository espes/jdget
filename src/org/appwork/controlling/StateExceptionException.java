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

/**
 * @author thomas
 * 
 */
public class StateExceptionException extends Exception {

    private StateMachine stateMachine;

    /**
     * @return the stateMachine
     */
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     * @param stateMachine
     *            the stateMachine to set
     */
    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

    private State state;

    /**
     * @param st
     * @param e
     */
    public StateExceptionException(StateMachine st, State e) {

        stateMachine = st;
        state = e;

    }

}
