//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package jd.controlling.reconnect.pluginsinc.liveheader;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jdownloader.logging.LogController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CLRConverter {

    public static String[] createLiveHeader(String clr) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            InputSource inSource = new InputSource(new StringReader(clr));

            Document doc = factory.newDocumentBuilder().parse(inSource);

            NodeList nodes = doc.getFirstChild().getChildNodes();
            String routerName = null;
            StringBuilder hlh = new StringBuilder();
            hlh.append("[[[HSRC]]]\r\n");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != 1) continue;

                if (node.getNodeName().equalsIgnoreCase("router")) {
                    routerName = node.getAttributes().getNamedItem("name").getNodeValue().trim();
                } else if (node.getNodeName().equalsIgnoreCase("command")) {
                    hlh.append("    [[[STEP]]]\r\n");
                    hlh.append("        [[[REQUEST]]]\r\n");
                    String method = node.getAttributes().getNamedItem("method").getNodeValue().trim();
                    String action = node.getAttributes().getNamedItem("action").getNodeValue().trim();
                    String basicauth = null;
                    if (method.equalsIgnoreCase("post")) {
                        hlh.append("            ").append(method.toUpperCase()).append(" /").append(action).append(" HTTP/1.1\r\n");
                    } else if (method.equalsIgnoreCase("get")) {
                    } else if (method.equalsIgnoreCase("auth")) {
                        basicauth = action;
                    } else {
                        LogController.CL().severe("UNKNOWN METHOD: " + method);
                    }
                    NodeList params = node.getChildNodes();
                    HashMap<String, String> p = new HashMap<String, String>();
                    StringBuilder post = new StringBuilder();
                    for (int ii = 0; ii < params.getLength(); ii++) {
                        Node param = params.item(ii);
                        try {
                            String key = param.getAttributes().getNamedItem("name").getNodeValue();
                            String value = param.getAttributes().getNamedItem("value").getNodeValue();
                            p.put(key, value);
                            if (post.length() == 0) {
                                post.append(key).append("=").append(value);
                            } else {
                                post.append("&").append(key).append("=").append(value);
                            }
                        } catch (Exception ee) {
                            continue;
                        }

                    }

                    if (method.equalsIgnoreCase("post")) {
                        hlh.append("            Host: %%%routerip%%%\r\n");
                        CLRConverter.inputAuth(hlh, basicauth);
                        hlh.append("\r\n");
                        hlh.append(post.toString().trim());
                        hlh.append("\r\n");
                    } else {
                        if (post.length() == 0) {
                            hlh.append("            ").append(method.toUpperCase()).append(" /").append(action).append(" HTTP/1.1\r\n");
                        } else {
                            hlh.append("            ").append(method.toUpperCase()).append(" /").append(action).append("?").append(post.toString().trim()).append(" HTTP/1.1\r\n");
                        }
                        hlh.append("            Host: %%%routerip%%%\r\n");
                        CLRConverter.inputAuth(hlh, basicauth);
                    }
                    hlh.append("        [[[/REQUEST]]]\r\n");
                    hlh.append("    [[[/STEP]]]\r\n");
                } else {
                    LogController.CL().info("UNKNOWN COMMAND: " + node.getNodeName());
                }
            }
            hlh.append("[[[/HSRC]]]");

            return new String[] { routerName, hlh.toString() };
        } catch (Throwable e) {
            LogController.CL().log(e);
            return null;
        }
    }

    private static void inputAuth(StringBuilder hlh, String basicauth) {
        if (basicauth != null) {
            if (basicauth.equalsIgnoreCase("")) {
                hlh.append("            Authorization: Basic %%%basicauth%%%\r\n");
            } else {
                LogController.CL().severe("UNKNOWN AUTH TYPE");
            }
        }
    }
}
