package com.liveensure.util;
/* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or
* sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The  above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
* IN THE SOFTWARE. */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class Base64.
 */
public class Base64 {

   /** Character Table */
   static final char[] charTab =
       "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
           .toCharArray();

    /** URL-safe Character Table */
    static final char[] charTabUrl =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_~"
            .toCharArray();
   /**
    * Encode.
    * 
    * @param data byte array of data to encode to Base64
    * 
    * @return a String containing the Base 64 representation of the byte array
    */
   public static String encode(byte[] data) {
       return encode(data, false);
   }

    /**
     * Encode with URL-safe character table.
     *
     * @param data byte array of data to encode to Base64
     *
     * @return a String containing the Base 64 representation of the byte array
     */
    public static String encodeUrl(byte[] data) {
        return encode(charTabUrl, data, false);
    }

   /**
    * Encode.
    * 
    * @param data byte array of data to encode to Base64
    * @param lineBreaks a boolean flag on whether to include line breaks every 64 characters in the output string
    * 
    * @return a String containing the Base 64 representation of the byte array
    */
   public static String encode(byte[] data, boolean lineBreaks) {
       return encode(charTab, data, 0, data.length, null, lineBreaks).toString();
   }

    /**
     * Encode.
     *
     * @param data byte array of data to encode to Base64
     * @param lineBreaks a boolean flag on whether to include line breaks every 64 characters in the output string
     *
     * @return a String containing the Base 64 representation of the byte array
     */
    public static String encode(char[] table, byte[] data, boolean lineBreaks) {
        return encode(table, data, 0, data.length, null, lineBreaks).toString();
    }

   /**
    * Encodes the part of the given byte array denoted by start and
    * len to the Base64 format.  The encoded data is appended to the
    * given StringBuffer. If no StringBuffer is given, a new one is
    * created automatically. The StringBuffer is the return value of
    * this method.
    * 
    * @param data the data
    * @param start the start
    * @param len the len
    * @param buf the buf
    * @param lineBreaks the line breaks
    * 
    * @return the string buffer
    */

   public static StringBuffer encode(
       char[] table,
       byte[] data,
       int start,
       int len,
       StringBuffer buf,
       boolean lineBreaks) {

       if (buf == null)
           buf = new StringBuffer(data.length * 3 / 2);

       int end = len - 3;
       int i = start;
       int n = 0;
       char padding = (table.length == 65) ? table[64] : '=';

       while (i <= end) {
           int d =
               ((((int) data[i]) & 0x0ff) << 16)
                   | ((((int) data[i + 1]) & 0x0ff) << 8)
                   | (((int) data[i + 2]) & 0x0ff);

           buf.append(table[(d >> 18) & 63]);
           buf.append(table[(d >> 12) & 63]);
           buf.append(table[(d >> 6) & 63]);
           buf.append(table[d & 63]);

           i += 3;

           if (lineBreaks && n++ >= 14) {
               n = 0;
               buf.append("\r\n");
           }
       }

       if (i == start + len - 2) {
           int d =
               ((((int) data[i]) & 0x0ff) << 16)
                   | ((((int) data[i + 1]) & 255) << 8);

           buf.append(table[(d >> 18) & 63]);
           buf.append(table[(d >> 12) & 63]);
           buf.append(table[(d >> 6) & 63]);
           buf.append(padding);
       }
       else if (i == start + len - 1) {
           int d = (((int) data[i]) & 0x0ff) << 16;

           buf.append(table[(d >> 18) & 63]);
           buf.append(table[(d >> 12) & 63]);
           buf.append(padding);
           buf.append(padding);
       }

       return buf;
   }

   /**
    * Decode.
    * 
    * @param c in input character
    * 
    * @return an integer representation of the Base 64 encode string
    */
   static int decode(char c) {

       if (c >= 'A' && c <= 'Z')
           return ((int) c) - 65;
       else if (c >= 'a' && c <= 'z')
           return ((int) c) - 97 + 26;
       else if (c >= '0' && c <= '9')
           return ((int) c) - 48 + 26 + 26;
       else
           switch (c) {
               case '-' :
               case '+' :
                   return 62;
               case '_' :
               case '/' :
                   return 63;
               case '~' :
               case '=' :
                   return 0;
               default :
                   throw new RuntimeException(
                       "unexpected code: " + c);
           }
   }

   /**
    * Decodes the given Base64 encoded String to a new byte array.
    * The byte array holding the decoded data is returned.
    * 
    * @param s a String containing Base 64 data
    * 
    * @return an Array of bytes containing the decoded data
    */

   public static byte[] decode(String s) {

       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       try {

           decode(s, bos);

       }
       catch (IOException e) {
           throw new RuntimeException();
       }
       return bos.toByteArray();
   }

   /**
    * Decode.
    * 
    * @param s a String containing Base 64 data
    * @param os The OutputStream object to write the decoded data out to
    * 
    * @throws java.io.IOException Signals that an I/O exception has occurred.
    */
   public static void decode(String s, OutputStream os)
       throws IOException {
       int i = 0;

       int len = s.length();

       while (true) {
           while (i < len && s.charAt(i) <= ' ')
               i++;

           if (i == len)
               break;

           int tri =
               (decode(s.charAt(i)) << 18)
                   + (decode(s.charAt(i + 1)) << 12)
                   + (decode(s.charAt(i + 2)) << 6)
                   + (decode(s.charAt(i + 3)));


           os.write((tri >> 16) & 255);
           if (s.charAt(i + 2) == '=' || s.charAt(i + 2) == '~')
               break;
           os.write((tri >> 8) & 255);
           if (s.charAt(i + 3) == '=' || s.charAt(i + 3) == '~')
               break;
           os.write(tri & 255);

           i += 4;
       }
   }

    public static void main(String args[]) {
        if (args.length < 1) {
            //System.err.println("Usage: java Base64 'string to encode' ['next string' ...]");
            //System.exit(1);
            System.out.println(encodeUrl("City where you were born?:New York".getBytes()));
        } else {
            for (int i = 0; i < args.length; i++) {
                System.out.println(encode(args[i].getBytes()));
            }
        }
    }
}
