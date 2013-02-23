package org.appwork.swing.components.circlebar;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.ToolTipHandler;
import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.swing.components.tooltips.TooltipTextDelegateFactory;
import org.appwork.utils.event.predefined.changeevent.ChangeEventSender;

public class CircledProgressBar extends JComponent implements ToolTipHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -3518805542131925575L;

    private BoundedRangeModel       model;

    private ChangeListener          changeListener;

    private boolean                 indeterminate;
    private final ChangeEventSender eventSender;
    private IconPainter             valueClipPainter;
    private IconPainter             nonvalueClipPainter;
    /**
     * @see #getUIClassID
     */
    private static final String     UI_CLASS_ID = "CircleProgressBarUI";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#createExtTooltip()
     */
    private TooltipFactory          tooltipFactory;

    /**
     * 
     */
    public CircledProgressBar() {
        this(new DefaultBoundedRangeModel());
      
    }
    public boolean isFocusable() {
        return false;
    }
    /**
     * @param model
     */
    public CircledProgressBar(final BoundedRangeModel model) {
        this.eventSender = new ChangeEventSender();
        this.tooltipFactory = new TooltipTextDelegateFactory(this);
        this.installPainer();
        this.setModel(model);
        BasicProgressBarUI.class.getAnnotations();
        this.updateUI();
        this.setIndeterminate(false);

    }

    /**
     * delegates the changeevents. you can override this method to implement
     * different eventhandling
     * 
     * @return
     */
    protected ChangeListener createChangeListener() {

        return new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent e) {
                CircledProgressBar.this.eventSender.fireEvent(new org.appwork.utils.event.predefined.changeevent.ChangeEvent(e.getSource()));
            }

        };
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
        return this.tooltipFactory.createTooltip();
    }

    /**
     * @return
     */
    public int getAnimationFPS() {
        return 25;
    }

    /**
     * @return
     */
    public float getCyclesPerSecond() {

        return 0.5f;
    }

    public ChangeEventSender getEventSender() {
        return this.eventSender;
    }

    public int getMaximum() {
        return this.getModel().getMaximum();
    }

    public int getMinimum() {
        return this.getModel().getMinimum();
    }

    public BoundedRangeModel getModel() {
        return this.model;
    }

    public IconPainter getNonvalueClipPainter() {
        return this.nonvalueClipPainter;
    }

    public TooltipFactory getTooltipFactory() {
        return this.tooltipFactory;
    }

    @Override
    public String getUIClassID() {
        return CircledProgressBar.UI_CLASS_ID;
    }

    public int getValue() {
        return this.getModel().getValue();
    }

    public IconPainter getValueClipPainter() {
        return this.valueClipPainter;
    }

    /**
     * 
     */
    private void installPainer() {
        this.valueClipPainter = new IconPainter() {

            @Override
            public void paint(final CircledProgressBar bar, final Graphics2D g2, final Shape shape, final int diameter, final double progress) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CircledProgressBar.this.getForeground());
                final Area a = new Area(shape);
                a.intersect(new Area(new Ellipse2D.Float(0, 0, diameter, diameter)));

                g2.fill(a);

            }
        };

    }

    /**
     * This methods flips value and nonvalue painter. This can be used to
     * simulate a continuous running bar, just by inverting the painters and
     * setting the value to 0 if the model reaches it's maximum
     */
    protected void invertPainter() {

        final IconPainter fg = this.valueClipPainter;
        this.valueClipPainter = this.nonvalueClipPainter;
        this.nonvalueClipPainter = fg;

    }

    public boolean isIndeterminate() {
        return this.indeterminate;
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

    /**
     * @param b
     */
    public void setIndeterminate(final boolean newValue) {
        final boolean oldValue = this.indeterminate;
        if (oldValue == newValue) { return; }
        this.indeterminate = newValue;
        this.firePropertyChange("indeterminate", oldValue, this.indeterminate);
    }

    public void setMaximum(final int n) {
        this.getModel().setMaximum(n);
    }

    public void setMinimum(final int n) {
        this.getModel().setMinimum(n);
    }

    /**
     * @param model
     */
    public synchronized void setModel(final BoundedRangeModel model) {
        if (this.model == model) { return; }
        if (this.model != null) {
            model.removeChangeListener(this.changeListener);
        }
        this.changeListener = this.createChangeListener();
        model.addChangeListener(this.changeListener);
        this.model = model;

        this.repaint();

    }

    public void setNonvalueClipPainter(final IconPainter backgroundPainter) {
        this.nonvalueClipPainter = backgroundPainter;
    }

    /**
     * @param string
     */
    public void setString(final String string) {
        // TODO Auto-generated method stub

    }

    /**
     * @param b
     */
    public void setStringPainted(final boolean b) {
        // TODO Auto-generated method stub

    }

    public void setTooltipFactory(final TooltipFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        ToolTipController.getInstance().register(this);
    }

    @Override
    public void setToolTipText(final String text) {

        this.putClientProperty(JComponent.TOOL_TIP_TEXT_KEY, text);

        if (text == null || text.length() == 0) {
            ToolTipController.getInstance().unregister(this);
        } else {
            ToolTipController.getInstance().register(this);
        }
    }

    public void setUI(final CircleProgressBarUI ui) {
        super.setUI(ui);

    }

    public void setValue(final int n) {
        final BoundedRangeModel brm = this.getModel();
        final int oldValue = brm.getValue();
        brm.setValue(n);

    }

    /**
     * @param iconPainter
     */
    public void setValueClipPainter(final IconPainter iconPainter) {
        this.valueClipPainter = iconPainter;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.swing.components.tooltips.ToolTipHandler#updateTooltip(org
     * .appwork.swing.components.tooltips.ExtTooltip, java.awt.event.MouseEvent)
     */
    @Override
    public int getTooltipDelay(Point mousePositionOnScreen) {  return 0;    }  @Override public boolean updateTooltip(final ExtTooltip activeToolTip, final MouseEvent e) {

        return false;
    }

    /**
     * Resets the UI property to a value from the current look and feel.
     * 
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        // final CircleProgressBarUI newUI = (CircleProgressBarUI)
        // UIManager.getUI(this);
        this.setUI(new BasicCircleProgressBarUI());
    }

}
