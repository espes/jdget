package org.appwork.swing;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class MigPanel extends JPanel {

    private static final long serialVersionUID = 5744502853913432797L;

    /**
     * 
     * @param constraints
     * @param columns
     * @param rows
     */
    public MigPanel(final String constraints, final String columns, final String rows) {
        super(new MigLayout(constraints, columns, rows));
        
        
    }

   

}
