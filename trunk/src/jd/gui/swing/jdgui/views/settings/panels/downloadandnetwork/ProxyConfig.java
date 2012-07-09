package jd.gui.swing.jdgui.views.settings.panels.downloadandnetwork;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;

import jd.controlling.IOEQ;
import jd.controlling.proxy.ProxyController;
import jd.controlling.proxy.ProxyEvent;
import jd.controlling.proxy.ProxyInfo;
import jd.gui.swing.jdgui.views.settings.ConfigPanel;

import org.appwork.swing.MigPanel;
import org.appwork.swing.components.ExtButton;
import org.appwork.swing.exttable.utils.MinimumSelectionObserver;
import org.appwork.utils.Regex;
import org.appwork.utils.event.DefaultEventListener;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.settings.AbstractConfigPanel;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.translate._JDT;

public class ProxyConfig extends AbstractConfigPanel implements DefaultEventListener<ProxyEvent<ProxyInfo>> {

    public String getTitle() {
        return _JDT._.gui_settings_proxy_title();
    }

    private static final long  serialVersionUID = -521958649780869375L;

    private ProxyTable         table;

    private ExtButton          btnAdd;

    private ExtButton          btnRemove;
    private ExtButton          btnAuto;

    private ScheduledFuture<?> timer            = null;

    private ExtButton          btImport;

    private ExtButton          btExport;

    public ProxyConfig() {
        super();

        this.addHeader(getTitle(), NewTheme.I().getIcon("proxy_rotate", 32));
        this.addDescriptionPlain(_JDT._.gui_settings_proxy_description());

        table = new ProxyTable();

        JScrollPane sp = new JScrollPane(table);
        this.add(sp, "gapleft 37,growx, pushx,spanx,pushy,growy");
        MigPanel toolbar = new MigPanel("ins 0", "[][][grow,fill]", "[]");
        toolbar.setOpaque(false);
        btnAdd = new ExtButton(new ProxyAddAction(table));
        btnAuto = new ExtButton(new ProxyAutoAction());
        ProxyDeleteAction dl;
        btnRemove = new ExtButton(dl = new ProxyDeleteAction(table));
        btImport = new ExtButton(new AppAction() {
            {
                setName(_GUI._.LinkgrabberFilter_LinkgrabberFilter_import());
                setIconKey("import");
            }

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    String txt = Dialog.getInstance().showInputDialog(Dialog.STYLE_LARGE, _GUI._.ProxyConfig_actionPerformed_import_title_(), _GUI._.ProxyConfig_actionPerformed_import_proxies_explain_(), null, NewTheme.I().getIcon("proxy", 32), null, null);
                    final ArrayList<HTTPProxy> list = new ArrayList<HTTPProxy>();
                    for (String s : Regex.getLines(txt)) {
                        try {
                            int i = s.indexOf("://");
                            String protocol = "http";
                            if (i > 0) {
                                protocol = s.substring(0, i);
                                s = "http://" + s.substring(i + 3);
                            }
                            URL url = null;

                            url = new URL(s);

                            String user = null;
                            String pass = null;
                            String userInfo = url.getUserInfo();
                            if (userInfo != null) {
                                int in = userInfo.indexOf(":");
                                if (in >= 0) {
                                    user = (userInfo.substring(0, in));
                                    pass = (userInfo.substring(in + 1));
                                } else {
                                    user = (userInfo);
                                }
                            }
                            if ("socks5".equalsIgnoreCase(protocol)) {

                                final HTTPProxy ret = new HTTPProxy(HTTPProxy.TYPE.SOCKS5, url.getHost(), url.getPort());
                                ret.setUser(user);
                                ret.setPass(pass);
                                list.add(ret);
                            } else if ("socks4".equalsIgnoreCase(protocol)) {
                                final HTTPProxy ret = new HTTPProxy(HTTPProxy.TYPE.SOCKS4, url.getHost(), url.getPort());
                                ret.setUser(user);
                                list.add(ret);
                            } else {
                                final HTTPProxy ret = new HTTPProxy(HTTPProxy.TYPE.HTTP, url.getHost(), url.getPort());
                                ret.setUser(user);
                                ret.setPass(pass);
                                list.add(ret);

                            }
                        } catch (MalformedURLException e2) {
                            e2.printStackTrace();
                        }

                    }
                    IOEQ.add(new Runnable() {

                        public void run() {
                            ProxyController.getInstance().addProxy(list);
                        }
                    });
                } catch (DialogClosedException e1) {
                    e1.printStackTrace();
                } catch (DialogCanceledException e1) {
                    e1.printStackTrace();
                }
            }
        });
        btExport = new ExtButton(new AppAction() {
            {
                setName(_GUI._.LinkgrabberFilter_LinkgrabberFilter_export());
                setIconKey("export");
            }

            @Override
            public void actionPerformed(ActionEvent e) {

                ProxyTable.export(table.getExtTableModel().getElements());
            }
        });
        // tb.add(, "height 26!,sg 2");
        //
        // tb.add(, "height 26!,sg 2");
        table.getSelectionModel().addListSelectionListener(new MinimumSelectionObserver(table, dl, 1) {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                boolean canremove = false;
                ArrayList<ProxyInfo> selected = ProxyConfig.this.table.getExtTableModel().getSelectedObjects();
                if (selected != null) {
                    for (ProxyInfo pi : selected) {
                        if (pi.isRemote()) {
                            canremove = true;
                            break;
                        }
                    }
                }
                action.setEnabled(canremove);
            }
        });

        toolbar.add(btnAdd, "sg 1,height 26!");
        toolbar.add(btnRemove, "sg 1,height 26!");
        toolbar.add(Box.createHorizontalGlue(), "pushx,growx");

        toolbar.add(btImport, "sg 1,height 26!");
        toolbar.add(btExport, "height 26!");
        toolbar.add(btnAuto, "height 26!");

        add(toolbar, "gapleft 37,growx,spanx");

    }

    @Override
    public ImageIcon getIcon() {
        return NewTheme.I().getIcon("proxy_rotate", ConfigPanel.ICON_SIZE);
    }

    @Override
    public void save() {
    }

    @Override
    public void updateContents() {
        IOEQ.add(new Runnable() {

            public void run() {
                table.getExtTableModel()._fireTableStructureChanged(ProxyController.getInstance().getList(), false);
            }

        }, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdownloader.gui.settings.AbstractConfigPanel#onShow()
     */
    @Override
    protected void onShow() {
        super.onShow();
        synchronized (this) {
            if (timer != null) timer.cancel(false);
            timer = IOEQ.TIMINGQUEUE.scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    table.repaint();
                }

            }, 250, 1000, TimeUnit.MILLISECONDS);
        }
        ProxyController.getInstance().getEventSender().addListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdownloader.gui.settings.AbstractConfigPanel#onHide()
     */
    @Override
    protected void onHide() {
        super.onHide();
        synchronized (this) {
            if (timer != null) {
                timer.cancel(false);
                timer = null;
            }
        }
        ProxyController.getInstance().getEventSender().removeListener(this);
    }

    public void onEvent(ProxyEvent<ProxyInfo> event) {
        switch (event.getType()) {
        case REFRESH:
            new EDTRunner() {
                @Override
                protected void runInEDT() {
                    table.repaint();
                };
            };
            break;
        default:
            table.getExtTableModel()._fireTableStructureChanged(ProxyController.getInstance().getList(), false);
            break;
        }
    }

}
