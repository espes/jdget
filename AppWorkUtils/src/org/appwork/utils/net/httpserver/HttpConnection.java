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
import java.util.regex.Pattern;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.net.protocol.http.HTTPConstants.ResponseCode;
import org.appwork.utils.Exceptions;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpconnection.HTTPConnectionUtils;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HeadRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.KeyValuePair;
import org.appwork.utils.net.httpserver.requests.OptionsRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;
import org.appwork.utils.net.httpserver.responses.HttpResponse;

/**
 * @author daniel
 * 
 */
public class HttpConnection implements Runnable {

    public static enum HttpConnectionType {
        HEAD,
        GET,
        POST,
        OPTIONS,
        UNKNOWN
    }

    public static List<KeyValuePair> parseParameterList(final String requestedParameters) throws IOException {
        final List<KeyValuePair> requestedURLParameters = new LinkedList<KeyValuePair>();
        if (!StringUtils.isEmpty(requestedParameters)) {
            /* build requestedParamters */
            final String[] parameters = requestedParameters.split("&(?!#)");
            for (final String parameter : parameters) {
                /* we only want the first = be parsed */
                final String params[] = parameter.split("=", 2);
                if (params.length == 1) {
                    /* no value */
                    requestedURLParameters.add(new KeyValuePair(null, URLDecoder.decode(params[0], "UTF-8")));
                } else {
                    /* key = value */
                    if ("_".equals(params[0])) {
                        /* we remove random timestamp from jquery here */
                        // System.out.println("remove timestamp param from jquery: "
                        // + params[1]);
                        continue;
                    }
                    requestedURLParameters.add(new KeyValuePair(URLDecoder.decode(params[0], "UTF-8"), URLDecoder.decode(params[1], "UTF-8")));
                }
            }
        }
        return requestedURLParameters;
    }

    protected final HttpServer   server;
    protected Socket             clientSocket        = null;
    protected boolean            responseHeadersSent = false;

    protected HttpResponse       response            = null;

    protected InputStream        is                  = null;

    protected OutputStream       os                  = null;
    protected HttpRequest        request;

    private static final Pattern METHOD              = Pattern.compile("(GET|POST|HEAD|OPTIONS)");
    private static final Pattern REQUESTLINE         = Pattern.compile(" (/.*?) ");

    private static final Pattern REQUESTURL          = Pattern.compile("(/.*?)($|\\?)");

    private static final Pattern REQUESTPARAM        = Pattern.compile("\\?(.+)");

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

    protected GetRequest buildGetRequest() throws IOException {
        return new GetRequest(this);
    }

    protected HeadRequest buildHeadRequest() throws IOException {
        return new HeadRequest(this);
    }

    protected OptionsRequest buildOptionsRequest() throws IOException {
        return new OptionsRequest(this);
    }

    protected PostRequest buildPostRequest() throws IOException {
        return new PostRequest(this);
    }

    /**
     * parses the request and creates a GET/POST-Request Object and fills it
     * with all received data
     * 
     * @return
     * @throws IOException
     */
    protected HttpRequest buildRequest() throws IOException {
        /* read request Method and Path */
        final String requestLine = this.parseRequestLine();
        if (StringUtils.isEmpty(requestLine)) { throw new IOException("Empty RequestLine"); }
        // TOTO: requestLine may be "" in some cases (chrome pre connection...?)
        final HttpConnectionType connectionType = this.parseConnectionType(requestLine);
        final String requestedURL = new Regex(requestLine, HttpConnection.REQUESTLINE).getMatch(0);
        final String requestedPath = new Regex(requestedURL, HttpConnection.REQUESTURL).getMatch(0);
        final List<KeyValuePair> requestedURLParameters = this.parseRequestURLParams(requestedURL);
        /* read request Headers */
        final HeaderCollection requestHeaders = this.parseRequestHeaders();
        final HttpRequest request;
        switch (connectionType) {
        case POST:
            request = this.buildPostRequest();
            break;
        case GET:
            request = this.buildGetRequest();
            break;
        case OPTIONS:
            request = this.buildOptionsRequest();
            break;
        case HEAD:
            request = this.buildHeadRequest();
            break;
        default:
            throw new IOException("Unsupported " + requestLine);
        }
        /* parse remoteClientAddresses */
        request.setRemoteAddress(this.getRemoteAddress(requestHeaders));
        request.setRequestedURLParameters(requestedURLParameters);
        request.setRequestedPath(requestedPath);
        request.setRequestedURL(requestedURL);
        request.setRequestHeaders(requestHeaders);
        return request;
    }

