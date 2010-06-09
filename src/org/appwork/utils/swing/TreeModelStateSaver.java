/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.appwork.utils.logging.Log;

/**
 * This class is used to save the selection states and expanded states of each
 * tree node before rebuilding the tree. After rebuilding, the model is able to
 * restore these states.
 * 
 * @author $Author: unknown$
 * 
 */
public class TreeModelStateSaver {

    protected JTree tree;
    /**
     * Stores for each node the expanded state
     */
    private HashMap<Object, Boolean> expandCache;

    /**
     * @param selectedPathes
     *            the selectedPathes to set
     */
    public void setSelectedPathes(TreePath[] selectedPathes) {
        this.selectedPathes = selectedPathes;
    }

    /**
     * @return the expandCache
     */
    public HashMap<Object, Boolean> getExpandCache() {
        return expandCache;
    }

    /**
     * treePath for internal use
     */
    private TreePath treePath;
    /**
     * Stores all selected Pathes
     */
    protected TreePath[] selectedPathes;

    /**
     * @param tree
     */
    public TreeModelStateSaver(JTree tree) {
        this.tree = tree;
        this.expandCache = new HashMap<Object, Boolean>();
    }

    /**
     * Save the current state of the tree
     */
    public void save() {
        saveState(tree.getModel().getRoot(), new ArrayList<Object>());
        selectedPathes = tree.getSelectionPaths();
    }

    /**
     * Saves the expaned states of each node to cacheMap runs rekursive
     * 
     * @param root
     */
    private void saveState(Object node, ArrayList<Object> path) {
        path.add(node);
        try {
            treePath = new TreePath(path.toArray(new Object[] {}));
            expandCache.put(node, tree.isExpanded(treePath));
        } catch (Exception e) {
            Log.exception(e);

        }
        int max = tree.getModel().getChildCount(node);
        for (int i = 0; i < max; i++) {
            try {
                saveState(tree.getModel().getChild(node, i), new ArrayList<Object>(path));
            } catch (Exception e) {
                Log.exception(e);
            }
            tree.getModel().getChildCount(node);
        }
    }

    /**
     * Restore the saved tree state
     */
    public void restore() {
        restoreState(tree.getModel().getRoot(), new ArrayList<Object>());
        if (selectedPathes != null && selectedPathes.length > 0) {
            tree.getSelectionModel().clearSelection();
            tree.getSelectionModel().setSelectionPaths(selectedPathes);
        }
    }

    /**
     * @return the {@link TreeModelStateSaver#selectedPathes}
     * @see TreeModelStateSaver#selectedPathes
     */
    public TreePath[] getSelectedPathes() {
        return selectedPathes;
    }

    protected void restoreState(Object node, ArrayList<Object> path) {
        if (node == null) return;
        path.add(node);
        treePath = new TreePath(path.toArray(new Object[] {}));
        Boolean bo = expandCache.get(node);
        if (bo != null && bo.booleanValue()) {
            tree.expandPath(treePath);
        }

        for (int i = 0; i < tree.getModel().getChildCount(node); i++) {
            restoreState(tree.getModel().getChild(node, i), new ArrayList<Object>(path));
        }
    }

}
