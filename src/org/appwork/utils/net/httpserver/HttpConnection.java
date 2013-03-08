/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.Exceptions;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpconnection.HTTPConnectionUtils;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.JSonRequest;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HeadRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.OptionsRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class HttpConnection implements Runnable {

    public static LinkedList<String[]> parseParameterList(final String requestedParameters) throws IOException {
        final LinkedList<String[]> requestedURLParameters = new LinkedList<String[]>();
        if (!StringUtils.isEmpty(requestedParameters)) {
            /* build requestedParamters */
            final String[] parameters = requestedParameters.split("&(?!#)");
            for (final String parameter : parameters) {
                /* we only want the first = be parsed */
                final String params[] = parameter.split("=", 2);
                if (params.length == 1) {
                    /* no value */
                    requestedURLParameters.add(new String[] { URLDecoder.decode(params[0], "UTF-8"), null });
                } else {
                    /* key = value */
                    if ("_".equals(params[0])) {
                        /* we remove random timestamp from jquery here */
                        // System.out.println("remove timestamp param from jquery: "
                        // + params[1]);
                        continue;
                    }
                    requestedURLParameters.add(new String[] { URLDecoder.decode(params[0], "UTF-8"), URLDecoder.decode(params[1], "UTF-8") });
                }
            }
        }
        return requestedURLParameters;
    }

    protected final HttpServer server;
    protected Socket           clientSocket        = null;
    protected boolean          responseHeadersSent = false;

    protected HttpResponse     response            = null;
    protected InputStream      is                  = null;
    protected OutputStream     os                  = null;

    public HttpConnection(final HttpServer server, final InputStream is, final OutputStream os) {
        this.server = server;
        this.is = is;
        this.os = os;
    }

    public HttpConnection(final HttpServer server, final Socket clientSocket) throws IOException {
        this(server, clientSocket.getInputStream(), clientSocket.getOutputStream());
        this.clientSocket = clientSocket;
        this.clientSocket.setSoTimeout(60 * 1000);
    }

    /**
     * parses the request and creates a GET/POST-Request Object and fills it
     * with all received data
     * 
     * @return
     * @throws IOException
     */
    protected HttpRequest buildRequest() throws IOException {
        HttpRequest request = null;
        /* read request Method and Path */
        ByteBuffer header = HTTPConnectionUtils.readheader(this.is, true);
        byte[] bytesRequestLine = new byte[header.limit()];
        header.get(bytesRequestLine);
        String requestLine = this.preProcessRequestLine(new String(bytesRequestLine, "ISO-8859-1").trim());
        String method = new Regex(requestLine, "(GET|POST|HEAD|OPTIONS)").getMatch(0);
        final String requestedURL = new Regex(requestLine, " (/.*?) ").getMatch(0);
        final String requestedPath = new Regex(requestedURL, "(/.*?)($|\\?)").getMatch(0);
        final String requestedParameters = new Regex(requestedURL, "\\?(.+)").getMatch(0);
        final LinkedList<String[]> requestedURLParameters = HttpConnection.parseParameterList(requestedParameters);
        /* read request Headers */
        ByteBuffer headers = HTTPConnectionUtils.readheader(this.is, false);
        byte[] bytesHeaders = new byte[headers.limit()];
        headers.get(bytesHeaders);
        headers = null;
        String[] headerStrings = new String(bytesHeaders, "ISO-8859-1").split("(\r\n)|(\n)");
        /* build requestHeaders HashMap */
        final HeaderCollection requestHeaders = new HeaderCollection();
        for (final String line : headerStrings) {
            String key = null;
            String value = null;
            int index = 0;
            if ((index = line.indexOf(": ")) > 0) {
                key = line.substring(0, index);
                value = line.substring(index + 2);
            } else if ((index = line.indexOf(":")) > 0) {
                /* buggy clients that don't have :space ARG */
                key = line.substring(0, index);
                value = line.substring(index + 1);
            } else {
                key = null;
                value = line;
            }
            requestHeaders.add(new HTTPHeader(key, value));
        }
        header = null;
        bytesRequestLine = null;
        bytesHeaders = null;
        headerStrings = null;
        /* create Request and fill it */
        if ("GET".equalsIgnoreCase(method)) {
            request = new GetRequest();
        } else if ("POST".equalsIgnoreCase(method)) {
            request = new PostRequest(this);
        } else if ("HEAD".equalsIgnoreCase(method)) {
            request = new HeadRequest();
        } else if ("OPTIONS".equalsIgnoreCase(method)) {
            request = new OptionsRequest();
        } else {
            throw new IOException("Unsupported " + requestLine);
        }
        method = null;
        requestLine = null;
        /* parse remoteClientAddresses */
        final java.util.List<String> remoteAddress = new ArrayList<String>();
        if (this.clientSocket != null) {
            remoteAddress.add(this.clientSocket.getInetAddress().getHostAddress());
        }
        final HTTPHeader forwardedFor = requestHeaders.get("X-Forwarded-For");
        if (forwardedFor != null && !StringUtils.isEmpty(forwardedFor.getValue())) {
            final String addresses[] = forwardedFor.getValue().split(", ");
            for (final String ip : addresses) {
                remoteAddress.add(ip.trim());
            }
        }
        request.setRemoteAddress(remoteAddress);
        request.setRequestedURLParameters(requestedURLParameters);
        request.setRequestedPath(requestedPath);
        request.setRequestedURL(requestedURL);
        request.setRequestHeaders(requestHeaders);
        return request;
    }

    public boolean closableStreams() {
        return this.clientSocket == null;
    }

    public void close() {
    }

    /**
     * closes the client socket and removes this connection from server
     * connection pool
     */
    public void closeConnection() {
        if (this.clientSocket == null) { return; }
        try {
            this.clientSocket.shutdownOutput();
        } catch (final Throwable nothing) {
        }
        try {
            this.clientSocket.close();
        } catch (final Throwable nothing) {
        }
    }

    public byte[] getAESJSon_IV(final String ID) {
        return null;
    }

    public byte[] getAESJSon_KEY(final String ID) {
        return null;
    }

    public List<HttpRequestHandler> getHandler() {
        synchronized (this.server.getHandler()) {
            return this.server.getHandler();
        }
    }

    /**
     * @return
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        return this.is;
    }

    /**
     * return the outputStream for this connection. send response headers if
     * they have not been sent yet send yet
     * 
     * @return
     * @throws IOException
     */
    public synchronized OutputStream getOutputStream() throws IOException {
        this.sendResponseHeaders();
        return this.os;
    }

    /**
     * @param aesJsonRequest
     * @return
     */
    public boolean isJSonRequestValid(final JSonRequest aesJsonRequest) {
        return false;
    }

    /**
     * @param response2
     * @return
     */
    protected HttpResponse preProcessHttpResponse(final HttpResponse response) {
        return response;
    }

    protected String preProcessRequestLine(final String requestLine) throws IOException {
        return requestLine;
    }

    protected void requestReceived(final HttpRequest request) {
    }

    @Override
    public void run() {
        boolean closeConnection = true;
        try {
            final HttpRequest request = this.buildRequest();
            // if(Log.L.isLoggable(Level.FINER)) Log.L.finer(request+"");
            this.response = new HttpResponse(this);
            this.response = this.preProcessHttpResponse(this.response);
            this.requestReceived(request);
            boolean handled = false;
            for (final HttpRequestHandler handler : this.getHandler()) {
                if (request instanceof GetRequest) {
                    if (handler.onGetRequest((GetRequest) request, this.response)) {
                        handled = true;
                        break;
                    }
                } else if (request instanceof PostRequest) {
                    if (handler.onPostRequest((PostRequest) request, this.response)) {
                        handled = true;
                        break;
                    }
                }
            }
            if (!handled) {
                /* generate error handler */
                this.response.setResponseCode(ResponseCode.SERVERERROR_NOT_IMPLEMENTED);
            }
            /* send response headers if they have not been sent yet send yet */
            if (this.response.isResponseAsync() == false) {
                this.response.getOutputStream();
            } else {
                closeConnection = false;
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            Log.L.severe(e.getMessage());
            try {
                this.response = new HttpResponse(this);
                this.response.setResponseCode(ResponseCode.SERVERERROR_INTERNAL);
                final byte[] bytes = Exceptions.getStackTrace(e).getBytes("UTF-8");
                this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text; charset=UTF-8"));
                this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
                this.response.getOutputStream().write(bytes);
                this.response.getOutputStream().flush();
            } catch (final Throwable nothing) {
            }
        } finally {
            if (closeConnection) {
                this.closeConnection();
                this.close();
            }
        }
    }

    /**
     * this function sends the response headers
     * 
     * @throws IOException
     */
    protected synchronized void sendResponseHeaders() throws IOException {
        try {
            if (this.responseHeadersSent == true) {
                //
                throw new IOException("Headers already send!");
            }
            if (this.response != null) {
                final OutputStream out = this.os;
                out.write(HttpResponse.HTTP11);
                out.write(this.response.getResponseCode().getBytes());
                out.write(HttpResponse.NEWLINE);
                for (final HTTPHeader h : this.response.getResponseHeaders()) {
                    out.write(h.getKey().getBytes("ISO-8859-1"));
                    out.write(HTTPHeader.DELIMINATOR);
                    out.write(h.getValue().getBytes("ISO-8859-1"));
                    out.write(HttpResponse.NEWLINE);
                }
                out.write(HttpResponse.NEWLINE);
                out.flush();
            }
        } finally {
            this.responseHeadersSent = true;
        }
    }

    @Override
    public String toString() {
        if (this.clientSocket != null) {
            return "HttpConnectionThread: " + this.clientSocket.toString();
        } else {
            return "HttpConnectionThread: IS and OS";
        }

    }
}
