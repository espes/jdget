package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface;
import org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AccountInterfaceAsync {

	void addAccount(String premiumHoster, String username, String password,
			AsyncCallback<Boolean> callback);

	void disableAccounts(long[] ids, AsyncCallback<Void> callback);

	void enableAccounts(long[] ids, AsyncCallback<Void> callback);

	void getPremiumHosterUrl(String hoster, AsyncCallback<String> callback);

	void listAccounts(AccountQueryInterface query,
			AsyncCallback<ArrayList<AccountStorableInterface>> callback);

	void listPremiumHoster(AsyncCallback<ArrayList<String>> callback);

	void listPremiumHosterUrls(AsyncCallback<HashMap<String, String>> callback);

	void removeAccounts(long[] ids, AsyncCallback<Void> callback);

	void setUserNameAndPassword(long accountId, String username,
			String password, AsyncCallback<Boolean> callback);

}
