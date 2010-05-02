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

    // private Throwable cause;

    public State(int stateID, String label) {
        this.id = stateID;
        this.label = label;
        parents = new ArrayList<State>();
        children = new ArrayList<State>();
    }

    public State(String label) {
        this(INIT_STATE, label);
    }

    public void addChildren(State... states) {
        for (State s : states) {
            children.add(s);
        }
    }

    @Override
    public String toString() {
        return this.label + "-" + this.id + "(" + hashCode() + ")";
    }

    public int getID() {
        return this.id;
    }

}
