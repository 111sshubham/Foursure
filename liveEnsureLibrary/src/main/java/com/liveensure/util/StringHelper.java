/*
 * Created on Mar 2, 2005 by marc
 * $LastChangedBy: dhagberg $
 * $LastChangedDate: 2006-01-19 00:43:40 -0700 (Thu, 19 Jan 2006) $
 * $LastChangedRevision: 453 $
 */
package com.liveensure.util;

/**
 * @author marc
 * 
 *         Simple string utilities used by client, server and engine classes.
 */
public class StringHelper {
	private static final String DEFAULT_STRING = "Donec sapien leo, sagittis ac, pretium et, porta eget, sem. "
			+ "Fusce viverra gravida dui. Morbi ac quam. Sed at eros vel orci";

    private static final int DEFAULT_WIDTH = 4;

	/**
	 * Returns true if the supplied string is null or 0 length. Note that a
	 * string consisting of spaces is <em>not</em> considered empty. (You could
	 * call isEmpty(s.trim()) to acheive that goal.)
	 * 
	 * @param s
	 *            The string to test
	 * @return true if the string to test is empty or null
	 */
	public static boolean isEmpty(String s) {
		return ((s == null) || s.length() == 0);
	}

	/**
	 * Returns null if the string passed in is either null or empty, otherwise
	 * returns the string itself, trimmed of all leading/trailing whitespace.
	 * 
	 * @param s
	 *            The string to test (may be null)
	 * @return null if s is null or empty, trimmed value if non-empty.
	 */
	public static String trimOrNull(String s) {
		if (s == null || (s = s.trim()).length() == 0) {
			return null;
		}
		return s;
	}

	/**
	 * A routine that will guarantee a string of length
	 * <code>end - begin + 1</code> formed as much as possible from the
	 * substring command on the supplied string. If the beginning index is
	 * beyond the length of the string, it will be wrapped using a simple
	 * modulus. If the length required goes beyond the end of the string, the
	 * result will be pulled from concatenated copies of the original string.
	 * 
	 * Null and empty strings will return a string pulled from a default list of
	 * characters.
	 * 
	 * @param s
	 * @param begin
	 * @param end
	 * @return a guaranteed substring
	 */
	public static String guaranteedSubstring(String s, int begin, int end) {
		if (end < begin) {
			throw new IllegalArgumentException(
					"Ending index must be greater than beginning");
		} else if (begin < 0) {
			throw new IllegalArgumentException(
					"Begin index must be 0 or greater");
		}
		if (isEmpty(s)) {
			s = DEFAULT_STRING;
		}
		int strlen = s.length();
		int sublen = end - begin;
		if (begin >= strlen) {
			begin %= strlen;
			end = begin + sublen;
		}
		while (end > strlen) {
			s += DEFAULT_STRING;
			strlen = s.length();
		}
		return s.substring(begin, end);
	}

	/**
	 * Escape special characters like \r, \n, \t, and hex chars.
	 */
	public static String escapeSpecial(String src) {
		if (src == null) {
			return null;
		}
		int len = src.length();
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; ++i) {
			appendEscaped(sb, src.charAt(i));
		}
		return sb.toString();
	}

	/**
	 * Escape special characters like \r, \n, \t, and hex chars from a byte
	 * array, useful if array contains MOSTLY ascii chars.
	 */
	public static String escapeSpecial(byte[] src) {
		if (src == null) {
			return null;
		}
		int len = src.length;
		StringBuffer sb = new StringBuffer(len);
		char c;
		for (int i = 0; i < len; ++i) {
			c = (char) (src[i] & 0xFF);
			appendEscaped(sb, c);
		}
		return sb.toString();
	}

    /**
     * Obfuscate and encode a string for communication with the live-identity
     * service.
     *
     * @param sessionToken a relatively unique, pseudo-random per-session identifier
     * @param data the actual data to obscure
     * @return a single base64url encoded string
     */
    public static String obscure(String sessionToken, String data) {
        StringBuffer buf = new StringBuffer();
        int r = (int)(Math.random() * (sessionToken.length() - DEFAULT_WIDTH));
        String prefix = sessionToken.substring(r, r + DEFAULT_WIDTH);
        buf.append(prefix);
        buf.append(data);
        return Base64.encodeUrl(buf.toString().getBytes());
    }

    /**
     * Decodes and runs a minimal check on validity of an encoded packet of text.
     * 
     * @param sessionToken a relatively unique, pseudo-random per-session identifier
     * @param data the obscured text to unpack
     * @return a single cleartext string or null if the packet is not valid
     */
    public static String illuminate(String sessionToken, String data) {
        String raw = new String(Base64.decode(data));
        if (sessionToken.indexOf(raw.substring(0, DEFAULT_WIDTH)) > -1) {
        	//System.out.println("illumate: returning " + raw.substring(DEFAULT_WIDTH));
            return raw.substring(DEFAULT_WIDTH);
        }
        return null;
    }

	private static StringBuffer appendEscaped(StringBuffer sb, char c) {
		if (c > '\u001f' && c < '\u007f') {
			sb.append(c);
		} else if ('\r' == c) {
			sb.append("\\r");
		} else if ('\n' == c) {
			sb.append("\\n");
		} else if ('\t' == c) {
			sb.append("\\t");
		} else if ('\\' == c) {
			sb.append("\\");
		} else if (c <= '\u00ff') {
			String s = Integer.toHexString(c);
			sb.append("\\x");
			if (s.length() < 2) {
				sb.append('0');
			}
			sb.append(s);
		} else {
			String s = Integer.toHexString(c);
			sb.append("\\u");
			if (s.length() < 4) {
				sb.append('0');
			}
		}
		return sb;
	}

}
