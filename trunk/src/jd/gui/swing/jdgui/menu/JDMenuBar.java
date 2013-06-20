package jd.gui.swing.jdgui.menu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import jd.SecondLevelLaunch;

import org.appwork.utils.StringUtils;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.contextmenu.MenuContainer;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuItemProperty;
import org.jdownloader.controlling.contextmenu.gui.MenuBuilder;
import org.jdownloader.extensions.ExtensionNotLoadedException;
import org.jdownloader.gui.mainmenu.MainMenuManager;
import org.jdownloader.images.NewTheme;
import org.jdownloader.logging.LogController;

public class JDMenuBar extends JMenuBar implements MouseListener {
    private static final JDMenuBar INSTANCE = new JDMenuBar();

    /**
     * get the only existing instance of JDMenuBar. This is a singleton
     * 
     * @return
     */
    public static JDMenuBar getInstance() {
        return JDMenuBar.INSTANCE;
    }

    private static final long serialVersionUID = 6758718947311901334L;
    private LogSource         logger;

    /**
     * Create a new instance of JDMenuBar. This is a singleton class. Access the only existing instance by using {@link #getInstance()}.
     */
    private JDMenuBar() {
        super();
        // updateLayout();
        SecondLevelLaunch.GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {

                new EDTRunner() {

                    @Override
                    protected void runInEDT() {
                        updateLayout();
                        // add(new FileMenu());
                        // // add(new EditMenu());
                        // add(new SettingsMenu());
                        // add(AddonsMenu.getInstance());
                        //
                        // add(new AboutMenu());
                    }
                };
            }
        });

        this.addMouseListener(this);
        logger = LogController.getInstance().getLogger(JDMenuBar.class.getName());

    }

    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        comp.addMouseListener(this);
    }

    public void updateLayout() {

        removeAll();

        new MenuBuilder(MainMenuManager.getInstance(), this, null, MainMenuManager.getInstance().getMenuData()) {

            @Override
            protected void addContainer(JComponent root, MenuItemData inst) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ExtensionNotLoadedException {
                final JMenu submenu = (JMenu) inst.addTo(root, selection);

                // final Field f = KeyEvent.class.getField("VK_" + Character.toUpperCase(mnemonic));
                // final int m = (Integer) f.get(null);
                // putValue(AbstractAction.MNEMONIC_KEY, m);
                // putValue(AbstractAction.DISPLAYED_MNEMONIC_INDEX_KEY, getName().indexOf(m));
                if (submenu != null) {
                    applyMnemonic(root, submenu);
                    if (root == JDMenuBar.this) submenu.setIcon(null);
                    createLayer(submenu, (MenuContainer) inst);
                }
            }

            @Override
            protected void addAction(JComponent root, MenuItemData inst) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ExtensionNotLoadedException {
                if (root instanceof JMenu) {
                    JComponent comp = inst.addTo(root, selection);
                    if (comp instanceof AbstractButton) {
                        applyMnemonic(root, (AbstractButton) comp);
                    }
                } else {

                    // WAHHHHHH this is an ugly gui hack!
                    // this peace of code loads the synthetica MenuPainter given in the LAFOptions and paints a JMenu Mouseover Background
                    // to a JButton or a jToggleBUtton
                    AppAction action = inst.createAction(selection);

                    if (StringUtils.isNotEmpty(inst.getShortcut())) {
                        action.setAccelerator(KeyStroke.getKeyStroke(inst.getShortcut()));
                    }

                    /*
                     * JMenuBar uses Boxlayout. BoxLayout always tries to strech the components to their Maximum Width. Fixes
                     * http://svn.jdownloader.org/issues/8509
                     */
                    if (StringUtils.isNotEmpty(inst._getShortcut())) {
                        action.setAccelerator(KeyStroke.getKeyStroke(inst._getShortcut()));
                    }
                    AbstractButton ret;
                    if (action.isToggle()) {
                        ret = new MenuJToggleButton(action);
                        ImageIcon icon;

                        ret.setIcon(icon = NewTheme.I().getCheckBoxImage(action.getIconKey(), false, 18));
                        ret.setRolloverIcon(icon);
                        ret.setSelectedIcon(icon = NewTheme.I().getCheckBoxImage(action.getIconKey(), true, 18));
                        ret.setRolloverSelectedIcon(icon);

                    } else {
                        ret = new ExtMenuButton(action);

                        ret.setIcon(NewTheme.I().getIcon(action.getIconKey(), 18));

                    }
                    // JMenuItem
                    // ret.setBorderPainted(false);
                    final AbstractButton bt = ret;

                    bt.setBorderPainted(false);
                    bt.setOpaque(false);

                    // final Object value = action.getValue(Action.ACCELERATOR_KEY);
                    // if (value == null) {
                    // continue;
                    // }
                    // final KeyStroke ks = (KeyStroke) value;
                    //
                    // this.rootpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, action);
                    // this.rootpane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, action);
                    // this.rootpane.getActionMap().put(action, action);
                    //
                    // final String shortCut = action.getShortCutString();
                    // if (bt != null) {
                    // if (StringUtils.isEmpty(action.getTooltipText())) {
                    // ret.setToolTipText(shortCut != null ? " [" + shortCut + "]" : null);
                    // } else {
                    // ret.setToolTipText(action.getTooltipText() + (shortCut != null ? " [" + shortCut + "]" : ""));
                    // }
                    //
                    // }

                    ret.getAccessibleContext().setAccessibleName(action.getName());
                    ret.getAccessibleContext().setAccessibleDescription(action.getTooltipText());
                    if (StringUtils.isNotEmpty(inst.getName())) {
                        ret.setText(inst.getName());
                    }
                    if (StringUtils.isNotEmpty(inst.getIconKey())) {
                        ret.setIcon(NewTheme.I().getIcon(inst.getIconKey(), 20));
                    }

                    if (ret instanceof AbstractButton) {
                        applyMnemonic(root, (AbstractButton) ret);
                    }

                    if (!ret.isEnabled() && inst.mergeProperties().contains(MenuItemProperty.HIDE_IF_DISABLED)) return;

                    root.add(ret);
                }
            }

        }.run();

        if (getMenuCount() == 0) {

            new MenuBuilder(MainMenuManager.getInstance(), this, null, MainMenuManager.getInstance().createDefaultStructure()) {

                @Override
                protected void addContainer(JComponent root, MenuItemData inst) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ExtensionNotLoadedException {
                    final JMenu submenu = (JMenu) inst.addTo(root, selection);
                    if (root == JDMenuBar.this) submenu.setIcon(null);
                    createLayer(submenu, (MenuContainer) inst);
                }

                @Override
                protected void addAction(JComponent root, MenuItemData inst) throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, ExtensionNotLoadedException {
                    inst.addTo(root, selection);
                }

            }.run();
        }
        ;
        // MenuContainerRoot dret.nMenuManager.getInstance().getMenuData();
        // for(MenuItemData mid:dat.getItems()){
        // if(!mid.showItem(null)) continue;
        repaint();

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == 3) {

            MainMenuManager.getInstance().openGui();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
