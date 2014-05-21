package org.jdownloader.gui.notify.gui;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import jd.gui.swing.jdgui.JDGui;

import org.appwork.utils.StringUtils;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.windowmanager.WindowManager;
import org.jdownloader.gui.jdtrayicon.ScreenStack;
import org.jdownloader.gui.notify.gui.BubbleNotifyConfig.Anchor;
import org.jdownloader.logging.LogController;

public class Balloner implements ComponentListener {
    private HashMap<GraphicsDevice, ScreenStack> stacks;
    private JFrame                               owner;
    private LogSource                            logger;
    
    public Balloner(JFrame owner) {
        this.owner = owner;
        stacks = new HashMap<GraphicsDevice, ScreenStack>();
        logger = LogController.getInstance().getLogger(Balloner.class.getName());
    }
    
    public JFrame getOwner() {
        return owner;
    }
    
    public void add(final AbstractNotifyWindow notify) {
        new EDTRunner() {
            
            @Override
            protected void runInEDT() {
                ScreenStack screenStack = getScreenStack();
                
                screenStack.add(notify);
                notify.setScreenStack(screenStack);
                notify.setController(Balloner.this);
                notify.addComponentListener(Balloner.this);
                
                layout(screenStack);
                notify.setVisible(true);
                notify.setAlwaysOnTop(true);
                
                // WindowManager.getInstance().setVisible(notify, true, FrameState.TO_FRONT);
            }
        };
        
    }
    
    protected void layout(ScreenStack screenStack) {
        
        Rectangle bounds = getVisibleBounds(screenStack);
        
        int y = 0;
        int x = 0;
        int width = 0;
        for (AbstractNotifyWindow n : screenStack) {
            if (!n.isDisposed()) {
                
                n.setPreferredSize(null);
                width = Math.max(width, n.getPreferredSize().width);
                
            }
            
        }
        
        ArrayList<AbstractNotifyWindow> removes = new ArrayList<AbstractNotifyWindow>();
        for (AbstractNotifyWindow n : screenStack) {
            if (n.isDisposed()) {
                // remove badly implemented ballons
                removes.add(n);
                n.removeComponentListener(this);
                continue;
                
            }
            Dimension ps = n.getPreferredSize();
            ps.width = width;
            n.setPreferredSize(ps);
            n.pack();
            Point sl = calculateLocation(bounds, ps, startAnchor, startPoint);
            Point el = calculateLocation(bounds, ps, endAnchor, endPoint);
            Point position = calculateLocation(bounds, ps, anchor, anchorPoint);
            position.y += y;
            if (el.x != position.x) {
                // move left or right
                el.y += y;
            }
            n.setStartLocation(sl);
            n.setEndLocation(el);
            n.setPreferedLocation(position.x, position.y);
            if (anchorPoint.y < 0) {
                y -= ps.height + getMargin();
            } else {
                y += ps.height + getMargin();
            }
            
        }
        screenStack.removeAll(removes);
    }
    
    private Point calculateLocation(Rectangle bounds, Dimension ps, Anchor anchor, Point point) {
        int ax = 0;
        int ay = 0;
        switch (anchor) {
            case BOTTOM_LEFT:
                ay += ps.height;
                ax += 0;
                break;
            case BOTTOM_RIGHT:
                ay += ps.height;
                ax += ps.width;
                
                break;
            case TOP_LEFT:
                ay += 0;
                ax += 0;
                break;
            case TOP_RIGHT:
                ay += 0;
                ax += ps.width;
                break;
        }
        int px = 0;
        int py = 0;
        if (point.x < 0) {
            px = bounds.x + bounds.width + point.x + 1;
        } else {
            px = bounds.x + startPoint.x;
        }
        if (point.y < 0) {
            py = bounds.y + bounds.height + point.y + 1;
        } else {
            py = bounds.y + point.y;
        }
        
        return new Point(px - ax, py - ay);
    }
    
