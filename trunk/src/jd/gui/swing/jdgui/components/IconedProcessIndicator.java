package jd.gui.swing.jdgui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import jd.gui.swing.laf.LookAndFeelController;

import org.appwork.swing.components.circlebar.CircledProgressBar;
import org.appwork.swing.components.circlebar.ImagePainter;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.PanelToolTip;
import org.appwork.swing.components.tooltips.ToolTipController;
import org.appwork.swing.components.tooltips.TooltipPanel;
import org.appwork.utils.swing.SwingUtils;

public class IconedProcessIndicator extends CircledProgressBar implements MouseListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1627427388265904122L;
    private boolean           active;

    protected boolean isActive() {
        return active;
    }

    protected ImagePainter activeValuePainter;
    protected ImagePainter activeNonValuePainter;
    protected ImagePainter valuePainter;
    protected ImagePainter nonValuePainter;
    private int            size;

    @Override
    public boolean isTooltipWithoutFocusEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    protected IconedProcessIndicator(int size) {

        this.size = size;
    }

    public Dimension getSize() {
        return new Dimension(size, size);
    }

    public Dimension getSize(Dimension rv) {
        rv.setSize(size, size);

        return rv;
    }

    public int getWidth() {
        return size;
    }

    /**
     * Returns the current height of this component. This method is preferable
     * to writing <code>component.getBounds().height</code>, or
     * <code>component.getSize().height</code> because it doesn't cause any heap
     * allocations.
     * 
     * @return the current height of this component
     */
    public int getHeight() {
        return size;
    }

    public IconedProcessIndicator(ImageIcon icon) {
        super();
        size = 22;
        valuePainter = getPainer(icon, 1.0f);

        valuePainter.setBackground(Color.WHITE);
        valuePainter.setForeground(Color.GRAY);
        nonValuePainter = getPainer(icon, 0.5f);
        activeValuePainter = getPainer(icon, 1.0f);
        activeValuePainter.setBackground(Color.WHITE);
        activeValuePainter.setForeground(Color.GREEN);

        activeNonValuePainter = getPainer(icon, 0.5f);
        activeNonValuePainter.setBackground(Color.LIGHT_GRAY);
        activeNonValuePainter.setForeground(Color.GREEN);
        ToolTipController.getInstance().register(this);
        setActive(false);
        addMouseListener(this);
    }

    public ExtTooltip createExtTooltip(final Point mousePosition) {
        IconedProcessIndicator comp = new IconedProcessIndicator(32);

        comp.valuePainter = valuePainter;
        comp.nonValuePainter = nonValuePainter;
        comp.activeValuePainter = activeValuePainter;
        comp.activeNonValuePainter = activeNonValuePainter;
        comp.setActive(active);
        comp.setEnabled(isEnabled());
        comp.setIndeterminate(isIndeterminate());
        comp.setPreferredSize(new Dimension(32, 32));
        comp.setValue(getValue());
        TooltipPanel panel = new TooltipPanel("ins 0,wrap 2", "[][grow,fill]", "[]0[grow,fill]");

        comp.setOpaque(false);

        JLabel lbl = new JLabel(toString());
        lbl.setForeground(new Color(LookAndFeelController.getInstance().getLAFOptions().getTooltipForegroundColor()));
        JTextArea txt = new JTextArea();
        SwingUtils.setOpaque(txt, false);
        txt.setBorder(null);
        txt.setForeground(new Color(LookAndFeelController.getInstance().getLAFOptions().getTooltipForegroundColor()));
        // p.add(lbl);
        panel.add(comp, "spany 2,aligny top");
        panel.add(SwingUtils.toBold(lbl));
        panel.add(txt);
        lbl.setText(getTitle());
        txt.setText(getDescription());

        return new PanelToolTip(panel);
    }

    private String title       = null;
    private String description = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIndeterminate(final boolean newValue) {
        super.setIndeterminate(newValue);
        setActive(newValue);
    }

    protected void setActive(boolean newValue) {
        active = newValue;

        if (active) {
            this.setValueClipPainter(activeValuePainter);
            this.setNonvalueClipPainter(activeNonValuePainter);

        } else {
            this.setValueClipPainter(valuePainter);
            this.setNonvalueClipPainter(nonValuePainter);
        }
    }

    private ImagePainter getPainer(ImageIcon icon, float f) {
        ImagePainter ret = new ImagePainter(icon.getImage(), f);
        ret.setBackground(Color.WHITE);
        ret.setForeground(Color.LIGHT_GRAY);
        return ret;
    }

    public void mouseClicked(MouseEvent e) {
        ToolTipController.getInstance().show(this);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}
