package jd.http.requests;

import java.io.IOException;
import java.net.MalformedURLException;

import jd.http.Request;
import jd.parser.html.Form;

import org.appwork.utils.net.httpconnection.HTTPConnection.RequestMethod;

public class PutRequest extends PostRequest {
    
    public PutRequest(final Form form) throws MalformedURLException {
        super(form);
    }
    
    public PutRequest(final Request cloneRequest) {
        super(cloneRequest);
    }
    
    public PutRequest(final String url) throws MalformedURLException {
        super(url);
    }
    
    @Override
    public PutRequest cloneRequest() {
        return new PutRequest(this);
    }
    
    @Override
    public void preRequest() throws IOException {
        super.preRequest();
        this.httpConnection.setRequestMethod(RequestMethod.PUT);
    }
    
}
