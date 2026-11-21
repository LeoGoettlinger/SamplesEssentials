package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface LocalCacheFactory {
   Lookup LOOKUP = MethodHandles.lookup();
   MethodType FACTORY = MethodType.methodType(void.class, Caffeine.class, AsyncCacheLoader.class, boolean.class);
   MethodType FACTORY_CALL = FACTORY.changeReturnType(BoundedLocalCache.class);
   ConcurrentMap<String, LocalCacheFactory> FACTORIES = new ConcurrentHashMap<>();
   String EXPIRES_AFTER_ACCESS_NANOS = "expiresAfterAccessNanos";
   String EXPIRES_AFTER_WRITE_NANOS = "expiresAfterWriteNanos";
   String REFRESH_AFTER_WRITE_NANOS = "refreshAfterWriteNanos";
   String WEIGHTED_SIZE = "weightedSize";
   String MAXIMUM = "maximum";

   <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) throws Throwable;

   static <K, V> BoundedLocalCache<K, V> newBoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean isAsync) {
      String className = getClassName(builder);
      LocalCacheFactory factory = loadFactory(className);

      try {
         return factory.newInstance(builder, cacheLoader, isAsync);
      } catch (Error | RuntimeException var6) {
         throw var6;
      } catch (Throwable var7) {
         throw new IllegalStateException(className, var7);
      }
   }

   static String getClassName(Caffeine<?, ?> builder) {
      StringBuilder className = new StringBuilder();
      if (builder.isStrongKeys()) {
         className.append('S');
      } else {
         className.append('W');
      }

      if (builder.isStrongValues()) {
         className.append('S');
      } else {
         className.append('I');
      }

      if (builder.removalListener != null) {
         className.append('L');
      }

      if (builder.isRecordingStats()) {
         className.append('S');
      }

      if (builder.evicts()) {
         className.append('M');
         if (builder.isWeighted()) {
            className.append('W');
         } else {
            className.append('S');
         }
      }

      if (builder.expiresAfterAccess() || builder.expiresVariable()) {
         className.append('A');
      }

      if (builder.expiresAfterWrite()) {
         className.append('W');
      }

      if (builder.refreshAfterWrite()) {
         className.append('R');
      }

      return className.toString();
   }

   static LocalCacheFactory loadFactory(String className) {
      LocalCacheFactory factory = FACTORIES.get(className);
      if (factory == null) {
         factory = FACTORIES.computeIfAbsent(className, LocalCacheFactory::newFactory);
      }

      return factory;
   }

   static LocalCacheFactory newFactory(String className) {
      try {
         Class<?> clazz = LOOKUP.findClass(LocalCacheFactory.class.getPackageName() + "." + className);

         try {
            return (LocalCacheFactory)LOOKUP.findStaticVarHandle(clazz, "FACTORY", LocalCacheFactory.class).get();
         } catch (NoSuchFieldException var3) {
            return new LocalCacheFactory.MethodHandleBasedFactory(clazz);
         }
      } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException var4) {
         throw new IllegalStateException(className, var4);
      }
   }

   public static final class MethodHandleBasedFactory implements LocalCacheFactory {
      final MethodHandle methodHandle;

      MethodHandleBasedFactory(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException {
         this.methodHandle = LOOKUP.findConstructor(clazz, FACTORY).asType(FACTORY_CALL);
      }

      @Override
      public <K, V> BoundedLocalCache<K, V> newInstance(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<? super K, V> cacheLoader, boolean async) throws Throwable {
         return (BoundedLocalCache)this.methodHandle.invokeExact((Caffeine)builder, (AsyncCacheLoader)cacheLoader, (boolean)async);
      }
   }
}
