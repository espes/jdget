/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.proxyvole
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.proxyvole;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import org.appwork.utils.logging.Log;

import com.btr.proxy.search.ProxySearch;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogBackEnd;
import com.btr.proxy.util.Logger.LogLevel;

/**
 * @author Thomas
 * 
 */
public class ProxyVoleUtils {
    public static void main(final String[] args) throws MalformedURLException, URISyntaxException {

        Log.L.setLevel(Level.ALL);
        Logger.setBackend(new LogBackEnd() {

            @Override
            public void log(final Class<?> arg0, final LogLevel arg1, final String arg2, final Object... arg3) {

                Log.L.log(Level.ALL, arg2, arg3);
            }

            @Override
            public boolean isLogginEnabled(final LogLevel arg0) {

                return true;
            }
        });
        final ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        
        final ProxySelector myProxySelector = proxySearch.getProxySelector();
        List<Proxy> proxy = myProxySelector.select(new URL("http://www.youtube.com/").toURI());
        System.out.println(proxy);
        proxy = myProxySelector.select(new URL("http://google.com/").toURI());
        System.out.println(proxy);
    }
}
