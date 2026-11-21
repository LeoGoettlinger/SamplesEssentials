package org.sethomegui.SetHomeGUI.libs.redis.jedis.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Deprecated
public interface Hashing {
   Hashing MURMUR_HASH = new MurmurHash();
   ThreadLocal<MessageDigest> md5Holder = new ThreadLocal<>();
   Hashing MD5 = new Hashing() {
      @Override
      public long hash(String key) {
         return this.hash(SafeEncoder.encode(key));
      }

      @Override
      public long hash(byte[] key) {
         try {
            if (md5Holder.get() == null) {
               md5Holder.set(MessageDigest.getInstance("MD5"));
            }
         } catch (NoSuchAlgorithmException var4) {
            throw new IllegalStateException("++++ no md5 algorithm found");
         }

         MessageDigest md5 = (MessageDigest)md5Holder.get();
         md5.reset();
         md5.update(key);
         byte[] bKey = md5.digest();
         return (long)(bKey[3] & 255) << 24 | (long)(bKey[2] & 255) << 16 | (long)(bKey[1] & 255) << 8 | bKey[0] & 255;
      }
   };

   long hash(String var1);

   long hash(byte[] var1);
}
