package org.sethomegui.SetHomeGUI.libs.redis.jedis.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class SafeEncoder {
   public static volatile Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

   private SafeEncoder() {
      throw new InstantiationError("Must not instantiate this class");
   }

   public static byte[][] encodeMany(String... strs) {
      byte[][] many = new byte[strs.length][];

      for (int i = 0; i < strs.length; i++) {
         many[i] = encode(strs[i]);
      }

      return many;
   }

   public static byte[] encode(String str) {
      if (str == null) {
         throw new IllegalArgumentException("null value cannot be sent to redis");
      } else {
         return str.getBytes(DEFAULT_CHARSET);
      }
   }

   public static String encode(byte[] data) {
      return new String(data, DEFAULT_CHARSET);
   }

   public static Object encodeObject(Object dataToEncode) {
      if (dataToEncode instanceof byte[]) {
         return encode((byte[])dataToEncode);
      } else if (dataToEncode instanceof KeyValue) {
         KeyValue keyValue = (KeyValue)dataToEncode;
         return new KeyValue<>(encodeObject(keyValue.getKey()), encodeObject(keyValue.getValue()));
      } else if (!(dataToEncode instanceof List)) {
         return dataToEncode;
      } else {
         List arrayToDecode = (List)dataToEncode;
         List returnValueArray = new ArrayList(arrayToDecode.size());

         for (Object arrayEntry : arrayToDecode) {
            returnValueArray.add(encodeObject(arrayEntry));
         }

         return returnValueArray;
      }
   }
}
