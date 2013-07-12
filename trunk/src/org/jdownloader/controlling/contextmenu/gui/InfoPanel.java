package org.jdownloader.controlling.contextmenu.gui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.appwork.swing.MigPanel;
import org.appwork.swing.components.ExtButton;
import org.appwork.swing.components.ExtTextField;
import org.appwork.utils.GetterSetter;
import org.appwork.utils.KeyUtils;
import org.appwork.utils.ReflectionUtils;
import org.appwork.utils.StringUtils;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;
import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.contextmenu.Customizer;
import org.jdownloader.controlling.contextmenu.MenuContainer;
import org.jdownloader.controlling.contextmenu.MenuItemData;
import org.jdownloader.controlling.contextmenu.MenuLink;
import org.jdownloader.controlling.contextmenu.SeperatorData;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;

public class InfoPanel extends MigPanel implements ActionListener {

    private JLabel       label;

    private MenuItemData item;
    private ExtTextField name;
    private ExtButton    iconChange;

    private JLabel       iconlabel;

    private ManagerFrame managerFrame;

    private JCheckBox    visibleBox;

    private ExtTextField shortcut;

    protected KeyStroke  currentShortcut;
    private CustomPanel  customPanel;

    public InfoPanel(ManagerFrame m) {
        super("ins 5,wrap 2", "[grow,fill][]", "[22!][]");
        this.managerFrame = m;
        label = SwingUtils.toBold(new JLabel());

        add(label);

        add(new JSeparator(), "spanx");
        add(SwingUtils.toBold(new JLabel(_GUI._.InfoPanel_InfoPanel_properties_())), "spanx");
        // MenuItemProperty.HIDE_IF_DISABLED;
        // MenuItemProperty.HIDE_IF_OPENFILE_IS_UNSUPPORTED;
        // MenuItemProperty.HIDE_IF_OUTPUT_NOT_EXISTING;

        visibleBox = new JCheckBox();
        visibleBox.addActionListener(this);
        name = new ExtTextField() {

            @Override
            public void onChanged() {
                item.setName(name.getText());

                updateHeaderLabel(item);
                managerFrame.fireUpdate();

            }

        };
        name.setHelpText(_GUI._.InfoPanel_InfoPanel_customname_help());
        iconChange = new ExtButton(new AppAction() {
            {
                setName(_GUI._.InfoPanel_changeicon());
            }

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    final JPopupMenu p = new JPopupMenu();

                    URL url = NewTheme.I().getURL("images/", "help", ".png");

                    File imagesDir;

                    imagesDir = new File(url.toURI()).getParentFile();

                    String[] names = imagesDir.list(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            return name.endsWith(".png");
                        }
                    });

                    final JList list = new JList(names);
                    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                    final ListCellRenderer org = list.getCellRenderer();
                    list.setCellRenderer(new ListCellRenderer() {

                        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            String key = value.toString().substring(0, value.toString().length() - 4);
                            JLabel ret = (JLabel) org.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
                            ret.setIcon(NewTheme.I().getIcon(key, 20));
                            return ret;
                        }
                    });
                    list.setFixedCellHeight(22);
                    list.setFixedCellWidth(22);
                    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                        public void valueChanged(ListSelectionEvent e) {
                            String v = list.getSelectedValue().toString();
                            v = v.substring(0, v.length() - 4);
                            item.setIconKey(v);
                            updateInfo(item);
                            p.setVisible(false);
                            managerFrame.fireUpdate();
                        }
                    });
                    p.add(list);
                    p.show(iconChange, 0, iconChange.getHeight());
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        // icon=new JLabel(9)
        add(label(_GUI._.InfoPanel_InfoPanel_itemname_()));
        add(name, "newline,spanx");
        add(iconlabel = label(_GUI._.InfoPanel_InfoPanel_icon()), "height 22!");
        add(iconChange, "newline,spanx");
        shortcut = new ExtTextField();
        shortcut.setHelpText(_GUI._.InfoPanel_InfoPanel_shortcuthelp());
        shortcut.setEditable(false);
        shortcut.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                String msg1 = KeyUtils.getShortcutString(event, true);
                currentShortcut = KeyStroke.getKeyStroke(event.getKeyCode(), event.getModifiers());
                shortcut.setText(msg1);
                save();
            }

        });
        if (managerFrame.getManager().isAcceleratorsEnabled()) {
            add(label(_GUI._.InfoPanel_InfoPanel_shortcuts()));
            add(shortcut, "newline");
            add(new JButton(new AppAction() {
                {
                    setIconKey("reset");
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    new EDTRunner() {

                        @Override
                        protected void runInEDT() {
                            currentShortcut = null;
                            shortcut.setText("");
                            save();
                        }
                    };
                }

            }), "width 22!,height 22!");
        }

        add(label(_GUI._.InfoPanel_InfoPanel_hidden_2()));
        add(visibleBox, "spanx");

        add(new JSeparator(), "spanx");
        customPanel = new CustomPanel(managerFrame);
        add(customPanel, "spanx,growx");
    }

    private JLabel label(String infoPanel_InfoPanel_hideIfDisabled) {
        return new JLabel(infoPanel_InfoPanel_hideIfDisabled);
    }

    /**
     * @param lastPathComponent
     */
    public void updateInfo(final MenuItemData value) {
        // getParent().revalidate();
        // Component.revalidate is 1.7 only - that's why we have to cast
        JComponent p = (JComponent) getParent().getParent().getParent();

        p.revalidate();

        this.item = value;
        if (value == null) {

            label.setText("");
            return;
        }
        visibleBox.setSelected(value.isVisible());
        if (StringUtils.isNotEmpty(value.getShortcut())) {
            currentShortcut = KeyStroke.getKeyStroke(value.getShortcut());
            if (currentShortcut != null) {
                shortcut.setText(KeyUtils.getShortcutString(currentShortcut, true));
            } else {
                shortcut.setText("");
            }
        } else {
            currentShortcut = null;
            shortcut.setText("");
        }
        MenuItemData mid = ((MenuItemData) value);
        Rectangle bounds = null;
        if (mid.getIconKey() != null) {

            iconlabel.setIcon(NewTheme.I().getIcon(mid.getIconKey(), 22));
        } else {
            iconlabel.setIcon(null);
        }

        String n = mid.getName();

        name.setText(n);

        // renderer.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED));
        updateHeaderLabel(mid);
        customPanel.removeAll();

        AppAction action;
        try {
            if (mid.getActionData() != null) {
                action = mid.createAction(null);
                for (GetterSetter gs : ReflectionUtils.getGettersSetteres(action.getClass())) {

                    if (gs.hasGetter() && gs.hasSetter()) {
                        if (gs.hasAnnotation(Customizer.class)) {
                            customPanel.add(mid.getActionData(), action, gs);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateHeaderLabel(MenuItemData mid) {

        String type = null;
        String name = mid.getName();
        Icon icon = null;
        if (mid.getIconKey() != null) {
            icon = (NewTheme.I().getIcon(mid.getIconKey(), 20));
        }

        if (mid instanceof MenuContainer) {

            type = _GUI._.InfoPanel_update_submenu();

            // label.setText(_GUI._.InfoPanel_updateInfo_header_actionlabel(, ));

        } else if (mid instanceof SeperatorData) {

            name = _GUI._.Renderer_getTreeCellRendererComponent_seperator();

        } else {
            if (mid instanceof MenuLink) {
                type = _GUI._.InfoPanel_update_link();

            } else {
                if (mid._isValidated()) {
                    try {
                        AppAction action = mid.createAction(null);

                        if (StringUtils.isEmpty(name)) {
                            name = action.getName();
                        }
                        type = _GUI._.InfoPanel_update_action();
                        if (icon == null) {
                            icon = action.getSmallIcon();
                        }
                    } catch (Exception e) {

                    }
                }
                if (StringUtils.isEmpty(name)) {
                    name = mid.getActionData().getName();
                }
                if (icon == null) {
                    if (mid.getActionData().getIconKey() != null) {
                        icon = NewTheme.I().getIcon(mid.getActionData().getIconKey(), 18);
                    }
                }
                if (StringUtils.isEmpty(name)) {

                    name = mid.getActionData().getClazzName();
                    name = name.substring(name.lastIndexOf(".") + 1);

                }

            }

        }
        if (StringUtils.isNotEmpty(type)) {
            label.setText(_GUI._.InfoPanel_updateInfo_header_actionlabel(name, type));
        } else {
            label.setText(name);
        }
        label.setIcon(icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        save();

    }

    private void save() {
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                item.setVisible(visibleBox.isSelected());
                item.setShortcut(currentShortcut == null ? null : currentShortcut.toString());
                managerFrame.repaint();
            }
        }.waitForEDT();

    }
}
