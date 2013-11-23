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
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.BasicGui;
import org.appwork.resources.AWUTheme;
import org.appwork.swing.components.tooltips.ExtTooltip;
import org.appwork.swing.components.tooltips.TooltipFactory;
import org.appwork.swing.components.tooltips.TooltipPanel;
import org.appwork.utils.swing.EDTRunner;

/**
 * @author thomas
 * 
 */
public class TEst {
    private static boolean RUNNING = true;

    public static void main(final String[] args) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                new BasicGui("CircledProgressBar") {

                    @Override
                    protected void layoutPanel() {

                        final JProgressBar bar = new JProgressBar(0, 100);
                        bar.setToolTipText("BLA");
                        final BoundedRangeModel model = bar.getModel();

                        final CircledProgressBar cbar = new CircledProgressBar(model);
                        cbar.setTooltipFactory(new TooltipFactory() {

                            @Override
                            public ExtTooltip createTooltip() {
                                final ExtTooltip tt = new ExtTooltip() {

                                    /**
                                     * 
                                     */
                                    private static final long serialVersionUID = -1978297969679347066L;

                                    @Override
                                    public TooltipPanel createContent() {
                                        final TooltipPanel p = new TooltipPanel("ins 5,wrap 1", "[]", "[]");
                                        p.add(new JButton(new AbstractAction() {
                                            /**
                                             * 
                                             */
                                            private static final long serialVersionUID = 5385975776993345514L;

                                            {
                                                putValue(Action.NAME, "Drück mich alder!");
                                            }

                                            @Override
                                            public void actionPerformed(final ActionEvent e) {

                                            }
                                        }));

                                        return p;
                                    }

                                    @Override
                                    public String toText() {
                                        // TODO Auto-generated method stub
                                        return null;
                                    }
                                };

                                return tt;
                            }
                        });
                        cbar.setOpaque(false);
                        final CircledProgressBar iconBar = new CircledProgressBar(model);
                        iconBar.setPreferredSize(new Dimension(48, 32));
                        final ImagePainter painter = new ImagePainter(AWUTheme.I().getIcon("close", 32), 1.0f);
                        iconBar.setValueClipPainter(painter);
                        painter.setBackground(Color.GREEN);
                        iconBar.setNonvalueClipPainter(new ImagePainter(AWUTheme.I().getIcon("close", 32), 0.3f));
                    

                        final CircledProgressBar test = new CircledProgressBar();
                        final ImagePainter valuePainter = new ImagePainter(AWUTheme.I().getIcon("dev", 32), 1.0f);
                        // valuePainter.setForeground(Color.BLACK);
                        final ImagePainter nonvaluePainter = new ImagePainter(AWUTheme.I().getIcon("dev", 32), 0.3f);
                        test.setValueClipPainter(valuePainter);
                        test.setNonvalueClipPainter(nonvaluePainter);
                        test.setMaximum(360);
                        test.setToolTipText("Blabla Leberkäs");
                        test.setValue(90);
                        test.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));
                        iconBar.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));
                        
                        bar.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));
                        cbar.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE));
                        painter.setForeground(Color.RED);
   
                        getFrame().getContentPane().setLayout(new MigLayout("ins 4, wrap 3", "[][][grow,fill]", "[grow,fill,32!]"));
//                        getFrame().getContentPane().add(cbar, "height 32!,width 32!");
//                        getFrame().getContentPane().add(iconBar,"height 32!,width 128!");
            
//                        getFrame().getContentPane().add(bar);
                        getFrame().getContentPane().add(test, "height 64!,width 64!");

                        getFrame().getContentPane().add(new JButton(new AbstractAction() {
                            /**
                             * 
                             */
                            private static final long serialVersionUID = -7967957296219315456L;

                            {
                                putValue(Action.NAME, "Toggle Indeterminated");
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
                        getFrame().getContentPane().add(bt = new JButton(new AbstractAction() {
                            /**
                             * 
                             */
                            private static final long serialVersionUID = -7726007502976853379L;

                            {
                                putValue(Action.NAME, "Toggle RUN");
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
                                        Thread.sleep(200);
                                        if (TEst.RUNNING) {
                                            new EDTRunner() {

                                                @Override
                                                protected void runInEDT() {
                                                    model.setValue(model.getValue() + direction);
                                                    iconBar.setToolTipText((int) (Math.random() * 100) + " %");
                                                    if (Math.random() < 0.1) {
                                                        iconBar.setToolTipText("lfdsifgsdkbfd sdkf jhdsafjhsafgj sdafsjdhfga jsdfgahjd gkj");
                                                    }
                                                    if (Math.random() < 0.1) {
                                                        iconBar.setToolTipText("lfd\r\nsifgs\r\ndkb\r\nfd sdkf\r\n jhdsafj\r\nhsafgj\r\n sdafsjd\r\nhfga \r\njsdfgahj\r\nd gkj");
                                                    }
                                                    if (model.getValue() == model.getMaximum() || model.getValue() == model.getMinimum()) {
                                                        model.setValue(0);

                                                    }
                                                }
                                            };

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
        };

    }
}
