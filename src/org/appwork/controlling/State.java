package org.appwork.controlling;

import java.util.ArrayList;

public class State {

    public static final int INIT_STATE = -1;
    private String label;
    private ArrayList<State> parents;

    /**
     * @return the parents
     */
    public ArrayList<State> getParents() {
        return parents;
    }

    /**
     * @return the children
     */
    public ArrayList<State> getChildren() {
        return children;
    }

    private ArrayList<State> children;
    private int id;
    private Throwable cause;

    /**
     * @param stateID
     * @param string
     */
    public State(int stateID, String label) {
        this.id = stateID;
        this.label = label;
        parents = new ArrayList<State>();
        children = new ArrayList<State>();
    }

    /**
     * @param string
     */
    public State(String label) {
        this(INIT_STATE, label);
    }

    /**
     * @param addLink
     * @param runningState
     * @return
     */
    public void addChildren(State... states) {
        for (State s : states)
            children.add(s);

    }

    public String toString() {
        return this.label + "-" + this.id + "(" + hashCode() + ")";
    }

    /**
     * @return
     */
    public int getID() {
        // TODO Auto-generated method stub
        return this.id;
    }

    /**
     * Sets the exception that is responsible for this state
     * 
     * @param e
     */
    public void setCause(Throwable e) {
        cause = e;

    }

    /**
     * @return the cause
     */
    public Throwable getCause() {
        return cause;
    }

}
