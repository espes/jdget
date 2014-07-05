package org.appwork.swing.exttable;

import java.util.Comparator;

/**
 * Default Rowsorter
 * 
 * @author $Author: unknown$
 * 
 */
public class ExtDefaultRowSorter<E> implements Comparator<E> {

    protected String sortOrderIdentifier = ExtColumn.SORT_ASC;

    @SuppressWarnings("unchecked")
    public int compare(final E o1, final E o2) {
        if (ExtColumn.SORT_ASC.equals(this.getSortOrderIdentifier())) {
            if (o1 instanceof Comparable<?>) { return ((Comparable<E>) o1).compareTo(o2); }
            return o1.toString().compareTo(o2.toString());
        } else {
            if (o1 instanceof Comparable<?>) { return ((Comparable<E>) o2).compareTo(o1); }
            return o2.toString().compareTo(o1.toString());
        }
    }

    public String getSortOrderIdentifier() {
        return this.sortOrderIdentifier;
    }

    /**
     * @param sortOrderToggle
     *            the {@link ExtDefaultRowSorter#sortOrderToggle} to set
     * @see ExtDefaultRowSorter#sortOrderToggle
     */
    public void setSortOrderIdentifier(final String sortOrderIdentifier) {
        this.sortOrderIdentifier = sortOrderIdentifier;
    }

}
