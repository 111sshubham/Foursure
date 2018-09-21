package com.liveensure.core;

public enum DynamicChallengeType {
	NONE,
	PROMPT,
	TEMPLATE,
	PIN,
	LAT_LONG,
	HOME,
	UNKNOWN;
	// N.B. - always add any new challenge types BEFORE the "UNKNOWN" type - that should always be the last one
}
