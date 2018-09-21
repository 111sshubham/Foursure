package com.liveensure.util;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Vector;

/**
 * The Class MASTStringUtil.
 * 
 * A collection of string utilities to be used when the native platform may not provide these functions.
 * 
 */
public class MASTStringUtil {

    /**
     * Take an input string and a delimiter and break up the input string on each occurrence of the delimiter
     * 
     * @param s a String to split
     * @param delim a String to break up the target String on
     * 
     * @return an Array of String objects for each of the substrings created during the split
     */
    public static String[] split(String s, String delim) {
        return split(s, delim, 0);
    }
    
    /**
     * Take an input string and a delimiter and break up the input string on each occurrence of the delimiter.
     * Stop breaking up the string when <code>max</code> substrings have been created
     * 
     * @param s a String to split
     * @param delim a String to break up the target String on
     * @param max the max
     * 
     * @return an Array of String objects for each of the substrings created during the split, guaranteed to be
     * 		   no larger than <code>max</code> elements
     */
    public static String[] split(String s, String delim, int max) {
        String[] result;
        try {
            Vector<String> v = new Vector<String>();
            int x = 0;
            int y = s.indexOf(delim);
            if (max <= 0) {
                max = s.length() + 1;
            }
            
            while ((y != -1) && (v.size() < max)) {
                v.addElement(s.substring(x, y));
                x = y + delim.length();
                y = s.indexOf(delim, x);
            }
            v.addElement(s.substring(x, s.length()));
            result = new String[v.size()];
            for (int i = 0; i < v.size(); i++) {
                result[i] = v.elementAt(i).toString();
            }
        } catch (Exception e) {
            String[] r = { e.toString() };
            return r;
        }
        return result;
    }

    /**
     * Take a string and encode it to acceptable URL encoding, taking characters such as spaces, question marks, etc
     * and converting them to their URL escaped equivalents (e.g. space = %20)
     * 
     * @param value a String to URL Encode
     * 
     * @return a URL encoded version of the input string
     * 
     * @throws java.io.UnsupportedEncodingException the unsupported encoding exception
     */
    public static String urlEncode(String value) throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        byte[] utf;
        utf = value.getBytes("UTF-8"); // could throw uee here, we pass on to
        // caller
        for (int i = 0; i < utf.length; ++i) {
            char b = (char) utf[i];
            // if (b == ' ')
            // buf.append('+');

            // else
            if (isRFC3986Unreserved(b))
                buf.append(b);

            else {
                buf.append('%');
                buf.append(Integer.toHexString(b).toUpperCase(Locale.US)); // u.c. per
                // RFC 3986
            }
        }
        return buf.toString();
    }

    private static boolean isRFC3986Unreserved(char b) {
        return (b >= 'A' && b <= 'Z')

        || (b >= 'a' && b <= 'z')

        || Character.isDigit(b)

        || ".-~_".indexOf(b) >= 0;
    }

    /**
     * Trim leading and trailing whitespace from a String (this is an identical implementation to the 
     * normal Java String.trim(), reimplemented here for platforms where this is not available or does
     * not actually work).  Whitespace is defined as any character with an ASCII value of 32 or less
     * 
     * @param stringToTrim the string to trim
     * 
     * @return a String object which is the input string with all leading and trailing whitespace removed
     */
    public static String trim(String stringToTrim) {
        // looks like the rim implementation of String.trim() doesn't actually
        // do anything, so we do it here. sigh.
        String result = "";
        int start = 0, end = 0;
        boolean startFound = false;
        boolean endFound = false;

        if (stringToTrim == null)
            return result;
        int len = stringToTrim.length();
        if (len == 0)
            return result;
        if (len == 1 && stringToTrim.charAt(0) < 33)
            return result;
        // find first non-whitespace char
        for (int i = 0; !startFound && i < len; i++) {
            if (stringToTrim.charAt(i) > 32) {
                start = i;
                startFound = true;
            }
        }
        if (!startFound)
            return result;
        // find last non-whitespace char
        for (int i = len - 1; !endFound && i >= 0; i--) {
            if (stringToTrim.charAt(i) > 32) {
                end = i;
                endFound = true;
            }
        }
        if (!endFound || end <= start)
            return result;
        result = stringToTrim.substring(start, end + 1);
        return result;
    }
}
