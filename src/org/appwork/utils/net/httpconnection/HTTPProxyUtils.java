/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpconnection;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

/**
 * @author daniel
 * 
 */
public class HTTPProxyUtils {

    public static List<InetAddress> getLocalIPs() {
        return HTTPProxyUtils.getLocalIPs(false);
    }

    public static List<InetAddress> getLocalIPs(final boolean allowLoopback) {
        final HashSet<InetAddress> ipsLocal = new HashSet<InetAddress>();
        try {
            final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                /* find all network interfaces and their addresses */
                NetworkInterface cur = nets.nextElement();
                Enumeration<InetAddress> addrs = cur.getInetAddresses();
                InetAddress addr;
                while (addrs.hasMoreElements()) {
                    addr = addrs.nextElement();
                    if (addr == null) {
                        continue;
                    }
                    /* only show ipv4 addresses and non loopback */
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    if (allowLoopback == false && addr.isLoopbackAddress()) {
                        continue;
                    }
                    ipsLocal.add(addr);
                }
                /* find all subinterfaces for each network interface, eg. eth0.1 */
                final Enumeration<NetworkInterface> nets2 = cur.getSubInterfaces();
                while (nets2.hasMoreElements()) {
                    cur = nets2.nextElement();
                    addrs = cur.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        addr = addrs.nextElement();
                        if (addr == null) {
                            continue;
                        }
                        /* only show ipv4 addresses and non loopback */
                        if (!(addr instanceof Inet4Address)) {
                            continue;
                        }
                        if (allowLoopback == false && addr.isLoopbackAddress()) {
                            continue;
                        }
                        ipsLocal.add(addr);
                    }
                }
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableList(new ArrayList<InetAddress>(ipsLocal));
    }
}
