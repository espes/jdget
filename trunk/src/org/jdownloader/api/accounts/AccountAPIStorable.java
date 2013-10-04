package org.jdownloader.api.accounts;

import jd.plugins.Account;

import org.appwork.remoteapi.QueryResponseMap;
import org.appwork.storage.Storable;

public class AccountAPIStorable implements Storable {

    private Account          acc;
    private QueryResponseMap infoMap = null;

    public long getUUID() {
        return acc.getId().getID();
    }

    public String getHostname() {
        return acc.getHoster();
    }

    public QueryResponseMap getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(QueryResponseMap infoMap) {
        this.infoMap = infoMap;
    }

    @SuppressWarnings("unused")
    private AccountAPIStorable(/* Storable */) {
    }

    public AccountAPIStorable(Account acc) {
        this.acc = acc;
    }

}
