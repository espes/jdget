/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.circlebar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.circlebar;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.BasicGui;
import org.appwork.resources.AWUTheme;

/**
 * @author thomas
 * 
 */
public class TEst {
    private static boolean RUNNING = true;

    public static void main(final String[] args) {
        new BasicGui("CircledProgressBar") {

            @Override
            protected void layoutPanel() {

                final JProgressBar bar = new JProgressBar(0, 100);
                bar.setToolTipText("BLA");
                final BoundedRangeModel model = bar.getModel();

                final CircledProgressBar cbar = new CircledProgressBar(model);

                cbar.setOpaque(false);
                final CircledProgressBar iconBar = new CircledProgressBar(model);
                final ImagePainter painter = new ImagePainter(AWUTheme.I().getIcon("close", 32).getImage(), 1.0f);
                iconBar.setValueClipPainter(painter);
                painter.setBackground(Color.GREEN);
                iconBar.setNonvalueClipPainter(new ImagePainter(AWUTheme.I().getIcon("close", 32).getImage(), 0.3f));

                this.getFrame().getContentPane().setLayout(new MigLayout("ins 4, wrap 3", "[][][grow,fill]", "[grow,fill,32!]"));
                this.getFrame().getContentPane().add(cbar, "height 32!,width 32!");
                this.getFrame().getContentPane().add(iconBar);
                painter.setForeground(Color.RED);
                this.getFrame().getContentPane().add(bar);

                final CircledProgressBar test = new CircledProgressBar();
                final ImagePainter valuePainter = new ImagePainter(AWUTheme.I().getIcon("dev", 32).getImage(), 1.0f);
                // valuePainter.setForeground(Color.BLACK);
                final ImagePainter nonvaluePainter = new ImagePainter(AWUTheme.I().getIcon("dev", 32).getImage(), 0.3f);
                test.setValueClipPainter(valuePainter);
                test.setNonvalueClipPainter(nonvaluePainter);
                test.setMaximum(360);
                test.setToolTipText("Blabla Leberkäs");
                test.setValue(90);
                this.getFrame().getContentPane().add(test, "height 64!,width 64!");

                this.getFrame().getContentPane().add(new JButton(new AbstractAction() {
                    {
                        this.putValue(Action.NAME, "Toggle Indeterminated");
                    }

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final boolean in = !iconBar.isIndeterminate();
                        iconBar.setIndeterminate(in);
                        bar.setIndeterminate(in);
                        cbar.setIndeterminate(in);
                    }
                }));

                JButton bt;
                this.getFrame().getContentPane().add(bt = new JButton(new AbstractAction() {
                    {
                        this.putValue(Action.NAME, "Toggle RUN");
                    }

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        TEst.RUNNING = !TEst.RUNNING;
                    }
                }));
                bt.setToolTipText("BLA2");
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final int direction = 1;
                        while (true) {
                            try {
                                Thread.sleep(30);
                                if (TEst.RUNNING) {
                                    model.setValue(model.getValue() + direction);
                                    iconBar.setToolTipText(model.getValue() + " %");
                                    if (model.getValue() == model.getMaximum() || model.getValue() == model.getMinimum()) {
                                        model.setValue(0);

                                    }
                                }

                            } catch (final InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }

            @Override
            protected void requestExit() {
                System.exit(1);
            }
        };
    }
}
