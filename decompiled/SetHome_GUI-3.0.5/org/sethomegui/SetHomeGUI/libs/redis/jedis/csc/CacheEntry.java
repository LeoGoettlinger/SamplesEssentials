package org.sethomegui.SetHomeGUI.libs.redis.jedis.csc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisCacheException;

public class CacheEntry<T> {
   private final CacheKey<T> cacheKey;
   private final WeakReference<CacheConnection> connection;
   private final byte[] bytes;

   public CacheEntry(CacheKey<T> cacheKey, T value, CacheConnection connection) {
      this.cacheKey = cacheKey;
      this.connection = new WeakReference<>(connection);
      this.bytes = toBytes(value);
   }

   public CacheKey<T> getCacheKey() {
      return this.cacheKey;
   }

   public T getValue() {
      return this.toObject(this.bytes);
   }

   public CacheConnection getConnection() {
      return this.connection.get();
   }

   private static byte[] toBytes(Object object) {
      try (
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
      ) {
         oos.writeObject(object);
         oos.flush();
         oos.close();
         return baos.toByteArray();
      } catch (IOException var34) {
         throw new JedisCacheException("Failed to serialize object", var34);
      }
   }

   private T toObject(byte[] data) {
      try (
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         ObjectInputStream ois = new ObjectInputStream(bais);
      ) {
         return (T)ois.readObject();
      } catch (ClassNotFoundException | IOException var35) {
         throw new JedisCacheException("Failed to deserialize object", var35);
      }
   }
}
