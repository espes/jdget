package jd.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jd.http.requests.PostFormDataRequest;
import jd.http.requests.PostRequest;

import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.HTTPProxyHTTPConnectionImpl;

public class URLConnectionAdapterHTTPProxyImpl extends HTTPProxyHTTPConnectionImpl implements URLConnectionAdapter {

    /** Carriage return + Line Feed */
    private static final String CRLF = "\r\n";

    /** The request. */
    private Request             request;

    /**
     * constructor
     * 
     * @param url
     *            the {@link URL}
     * @param proxy
     *            the {@link HTTPProxy}
     */
    public URLConnectionAdapterHTTPProxyImpl(final URL url, final HTTPProxy proxy) {
        super(url, proxy);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getErrorStream() {
        try {
            return super.getInputStream();
        } catch (final IOException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getLongContentLength() {
        return this.getContentLength();
    }

    /** {@inheritDoc} */
    @Override
    public Request getRequest() {
        return this.request;
    }

    /** {@inheritDoc} */
    @Override
    public void setRequest(final Request request) {
        this.request = request;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(300);
        sb.append(this.getRequestInfo());
        Request req = this.getRequest();
        if (req != null) {
            if (req instanceof PostRequest) {
                String log = ((PostRequest) req).log();
                if (log != null) {
                    sb.append(log);
                }
            } else if (req instanceof PostFormDataRequest) {
                String postDataString = ((PostFormDataRequest) req).getPostDataString();
                if (postDataString != null) {
                    sb.append(postDataString);
                }
            }
            sb.append(CRLF);
        }
        sb.append(this.getResponseInfo());
        return sb.toString();
    }
}
