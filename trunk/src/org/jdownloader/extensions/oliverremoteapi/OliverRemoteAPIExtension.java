package org.jdownloader.extensions.oliverremoteapi;

import jd.plugins.AddonPanel;

import org.appwork.txtresource.TranslateInterface;
import org.jdownloader.api.RemoteAPIController;
import org.jdownloader.extensions.AbstractExtension;
import org.jdownloader.extensions.ExtensionConfigPanel;
import org.jdownloader.extensions.StartException;
import org.jdownloader.extensions.StopException;
import org.jdownloader.extensions.oliverremoteapi.api.captcha.CaptchaMobileAPIImpl;
import org.jdownloader.extensions.oliverremoteapi.api.content.ContentMobileAPIImpl;
import org.jdownloader.extensions.oliverremoteapi.api.downloads.DownloadsMobileAPIImpl;
import org.jdownloader.extensions.oliverremoteapi.api.linkcollector.LinkCollectorMobileAPIImpl;
import org.jdownloader.extensions.oliverremoteapi.api.toolbar.JDownloaderToolBarMobileAPIImpl;
import org.jdownloader.logging.LogController;

public class OliverRemoteAPIExtension extends AbstractExtension<OliverRemoteAPIConfig, TranslateInterface> {

    private DownloadsMobileAPIImpl          dma;
    private LinkCollectorMobileAPIImpl      lca;
    private CaptchaMobileAPIImpl            cma;
    private ContentMobileAPIImpl            coma;
    private JDownloaderToolBarMobileAPIImpl tma;

    @Override
    public boolean isDefaultEnabled() {
        return true;
    }

    @Override
    protected void stop() throws StopException {
        try {
            RemoteAPIController remoteAPI = RemoteAPIController.getInstance();
            remoteAPI.unregister(dma);
            remoteAPI.unregister(lca);
            remoteAPI.unregister(cma);
            remoteAPI.unregister(coma);
            remoteAPI.unregister(tma);
        } catch (final Throwable e) {
            LogController.CL().log(e);
            throw new StopException(e.getMessage());
        }
    }

    @Override
    protected void start() throws StartException {
        try {
            RemoteAPIController remoteAPI = RemoteAPIController.getInstance();
            remoteAPI.register(dma = new DownloadsMobileAPIImpl());
            remoteAPI.register(lca = new LinkCollectorMobileAPIImpl());
            remoteAPI.register(cma = new CaptchaMobileAPIImpl());
            remoteAPI.register(coma = new ContentMobileAPIImpl());
            remoteAPI.register(tma = new JDownloaderToolBarMobileAPIImpl());
        } catch (final Throwable e) {
            LogController.CL().log(e);
            throw new StartException(e);
        }
    }

    @Override
    protected void initExtension() throws StartException {
        setTitle("oliverremoteapi");
    }

    @Override
    public ExtensionConfigPanel<?> getConfigPanel() {
        return new OliverRemoteAPIConfigPanel(this);
    }

    @Override
    public boolean hasConfigPanel() {
        return true;
    }

    @Override
    public String getDescription() {
        return "RemoteAPI by Oliver";
    }

    @Override
    public AddonPanel<? extends AbstractExtension<OliverRemoteAPIConfig, TranslateInterface>> getGUI() {
        return null;
    }

}
