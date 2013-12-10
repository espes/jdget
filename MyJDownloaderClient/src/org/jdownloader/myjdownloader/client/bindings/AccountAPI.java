package org.jdownloader.myjdownloader.client.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ApiNamespace(AccountAPI.ACCOUNTS)
public interface AccountAPI {
    public static final String ACCOUNTS = "accounts";

    public boolean addAccount(String premiumHoster, String username, String password);

    public ArrayList<AccountStorable> queryAccounts(AccountQuery query);

    public ArrayList<String> listPremiumHoster();

    public AccountStorable getAccountInfo(long id);

    public boolean removeAccounts(long[] ids);

    public boolean enableAccounts(List<Long> ids);

    public boolean disableAccounts(List<Long> ids);

    public boolean setEnabledState(boolean enabled, long[] ids);

    byte[] premiumHosterIcon(String premiumHoster);

    HashMap<String, String> listPremiumHosterUrls();

    boolean updateAccount(long accountId, String username, String password);

    String getPremiumHosterUrl(String hoster);
}
