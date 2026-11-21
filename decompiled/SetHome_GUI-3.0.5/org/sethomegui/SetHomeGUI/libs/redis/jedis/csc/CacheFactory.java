package org.sethomegui.SetHomeGUI.libs.redis.jedis.csc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisCacheException;

public final class CacheFactory {
   public static Cache getCache(CacheConfig config) {
      if (config.getCacheClass() == null) {
         if (config.getCacheable() == null) {
            throw new JedisCacheException("Cacheable is required to create the default cache!");
         } else {
            return new DefaultCache(config.getMaxSize(), config.getCacheable(), getEvictionPolicy(config));
         }
      } else {
         return instantiateCustomCache(config);
      }
   }

   private static Cache instantiateCustomCache(CacheConfig config) {
      try {
         if (config.getCacheable() != null) {
            Constructor ctorWithCacheable = findConstructorWithCacheable(config.getCacheClass());
            if (ctorWithCacheable != null) {
               return (Cache)ctorWithCacheable.newInstance(config.getMaxSize(), getEvictionPolicy(config), config.getCacheable());
            }
         }

         Constructor ctor = getConstructor(config.getCacheClass());
         return (Cache)ctor.newInstance(config.getMaxSize(), getEvictionPolicy(config));
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | InstantiationException var2) {
         throw new JedisCacheException("Failed to insantiate custom cache type!", var2);
      }
   }

   private static Constructor findConstructorWithCacheable(Class customCacheType) {
      return Arrays.stream(customCacheType.getConstructors())
         .filter(ctor -> Arrays.equals((Object[])ctor.getParameterTypes(), (Object[])(new Class[]{int.class, EvictionPolicy.class, Cacheable.class})))
         .findFirst()
         .orElse(null);
   }

   private static Constructor getConstructor(Class customCacheType) {
      try {
         return customCacheType.getConstructor(int.class, EvictionPolicy.class);
      } catch (NoSuchMethodException var3) {
         String className = customCacheType.getName();
         throw new JedisCacheException(
            String.format(
               "Failed to find compatible constructor for custom cache type!  Provide one of these;\n - %s(int maxSize, EvictionPolicy evictionPolicy)\n - %s(int maxSize, EvictionPolicy evictionPolicy, Cacheable cacheable)",
               className,
               className
            ),
            var3
         );
      }
   }

   private static EvictionPolicy getEvictionPolicy(CacheConfig config) {
      return (EvictionPolicy)(config.getEvictionPolicy() == null ? new LRUEviction(config.getMaxSize()) : config.getEvictionPolicy());
   }
}
