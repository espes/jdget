package jd.controlling.downloadcontroller;

import jd.plugins.Account;
import jd.plugins.DownloadLink;

public abstract interface SingleDownloadControllerHandler {

    /**
     * returns false if SingleDownloadController should proceed with handling
     * this Link after plugin is done
     * 
     * @param link
     * @return
     */
    public abstract boolean handleDownloadLink(DownloadLink link, Account acc);

}
