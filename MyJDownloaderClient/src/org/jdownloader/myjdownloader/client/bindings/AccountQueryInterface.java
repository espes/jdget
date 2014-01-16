package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashSet;

public interface AccountQueryInterface {

	public abstract HashSet<Long> getUUIDList();

	public abstract void setUUIDList(HashSet<Long> ids);

	public abstract boolean isUserName();

	public abstract void setUserName(boolean userName);

	public abstract boolean isValidUntil();

	public abstract void setValidUntil(boolean validUntil);

	public abstract boolean isTrafficLeft();

	public abstract void setTrafficLeft(boolean trafficLeft);

	public abstract boolean isTrafficMax();

	public abstract void setTrafficMax(boolean trafficMax);

	public abstract boolean isEnabled();

	public abstract void setEnabled(boolean enabled);

	public abstract boolean isValid();

	public abstract void setValid(boolean valid);

	public abstract int getStartAt();

	public abstract void setStartAt(int startAt);

	public abstract void setMaxResults(int maxResults);

	public abstract int getMaxResults();

	public abstract boolean isError();

	public abstract void setError(boolean error);

}