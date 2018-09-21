package com.liveensure.util;

/*
 * A Java implementation of the Secure Hash Algorithm, SHA-1, as defined
 * in FIPS PUB 180-1
 * Copyright (C) Sam Ruby 2004
 * All rights reserved
 *
 * Based on code Copyright (C) Paul Johnston 2000 - 2002.
 * See http://pajhome.org.uk/site/legal.html for details.
 *
 * Converted to Java by Russell Beattie 2004
 * Base64 logic and inlining by Sam Ruby 2004
 * Bug fix correcting single bit error in base64 code by John Wilson
 *
 *                                BSD License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class SHA1
{
 private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 public static final int B64_STD_CHARS = 0;
 public static final int B64_ALT_CHARS = 1;
 private static final byte[][] B64_CHARS = new byte[][] {
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(),
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".getBytes()
 };

 /*
  * Bitwise rotate a 32-bit number to the left
  */
 private static int rol(int num, int cnt)
 {
  return (num << cnt) | (num >>> (32 - cnt));
 }

 /**
  * Take a string and return the base64 representation of its SHA-1.
  */
 public static String encodeBase64(String str) {
  try {
   return encodeBase64(str.getBytes("UTF-8"), B64_STD_CHARS);
  } catch( java.io.UnsupportedEncodingException ioe ) {
   throw new RuntimeException("Missing UTF-8 encoding");
  }
 }
 
 /**
  * Take a string and return the base64 representation of its SHA-1, with
  * optional "alternative" alphabet for the + and / characters.
  */
 public static String encodeBase64(String str, int base64CharsId) {
  try {
   return encodeBase64(str.getBytes("UTF-8"), base64CharsId);
  } catch( java.io.UnsupportedEncodingException ioe ) {
   throw new RuntimeException("Missing UTF-8 encoding");
  }
 }
 
 /**
  * Take byte array input and return the base64 representation of its SHA1
  */
 public static String encodeBase64(byte[] x, int base64CharsId) {
  if( base64CharsId < 0 || base64CharsId >= B64_CHARS.length ) {
   base64CharsId = B64_STD_CHARS;
  }
  // Convert a string to a sequence of 16-word blocks, stored as an array.
  // Append padding bits and the length, as described in the SHA1 standard

  int[] blks = new int[(((x.length + 8) >> 6) + 1) * 16];
  int i;
  for (i = 0; i < x.length; i++)
  {
   blks[i >> 2] |= (x[i]&0xFF) << (24 - (i % 4) * 8);
  }
  blks[i >> 2] |= 0x80 << (24 - (i % 4) * 8);
  blks[blks.length - 1] = x.length * 8;
  // calculate 160 bit SHA1 hash of the sequence of blocks
  int[] w = new int[80];
  int a = 1732584193;
  int b = -271733879;
  int c = -1732584194;
  int d = 271733878;
  int e = -1009589776;
  for (i = 0; i < blks.length; i += 16)
  {
   int olda = a;
   int oldb = b;
   int oldc = c;
   int oldd = d;
   int olde = e;
   for (int j = 0; j < 80; j++)
   {
    w[j] = (j < 16) ? blks[i + j] : (rol(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1));
    int t = rol(a, 5)
      + e
      + w[j]
      + ((j < 20) ? 1518500249 + ((b & c) | ((~b) & d)) : (j < 40) ? 1859775393 + (b ^ c ^ d) : (j < 60) ? -1894007588
        + ((b & c) | (b & d) | (c & d)) : -899497514 + (b ^ c ^ d));
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = t;
   }
   a = a + olda;
   b = b + oldb;
   c = c + oldc;
   d = d + oldd;
   e = e + olde;
  }
  // Convert 160 bit hash to base64
  int[] words = { a, b, c, d, e, 0 };
  byte[] base64 = B64_CHARS[base64CharsId];
  byte[] result = new byte[28];
  for (i = 0; i < 27; i++)
  {
   int start = i * 6;
   int word = start >> 5;
   int offset = start & 0x1f;
   if (offset <= 26)
   {
    result[i] = base64[(words[word] >> (26 - offset)) & 0x3F];
   }
   else if (offset == 28)
   {
    result[i] = base64[(((words[word] & 0x0F) << 2) | ((words[word + 1] >> 30) & 0x03)) & 0x3F];
   }
   else
   {
    result[i] = base64[(((words[word] & 0x03) << 4) | ((words[word + 1] >> 28) & 0x0F)) & 0x3F];
   }
  }
  result[27] = '=';
  return new String(result);
 }

 /**
  * Return hash hex-encoded
  */
 public static String encodeHex(String str)
 {
  try {
   return encodeHex(str.getBytes("UTF-8"));
  } catch( java.io.UnsupportedEncodingException ioe ) {
   throw new RuntimeException("Missing UTF-8 encoding");
  }
 }

 /**
  * Return hash hex-encoded
  */
 public static String encodeHex(byte[] x)
 {
  // Convert a string to a sequence of 16-word blocks, stored as an array.
  // Append padding bits and the length, as described in the SHA1 standard
  int[] blks = new int[(((x.length + 8) >> 6) + 1) * 16];
  int i;
  for (i = 0; i < x.length; i++)
  {
   blks[i >> 2] |= (x[i]&0xFF) << (24 - (i % 4) * 8);
  }
  blks[i >> 2] |= 0x80 << (24 - (i % 4) * 8);
  blks[blks.length - 1] = x.length * 8;
  // calculate 160 bit SHA1 hash of the sequence of blocks
  int[] w = new int[80];
  int a = 1732584193;
  int b = -271733879;
  int c = -1732584194;
  int d = 271733878;
  int e = -1009589776;
  for (i = 0; i < blks.length; i += 16)
  {
   int olda = a;
   int oldb = b;
   int oldc = c;
   int oldd = d;
   int olde = e;
   for (int j = 0; j < 80; j++)
   {
    w[j] = (j < 16) ? blks[i + j] : (rol(w[j - 3] ^ w[j - 8] ^ w[j - 14] ^ w[j - 16], 1));
    int t = rol(a, 5)
      + e
      + w[j]
      + ((j < 20) ? 1518500249 + ((b & c) | ((~b) & d)) : (j < 40) ? 1859775393 + (b ^ c ^ d) : (j < 60) ? -1894007588
        + ((b & c) | (b & d) | (c & d)) : -899497514 + (b ^ c ^ d));
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = t;
   }
   a = a + olda;
   b = b + oldb;
   c = c + oldc;
   d = d + oldd;
   e = e + olde;
  }
  // convert 160 bits to Hex value
  // data is in 5 4-byte words
  int[] words = { a, b, c, d, e };
  byte[] data = new byte[(words.length * 4)];
  for (int y = 0,o=0; y < words.length; y++)
  {
   data[o++] = (byte) ((words[y] >> 24));
   data[o++] = (byte) ((words[y] >> 16));
   data[o++] = (byte) ((words[y] >> 8));
   data[o++] = (byte) ((words[y]));
  }
  int l = data.length;
  char[] out = new char[l << 1];
  // two characters form the hex value.
  for (int z = 0, j = 0; z < l; z++)
  {
   out[j++] = DIGITS[(0xF0 & data[z]) >>> 4];
   out[j++] = DIGITS[0x0F & data[z]];
  }
  
  return new String(out);
 }
}
