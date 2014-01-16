package org.jdownloader.myjdownloader.client.bindings;

public interface AccountStorableInterface extends Storable {

	public abstract String getErrorType();

	public abstract void setErrorType(String errorType);

	public abstract String getErrorString();

	public abstract void setErrorString(String errorString);

	public abstract String getHostname();

	public abstract long getTrafficLeft();

	public abstract long getTrafficMax();

	public abstract String getUsername();

	public abstract long getUUID();

	public abstract long getValidUntil();

	public abstract boolean isEnabled();

	public abstract boolean isValid();

	public abstract void setEnabled(boolean enabled);

	public abstract void setHostname(String hostname);

	public abstract void setTrafficLeft(long trafficLeft);

	public abstract void setTrafficMax(long trafficMax);

	public abstract void setUsername(String username);

	public abstract void setUUID(long uUID);

	public abstract void setValid(boolean valid);

	public abstract void setValidUntil(long validUntil);

}