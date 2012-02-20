package jd.gui.swing.jdgui.menu;

import javax.swing.JMenuBar;

import jd.Main;

public class JDMenuBar extends JMenuBar {

    private static final long serialVersionUID = 6758718947311901334L;

    public JDMenuBar() {
        super();
        Main.GUI_COMPLETE.executeWhenReached(new Runnable() {

            public void run() {
                add(new FileMenu());
                // add(new EditMenu());
                add(new SettingsMenu());
                add(AddonsMenu.getInstance());
                add(WindowMenu.getInstance());
                add(new AboutMenu());
            }

        });

    }

}
