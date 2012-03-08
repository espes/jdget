//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.controlling.reconnect;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.controlling.JDLogger;
import jd.controlling.reconnect.ipcheck.IP;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.DynByteBuffer;
import jd.nutils.Executer;
import jd.nutils.ProcessListener;
import jd.utils.JDUtilities;

import org.appwork.utils.Regex;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.os.CrossSystem;

public class RouterUtils {

    private static final String PATTERN_WIN_ARP = "..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?";

    private static class WebServerChecker {

        private final String host;
        private InetAddress  address = null;

        public WebServerChecker(final String host) {
            this.host = host;
        }

        public InetAddress getAddress() {
            return this.address;
        }

        public void go() throws Exception {

            if (RouterUtils.checkPort(this.host)) {
                address = InetAddress.getByName(this.host);
            }

        }
    }

    private static InetAddress ADDRESS_CACHE;

    /**
     * Runs throw a predefined Host Table (multithreaded) and checks if there is
     * a service on port 80. returns the ip if there is a webservice on any
     * adress. See {@link #updateHostTable()}
     * 
     * @return
     */
    private static InetAddress ASYNCH_RETURN;

    private static String callArpTool(final String ipAddress) throws IOException, InterruptedException {

        if (CrossSystem.isWindows()) {
            return RouterUtils.callArpToolWindows(ipAddress);
        } else {

            return RouterUtils.callArpToolDefault(ipAddress);
        }
    }

    private static String callArpToolDefault(final String ipAddress) throws IOException, InterruptedException {
        String out = null;
        final InetAddress hostAddress = InetAddress.getByName(ipAddress);
        ProcessBuilder pb = null;
        try {
            pb = new ProcessBuilder(new String[] { "ping", ipAddress });
            pb.start();
            /*-n for dont resolv ip, MUCH MUCH faster*/
            out = JDUtilities.runCommand("arp", new String[] { "-n", ipAddress }, null, 10);
            pb.directory();
            if (!out.matches("(?is).*((" + hostAddress.getHostName() + "|" + hostAddress.getHostAddress() + ").*..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?|.*..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?.*(" + hostAddress.getHostName() + "|" + hostAddress.getHostAddress() + ")).*")) {
                out = null;
            }
        } catch (final Exception e) {
            if (pb != null) {
                pb.directory();
            }
        }
        if (out == null || out.trim().length() == 0) {
            try {
                pb = new ProcessBuilder(new String[] { "ping", ipAddress });
                pb.start();
                out = JDUtilities.runCommand("ip", new String[] { "neigh", "show" }, null, 10);
                pb.directory();
                if (out != null) {
                    if (!out.matches("(?is).*((" + hostAddress.getHostName() + "|" + hostAddress.getHostAddress() + ").*..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?|.*..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?[:\\-]..?.*(" + hostAddress.getHostName() + "|" + hostAddress.getHostAddress() + ")).*")) {
                        out = null;
                    } else {
                        out = new Regex(out, "(" + hostAddress.getHostName() + "|" + hostAddress.getHostAddress() + ")[^\r\n]*").getMatch(-1);
                    }
                }
            } catch (final Exception e) {
                if (pb != null) {
                    pb.directory();
                }
            }
        }
        return out;
    }

    private static String callArpToolWindows(final String ipAddress) throws IOException, InterruptedException {
        final ProcessBuilder pb = new ProcessBuilder(new String[] { "ping", ipAddress });
        pb.start();

        final String[] parts = JDUtilities.runCommand("arp", new String[] { "-a" }, null, 10).split(System.getProperty("line.separator"));
        pb.directory();
        for (final String part : parts) {
            if (part.indexOf(ipAddress) > -1 && new Regex(part, PATTERN_WIN_ARP).matches()) { return part; }
        }
        return null;
    }

    /**
     * checks if there is a open port at host. e.gh. test if there is a
     * webserverr unning on this port
     * 
     * @param host
     * @return
     */
    public static boolean checkPort(final String host) {
        return (checkPort(host, 80) || checkPort(host, 443));
    }

