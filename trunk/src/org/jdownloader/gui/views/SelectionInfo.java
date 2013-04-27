package org.jdownloader.gui.views;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.controlling.packagecontroller.AbstractPackageNode;

import org.appwork.swing.exttable.ExtColumn;
import org.appwork.utils.BinaryLogic;
import org.jdownloader.gui.views.components.packagetable.PackageControllerTable;

public class SelectionInfo<PackageType extends AbstractPackageNode<ChildrenType, PackageType>, ChildrenType extends AbstractPackageChildrenNode<PackageType>> {

    private static List<AbstractNode> pack(AbstractNode clicked) {
        java.util.List<AbstractNode> ret = new ArrayList<AbstractNode>();
        ret.add(clicked);
        return ret;
    }

    private List<PackageType>                                 allPackages;
    private AbstractNode                                      contextObject;
    private List<PackageType>                                 fullPackages;
    private List<PackageType>                                 incompletePackages;
    private HashMap<PackageType, List<ChildrenType>>          incompleteSelectecPackages;
    private KeyEvent                                          keyEvent;
    private MouseEvent                                        mouseEvent;
    private LinkedHashSet<AbstractNode>                       rawMap;

    private List<? extends AbstractNode>                      rawSelection;

    private List<ChildrenType>                                children;

    private PackageControllerTable<PackageType, ChildrenType> table;
    private ExtColumn<AbstractNode>                           contextColumn;

    public PackageControllerTable<PackageType, ChildrenType> getTable() {
        return table;
    }

    public SelectionInfo(AbstractNode clicked) {
        this(clicked, pack(clicked));
    }

    public SelectionInfo(AbstractNode contextObject, List<? extends AbstractNode> selection) {
        this(contextObject, selection, null);

    }

    public SelectionInfo(AbstractNode contextObject, List<? extends AbstractNode> selection, MouseEvent event) {
        this(contextObject, selection, event, null);
    }

    public SelectionInfo(AbstractNode contextObject, List<? extends AbstractNode> selection, MouseEvent event, KeyEvent kEvent) {

        this(contextObject, selection, event, kEvent, null);
    }

    public SelectionInfo(AbstractNode contextObject, List<? extends AbstractNode> selection, MouseEvent event, KeyEvent kEvent, PackageControllerTable<PackageType, ChildrenType> table) {

        this.contextObject = contextObject;
        rawSelection = selection == null ? new ArrayList<AbstractNode>() : selection;
        children = new ArrayList<ChildrenType>();
        allPackages = new ArrayList<PackageType>();
        fullPackages = new ArrayList<PackageType>();
        incompletePackages = new ArrayList<PackageType>();
        incompleteSelectecPackages = new HashMap<PackageType, List<ChildrenType>>();

        rawMap = new LinkedHashSet<AbstractNode>();

        // System.out.println(kEvent);

        this.mouseEvent = event;
        this.keyEvent = kEvent;
        this.table = table;
        // System.out.println(isShiftDown());
        agregate();
    }

    public SelectionInfo(List<? extends AbstractNode> selection) {
        this(null, selection, null);

    }

