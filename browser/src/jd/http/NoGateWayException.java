package jd.http;

import java.io.IOException;

public class NoGateWayException extends IOException {

    private ProxySelectorInterface selector;

    public ProxySelectorInterface getSelector() {
        return selector;
    }

    public NoGateWayException(ProxySelectorInterface selector, String string) {
        super(string);
        this.selector = selector;
    }

}
