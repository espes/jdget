package org.appwork.app.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;

public class Statusbar extends JMenuBar {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;

    private final JLabel       tip;

    private JLabel             help;

    private final JLabel       contextLabel;
    private JLabel             url;
    private JLabel             urlLabel;
    private final MouseAdapter mouseHoverAdapter;

    private String             manufactorUrl;

    public Statusbar(final String manufactor, final String manufactorUrl) {
        this.manufactorUrl = manufactorUrl;
        mouseHoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {

                String tt = ((JComponent) e.getSource()).getToolTipText();
                if (tt == null || tt.trim().length() == 0) {
                    tt = ((JComponent) e.getSource()).getName();
                }
                setTip(tt);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                final Rectangle rec = ((JComponent) e.getSource()).getBounds();
                // some components, like jspinner exit when mouse moves in
                // the interactive area, like the textfield.
                // we check here if we really moved OUT

                if (e.getPoint().x < 0 || e.getPoint().y < 0 || e.getPoint().y >= rec.height || e.getPoint().x >= rec.width) {
                    setTip("");
                }
            }
        };
        removeAll();
        setLayout(new MigLayout("ins 2", "[][][grow,fill][]", "[]"));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, getBackground().darker().darker()));
        // ToolTipManager.sharedInstance().setEnabled(false);
        // ToolTipManager.sharedInstance().setDismissDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(2000);
        ToolTipManager.sharedInstance().setInitialDelay(2000);
        try {
            help = new JLabel(ImageProvider.getImageIcon("info", 16, 16, true));

            help.setToolTipText(APPWORKUTILS.Statusbar_Statusbar_tooltip.s());
            this.add(help);
        } catch (final IOException e2) {

            org.appwork.utils.logging.Log.exception(e2);
        }
        tip = new JLabel("");
        tip.setForeground(Color.GRAY);
        this.add(tip);

        contextLabel = new JLabel("");
        contextLabel.setForeground(Color.GRAY);
        contextLabel.setHorizontalTextPosition(SwingConstants.RIGHT);

        this.add(Box.createHorizontalGlue(), "pushx,growx");
        this.add(contextLabel);

        try {
            urlLabel = new JLabel(manufactor);

            urlLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (Statusbar.this.manufactorUrl == null) { return; }
                    try {
                        CrossSystem.openURL(new URL(Statusbar.this.manufactorUrl));
                    } catch (final MalformedURLException e1) {

                        org.appwork.utils.logging.Log.exception(e1);
                    }

                }

            });
            urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            urlLabel.setForeground(Color.GRAY);
            urlLabel.setToolTipText(APPWORKUTILS.Statusbar_Statusbar_visiturl_tooltip.s());
            urlLabel.setIcon(ImageProvider.getImageIcon("appicon", 16, 16, true));
            urlLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            urlLabel.addMouseListener(new MouseAdapter() {

                private Font originalFont;

                @Override
                @SuppressWarnings({ "unchecked", "rawtypes" })
                public void mouseEntered(final MouseEvent evt) {
                    originalFont = urlLabel.getFont();
                    if (urlLabel.isEnabled()) {
                        final Map attributes = originalFont.getAttributes();
                        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                        urlLabel.setFont(originalFont.deriveFont(attributes));
                    }
                }

                @Override
                public void mouseExited(final MouseEvent evt) {
                    urlLabel.setFont(originalFont);
                }

            });
            this.add(urlLabel);
        } catch (final IOException e) {
            org.appwork.utils.logging.Log.exception(e);
        }

        registerAllToolTip(this);
    }

    public void registerAllToolTip(final JComponent component) {
        for (final Component c : component.getComponents()) {
            registerToolTip(c);
        }
    }

    public void registerToolTip(final Component c) {
        c.removeMouseListener(mouseHoverAdapter);
        c.addMouseListener(mouseHoverAdapter);

    }

    public void setContextInfo(final String s) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                contextLabel.setText(s);
            }
        };

    }

    public void setManufactor(final String manuf, final String url) {
        manufactorUrl = url;
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                Statusbar.this.url.setText(manuf);
            }
        };
    }

    public void setTip(final String text) {
        if (text == null) {
            tip.setText("");
        } else {
            tip.setText(text.replaceAll("<.*?>", " "));
        }
    }

}
