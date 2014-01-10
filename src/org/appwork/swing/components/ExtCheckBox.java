package org.appwork.swing.components;

import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.swing.models.ConfigToggleButtonModel;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;

public class ExtCheckBox extends JCheckBox implements  ToolTipHandler {

    /**
     * 
     */
    private static final long          serialVersionUID = 3223817461429862778L;
    private JComponent[]               dependencies;

    private TooltipTextDelegateFactory tooltipFactory;

    /**
     * @param filename
     * @param lblFilename
     */
    public ExtCheckBox(final JComponent... components) {
        super();
        tooltipFactory = new TooltipTextDelegateFactory(this);
        SwingUtils.setOpaque(this, false);
//        addActionListener(this);
        setDependencies(components);

    }

    /**
     * @param class1
     * @param string
     * @param table
     * @param btadd
     * @param btRemove
     */
    public ExtCheckBox(final BooleanKeyHandler keyHandler, final JComponent... components) {
        this(components);
        setModel(new ConfigToggleButtonModel(keyHandler));
        updateDependencies();

    }
    public void setModel(final ButtonModel newModel) {
        super.setModel(newModel);
        newModel.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(final ItemEvent e) {
              
                new EDTRunner(){

                 @Override
                 protected void runInEDT() {
                    updateDependencies();
                 }
                    
                }; 
            }
        });
       
      
    }
    public JComponent[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(final JComponent... dependencies) {
        this.dependencies = dependencies;

        updateDependencies();
    }

    public TooltipTextDelegateFactory getTooltipFactory() {
        return tooltipFactory;
    }

    public void setTooltipFactory(final TooltipTextDelegateFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
    }

//    public void setSelected(boolean b) {
//
//        super.setSelected(b);
//
//        updateDependencies();
//    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        updateDependencies();
//    }

    /**
     * 
     */
    public void updateDependencies() {

        if (dependencies != null) {
            for (final JComponent c : dependencies) {
                c.setEnabled(getDependenciesLogic(c,isSelected()));
            }
        }

    }
    /**
 * @param c
     * @param b 
 * @return
 */
    protected boolean getDependenciesLogic(final JComponent c, final boolean b) {
        // TODO Auto-generated method stub
        return b;
    }

    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip
     * (java.awt.Point)
     */
    @Override
    public ExtTooltip createExtTooltip(final Point mousePosition) {

        return getTooltipFactory().createTooltip();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.swing.components.tooltips.ToolTipHandler#
     * isTooltipDisabledUntilNextRefocus()
     */
    @Override
    public boolean isTooltipDisabledUntilNextRefocus() {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(final Point mousePositionOnScreen) {  return 0;    }  @Override public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

}
