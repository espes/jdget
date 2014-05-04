package org.jdownloader.api;

import java.io.IOException;

import org.appwork.remoteapi.InterfaceHandler;
import org.appwork.remoteapi.RemoteAPIRequest;
import org.appwork.utils.net.httpserver.requests.HttpRequest;
import org.jdownloader.api.myjdownloader.MyJDownloaderRequestInterface;

public class MyJDRemoteAPIRequest extends RemoteAPIRequest {

    public MyJDRemoteAPIRequest(InterfaceHandler<?> iface, String methodName, String[] parameters, MyJDownloaderRequestInterface request) throws IOException {
        super(iface, methodName, parameters, (HttpRequest) request, request.getJqueryCallback());
    }

    public MyJDownloaderRequestInterface getRequest() {
        return (MyJDownloaderRequestInterface) super.getHttpRequest();
    }
}