    @SuppressWarnings("unchecked")
    public void agregate() {
        java.util.List<AbstractNode> raw = new ArrayList<AbstractNode>(rawSelection);
        LinkedHashSet<AbstractNode> has = rawSelection == null ? new LinkedHashSet<AbstractNode>() : new LinkedHashSet<AbstractNode>(rawSelection);
        // LinkedHashSet<AbstractNode> notSelectedParents = new LinkedHashSet<AbstractNode>();
        // if we selected a link, and not its parent, this parent will not be agregated. That's why we add them here.
        for (AbstractNode node : rawSelection) {
            rawMap.add(node);
            if (node instanceof AbstractPackageChildrenNode) {
                if (!has.contains(((AbstractPackageChildrenNode) node).getParentNode())) {
                    if (has.add((AbstractNode) ((AbstractPackageChildrenNode) node).getParentNode())) {
                        raw.add((AbstractNode) ((AbstractPackageChildrenNode) node).getParentNode());

                    }

                }
            }
        }

        LinkedHashSet<ChildrenType> ret = new LinkedHashSet<ChildrenType>();
        LinkedHashSet<PackageType> allPkg = new LinkedHashSet<PackageType>();
        LinkedHashSet<PackageType> fullPkg = new LinkedHashSet<PackageType>();
        LinkedHashSet<PackageType> incPkg = new LinkedHashSet<PackageType>();

        for (AbstractNode node : raw) {

            if (node instanceof AbstractPackageChildrenNode) {
                ret.add((ChildrenType) node);
                allPkg.add(((ChildrenType) node).getParentNode());
            } else {

                // if we selected a package, and ALL it's links, we want all
                // links
                // if we selected a package, and nly afew links, we probably
                // want only these few links.
                // if we selected a package, and it is NOT expanded, we want
                // all
                // links
                allPkg.add((PackageType) node);
                if (!((PackageType) node).isExpanded()) {
                    // add allTODO
                    List<ChildrenType> childs = ((PackageType) node).getChildren();
                    ret.addAll(childs);
                    fullPkg.add((PackageType) node);

                } else {
                    List<ChildrenType> childs = ((PackageType) node).getChildren();
                    boolean containsNone = true;
                    boolean containsAll = true;
                    java.util.List<ChildrenType> selected = new ArrayList<ChildrenType>();
                    for (ChildrenType l : childs) {
                        if (has.contains(l)) {
                            selected.add(l);
                            containsNone = false;
                        } else {
                            containsAll = false;
                        }

                    }
                    if (containsAll || containsNone) {
                        ret.addAll(childs);
                        fullPkg.add((PackageType) node);
                    } else {
                        if (incPkg.add((PackageType) node)) {
                            incompleteSelectecPackages.put((PackageType) node, selected);
                        }

                    }
                }
            }
        }

        children.addAll(ret);
        allPackages.addAll(allPkg);
        fullPackages.addAll(fullPkg);
        incompletePackages.addAll(incPkg);

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
        try {
            return getContextPackage();
        } catch (BadContextException e) {
            if (children.size() == 0) throw new BadContextException("Invalid Context");
            return children.get(0).getParentNode();
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
     * Returns a List of the rawselection. Contains packages and links as they were selected in the table. USe {@link #getChildren()}
     * instead
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
    public List<ChildrenType> getChildren() {
        return children;
    }

    /**
     * Not all links of a package may have been selected @see ( {@link #getIncompletePackages()}. to get a list of all selected links for a
     * certain package, use this method
     * 
     * @param pkg
     * @return
     */
    public List<ChildrenType> getSelectedLinksByPackage(PackageType pkg) {
        List<ChildrenType> ret = incompleteSelectecPackages.get(pkg);
        if (ret != null) return ret;
        return pkg.getChildren();

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

    /**
     * returns true if the shift key has been pressed when generating this instance
     * 
     * @return
     */
    public boolean isShiftDown() {
        // System.out.println(keyEvent.getModifiers());
        // System.out.println(keyEvent.isShiftDown());
        if (keyEvent != null && BinaryLogic.containsSome(keyEvent.getModifiers(), ActionEvent.SHIFT_MASK)) { return true; }
        if (mouseEvent != null && mouseEvent.isShiftDown()) return true;
        return false;
    }

    /**
     * Returns true if the {@link #getRawSelection()} contains l
     * 
     * @param l
     * @return
     */
    public boolean rawContains(AbstractNode l) {
        return rawMap.contains(l);
    }

    /** is true if ctrl+D is pressed */
    public boolean isAvoidRlyEnabled() {

        if (keyEvent != null) {
            if (keyEvent.isControlDown()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void setContextColumn(ExtColumn<AbstractNode> column) {
        contextColumn = column;
    }

    public ExtColumn<AbstractNode> getContextColumn() {
        return contextColumn;
    }
}
