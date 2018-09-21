package com.liveensure.util;

/**
 * Safe interface for hashing objects of unknown type.
 */
public interface Hashable {
    /**
     * Gets the appropriate string to hash from an object.
     * 
     * @return String that's hashable
     * @see MessageDigester
     */
    public String getHashable();
}
