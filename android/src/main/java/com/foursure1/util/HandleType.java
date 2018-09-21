package com.foursure1.util;

import java.io.Serializable;

/**
 * 
 * @author aapel
 *
 *         known FourSure user handle types
 */
public enum HandleType implements Serializable {
	UNKNOWN,
	EMAIL,
	FACEBOOK,
	TWITTER,
	SMS,
	LINKEDIN,
	GOOGLEPLUS;
}
