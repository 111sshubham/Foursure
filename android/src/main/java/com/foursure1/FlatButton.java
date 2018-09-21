package com.foursure1;

import com.foursure1.util.FactorType;

import android.widget.ImageView;

public class FlatButton {
	private ImageView view;
	private String name;
	private FactorType factorType;
	private int inactiveDrawableID;
	private int activeDrawableID;
	private boolean active;

	public FlatButton(ImageView v, String n, int idid, int adid) {
		view = v;
		name = n;
		inactiveDrawableID = idid;
		activeDrawableID = adid;
	}

	public FlatButton(ImageView v, String n, FactorType f, int idid, int adid) {
		view = v;
		name = n;
		factorType = f;
		inactiveDrawableID = idid;
		activeDrawableID = adid;
	}

	public ImageView getView() {
		return view;
	}

	public void setView(ImageView view) {
		this.view = view;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInactiveDrawableID() {
		return inactiveDrawableID;
	}

	public void setInactiveDrawableID(int inactiveDrawableID) {
		this.inactiveDrawableID = inactiveDrawableID;
	}

	public int getActiveDrawableID() {
		return activeDrawableID;
	}

	public void setActiveDrawableID(int activeDrawableID) {
		this.activeDrawableID = activeDrawableID;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public FactorType getFactorType() {
		return factorType;
	}

	public void setFactorType(FactorType factorType) {
		this.factorType = factorType;
	}

}
