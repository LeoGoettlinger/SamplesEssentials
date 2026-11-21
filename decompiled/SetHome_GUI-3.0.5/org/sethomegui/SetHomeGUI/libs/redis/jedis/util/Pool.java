package org.sethomegui.SetHomeGUI.libs.redis.jedis.util;

import org.sethomegui.SetHomeGUI.libs.pool2.PooledObjectFactory;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPool;
import org.sethomegui.SetHomeGUI.libs.pool2.impl.GenericObjectPoolConfig;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisException;

public class Pool<T> extends GenericObjectPool<T> {
   public Pool(GenericObjectPoolConfig<T> poolConfig, PooledObjectFactory<T> factory) {
      this(factory, poolConfig);
   }

   public Pool(PooledObjectFactory<T> factory, GenericObjectPoolConfig<T> poolConfig) {
      super(factory, poolConfig);
   }

   public Pool(PooledObjectFactory<T> factory) {
      super(factory);
   }

   @Override
   public void close() {
      this.destroy();
   }

   public void destroy() {
      try {
         super.close();
      } catch (RuntimeException var2) {
         throw new JedisException("Could not destroy the pool", var2);
      }
   }

   public T getResource() {
      try {
         return super.borrowObject();
      } catch (JedisException var2) {
         throw var2;
      } catch (Exception var3) {
         throw new JedisException("Could not get a resource from the pool", var3);
      }
   }

   public void returnResource(T resource) {
      if (resource != null) {
         try {
            super.returnObject(resource);
         } catch (RuntimeException var3) {
            throw new JedisException("Could not return the resource to the pool", var3);
         }
      }
   }

   public void returnBrokenResource(T resource) {
      if (resource != null) {
         try {
            super.invalidateObject(resource);
         } catch (Exception var3) {
            throw new JedisException("Could not return the broken resource to the pool", var3);
         }
      }
   }

   @Override
   public void addObjects(int count) {
      try {
         for (int i = 0; i < count; i++) {
            this.addObject();
         }
      } catch (Exception var3) {
         throw new JedisException("Error trying to add idle objects", var3);
      }
   }
}
