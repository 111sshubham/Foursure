package com.foursure1.util;

import java.io.Serializable;

/**
 * 
 * @author aapel
 *
 *         Known LiveEnsure auth factor types
 *
 */
public enum FactorType implements Serializable {
	NONE,
	DEVICE,
	BEHAVIOR,
	WEARABLE,
	LOCATION,
	KNOWLEDGE,
	PIN,
	TIME;
}
