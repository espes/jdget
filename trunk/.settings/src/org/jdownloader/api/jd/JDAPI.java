package org.jdownloader.api.jd;

import org.appwork.remoteapi.ApiNamespace;
import org.appwork.remoteapi.RemoteAPIInterface;

@ApiNamespace("jd")
public interface JDAPI extends RemoteAPIInterface {

    public long uptime();

    public long version();

    public String log();
}
