package jd.gui.swing.jdgui.views.settings.panels.proxy;

import java.awt.event.ActionEvent;
import java.util.List;

import jd.controlling.TaskQueue;
import jd.controlling.proxy.ProxyController;
import jd.controlling.proxy.ProxyInfo;

import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.MessageDialogImpl;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;

public class ProxyAutoAction extends AppAction {

    public ProxyAutoAction() {
        super();
        setName(_GUI._.ProxyAutoAction_actionPerformed_d_title());
        setIconKey("plugin");
    }

    /**
     * 
     */
    private static final long serialVersionUID = -197136045388327528L;

    public void actionPerformed(ActionEvent e) {
        TaskQueue.getQueue().add(new QueueAction<Void, RuntimeException>() {

            @Override
            protected Void run() throws RuntimeException {
                final List<HTTPProxy> list = ProxyController.autoConfig();
                if (list.size() > 0) {
                    ProxyController.getInstance().addProxy(list);
                    if (ProxyController.getInstance().getDefaultProxy().getType() == HTTPProxy.TYPE.DIRECT || ProxyController.getInstance().getDefaultProxy().getType() == HTTPProxy.TYPE.NONE) {
                        ProxyController.getInstance().setDefaultProxy(new ProxyInfo(list.get(0)));
                    }
                    new EDTRunner() {
                        @Override
                        protected void runInEDT() {
                            new MessageDialogImpl(0, _GUI._.ProxyAutoAction_run_added_proxies_(list.size())).show();
                        }
                    };
                } else {
                    new EDTRunner() {
                        @Override
                        protected void runInEDT() {
                            new MessageDialogImpl(0, _GUI._.ProxyAutoAction_run_added_proxies_(0)).show();
                        }
                    };
                }
                return null;
            }
        });

    }
}
