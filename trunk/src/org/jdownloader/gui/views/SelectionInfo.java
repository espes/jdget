package org.jdownloader.gui.views;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;

import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.utils.BinaryLogic;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTable;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModel;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTableModelFilter;

public class SelectionInfo<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> {

    private static List<AbstractNode> pack(AbstractNode clicked) {
        java.util.List<AbstractNode> ret = new ArraySet<AbstractNode>();
        ret.add(clicked);
        return ret;
    }

    private ArraySet<PackageType>                                              allPackages;
    private AbstractNode                                                       contextObject;
    private ArraySet<PackageType>                                              fullPackages;
    private ArraySet<PackageType>                                              incompletePackages;
    private HashMap<PackageType, ArraySet<ChildrenType>>                       incompleteSelectecPackages;
    private KeyEvent                                                           keyEvent;
    private MouseEvent                                                         mouseEvent;

    private List<? extends AbstractNode>                                       rawSelection;

    private ArraySet<ChildrenType>                                             children;

    private PackageControllerTable<PackageType, ChildrenType>                  table;
    private ExtColumn<AbstractNode>                                            contextColumn;
    private boolean                                                            shiftDown = false;
    private List<PackageControllerTableModelFilter<PackageType, ChildrenType>> filters   = null;

    public List<PackageControllerTableModelFilter<PackageType, ChildrenType>> getTableFilters() {
        return filters;
    }

    private ActionEvent            actionEvent;
    private ArraySet<AbstractNode> raw;

