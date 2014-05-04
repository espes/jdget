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

    private static final long serialVersionUID = 1920686119279132054L;

    private StateMachine stateMachine;
    private State state;

    /**
     * @param stateMachine
     * @paramstatee
     */
    public StateExceptionException(StateMachine stateMachine, State state) {
        this.stateMachine = stateMachine;
        this.state = state;
    }

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

}
