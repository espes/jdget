package org.appwork.utils.swing.dialog;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.appwork.app.gui.MigPanel;
import org.appwork.resources.AWUTheme;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.SwingUtils;

public class SimpleTextBallon extends BalloonDialog {

    /**
     * @param flag
     * @param comp
     * @param point
     * @throws OffScreenException
     */

    private final String    dTitle;
    private final String    dText;
    private JLabel          tit;
    private JButton         closer;
    private ImageIcon       ic;
    private final ImageIcon dIcon;

    /**
     * @param styleShowDoNotDisplayAgain
     * @param string
     * @param string2
     * @throws OffScreenException
     */
    public SimpleTextBallon(final int flag, final String title, final String text, final ImageIcon icon) throws OffScreenException {
        super(flag, (JComponent) null, (Point) null);
        this.dTitle = title;
        this.dText = text;
        this.dIcon = icon;
        this.setComponent(this.createContent());
        if (BinaryLogic.containsAll(flag, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            this.setDoNotShowAgainSelected(true);
        }

    }

    /**
     * @return
     */
    private JComponent createContent() {
        final MigPanel p = new MigPanel("ins 0,wrap 1", "[grow,fill]", "[]10[grow,fill]");
        p.setOpaque(false);
        final MigPanel header = new MigPanel("ins 0", "[grow,fill][20!]", "[]");
        header.setOpaque(false);
        this.tit = new JLabel(this.dTitle);
        this.tit.setOpaque(false);
        this.ic = AWUTheme.I().getIcon("close", -1);
        SwingUtils.toBold(this.tit);
        this.closer = new JButton(this.ic);
        this.closer.setContentAreaFilled(false);
        this.closer.setBorderPainted(false);
        this.closer.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                SimpleTextBallon.this.closer.setContentAreaFilled(true);
                SimpleTextBallon.this.closer.setBorderPainted(true);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                SimpleTextBallon.this.closer.setContentAreaFilled(false);
                SimpleTextBallon.this.closer.setBorderPainted(false);
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                // TODO Auto-generated method stub

            }
        });
        this.closer.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {

                Log.L.fine("Answer: Button<CANCEL>");
                SimpleTextBallon.this.setReturnmask(false);
                SimpleTextBallon.this.dispose();

            }
        });
        header.add(this.tit, "height 20!");
        header.add(this.closer, " width 16!,height 16!");
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, this.getShadowColor()));
        p.add(header);
        p.add(this.getContent(), "spanx");
        return p;
    }

    /**
     * @return
     */
    protected JComponent getContent() {
        final JTextArea tx = new JTextArea();
        tx.setEditable(false);
        tx.setFocusable(false);
        tx.setText(this.dText);
        tx.setOpaque(false);
        tx.putClientProperty("Synthetica.opaque", Boolean.FALSE);
        if (this.dIcon == null) {

            return tx;
        } else {

            final MigPanel p = new MigPanel("ins 0,wrap 2", "[]10[grow,fill]", "[]");
            p.setOpaque(false);
            JLabel lbl;
            p.add(lbl = new JLabel(this.dIcon), "aligny top,gaptop 2");
            lbl.setOpaque(false);
            p.add(tx);

            return p;
        }
    }

    @Override
    public int[] getContentInsets() {
        // TODO Auto-generated method stub
        return new int[] { 7, 7, 7, 7 };
    }

    @Override
    protected String getDontShowAgainKey() {
        return this.dTitle;
    }

    @Override
    public Paint getPaint(final BallonPanel panel) {
        return new GradientPaint(0, 0, Color.WHITE, panel.getWidth(), panel.getHeight(), new Color(240, 240, 240, 210));

    }

}
