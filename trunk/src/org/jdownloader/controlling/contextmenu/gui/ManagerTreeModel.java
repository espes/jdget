package org.jdownloader.controlling.contextmenu.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.controlling.contextmenu.MenuContainerRoot;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuItemData.Type;

public class ManagerTreeModel extends DefaultTreeModel implements TreeModel {

    private MenuContainerRoot data;
    private MenuManagerTree   tree;

    public ManagerTreeModel(MenuContainerRoot menuContainerRoot) {
        super(null, false);

        set(menuContainerRoot);

    }

    public void set(final MenuContainerRoot menuContainerRoot) {
        new Thread("LoadMenuContainerRoor") {

            public void run() {
                MenuContainerRoot menuContainerRoot2 = JSonStorage.restoreFromString(JSonStorage.serializeToJson(menuContainerRoot), new TypeRef<MenuContainerRoot>() {
                });
                menuContainerRoot2.validateFull();

                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        data = menuContainerRoot;
                        fireTreeStructureChanged(this, new Object[] { data }, null, null);

                    }
                };
            }
        }.start();
        // create a copy

    }

    @Override
    public Object getRoot() {
        return data;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((MenuItemData) ((MenuItemData) parent).getItems().get(index));
    }

    @Override
    public int getChildCount(Object parent) {
        return ((MenuItemData) parent).getItems() == null ? 0 : ((MenuItemData) parent).getItems().size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((MenuItemData) node).getType() != Type.CONTAINER;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((MenuItemData) parent).getItems().indexOf(child);
    }

    public void moveTo(TreePath obj, MenuItemData parent, int childIndex) {

        MenuItemData itemToMove = (MenuItemData) obj.getLastPathComponent();
        MenuItemData oldParent = ((MenuItemData) obj.getPathComponent(obj.getPathCount() - 2));
        // oldParent.getItems().remove(itemToMove);
        // int oldIndex = oldParent.getItems().indexOf(itemToMove);
        // if(oldIndex>=0)oldParent.getItems().set(oldIndex, element)
        if (childIndex < 0) {
            // dropped on
            oldParent.getItems().remove(itemToMove);
            parent.getItems().add(itemToMove);
        } else {
            List<MenuItemData> a = new ArrayList<MenuItemData>(parent.getItems().subList(0, childIndex));
            List<MenuItemData> b = new ArrayList<MenuItemData>(parent.getItems().subList(childIndex, parent.getItems().size()));

            if (!a.remove(itemToMove)) {
                b.remove(itemToMove);
            }
            ArrayList<MenuItemData> newlist = new ArrayList<MenuItemData>();

            newlist.addAll(a);
            newlist.add(itemToMove);
            newlist.addAll(b);
            parent.setItems(newlist);
            if (oldParent != parent) {
                oldParent.getItems().remove(itemToMove);
            }
        }
        // data
        fireTreeStructureChanged(this, new Object[] { data }, null, null);

    }

    public void remove(TreePath treePath) {
        if (treePath != null) {
            MenuItemData parent = (MenuItemData) treePath.getPathComponent(treePath.getPathCount() - 2);
            parent.getItems().remove(treePath.getLastPathComponent());
            fireTreeStructureChanged(this, new Object[] { data }, null, null);

        }

    }

    public TreePath addAction(TreePath treePath, MenuItemData menuItemData) {
        try {
            menuItemData._setValidated(true);
            if (treePath != null && treePath.getLastPathComponent() != data) {
                if (((MenuItemData) treePath.getLastPathComponent()).getType() == Type.CONTAINER) {

                    ((MenuItemData) treePath.getLastPathComponent()).getItems().add(menuItemData);

                    return treePath.pathByAddingChild(menuItemData);
                } else {
                    MenuItemData parent = (MenuItemData) treePath.getPathComponent(treePath.getPathCount() - 2);

                    int index = parent.getItems().indexOf(treePath.getLastPathComponent());

                    parent.getItems().add(index + 1, menuItemData);

                    return treePath.getParentPath().pathByAddingChild(menuItemData);

                }

            } else {
                data.getItems().add(menuItemData);
                return new TreePath(new Object[] { data, menuItemData });
            }
        } finally {

            fireTreeStructureChanged(this, new Object[] { data }, null, null);
        }

    }

    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        // String before = JSonStorage.toString(data);
        // make sure that we have valid menu structure
        data.validateFull();
        // String after = JSonStorage.toString(data);
        // if (!before.equals(after)) {
        // Dialog.getInstance().showMessageDialog("Menu Structure not allowed!");
        //
        // }
        if (tree != null) {
            TreePath[] paths = tree.getSelectionPaths();

            super.fireTreeStructureChanged(source, path, childIndices, children);

            if (paths != null) tree.setSelectionPaths(paths);
            tree.expandAll();
        }
    }

    public void fireUpdate() {
        fireTreeStructureChanged(this, new Object[] { data }, null, null);
    }

    public void setTree(MenuManagerTree extTree) {
        this.tree = extTree;
    }

}
