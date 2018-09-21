package com.foursure1.api;

import com.foursure1.util.FactorType;
import com.foursure1.util.ShareType;

public class AccessShareResponse extends LiveResponse {

	String shortCode;
	ShareType shareType;
	String shareContent;
	boolean author;
	private FactorType shareFactorType;
	private boolean trackingActive;
	private ShareType shieldType;

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public ShareType getShareType() {
		return shareType;
	}

	public void setShareType(ShareType type) {
		this.shareType = type;
	}

	public String getShareContent() {
		return shareContent;
	}

	public void setShareContent(String content) {
		this.shareContent = content;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	public boolean isTrackingActive() {
		return trackingActive;
	}

	public void setTrackingActive(boolean trackingActive) {
		this.trackingActive = trackingActive;
	}

	public FactorType getShareFactorType() {
		return shareFactorType;
	}

	public void setShareFactorType(FactorType shareFactorType) {
		this.shareFactorType = shareFactorType;
	}

	public ShareType getShieldType() {
		return shieldType;
	}

	public void setShieldType(ShareType shieldType) {
		this.shieldType = shieldType;
	}

}