    private static boolean checkPort(String host, int port) {
        Socket sock = null;
        URLConnectionAdapter con = null;
        try {

            sock = new Socket();
            sock.setSoTimeout(1000);
            sock.connect(new InetSocketAddress(host, port), 1000);
            Browser br = new Browser();
            br.setProxy(HTTPProxy.NONE);
            br.setConnectTimeout(2000);
            br.setReadTimeout(1000);
            br.setFollowRedirects(false);
            if (port == 443) {
                /* 443 is https */
                con = br.openGetConnection("https://" + host + ":443");
            } else {
                String portS = "";
                if (port != 80) {
                    portS = ":" + port;
                }
                /* fallback to normal http */
                con = br.openGetConnection("http://" + host + portS);
            }

            String redirect = br.getRedirectLocation();
            String domain = Browser.getHost(redirect);
            // some isps or DNS server redirect in case of no server found
            if (redirect != null && !InetAddress.getByName(domain).equals(InetAddress.getByName(host))) {
                // if we have redirects, the new domain should be the local one,
                // too
                return false;
            }

            return true;

        } catch (final Exception e) {
        } finally {
            try {
                sock.close();
            } catch (Throwable e) {
            }
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
        return false;
    }

    /**
     * Tries to find the router's ip adress and returns it.
     * 
     * @param force
     *            if false, jd uses a cached value if available
     * 
     * @return
     * @throws InterruptedException
     */
    public synchronized static InetAddress getAddress(final boolean force) throws InterruptedException {
        if (!force && RouterUtils.ADDRESS_CACHE != null) { return RouterUtils.ADDRESS_CACHE; }
        InetAddress address = null;
        try {
            try {
                address = RouterUtils.getIPFormNetStat();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (address != null) { return address; }
            address = RouterUtils.getIPFromRouteCommand();
            if (address != null) { return address; }
            address = RouterUtils.getIpFormHostTable();
            return address;
        } finally {
            RouterUtils.ADDRESS_CACHE = address;
        }
    }

    /**
     * Chekcs a Host table
     * 
     * @return
     * @throws InterruptedException
     */
    public static InetAddress getIpFormHostTable() throws InterruptedException {
        RouterUtils.updateHostTable();
        RouterUtils.ASYNCH_RETURN = null;
        // final int size = RouterUtils.HOST_NAMES.size();
        // final Threader threader = new Threader();
        // for (int i = 0; i < size; i++) {
        // threader.add(new WebServerChecker(RouterUtils.HOST_NAMES.get(i)));
        // }
        //
        // threader.addWorkerListener(new WorkerListener() {
        //
        // public void onThreadException(final Threader th, final JDRunnable
        // job, final Throwable e) {
        // }
        //
        // public void onThreadFinished(final Threader th, final JDRunnable
        // runnable) {
        // if (((WebServerChecker) runnable).getAddress() != null) {
        // th.interrupt();
        // RouterUtils.ASYNCH_RETURN = ((WebServerChecker)
        // runnable).getAddress();
        // }
        //
        // }
        //
        // public void onThreadStarts(final Threader threader, final JDRunnable
        // runnable) {
        // }
        //
        // });
        // threader.startAndWait();
        return RouterUtils.ASYNCH_RETURN;
    }

    /**
     * Calls netstat -nt to find the router's ip. returns null if nothing found
     * and the ip if found something;
     * 
     * 
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    public static InetAddress getIPFormNetStat() throws UnknownHostException, InterruptedException {

        final Pattern pat = Pattern.compile("^\\s*(?:0\\.0\\.0\\.0\\s*){1,2}((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)).*");
        final Executer exec = new Executer("netstat");
        exec.addParameter("-rn");
        exec.setWaitTimeout(5000);
        System.out.println(0);
        exec.start();
        exec.waitTimeout();

        final String[] out = Regex.getLines(exec.getOutputStream());
        for (final String string : out) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            final String m = new Regex(string, pat).getMatch(0);
            if (m != null && !"0.0.0.0".equals(m)) {
                if (checkPort(m)) { return InetAddress.getByName(m); }

            }
        }

        return null;
    }

    /**
     * Uses the /sbin/route command to determine the router's ip. works on linux
     * and mac.
     * 
     * @return
     */
    public static InetAddress getIPFromRouteCommand() {
        if (new File("/sbin/route").exists()) {

            if (CrossSystem.isMac()) {
                /* TODO: needs to get checked by a mac user */
                final Executer exec = new Executer("/sbin/route");
                exec.addParameters(new String[] { "-n", "get", "default" });
                exec.setRunin("/");
                exec.setWaitTimeout(1000);
                exec.start();
                exec.waitTimeout();
                final String routingt = exec.getOutputStream() + " \r\n " + exec.getErrorStream();
                final Pattern pattern = Pattern.compile("gateway: (\\S*)", Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(routingt);
                while (matcher.find()) {
                    final String hostname = matcher.group(1).trim();
                    if (!hostname.matches("[\\s]*\\*[\\s]*")) {
                        try {
                            final InetAddress ia = InetAddress.getByName(hostname);
                            /* first we try to connect to http */
                            if (RouterUtils.checkPort(hostname)) { return ia; }
                            /* then lets try https */
                            if (RouterUtils.checkPort(hostname)) { return ia; }
                        } catch (final Exception e) {
                            JDLogger.exception(e);
                        }
                    }

                }
            } else {
                /*
                 * we use route command to find gateway routes and test them for
                 * port 80,443
                 */
                final Executer exec = new Executer("/sbin/route");
                exec.addParameters(new String[] { "-n" });
                exec.setRunin("/");
                exec.setWaitTimeout(1000);
                exec.start();
                exec.waitTimeout();
                String routingt = exec.getOutputStream() + " \r\n " + exec.getErrorStream();
                routingt = routingt.replaceFirst(".*\n.*", "");

                final Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+.*?(\\d+\\.\\d+\\.\\d+\\.\\d+).*?G", Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(routingt);
                while (matcher.find()) {
                    final String hostname = matcher.group(1).trim();
                    if (!hostname.matches("[\\s]*\\*[\\s]*")) {
                        try {
                            final InetAddress ia = InetAddress.getByName(hostname);
                            /* first we try to connect to http */
                            if (RouterUtils.checkPort(hostname)) { return ia; }
                            /* then lets try https */
                            if (RouterUtils.checkPort(hostname)) { return ia; }

                        } catch (final Exception e) {
                            JDLogger.exception(e);
                        }
                    }

                }
            }

        }
        return null;
    }

    public static String getMacAddress(final InetAddress hostAddress) throws IOException, InterruptedException {
        final String resultLine = RouterUtils.callArpTool(hostAddress.getHostAddress());
        if (resultLine == null) { return null; }
        String rd = new Regex(resultLine, RouterUtils.PATTERN_WIN_ARP).getMatch(-1).replaceAll("-", ":");
        if (rd == null) { return null; }
        rd = rd.replaceAll("\\s", "0");
        final String[] d = rd.split("[:\\-]");
        final StringBuilder ret = new StringBuilder(18);
        for (final String string : d) {

            if (string.length() < 2) {
                ret.append('0');
            }
            ret.append(string);
            ret.append(':');
        }
        return ret.toString().substring(0, 17);
    }

    /**
     * Returns the MAC adress behind the ip
     * 
     * @param ip
     * @return
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getMacAddress(final String ip) throws UnknownHostException, IOException, InterruptedException {

        final String ret = RouterUtils.getMacAddress(InetAddress.getByName(ip));
        if (ret != null) { return ret.replace(":", "").replace("-", "").toUpperCase(); }
        return ret;
    }

    public static void main(String[] args) throws SocketException {
        System.out.println(getWindowsGateway());
    }

    /**
     * USes windows tracert command to find the gateway
     * 
     * @return
     */
    public static InetAddress getWindowsGateway() {
        if (CrossSystem.isWindows()) {
            // tested on win7
            final String[] ret = new String[1];
            final Executer exec = new Executer("tracert ");
            exec.addProcessListener(new ProcessListener() {

                private int counter = 0;

                public void onProcess(Executer exec, String latestLine, DynByteBuffer totalBuffer) {
                    if (latestLine.contains("*")) {
                        // timeouts
                        exec.interrupt();
                    }
                    final Matcher matcher = Pattern.compile(IP.IP_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(latestLine);
                    if (matcher.find()) {
                        if (counter++ > 0) {
                            ;
                            String firstRouteIP = matcher.group(0);
                            if (IP.isLocalIP(firstRouteIP)) {
                                ret[0] = firstRouteIP;
                            } else {
                                exec.interrupt();
                            }
                        }
                    }

                }

                public void onBufferChanged(Executer exec, DynByteBuffer totalBuffer, int latestReadNum) {
                }
            }, Executer.LISTENER_STDSTREAM);
            exec.addParameters(new String[] { "-d", "-h", "10", "-4", "jdownloader.org" });
            exec.setRunin("/");
            exec.setWaitTimeout(15000);
            exec.start();
            exec.waitTimeout();
            try {
                return InetAddress.getByName(ret[0]);
            } catch (Throwable e) {
                return null;
            }

        } else {
            throw new IllegalStateException("OS not supported");
        }

    }

    /**
     * This function tries to return of the internet connection is through a
     * direct modem connection.Works only for windows. tested on win 7
     * 
     * @return
     */
    public static boolean isWindowsModemConnection() {
        if (CrossSystem.isWindows()) {
            // tested on win7

            final Executer exec = new Executer("tracert ");
            exec.addParameters(new String[] { "-d", "-h", "1", "-4", "-w", "500", "jdownloader.org" });
            exec.setRunin("/");
            exec.setWaitTimeout(5000);
            exec.start();
            exec.waitTimeout();
            String routingt = exec.getOutputStream();
            String[] lines = Regex.getLines(routingt.trim());
            if (lines.length >= 2) {
                for (int i = 1; i < lines.length; i++) {

                    final Matcher matcher = Pattern.compile(IP.IP_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(lines[i]);
                    if (matcher.find()) {
                        String firstRouteIP = matcher.group(0);
                        if (IP.isLocalIP(firstRouteIP)) {
                            return false;
                        } else {
                            return true;
                        }

                    }
                }
            } else {
                throw new IllegalStateException("Not available (Offline?) Exception");
            }

        } else {
            throw new IllegalStateException("OS not supported");
        }
        return true;
    }

    /**
     * Returns all InetAddresses of the local Network devices.
     * 
     * @return
     */
    public static ArrayList<InetAddress> getNetworkDeviceAdresses() {
        final ArrayList<InetAddress> ret = new ArrayList<InetAddress>();
        try {

            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                final NetworkInterface ni = e.nextElement();

                final Enumeration<InetAddress> e2 = ni.getInetAddresses();

                while (e2.hasMoreElements()) {
                    final InetAddress ip = e2.nextElement();
                    if (ip.isLoopbackAddress()) {
                        break;
                    }
                    if (ip.getHostAddress().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                        ret.add(ip);
                    }
                }
            }
        } catch (final Exception e) {
            JDLogger.exception(e);
        }
        return ret;
    }

    /**
     * Updates the host table and adds the full ip range (0-255) of the local
     * devices to the table.
     */
    private static void updateHostTable() {
        String ip;

        for (final InetAddress ia : RouterUtils.getNetworkDeviceAdresses()) {
            // try {
            //
            // if (RouterUtils.validateIP(ia.getHostAddress() + "")) {
            // ip = ia.getHostAddress();
            //
            // if (ip != null && ip.lastIndexOf(".") != -1) {
            // final String host = ip.substring(0, ip.lastIndexOf(".")) + ".";
            // for (int i = 0; i < 255; i++) {
            // final String lhost = host + i;
            // if (!lhost.equals(ip) && !RouterUtils.HOST_NAMES.contains(lhost))
            // {
            // RouterUtils.HOST_NAMES.add(lhost);
            // }
            //
            // }
            // }
            // }
            // RouterUtils.HOST_NAMES.remove(ia.getHostName());
            // RouterUtils.HOST_NAMES.remove(ia.getHostAddress());
            // } catch (final Exception exc) {
            // JDLogger.exception(exc);
            // }
        }
    }

    /**
     * Validates the givvei ip. a) checks if it is a valid IP adress (regex) b)
     * checks if it is available within a timeout of 1500 ms
     * 
     * @param iPaddress
     * @return
     */
    public static boolean validateIP(final String iPaddress) {
        final Pattern IP_PATTERN = Pattern.compile("\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
        if (IP_PATTERN.matcher(iPaddress).matches()) {
            return true;
        } else {
            try {
                if (InetAddress.getByName(iPaddress).isReachable(1500)) { return true; }
            } catch (final Exception e) {
                JDLogger.exception(e);
            }
        }
        return false;
    }

}
