/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.appwork.utils.net.HeaderCollection;
import org.appwork.utils.net.httpserver.requests.GetRequest;
import org.appwork.utils.net.httpserver.requests.HeadRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequestInterface;
import org.appwork.utils.net.httpserver.requests.KeyValuePair;
import org.appwork.utils.net.httpserver.requests.OptionsRequest;
import org.appwork.utils.net.httpserver.requests.PostRequest;

/**
 * @author daniel
 * 
 */
public class RemoteAPIRequest implements HttpRequestInterface {

    public static enum REQUESTTYPE {
        HEAD,
        POST,
        OPTIONS,
        GET,
        UNKNOWN
    }

    private final InterfaceHandler<?> iface;

    private final String[]            parameters;
    protected final HttpRequest         request;

    private Method                    method;

    private int                       parameterCount;

    private final String              jqueryCallback;

    private final String              methodName;





    public RemoteAPIRequest(final InterfaceHandler<?> iface, final String methodName, final String[] parameters, final HttpRequest request, final String jqueryCallback) {
        this.iface = iface;
        this.parameters = parameters;
        this.request = request;
        this.methodName = methodName;
  
        this.jqueryCallback = jqueryCallback;
        method = this.iface.getMethod(methodName, this.parameters.length);
        try {
            parameterCount = iface.getParameterCount(method);
        } catch (final Throwable e) {
            method = null;
        }
    }

    public HttpRequest getHttpRequest() {
        return request;
    }

    public InterfaceHandler<?> getIface() {
        return iface;
    }

    public InputStream getInputStream() throws IOException {
        if (request instanceof PostRequest) { return ((PostRequest) request).getInputStream(); }
        return null;
    }

    /**
     * @return the jqueryCallback
     */
    public String getJqueryCallback() {
        return jqueryCallback;
    }

    /**
     * @return
     */
    public Method getMethod() {

        return method;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public String[] getParameters() {
        return parameters;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#
//     * getPostParameter()
//     */
//    @Override
//    public List<KeyValuePair> getPostParameter() throws IOException {
//        return request.getPostParameter();
//    }

    /**
     * @return
     */
    public List<String> getRemoteAdress() {
        return request.getRemoteAddress();
    }

    public String getRequestedPath() {
        return request.getRequestedPath();
    }

    public String getRequestedURL() {
        return request.getRequestedURL();
    }

    /**
     * @return the requestedURLParameters
     */
    public List<KeyValuePair> getRequestedURLParameters() {
        return request.getRequestedURLParameters();
    }

    public HeaderCollection getRequestHeaders() {
        return request.getRequestHeaders();
    }



    public REQUESTTYPE getRequestType() {
        if (request instanceof OptionsRequest) { return REQUESTTYPE.OPTIONS; }
        if (request instanceof HeadRequest) { return REQUESTTYPE.HEAD; }
        if (request instanceof PostRequest) { return REQUESTTYPE.POST; }
        if (request instanceof GetRequest) { return REQUESTTYPE.GET; }
        return REQUESTTYPE.UNKNOWN;
    }

    /**
     * @return
     */
    public String getSignature() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return
     */
    public long getRequestID() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.appwork.utils.net.httpserver.requests.HttpRequestInterface#getParameterbyKey(java.lang.String)
     */
    @Override
    public String getParameterbyKey(final String key) throws IOException {
        
            List<KeyValuePair> params = request.getRequestedURLParameters();
            if (params != null) {
                for (final KeyValuePair param : params) {
                    if (key.equalsIgnoreCase(param.key)) { return param.value; }
                }
            }
            if (request instanceof PostRequest) {
                params = ((PostRequest) request).getPostParameter();
                if (params != null) {
                    for (final KeyValuePair param : params) {
                        if (key.equalsIgnoreCase(param.key)) { return param.value; }
                    }
                }
            }
            
            return null;
        
      
    }


}
