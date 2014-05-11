package org.jdownloader.plugins.controller.host;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import jd.plugins.PluginForHost;

import org.jdownloader.plugins.controller.PluginInfo;

public class StaticHostPlugins {
    private static final List<Class<?>> CLASSES = Arrays.<Class<?>>asList(
        jd.plugins.hoster.CloudStorEs.class
    );

    public static List<PluginInfo<PluginForHost>> list() {
        List<PluginInfo<PluginForHost>> ret = new ArrayList<PluginInfo<PluginForHost>>();
        for (Class<?> c : CLASSES) {
            ret.add(new PluginInfo<PluginForHost>(null, (Class<PluginForHost>)c));
        }
        return ret;
    }
}
