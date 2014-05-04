package org.jdownloader.api.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appwork.utils.StringUtils;
import org.jdownloader.plugins.controller.crawler.CrawlerPluginController;
import org.jdownloader.plugins.controller.crawler.LazyCrawlerPlugin;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;

public class PluginsAPIImpl implements PluginsAPI {
    @Override
    public List<String> getPluginRegex(String URL) {
        List<String> ret = new ArrayList<String>();
        if (StringUtils.isNotEmpty(URL)) {
            URL = URL.replaceAll("^https?://(www.)?", "");
            for (LazyHostPlugin lhp : HostPluginController.getInstance().list()) {
                if (URL.equals(lhp.getDisplayName())) {
                    ret.add(lhp.getPattern().pattern());
                }
            }
            for (LazyCrawlerPlugin lhp : CrawlerPluginController.getInstance().list()) {
                if (URL.equals(lhp.getDisplayName())) {
                    ret.add(lhp.getPattern().pattern());
                }
            }
        }
        return ret;
    }

    @Override
    public HashMap<String, ArrayList<String>> getAllPluginRegex() {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        for (LazyHostPlugin lhp : HostPluginController.getInstance().list()) {
            ArrayList<String> list = map.get(lhp.getDisplayName());
            if (list == null) {
                list = new ArrayList<String>();
                map.put(lhp.getDisplayName(), list);
            }
            list.add(lhp.getPattern().pattern());
        }
        for (LazyCrawlerPlugin lhp : CrawlerPluginController.getInstance().list()) {
            ArrayList<String> list = map.get(lhp.getDisplayName());
            if (list == null) {
                list = new ArrayList<String>();
                map.put(lhp.getDisplayName(), list);
            }
            list.add(lhp.getPattern().pattern());
        }

        return map;
    }
}