    public SelectionInfo<PackageType, ChildrenType> setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
        return this;
    }

    public PackageControllerTable<PackageType, ChildrenType> getTable() {
        return table;
    }

    public SelectionInfo(AbstractNode contextObject, List<? extends AbstractNode> selection, MouseEvent event, KeyEvent kEvent, ActionEvent actionEvent, PackageControllerTable<PackageType, ChildrenType> table) {
        this.contextObject = contextObject;
        if (selection == null || selection.size() == 0) {
            if (contextObject == null) {
                rawSelection = new ArraySet<AbstractNode>();
            } else {
                rawSelection = pack(contextObject);
            }
        } else {
            rawSelection = selection;
        }

        children = new ArraySet<ChildrenType>();
        allPackages = new ArraySet<PackageType>();
        fullPackages = new ArraySet<PackageType>();
        incompletePackages = new ArraySet<PackageType>();
        incompleteSelectecPackages = new HashMap<PackageType, ArraySet<ChildrenType>>();

        // System.out.println(kEvent);

        setMouseEvent(event);
        setKeyEvent(kEvent);
        setActionEvent(actionEvent);
        this.table = table;
        if (table != null) {
            PackageControllerTableModel<PackageType, ChildrenType> tableModel = table.getModel();
            List<PackageControllerTableModelFilter<PackageType, ChildrenType>> lfilters = tableModel.getEnabledTableFilters();
            if (lfilters != null && lfilters.size() > 1) {
                filters = lfilters;
            }
        }

        // System.out.println(isShiftDown());
        agregate();

    }

    public boolean contains(AbstractPackageNode<?, ?> child) {

        return allPackages.contains(child);
    }

    public boolean contains(AbstractPackageChildrenNode<?> child) {

        return children.contains(child);

    }

    protected void setActionEvent(ActionEvent actionEvent) {
        this.actionEvent = actionEvent;
        if (actionEvent != null && BinaryLogic.containsSome(actionEvent.getModifiers(), ActionEvent.SHIFT_MASK)) {
            shiftDown = true;
        }
    }

    protected void setKeyEvent(KeyEvent kEvent) {
        this.keyEvent = kEvent;
        if (keyEvent != null && keyEvent.isShiftDown()) {
            shiftDown = true;
        }
    }

    protected void setMouseEvent(MouseEvent event) {
        this.mouseEvent = event;
        if (mouseEvent != null && mouseEvent.isShiftDown()) shiftDown = true;
    }

    //
    // public SelectionInfo(List<? extends AbstractNode> selection) {
    // this(null, selection, null);
    //
    // }

    private SelectionInfo() {
    }

    @SuppressWarnings("unchecked")
    protected void agregate() {
        raw = new ArraySet<AbstractNode>(rawSelection);
        // LinkedHashSet<AbstractNode> notSelectedParents = new LinkedHashSet<AbstractNode>();
        // if we selected a link, and not its parent, this parent will not be agregated. That's why we add them here.
        for (AbstractNode node : rawSelection) {
            if (node == null) continue;

            if (node instanceof AbstractPackageChildrenNode) {
                // if (!has.contains(((AbstractPackageChildrenNode) node).getParentNode())) {
                PackageType pkg = (PackageType) ((AbstractPackageChildrenNode) node).getParentNode();
                if (pkg != null && pkg.isExpanded()) {
                    raw.add(pkg);

                }

                // }
            }
        }

        for (AbstractNode node : raw) {
            if (node == null) continue;
            if (node instanceof AbstractPackageChildrenNode) {
                children.add((ChildrenType) node);

                allPackages.add(((ChildrenType) node).getParentNode());
            } else {

                // if we selected a package, and ALL it's links, we want all
                // links
                // if we selected a package, and nly afew links, we probably
                // want only these few links.
                // if we selected a package, and it is NOT expanded, we want
                // all
                // links
                allPackages.add((PackageType) node);
                if (!((PackageType) node).isExpanded()) {
                    // add allTODO
                    boolean readL = ((PackageType) node).getModifyLock().readLock();
                    try {
                        List<ChildrenType> childs = ((PackageType) node).getChildren();
                        ArraySet<ChildrenType> unFiltered = new ArraySet<ChildrenType>();
                        if (filters == null) {
                            children.addAll(childs);
                            fullPackages.add((PackageType) node);
                        } else {
                            for (ChildrenType l : childs) {
                                boolean filtered = false;
                                for (PackageControllerTableModelFilter<PackageType, ChildrenType> filter : filters) {
                                    if (filter.isFiltered(l)) {
                                        filtered = true;
                                        break;
                                    }
                                }
                                if (!filtered) {
                                    unFiltered.add(l);
                                }
                            }
                            children.addAll(unFiltered);
                            if (unFiltered.size() == childs.size()) {
                                fullPackages.add((PackageType) node);
                            } else {
                                incompleteSelectecPackages.put((PackageType) node, unFiltered);
                            }
                        }
                    } finally {
                        ((PackageType) node).getModifyLock().readUnlock(readL);
                    }

                } else {
                    boolean readL = ((PackageType) node).getModifyLock().readLock();
                    try {
                        List<ChildrenType> childs = ((PackageType) node).getChildren();
                        boolean containsNone = true;
                        boolean containsAll = true;
                        ArraySet<ChildrenType> selected = new ArraySet<ChildrenType>();
                        ArraySet<ChildrenType> unFiltered = new ArraySet<ChildrenType>();
                        for (ChildrenType l : childs) {
                            if (raw.contains(l)) {
                                selected.add(l);
                                containsNone = false;
                            } else {
                                containsAll = false;
                                if (filters != null) {
                                    boolean filtered = false;
                                    for (PackageControllerTableModelFilter<PackageType, ChildrenType> filter : filters) {
                                        if (filter.isFiltered(l)) {
                                            filtered = true;
                                            break;
                                        }
                                    }
                                    if (!filtered) unFiltered.add(l);
                                }
                            }

                        }
                        // table.getModel()
                        if (containsNone) {
                            // this is a special case. if the user selected only the package, we cannot simply add all children. We need to
                            // check if he works on a filtered view.
                            if (filters == null) {
                                children.addAll(childs);
                                fullPackages.add((PackageType) node);
                            } else {
                                children.addAll(unFiltered);
                                if (unFiltered.size() == childs.size()) {
                                    fullPackages.add((PackageType) node);
                                } else {
                                    incompleteSelectecPackages.put((PackageType) node, unFiltered);
                                }
                            }
                        } else if (containsAll) {

                            children.addAll(childs);
                            fullPackages.add((PackageType) node);
                        } else {
                            if (incompletePackages.add((PackageType) node)) {
                                incompleteSelectecPackages.put((PackageType) node, selected);
                            }

                        }
                    } finally {
                        ((PackageType) node).getModifyLock().readUnlock(readL);
                    }
                }
            }
        }

    }

    /**
     * A List of all packages in this selection. the list contains {@link #getFullPackages()} & {@link #getIncompletePackages()}
     * 
     * @return
     */
    public List<PackageType> getAllPackages() {
        return allPackages;
    }

    /**
     * 
     * @see #getContextLink()
     * @return
     */
    public ChildrenType getLink() {
        return getContextLink();
    }

    /**
     * if this object is a childcontext, this returns the child, else throws exception
     * 
     * @return
     */
    public ChildrenType getContextLink() {
        if (isLinkContext()) return (ChildrenType) contextObject;

        throw new BadContextException("Not available in Packagecontext");
    }

    /**
     * If there is a context Object, this method returns it. try to muse {@link #getContextLink()} or {@link #getContextPackage()} instead
     * 
     * @return
     */
    public AbstractNode getRawContext() {
        return contextObject;
    }

    /**
     * if we have packagecontext, this returns the package, else the child's PACKAGE
     * 
     * @return
     */
    public PackageType getContextPackage() {
        if (contextObject == null) throw new BadContextException("Context is null");
        if (isPackageContext()) {
            return (PackageType) contextObject;
        } else {
            return ((ChildrenType) contextObject).getParentNode();
        }

    }

    /**
     * Returns either the context pacakge, or the context link's package, or the first links package
     * 
     * @see #getContextPackage()
     * @return
     */
    public PackageType getFirstPackage() {

        if (contextObject == null) {
            if (children.size() == 0) throw new BadContextException("Invalid Context");
            return children.get(0).getParentNode();
        } else {

            return getContextPackage();
        }

    }

    /**
     * @see #getContextPackage()
     * @return
     */
    public PackageType getPackage() {
        return getContextPackage();
    }

    /**
     * Returns a list of packages. This list only contains packages that have their full linklist selected as well.
     * 
     * @see #getAllPackages()
     * @see #getIncompletePackages()
     * @return
     */
    public List<PackageType> getFullPackages() {
        return fullPackages;
    }

    /**
     * This method returns a list of packages. Only Packages whose linklist ist NOT completly selected as well are contained
     * 
     * @return
     */
    public List<PackageType> getIncompletePackages() {
        return incompletePackages;
    }

    /**
     * The KeyEvent when the selection has been created
     * 
     * @return
     */
    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    /**
     * The mouseevent when the selection was created
     * 
     * @return
     */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    /**
     * Returns a List of the rawselection. Contains packages and links as they were selected in the table. USe {@link #getChildren()} instead
     * 
     * @return
     */
    public List<? extends AbstractNode> getRawSelection() {
        return rawSelection;
    }

    /**
     * A list of all selected children. This list also contains the children of collapsed selected packages
     * 
     * @return
     */
    public ArraySet<ChildrenType> getChildren() {
        return children;
    }

    /**
     * Not all links of a package may have been selected @see ( {@link #getIncompletePackages()}. to get a list of all selected links for a certain package, use
     * this method
     * 
     * @param pkg
     * @return
     */
    public ArraySet<ChildrenType> getSelectedLinksByPackage(PackageType pkg) {
        ArraySet<ChildrenType> ret = incompleteSelectecPackages.get(pkg);
        if (ret != null) return ret;
        boolean readL = pkg.getModifyLock().readLock();
        try {
            return new ArraySet<ChildrenType>(pkg.getChildren());
        } finally {
            pkg.getModifyLock().readUnlock(readL);
        }

    }

    /**
     * true if the direct context is a link
     * 
     * @return
     */
    public boolean isLinkContext() {
        return contextObject != null && contextObject instanceof AbstractPackageChildrenNode;
    }

    /**
     * false if there are selected links
     * 
     * @return
     */
    public boolean isEmpty() {
        return children == null || children.size() == 0;
    }

    /**
     * true if the direct context is a package
     * 
     * @return
     */
    public boolean isPackageContext() {
        return contextObject != null && contextObject instanceof AbstractPackageNode;
    }

    public SelectionInfo<PackageType, ChildrenType> derive(AbstractNode contextObject2, MouseEvent event, KeyEvent kEvent, ActionEvent actionEvent, ExtColumn<AbstractNode> column) {
        SelectionInfo<PackageType, ChildrenType> ret = new SelectionInfo<PackageType, ChildrenType>();
        ret.actionEvent = this.actionEvent;
        ret.mouseEvent = mouseEvent;
        ret.allPackages = allPackages;
        ret.children = children;
        ret.contextColumn = this.contextColumn;
        ret.contextObject = this.contextObject;
        ret.filters = filters;
        ret.fullPackages = fullPackages;
        ret.incompletePackages = incompletePackages;
        ret.incompleteSelectecPackages = incompleteSelectecPackages;
        ret.keyEvent = this.keyEvent;
        ret.mouseEvent = mouseEvent;
        ret.raw = raw;
        ret.rawSelection = rawSelection;
        ret.shiftDown = shiftDown;
        ret.table = table;

        if (contextObject2 != null) {

            ret.contextObject = contextObject2;
        }
        if (event != null) {
            ret.setMouseEvent(event);

        }
        if (actionEvent != null) {
            ret.setActionEvent(actionEvent);

        }
        if (kEvent != null) {
            ret.setKeyEvent(kEvent);
        }
        if (column != null) {

            ret.contextColumn = column;
        }
        return ret;
    }

}
