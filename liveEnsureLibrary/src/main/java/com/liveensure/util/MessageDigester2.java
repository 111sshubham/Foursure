/*
 * Created on February 8, 2005 by Angel Dobbs-Sciortino
 * $LastChangedBy: angelic $
 * $LastChangedDate: 2006-01-17 18:11:31 -0700 (Tue, 17 Jan 2006) $
 * $LastChangedRevision: 437 $
 */
package com.liveensure.util;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;

/**
 * Offers static MessageDigest methods.
 * 
 * @author Angel Dobbs-Sciortino
 */
public class MessageDigester2 {
    private static final SecureRandom RAND = new SecureRandom();

    /**
     * Performs a hash on the byte array.
     * 
     * @param input
     *            the byte array to hash
     * @return the hash value
     */
    public static String getHash(byte[] input) {
        return SHA1.encodeHex(input);
    }

    /**
     * Performs a hash on the String.
     * 
     * @param input
     *            the String to hash
     * @return the hash value
     */
    public static String getHash(String input) {
        return SHA1.encodeHex(input);
    }

    /**
     * Performs a hash on the String.
     * 
     * @param input
     *            the String to hash
     * @return the hash value
     */
    public static String getHash(Hashable input) {
        return SHA1.encodeHex(input.getHashable());
    }

    /**
     * Iterates through an array of Hashable objects, performing a hash of
     * the concatenated String.
     */
    public static String getHash(Hashable[] objects) {
        StringBuffer input = new StringBuffer();
        if( objects != null ) {
            for( int i=0; i < objects.length; ++i ) {
                input.append(objects[i].getHashable());
            }
        }
        return getHash(input.toString());
    }

    /**
     * Iterates through the collection and concatenates the getHashable() methods
     * of the Hashable objects. Performs a hash of the concatenated String.
     * 
     * @param objects the Collection of Hashable objects to hash
     * @return the hash value
     * @see Hashable
     */
    public static String getHash(List<?> objects) {
        StringBuffer input = new StringBuffer("");
        if (objects != null) {
            for (Iterator<?> iter = objects.iterator(); iter.hasNext();) {
                input.append(((Hashable) iter.next()).getHashable());
            }
        }
        //LELogger.debug("RAW INPUT: " + input);
        String hash = getHash(input.toString());
        //LELogger.debug("HASHED:    " + hash);
        return hash;
    }

    /**
     * Generate a random secure token based on the given pattern.  The following
     * characters may be used in the pattern to dictate the results:
     * <ul>
     * <li>9 - numeric character from the set 0-9
     * <li>A/a - alphabetic character from the set a-z
     * <li>Z/z - alphanumeric character from the set a-z and 2-9 (minus chars
     *     'o', 'l', '0', and '1' which are too easy to get confused).
     * <li>H/h - hexadecimal character from the set 0-9 and a-f
     * </ul>
     */
    public static String secureToken(String pattern) {
        if (pattern == null || pattern.length() == 0) return null;
        char[] patChars = pattern.toLowerCase().toCharArray();
        int len = patChars.length;
        StringBuffer sb = new StringBuffer(len);
        for (int i=0; i < len; i++) {
            char c = patChars[i];
            switch (c) {
                case '9':
                    sb.append(TOKSET_9[RAND.nextInt(TOKLEN_9)]);
                    break;
                case 'a':
                    sb.append(TOKSET_A[RAND.nextInt(TOKLEN_A)]);
                    break;
                case 'z':
                    sb.append(TOKSET_Z[RAND.nextInt(TOKLEN_Z)]);
                    break;
                case 'h':
                    sb.append(TOKSET_H[RAND.nextInt(TOKLEN_H)]);
                    break;
                default:
                    sb.append(pattern.charAt(i));
            }
        }
        return sb.toString();
    }
    private static final char[] TOKSET_9 = {'0','1','2','3','4','5','6','7','8','9'};
    private static final int    TOKLEN_9 = TOKSET_9.length;
    private static final char[] TOKSET_A = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
    private static final int    TOKLEN_A = TOKSET_A.length;
    private static final char[] TOKSET_Z = {'a','b','c','d','e','f','g','h','i','j','k'/*,'l'*/,'m','n'/*,'o'*/,'p','q','r','s','t','u','v','w','x','y','z'/*,'0','1'*/,'2','3','4','5','6','7','8','9'};
    private static final int    TOKLEN_Z = TOKSET_Z.length;
    private static final char[] TOKSET_H = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    private static final int    TOKLEN_H = TOKSET_H.length;


    /**
     * Generate a random secure token composed of letters and numbers of a
     * length requested by the user. Exclude special Base64 characters such as +, /,
     * and = since they are significant in URL strings.
     */
    public static String secureToken(int length) {
        byte[] buf = new byte[length * 2];
        StringBuffer sb = new StringBuffer(length);
        while (sb.length() < length) {
            // fill up the buf array
            RAND.nextBytes(buf);
            char c;
            for (int i = 0; i < buf.length && sb.length() < length; ++i) {
                c = (char) ((buf[i] + 0x30) & 0x7F);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                        || (c >= '0' && c <= '9')) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Encode a byte array in Base64 form, optionally splitting into lines.
     */
    public static String toBase64(byte[] bytes, boolean chunked) {
        return Base64.encode(bytes);
    }

    /**
     * Decode a Base64 string into its binary binary form.
     */
    public static byte[] fromBase64(String in) {
        try {
            return Base64.decode(new String(in.getBytes("US-ASCII")));
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new Error("Should never happen!", ex);
        }
    }
}
