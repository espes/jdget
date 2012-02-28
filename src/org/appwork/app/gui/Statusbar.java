package org.appwork.app.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
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

import org.appwork.resources.AWUTheme;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;

public class Statusbar extends JMenuBar {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;

    protected final JLabel       tip;

    protected JLabel             help;

    protected final JLabel       contextLabel;
    protected JLabel             url;
    protected JLabel             urlLabel;
    private final MouseAdapter mouseHoverAdapter;

    private String             manufactorUrl;

    public Statusbar(final String manufactor, final String manufactorUrl) {
        this.manufactorUrl = manufactorUrl;
        this.mouseHoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {

                String tt = ((JComponent) e.getSource()).getToolTipText();
                if (tt == null || tt.trim().length() == 0) {
                    tt = ((JComponent) e.getSource()).getName();
                }
                Statusbar.this.setTip(tt);
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                final Rectangle rec = ((JComponent) e.getSource()).getBounds();
                // some components, like jspinner exit when mouse moves in
                // the interactive area, like the textfield.
                // we check here if we really moved OUT

                if (e.getPoint().x < 0 || e.getPoint().y < 0 || e.getPoint().y >= rec.height || e.getPoint().x >= rec.width) {
                    Statusbar.this.setTip("");
                }
            }
        };
//        this.removeAll();
    
        // ToolTipManager.sharedInstance().setEnabled(false);
        // ToolTipManager.sharedInstance().setDismissDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(2000);
        ToolTipManager.sharedInstance().setInitialDelay(2000);

        this.help = new JLabel(AWUTheme.I().getIcon("info", 16));

        this.help.setToolTipText(_AWU.T.Statusbar_Statusbar_visiturl_tooltip());


        this.tip = new JLabel("");
        this.tip.setForeground(Color.GRAY);
  

        this.contextLabel = new JLabel("");
        this.contextLabel.setForeground(Color.GRAY);
        this.contextLabel.setHorizontalTextPosition(SwingConstants.RIGHT);


        this.urlLabel = new JLabel(manufactor);

        this.urlLabel.addMouseListener(new MouseAdapter() {
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
        this.urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.urlLabel.setForeground(Color.GRAY);
        this.urlLabel.setToolTipText(_AWU.T.Statusbar_Statusbar_visiturl_tooltip());
        this.urlLabel.setIcon(AWUTheme.I().getIcon("appicon", 16));
        this.urlLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.urlLabel.addMouseListener(new MouseAdapter() {

            private Font originalFont;

            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void mouseEntered(final MouseEvent evt) {
                this.originalFont = Statusbar.this.urlLabel.getFont();
                if (Statusbar.this.urlLabel.isEnabled()) {
                    final Map attributes = this.originalFont.getAttributes();
                    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                    Statusbar.this.urlLabel.setFont(this.originalFont.deriveFont(attributes));
                }
            }

            @Override
            public void mouseExited(final MouseEvent evt) {
                Statusbar.this.urlLabel.setFont(this.originalFont);
            }

        });
       initLayout();
    }

    /**
     * 
     */
    protected void initLayout() {
        removeAll();
        this.setLayout(new MigLayout("ins 2", "[][][grow,fill][]", "[]"));
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, this.getBackground().darker().darker()));
        
        this.add(this.help);
        this.add(this.tip);
        this.add(Box.createHorizontalGlue(), "pushx,growx");
        this.add(this.contextLabel);
        this.add(this.urlLabel);

        this.registerAllToolTip(this);  
    }

    public void registerAllToolTip(final JComponent component) {
        for (final Component c : component.getComponents()) {
            this.registerToolTip(c);
        }
    }

    public void registerToolTip(final Component c) {
        c.removeMouseListener(this.mouseHoverAdapter);
        c.addMouseListener(this.mouseHoverAdapter);

    }

    public void setContextInfo(final String s) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                Statusbar.this.contextLabel.setText(s);
            }
        };

    }

    public void setManufactor(final String manuf, final String url) {
        this.manufactorUrl = url;
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                Statusbar.this.url.setText(manuf);
            }
        };
    }

    public void setTip(final String text) {
        if (text == null) {
            this.tip.setText("");
        } else {
            this.tip.setText(text.replaceAll("<.*?>", " "));
        }
    }

}
