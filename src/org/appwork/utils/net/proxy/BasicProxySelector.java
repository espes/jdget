package org.appwork.utils.net.proxy;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;

import sun.net.spi.DefaultProxySelector;

public class BasicProxySelector extends DefaultProxySelector {

    private static final ProxySelector INSTANCE = new BasicProxySelector();

    private BasicProxySelector() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.net.ProxySelector#connectFailed(java.net.URI,
     * java.net.SocketAddress, java.io.IOException)
     */
    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        
        throw new IllegalArgumentException("Connections to " + uri + " is not possible. Wrong Proxy?");
    }

    /**
     * @return
     */
    public static ProxySelector getInstance() {
        
        return INSTANCE;
    }

}
