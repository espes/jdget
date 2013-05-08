package org.jdownloader.controlling.contextmenu;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.exceptions.WTFException;
import org.appwork.storage.Storable;
import org.appwork.utils.StringUtils;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.actions.AppAction;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionController;
import org.jdownloader.extensions.ExtensionNotLoadedException;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.images.NewTheme;

public class MenuItemData implements Storable {
    private HashSet<MenuItemProperty> properties;

    private ArrayList<MenuItemData>   items;
    private String                    name;
    private String                    iconKey;
    private String                    className;
    private ActionData                actionData;

    public String _getIdentifier() {
        if (actionData != null) { return actionData.getClazzName(); }
        if (getClass() != MenuContainer.class && getClass() != MenuItemData.class) { return getClass().getName(); }
        if (StringUtils.isNotEmpty(className)) return className;
        return getIconKey() + ":" + getName();

    }

    public String toString() {
        return _getIdentifier() + "";
    }

    public ActionData getActionData() {
        return actionData;
    }

    public void setActionData(ActionData actionData) {

        this.actionData = actionData;
    }

    public String getClassName() {
        if (StringUtils.isNotEmpty(className)) return className;
        if (getClass() == MenuItemData.class) return null;
        return getClass().getName();
    }

    public void setClassName(String className) {

        this.className = className;
    }

    public static enum Type {
        ACTION,
        CONTAINER;
    }

    private Type              type = Type.ACTION;

    private boolean           validated;

    private Exception         validateException;

    private MenuContainerRoot root;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {

        this.type = type;
    }

    public MenuItemData(/* Storable */) {
        items = new ArrayList<MenuItemData>();
    }

    public ArrayList<MenuItemData> getItems() {

        return items;
    }

