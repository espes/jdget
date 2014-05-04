package org.jdownloader.myjdownloader.client.json;

import java.util.List;

public class DirectConnectionInfos {
    private List<DirectConnectionInfo> infos = null;
    
    public DirectConnectionInfos(/* Storable */) {
    }
    
    public List<DirectConnectionInfo> getInfos() {
        return this.infos;
    }
    
    public void setInfos(final List<DirectConnectionInfo> infos) {
        this.infos = infos;
    }
}
