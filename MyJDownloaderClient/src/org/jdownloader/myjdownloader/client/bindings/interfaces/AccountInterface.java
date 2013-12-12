package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import org.jdownloader.myjdownloader.client.bindings.AccountQuery;
import org.jdownloader.myjdownloader.client.bindings.AccountStorable;
import org.jdownloader.myjdownloader.client.bindings.ApiNamespace;

@ApiNamespace(AccountInterface.NAMESPACE)
public interface AccountInterface extends Linkable{
    public static final String NAMESPACE = "accountsV2";

    public boolean addAccount(String premiumHoster, String username, String password);

    public ArrayList<AccountStorable> listAccounts(AccountQuery query);

    public ArrayList<String> listPremiumHoster();

    public AccountStorable getAccountInfo(long id);

    public boolean removeAccounts(long[] ids);

    public boolean enableAccounts(long[] ids);

    public boolean disableAccounts(long[] ids);

    HashMap<String, String> listPremiumHosterUrls();

    boolean setUserNameAndPassword(long accountId, String username, String password);

    String getPremiumHosterUrl(String hoster);
}
