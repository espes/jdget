package jd.gui.swing.jdgui.components.toolbar.actions;

import java.awt.event.ActionEvent;

import jd.gui.swing.SwingGui;
import jd.gui.swing.jdgui.views.settings.ConfigurationView;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.swing.EDTRunner;
import org.jdownloader.gui.toolbar.action.ToolBarAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;

public class ShowSettingsAction extends ToolBarAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ShowSettingsAction(SelectionInfo<?, ?> selection) {

        setIconKey("settings");

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        new EDTRunner() {

            @Override
            protected void runInEDT() {

                JsonConfig.create(GraphicalUserInterfaceSettings.class).setConfigViewVisible(true);
                SwingGui.getInstance().setContent(ConfigurationView.getInstance(), true);
            }
        };
    }

    @Override
    public String createTooltip() {
        return _GUI._.action_settings_menu_tooltip();
    }

}
