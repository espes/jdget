package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountStorable extends AbstractJsonData implements AccountStorableInterface {

    private long    UUID;
    private boolean enabled;

    private boolean valid;

    private String  hostname;

    private String  username;
    private String  errorType;

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getErrorType()
	 */
    @Override
	public String getErrorType() {
        return errorType;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setErrorType(java.lang.String)
	 */
    @Override
	public void setErrorType(final String errorType) {
        this.errorType = errorType;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getErrorString()
	 */
    @Override
	public String getErrorString() {
        return errorString;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setErrorString(java.lang.String)
	 */
    @Override
	public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    private String errorString;
    private long   validUntil  = -1l;

    private long   trafficLeft = -1l;
    private long   trafficMax  = -1l;

    @SuppressWarnings("unused")
    protected AccountStorable(/* Storable */) {
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getHostname()
	 */
    @Override
	public String getHostname() {
        return hostname;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getTrafficLeft()
	 */
    @Override
	public long getTrafficLeft() {
        return trafficLeft;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getTrafficMax()
	 */
    @Override
	public long getTrafficMax() {
        return trafficMax;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getUsername()
	 */
    @Override
	public String getUsername() {
        return username;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getUUID()
	 */
    @Override
	public long getUUID() {
        return UUID;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#getValidUntil()
	 */
    @Override
	public long getValidUntil() {
        return validUntil;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#isEnabled()
	 */
    @Override
	public boolean isEnabled() {
        return enabled;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#isValid()
	 */
    @Override
	public boolean isValid() {
        return valid;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setEnabled(boolean)
	 */
    @Override
	public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setHostname(java.lang.String)
	 */
    @Override
	public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setTrafficLeft(long)
	 */
    @Override
	public void setTrafficLeft(final long trafficLeft) {
        this.trafficLeft = trafficLeft;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setTrafficMax(long)
	 */
    @Override
	public void setTrafficMax(final long trafficMax) {
        this.trafficMax = trafficMax;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setUsername(java.lang.String)
	 */
    @Override
	public void setUsername(final String username) {
        this.username = username;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setUUID(long)
	 */
    @Override
	public void setUUID(final long uUID) {
        UUID = uUID;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setValid(boolean)
	 */
    @Override
	public void setValid(final boolean valid) {
        this.valid = valid;
    }

    /* (non-Javadoc)
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountStorableInterface#setValidUntil(long)
	 */
    @Override
	public void setValidUntil(final long validUntil) {
        this.validUntil = validUntil;
    }

}