    protected int getMargin() {
        return 10;
    }
    
    private String screenID;
    private Point  startPoint  = new Point(-1, -1);
    private Point  endPoint    = new Point(-1, 0);
    private Anchor startAnchor = Anchor.TOP_RIGHT;
    private Anchor endAnchor   = Anchor.BOTTOM_RIGHT;
    private Point  anchorPoint;
    private Anchor anchor;
    
    protected Rectangle getVisibleBounds(ScreenStack screenStack) {
        if (CFG_BUBBLE.CFG.isBubbleAnchorRelativeToWindow() && JDGui.getInstance().getMainFrame().isVisible() && WindowManager.getInstance().hasFocus(JDGui.getInstance().getMainFrame())) {
            Rectangle ret = JDGui.getInstance().getMainFrame().getBounds();
            
            return ret;
        }
        final Rectangle bounds = screenStack.getScreen().getDefaultConfiguration().getBounds();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screenStack.getScreen().getDefaultConfiguration());
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.left + insets.right;
        bounds.height -= insets.top + insets.bottom;
        return bounds;
    }
    
    public void hide(AbstractNotifyWindow notify) {
        notify.onClose();
    }
    
    private ScreenStack getScreenStack() {
        final GraphicsDevice screen = getScreenDevice();
        synchronized (stacks) {
            
            ScreenStack ret = stacks.get(screen);
            if (ret == null) {
                ret = new ScreenStack(screen);
                stacks.put(screen, ret);
                
            }
            return ret;
        }
        
    }
    
    protected GraphicsDevice getScreenDevice() {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        if (getScreenID() != null) {
            for (final GraphicsDevice screen : screens) {
                
                if (StringUtils.equalsIgnoreCase(getScreenID(), screen.getIDstring())) return screen;
                
            }
        }
        if (getOwner() == null) {
        
        return ge.getDefaultScreenDevice(); }
        final Rectangle preferedRect = getOwner().getBounds();
        GraphicsDevice biggestInteresctionScreem = null;
        int biggestIntersection = -1;
        
        for (final GraphicsDevice screen : screens) {
            
            final Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            final Rectangle interSec = bounds.intersection(preferedRect);
            if (Math.max(interSec.width, 0) * Math.max(interSec.height, 0) > biggestIntersection || biggestInteresctionScreem == null) {
                biggestIntersection = Math.max(interSec.width, 0) * Math.max(interSec.height, 0);
                biggestInteresctionScreem = screen;
                if (interSec.equals(preferedRect)) {
                    break;
                }
            }
        }
        return biggestInteresctionScreem;
    }
    
    private String getScreenID() {
        return screenID;
    }
    
    public void setScreenID(String screenID) {
        this.screenID = screenID;
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        
    }
    
    @Override
    public void componentMoved(ComponentEvent e) {
        
    }
    
    @Override
    public void componentShown(ComponentEvent e) {
        
    }
    
    @Override
    public void componentHidden(ComponentEvent e) {
        
        if (e.getSource() instanceof AbstractNotifyWindow) {
            AbstractNotifyWindow notify = ((AbstractNotifyWindow) e.getSource());
            remove(notify);
        }
        
    }
    
    public void remove(AbstractNotifyWindow notify) {
        ScreenStack screenStack = getScreenStack();
        screenStack.remove(notify);
        notify.removeComponentListener(this);
        layout(screenStack);
    }
    
    public void setStartPoint(Point point, Anchor anchor) {
        startPoint = point;
        startAnchor = anchor;
    }
    
    public void setEndPoint(Point point, Anchor anchor) {
        endPoint = point;
        endAnchor = anchor;
    }
    
    public void setAnchorPoint(Point point, Anchor anchor) {
        anchorPoint = point;
        this.anchor = anchor;
    }
    
    public void relayout() {
        ScreenStack screenStack = getScreenStack();
        layout(screenStack);
    }
    
}
