package org.appwork.utils.swing.table;

import java.util.Comparator;

/**
 * Default Rowsorter
 * 
 * @author $Author: unknown$
 * 
 */
public class ExtDefaultRowSorter<E> implements Comparator<E> {

    private boolean sortOrderToggle = true;

    /**
     * @return the {@link ExtDefaultRowSorter#sortOrderToggle}
     * @see ExtDefaultRowSorter#sortOrderToggle
     */
    public boolean isSortOrderToggle() {
        return sortOrderToggle;
    }

    /**
     * @param sortOrderToggle
     *            the {@link ExtDefaultRowSorter#sortOrderToggle} to set
     * @see ExtDefaultRowSorter#sortOrderToggle
     */
    public void setSortOrderToggle(boolean sortOrderToggle) {
        this.sortOrderToggle = sortOrderToggle;
    }

    @SuppressWarnings("unchecked")
    public int compare(E o1, E o2) {
        if (sortOrderToggle) {
            if (o1 instanceof Comparable<?>) return ((Comparable<E>) o1).compareTo(o2);
            return o1.toString().compareTo(o2.toString());
        } else {
            if (o1 instanceof Comparable<?>) return ((Comparable<E>) o2).compareTo(o1);
            return o2.toString().compareTo(o1.toString());
        }
    }

}
