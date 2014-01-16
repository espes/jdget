package org.jdownloader.myjdownloader.client.bindings.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface;
import org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface;
import org.jdownloader.myjdownloader.client.bindings.ClientApiNameSpace;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@ClientApiNameSpace(AccountInterface.NAMESPACE)
@RemoteServiceRelativePath(AccountInterface.NAMESPACE)
public interface AccountInterface extends Linkable {
	public static final String NAMESPACE = "accountsV2";

	public boolean addAccount(String premiumHoster, String username,
			String password);

	public ArrayList<AccountStorableInterface> listAccounts(AccountQueryInterface query);

	public ArrayList<String> listPremiumHoster();

	public void removeAccounts(long[] ids);

	public void enableAccounts(long[] ids);

	public void disableAccounts(long[] ids);

	HashMap<String, String> listPremiumHosterUrls();

	boolean setUserNameAndPassword(long accountId, String username,
			String password);

	String getPremiumHosterUrl(String hoster);
}
