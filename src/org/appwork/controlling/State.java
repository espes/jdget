package org.appwork.controlling;

import java.util.ArrayList;

public class State {

    public static final int INIT_STATE = -1;

    /**
     * Links all State in a the parameter order
     * 
     * @param stateList
     */
    public static void link(final State... stateList) {
        State prev = null;
        for (final State s : stateList) {
            if (prev == null) {
                prev = s;
            } else {
                prev.addChildren(s);
                prev = s;
            }
        }

    }

    private final String           label;

    private final java.util.List<State> parents;

    private final java.util.List<State> children;

    private final int              id;

    public State(final int stateID, final String label) {
        id = stateID;
        this.label = label;
        parents = new ArrayList<State>();
        children = new ArrayList<State>();
    }

    // private Throwable cause;

    public State(final String label) {
        this(State.INIT_STATE, label);
    }

    public void addChildren(final State... states) {
        for (final State s : states) {
            children.add(s);
        }
    }

    /**
     * @return the children
     */
    public java.util.List<State> getChildren() {
        return children;
    }

    public int getID() {
        return id;
    }

    /**
     * @return the parents
     */
    public java.util.List<State> getParents() {
        return parents;
    }

    @Override
    public String toString() {
        return label + "-" + id + "(" + hashCode() + ")";
    }

    public String getLabel() {
        return this.label;
    }

}
