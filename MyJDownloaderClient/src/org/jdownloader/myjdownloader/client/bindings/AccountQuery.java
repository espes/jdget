package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashSet;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountQuery extends AbstractJsonData implements
		AccountQueryInterface {

	public AccountQuery(/* storable */) {

	}

	/**
	 * @param startAt
	 * @param maxResults
	 * @param userName
	 * @param validUntil
	 * @param trafficLeft
	 * @param trafficMax
	 * @param enabled
	 * @param valid
	 */
	public AccountQuery(final int startAt, final int maxResults,
			final boolean userName, final boolean validUntil,
			final boolean trafficLeft, final boolean trafficMax,
			final boolean enabled, final boolean valid) {
		super();
		this.startAt = startAt;
		this.maxResults = maxResults;
		this.userName = userName;
		this.validUntil = validUntil;
		this.trafficLeft = trafficLeft;
		this.trafficMax = trafficMax;
		this.enabled = enabled;
		this.valid = valid;
	}

	private boolean userName = false;
	private boolean validUntil = false;
	private boolean trafficLeft = false;
	private boolean error = false;
	private boolean trafficMax = false;
	/**
	 * only return these ids. if null all ids will be returned
	 */
	private HashSet<Long> UUIDList = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * getUUIDList()
	 */
	@Override
	public HashSet<Long> getUUIDList() {
		return UUIDList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setUUIDList(java.util.HashSet)
	 */
	@Override
	public void setUUIDList(final HashSet<Long> ids) {
		this.UUIDList = ids;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * isUserName()
	 */
	@Override
	public boolean isUserName() {
		return userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setUserName(boolean)
	 */
	@Override
	public void setUserName(final boolean userName) {
		this.userName = userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * isValidUntil()
	 */
	@Override
	public boolean isValidUntil() {
		return validUntil;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setValidUntil(boolean)
	 */
	@Override
	public void setValidUntil(final boolean validUntil) {
		this.validUntil = validUntil;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * isTrafficLeft()
	 */
	@Override
	public boolean isTrafficLeft() {
		return trafficLeft;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setTrafficLeft(boolean)
	 */
	@Override
	public void setTrafficLeft(final boolean trafficLeft) {
		this.trafficLeft = trafficLeft;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * isTrafficMax()
	 */
	@Override
	public boolean isTrafficMax() {
		return trafficMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setTrafficMax(boolean)
	 */
	@Override
	public void setTrafficMax(final boolean trafficMax) {
		this.trafficMax = trafficMax;
	}

	private boolean enabled = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#isEnabled
	 * ()
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setEnabled(boolean)
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#isValid
	 * ()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#setValid
	 * (boolean)
	 */
	@Override
	public void setValid(final boolean valid) {
		this.valid = valid;
	}

	private boolean valid = false;

	private int startAt = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * getStartAt()
	 */
	@Override
	public int getStartAt() {
		return startAt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setStartAt(int)
	 */
	@Override
	public void setStartAt(final int startAt) {
		this.startAt = startAt;
	}

	private int maxResults = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * setMaxResults(int)
	 */
	@Override
	public void setMaxResults(final int maxResults) {
		this.maxResults = maxResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#
	 * getMaxResults()
	 */
	@Override
	public int getMaxResults() {
		return maxResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#isError
	 * ()
	 */
	@Override
	public boolean isError() {
		return error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jdownloader.myjdownloader.client.bindings.AccountQueryInterface#setError
	 * (boolean)
	 */
	@Override
	public void setError(final boolean error) {
		this.error = error;
	}

}