    /**
     * @return
     */
    protected HttpResponse buildResponse() throws IOException {
        return new HttpResponse(this);
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

    protected boolean deferRequest(final HttpRequest request) throws Exception {
        return false;
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
        return this.getRawInputStream();
    }

    /**
     * return the outputStream for this connection. send response headers if
     * they have not been sent yet send yet
     * 
     * @return
     * @throws IOException
     */
    public OutputStream getOutputStream(final boolean sendResponseHeaders) throws IOException {
        if (sendResponseHeaders) {
            this.sendResponseHeaders();
        }
        return this.getRawOutputStream();
    }

    protected InputStream getRawInputStream() throws IOException {
        if (this.is == null) { throw new IllegalStateException("no RawInputStream available!"); }
        return this.is;
    }

    protected OutputStream getRawOutputStream() throws IOException {
        if (this.os == null) { throw new IllegalStateException("no RawOutputStream available!"); }
        return this.os;
    }

    protected List<String> getRemoteAddress(final HeaderCollection requestHeaders) {
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
        return remoteAddress;
    }

    public HttpRequest getRequest() {
        return this.request;
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    public boolean isResponseHeadersSent() {
        return this.responseHeadersSent;
    }

    public boolean onException(final Throwable e, final HttpRequest request, final HttpResponse response) throws IOException {
        if (e instanceof HttpConnectionExceptionHandler) { return ((HttpConnectionExceptionHandler) e).handle(response); }
        this.response = new HttpResponse(this);
        e.printStackTrace();
        this.response.setResponseCode(ResponseCode.SERVERERROR_INTERNAL);
        final byte[] bytes = Exceptions.getStackTrace(e).getBytes("UTF-8");
        this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_TYPE, "text; charset=UTF-8"));
        this.response.getResponseHeaders().add(new HTTPHeader(HTTPConstants.HEADER_REQUEST_CONTENT_LENGTH, bytes.length + ""));
        this.response.getOutputStream(true).write(bytes);
        this.response.getOutputStream(true).flush();
        return true;
    }

    protected void onUnhandled(final HttpRequest request, final HttpResponse response) throws IOException {
        response.setResponseCode(ResponseCode.SERVERERROR_NOT_IMPLEMENTED);
    }

    protected HttpConnectionType parseConnectionType(final String requestLine) throws IOException {

        final String method = new Regex(requestLine, HttpConnection.METHOD).getMatch(0);
        // TOTO: requestLine may be "" in some cases (chrome pre connection...?)
        try {
            return HttpConnectionType.valueOf(method);
        } catch (final Exception e) {
            return HttpConnectionType.UNKNOWN;
        }
    }

    protected HeaderCollection parseRequestHeaders() throws IOException {
        final ByteBuffer headers = this.readRequestHeaders();
        final String[] headerStrings;
        if (headers.hasArray()) {
            headerStrings = new String(headers.array(), headers.arrayOffset(), headers.limit(), "ISO-8859-1").split("(\r\n)|(\n)");
        } else {
            final byte[] bytesHeaders = new byte[headers.limit()];
            headers.get(bytesHeaders);
            headerStrings = new String(bytesHeaders, "ISO-8859-1").split("(\r\n)|(\n)");
        }
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
        return requestHeaders;
    }

    protected String parseRequestLine() throws IOException {
        final ByteBuffer header = this.readRequestLine();
        if (header.hasArray()) {
            return this.preProcessRequestLine(new String(header.array(), header.arrayOffset(), header.limit(), "ISO-8859-1").trim());
        } else {
            final byte[] bytesRequestLine = new byte[header.limit()];
            header.get(bytesRequestLine);
            return this.preProcessRequestLine(new String(bytesRequestLine, "ISO-8859-1").trim());
        }
    }

    protected List<KeyValuePair> parseRequestURLParams(final String requestURL) throws IOException {
        return HttpConnection.parseParameterList(new Regex(requestURL, HttpConnection.REQUESTPARAM).getMatch(0));
    }

    protected String preProcessRequestLine(final String requestLine) throws IOException {
        return requestLine;
    }

    protected ByteBuffer readRequestHeaders() throws IOException {
        return HTTPConnectionUtils.readheader(this.getInputStream(), false);
    }

    protected ByteBuffer readRequestLine() throws IOException {
        return HTTPConnectionUtils.readheader(this.getInputStream(), true);
    }

    @Override
    public void run() {
        boolean closeConnection = true;
        try {
            if (this.request == null) {
                this.request = this.buildRequest();
            }
            if (this.response == null) {
                this.response = this.buildResponse();
            }
            if (this.deferRequest(this.request)) {
                closeConnection = false;
            } else {
                boolean handled = false;
                if (this.request instanceof PostRequest) {
                    for (final HttpRequestHandler handler : this.getHandler()) {
                        if (handler.onPostRequest((PostRequest) this.request, this.response)) {
                            handled = true;
                            break;
                        }
                    }
                } else if (this.request instanceof GetRequest) {
                    for (final HttpRequestHandler handler : this.getHandler()) {
                        if (handler.onGetRequest((GetRequest) this.request, this.response)) {
                            handled = true;
                            break;
                        }
                    }
                }
                if (!handled) {
                    /* generate error handler */
                    this.onUnhandled(this.request, this.response);
                }
                /* send response headers if they have not been sent yet send yet */
                this.response.getOutputStream(true);
            }
        } catch (final Throwable e) {
            try {
                closeConnection = this.onException(e, this.request, this.response);
            } catch (final Throwable nothing) {
                nothing.printStackTrace();
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
    protected void sendResponseHeaders() throws IOException {
        try {
            if (this.isResponseHeadersSent()) {
                //
                throw new IOException("Headers already send!");
            }
            if (this.response != null) {
                final OutputStream out = this.getRawOutputStream();
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
            this.setResponseHeadersSent(true);
        }
    }

    protected void setResponseHeadersSent(final boolean responseHeadersSent) {
        this.responseHeadersSent = responseHeadersSent;
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
