package jd.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jd.http.requests.PostFormDataRequest;
import jd.http.requests.PostRequest;

import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.NativeHTTPConnectionImpl;

public class URLConnectionAdapterNative extends NativeHTTPConnectionImpl implements URLConnectionAdapter {

    private Request request;

    public URLConnectionAdapterNative(final URL url) {
        super(url);
    }

    public URLConnectionAdapterNative(final URL url, final HTTPProxy p) {
        super(url, p);
        // TODO Auto-generated constructor stub
    }

    @Override
    public InputStream getErrorStream() {
        try {
            return super.getInputStream();
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public long getLongContentLength() {
        return this.getContentLength();
    }

    @Override
    public Request getRequest() {
        return this.request;
    }

    @Override
    public void setRequest(final Request request) {
        this.request = request;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(300);
        sb.append(this.getRequestInfo());
        final Request req = this.getRequest();
        if (req != null) {
            if (req instanceof PostRequest) {
                final String log = ((PostRequest) req).log();
                if (log != null) {
                    sb.append(log);
                }
            } else if (req instanceof PostFormDataRequest) {
                final String postDataString = ((PostFormDataRequest) req).getPostDataString();
                if (postDataString != null) {
                    sb.append(postDataString);
                }
            }
            sb.append(URLConnectionAdapter.CRLF);
        }
        sb.append(this.getResponseInfo());
        return sb.toString();
    }

}
