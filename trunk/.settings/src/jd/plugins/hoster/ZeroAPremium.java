package jd.plugins.hoster;

import jd.PluginWrapper;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 15005 $", interfaceVersion = 2, names = { "0premium" }, urls = { "NOREALREGEXJUSTDUMMY" }, flags = { 2 })
public class ZeroAPremium extends PluginForHost {

    public ZeroAPremium(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://0premium.com");
    }

    @Override
    public String getAGBLink() {
        return null;
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        throw new PluginException(LinkStatus.ERROR_FATAL, "DUMMY PLUGIN");
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception {
        return AvailableStatus.FALSE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}