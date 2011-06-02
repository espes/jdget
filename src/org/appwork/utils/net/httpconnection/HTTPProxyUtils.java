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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * @author daniel
 * 
 */
public class HTTPProxyUtils {
    public static LinkedList<InetAddress> getLocalIPs() {
        final LinkedList<InetAddress> ipsLocal = new LinkedList<InetAddress>();
        try {
            final Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                /* find all network interfaces and their addresses */
                NetworkInterface cur = nets.nextElement();
                List<InterfaceAddress> addrs = cur.getInterfaceAddresses();
                for (final InterfaceAddress addr : addrs) {
                    /* only show ipv4 addresses and non loopback */
                    if (!(addr.getAddress() instanceof Inet4Address)) {
                        continue;
                    }
                    if (addr.getAddress().isLoopbackAddress()) {
                        continue;
                    }
                    final InetAddress ip = addr.getAddress();
                    if (!ipsLocal.contains(ip)) {
                        ipsLocal.add(ip);
                    }
                }
                /* find all subinterfaces for each network interface, eg. eth0.1 */
                final Enumeration<NetworkInterface> nets2 = cur.getSubInterfaces();
                while (nets2.hasMoreElements()) {
                    cur = nets2.nextElement();
                    addrs = cur.getInterfaceAddresses();
                    for (final InterfaceAddress addr : addrs) {
                        /* only show ipv4 addresses and non loopback */
                        if (!(addr.getAddress() instanceof Inet4Address)) {
                            continue;
                        }
                        if (addr.getAddress().isLoopbackAddress()) {
                            continue;
                        }
                        final InetAddress ip = addr.getAddress();
                        if (!ipsLocal.contains(ip)) {
                            ipsLocal.add(ip);
                        }
                    }
                }
            }
        } catch (final SocketException e) {
            e.printStackTrace();
        }
        return ipsLocal;
    }

}
