package jd.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import jd.http.requests.PostFormDataRequest;
import jd.http.requests.PostRequest;

import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.Socks4HTTPConnectionImpl;

public class URLConnectionAdapterSocks4Impl extends Socks4HTTPConnectionImpl implements URLConnectionAdapter {

    private Request request;

    public URLConnectionAdapterSocks4Impl(final URL url, final HTTPProxy proxy) {
        super(url, proxy);
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
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getRequestInfo());

        if (this.getRequest() != null) {
            if (this.getRequest() instanceof PostRequest) {
                if (((PostRequest) this.getRequest()).log() != null) {
                    sb.append(((PostRequest) this.getRequest()).log());
                }
                sb.append(new char[] { '\r', '\n' });
            } else if (this.getRequest() instanceof PostFormDataRequest) {
                if (((PostFormDataRequest) this.getRequest()).getPostDataString() != null) {
                    sb.append(((PostFormDataRequest) this.getRequest()).getPostDataString());
                }
                sb.append(new char[] { '\r', '\n' });
            }
        }

        sb.append(this.getResponseInfo());

        return sb.toString();
    }
}
