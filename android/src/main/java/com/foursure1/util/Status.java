package com.foursure1.util;

import java.io.Serializable;

/**
 * For devices and users:
 * - NONE implies created but not saved to the db
 * - PENDING implies saved to the db but never registered (i.e. never completed initial LE reg session)
 * - ACTIVE implies successfully registered
 * - INACTIVE implies administrative cancellation
 * 
 * For FourSure sessions:
 * - NONE implies created but not yet populated or saved to memcache
 * - PENDING implies populated and in memcache, but LE has not be started
 * - LE_STARTED implies LE session started by FourSure but no challenge(s) added
 * - LE_CHALLENGES_ADDED implies all appropriate challenges have been successfully added
 * - LE_AGENT_SEEN implies that polling is active and the agent has checked in
 * - LE_AUTH/LE_FAIL implies the final LE disposition is known
 * - SHARE_COMPLETE/FAILED/CANCELED implies the final FS disposition for a created share is known
 * - ACCESS_COMPLETE/FAILED/CANCELED implies the final FS disposition for an access is known
 *   + ("canceled" implies the user quit the mobile app before completing any flow)
 *   
 * @author aapel
 *
 */
public enum Status implements Serializable {
	NONE,
	PENDING,
	ACTIVE,
	INACTIVE,
	
	LE_STARTED,
	LE_CHALLENGES_ADDED,
	LE_AGENT_SEEN,
	LE_AUTHENTICATED,
	LE_FAILED,
	
	SHARE_COMPLETE,
	SHARE_FAILED,
	SHARE_CANCELED,
	
	ACCESS_COMPLETE,
	ACCESS_FAILED,
	ACCESS_CANCELED
	;
}