    public void setItems(ArrayList<MenuItemData> items) {

        this.items = items;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {

        this.iconKey = iconKey;
    }

    public void add(MenuItemData child) {
        items.add(child);
    }

    public HashSet<MenuItemProperty> getProperties() {
        return properties;
    }

    public void setProperties(HashSet<MenuItemProperty> properties) {

        this.properties = properties;
    }

    public MenuItemData(MenuItemProperty... ps) {
        properties = new HashSet<MenuItemProperty>();
        for (MenuItemProperty p : ps) {
            properties.add(p);
        }
    }

    public MenuItemData(ActionData actionData, MenuItemProperty... itemProperties) {
        this(itemProperties);
        setActionData(actionData);
    }

    public MenuItemData(Class<? extends AppAction> class1, MenuItemProperty... itemProperties) {
        this(new ActionData(class1), itemProperties);
    }

    public MenuItemData createValidatedItem() throws InstantiationException, IllegalAccessException, ClassNotFoundException, ExtensionNotLoadedException {

        if (className == null || getClass().getName().equals(className)) {
            this._setValidated(true);
            return this;
        }

        MenuItemData ret = createInstance(this);
        ret._setValidated(true);
        return ret;

    }

    protected MenuItemData createInstance(MenuItemData menuItemData) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ExtensionNotLoadedException {

        if (menuItemData.getClassName() == null) return menuItemData;

        MenuItemData ret = null;

        String packageName = AbstractExtension.class.getPackage().getName();
        if (menuItemData.getClassName().startsWith(packageName)) {

            ret = (MenuItemData) ExtensionController.getInstance().loadClass(menuItemData.getClassName()).newInstance();

        } else {
            ret = (MenuItemData) Class.forName(menuItemData.getClassName()).newInstance();
        }

        ret.setIconKey(menuItemData.getIconKey());
        ret.setName(menuItemData.getName());
        ret.setItems(menuItemData.getItems());
        ret.setType(menuItemData.getType());
        ret.setProperties(menuItemData.getProperties());

        return ret;

    }

    public JComponent createItem(SelectionInfo<?, ?> selection) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, ExtensionNotLoadedException {

        if (actionData == null) {
            //
            throw new WTFException("No ACTION");
        }
        AppAction action = createAction(selection);
        JMenuItem ret = action.isToggle() ? new JCheckBoxMenuItem(action) : new JMenuItem(action);
        if (StringUtils.isNotEmpty(name)) {
            ret.setText(name);
        }
        if (StringUtils.isNotEmpty(iconKey)) {
            ret.setIcon(NewTheme.I().getIcon(iconKey, 20));
        }
        return ret;

    }

    public AppAction createAction(SelectionInfo<?, ?> selection) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ExtensionNotLoadedException {
        if (!validated) {
            //
            throw new WTFException();
        }
        if (actionData == null) {
            //
            throw new WTFException("No ACTION");
        }
        Class<?> clazz = actionData._getClazz();

        if (selection == null) {

            try {
                Constructor<?> c = clazz.getConstructor(new Class[] {});
                AppAction action = (AppAction) c.newInstance(new Object[] {});
                return action;
            } catch (Exception e) {

            }

        }

        Constructor<?> c = clazz.getConstructor(new Class[] { SelectionInfo.class });
        AppAction action = (AppAction) c.newInstance(new Object[] { selection });
        actionData.setName(action.getName());
        actionData.setIconKey(action.getIconKey());

        return action;

    }

    public boolean showItem(SelectionInfo<?, ?> selection) {

        for (MenuItemProperty p : mergeProperties()) {
            switch (p) {
            case ALWAYS_HIDDEN:
                return false;
            case LINK_CONTEXT:
                if (!selection.isLinkContext()) return false;
                break;
            case PACKAGE_CONTEXT:
                if (!selection.isPackageContext()) return false;
                break;
            case HIDE_IF_DISABLED:
                break;
            case HIDE_IF_OPENFILE_IS_UNSUPPORTED:
                if (!CrossSystem.isOpenFileSupported()) return false;
                break;
            case HIDE_IF_OUTPUT_NOT_EXISTING:
                if (selection == null) return false;

                File file = null;

                if (selection.isLinkContext()) {
                    if (selection.getContextLink() instanceof DownloadLink) {
                        file = new File(((DownloadLink) selection.getContextLink()).getFileOutput());
                    } else {
                        throw new WTFException("TODO");
                    }

                } else {
                    if (selection.getContextPackage() instanceof FilePackage) {
                        file = new File(((FilePackage) selection.getContextPackage()).getDownloadDirectory());
                    } else {
                        throw new WTFException("TODO");
                    }

                }
                if (file == null || !file.exists()) return false;

            }
        }
        return true;
    }

    public JComponent addTo(JComponent root, SelectionInfo<?, ?> selection) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, ExtensionNotLoadedException {

        if (!showItem(selection)) return null;

        JComponent it;

        it = createItem(selection);

        if (it == null) return null;

        if (!it.isEnabled() && mergeProperties().contains(MenuItemProperty.HIDE_IF_DISABLED)) return null;

        root.add(it);
        return it;

    }

    public HashSet<MenuItemProperty> mergeProperties() {
        HashSet<MenuItemProperty> ret = new HashSet<MenuItemProperty>();
        if (getProperties() != null) ret.addAll(getProperties());
        if (actionData != null && actionData.getProperties() != null) ret.addAll(actionData.getProperties());
        return ret;
    }

    public List<MenuItemData> list() {

        List<MenuItemData> set = new ArrayList<MenuItemData>();
        set.add(this);
        if (getItems() != null) {
            for (MenuItemData d : getItems()) {
                set.addAll(d.list());
            }

        }
        return set;
    }

    public List<List<MenuItemData>> listPathes() {

        List<List<MenuItemData>> set = new ArrayList<List<MenuItemData>>();
        ArrayList<MenuItemData> newPath = new ArrayList<MenuItemData>();
        newPath.add(this);

        set.add(newPath);
        if (getItems() != null) {
            for (MenuItemData d : getItems()) {
                for (List<MenuItemData> p : d.listPathes()) {
                    newPath = new ArrayList<MenuItemData>();
                    newPath.add(this);
                    newPath.addAll(p);
                    set.add(newPath);
                }
            }

        }
        return set;
    }

    public String _getDescription() {
        if (getActionData() != null) {
            try {
                return createAction(null).getTooltipText();
            } catch (Exception e) {

            }
        }
        return null;
    }

    public Collection<String> _getItemIdentifiers() {
        HashSet<String> ret = new HashSet<String>();
        for (MenuItemData mid : getItems()) {
            ret.add(mid._getIdentifier());
        }
        return ret;
    }

    public void _setValidated(boolean b) {
        validated = true;
    }

    public boolean _isValidated() {
        return validated;
    }

    public void _setValidateException(Exception e) {
        this.validateException = e;
    }

    public Exception _getValidateException() {
        return validateException;
    }

    public MenuContainerRoot _getRoot() {
        return root;
    }

    public void _setRoot(MenuContainerRoot root) {
        this.root = root;
    }
}
