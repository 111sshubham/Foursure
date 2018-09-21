package com.foursure1.util;

import java.io.Serializable;

/**
 * 
 * @author aapel
 *
 *         known FourSure share types
 */
public enum ShareType implements Serializable {
	NONE,
	URL,
	TEXT,
	PING,
	UNKNOWN;
}
