package org.jdownloader.myjdownloader.client.bindings;

import java.util.HashSet;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class AccountQuery extends AbstractJsonData {

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

	public HashSet<Long> getUUIDList() {
		return UUIDList;
	}

	public void setUUIDList(final HashSet<Long> ids) {
		this.UUIDList = ids;
	}

	public boolean isUserName() {
		return userName;
	}

	public void setUserName(final boolean userName) {
		this.userName = userName;
	}

	public boolean isValidUntil() {
		return validUntil;
	}

	public void setValidUntil(final boolean validUntil) {
		this.validUntil = validUntil;
	}

	public boolean isTrafficLeft() {
		return trafficLeft;
	}

	public void setTrafficLeft(final boolean trafficLeft) {
		this.trafficLeft = trafficLeft;
	}

	public boolean isTrafficMax() {
		return trafficMax;
	}

	public void setTrafficMax(final boolean trafficMax) {
		this.trafficMax = trafficMax;
	}

	private boolean enabled = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(final boolean valid) {
		this.valid = valid;
	}

	private boolean valid = false;

	private int startAt = 0;

	public int getStartAt() {
		return startAt;
	}

	public void setStartAt(final int startAt) {
		this.startAt = startAt;
	}

	private int maxResults = -1;

	public void setMaxResults(final int maxResults) {
		this.maxResults = maxResults;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public boolean isError() {
		return error;
	}

	public void setError(final boolean error) {
		this.error = error;
	}

}