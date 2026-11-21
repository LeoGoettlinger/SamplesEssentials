package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;

interface NodeFactory<K, V> {
   Lookup LOOKUP = MethodHandles.lookup();
   MethodType FACTORY = MethodType.methodType(void.class);
   ConcurrentMap<String, NodeFactory<Object, Object>> FACTORIES = new ConcurrentHashMap<>();
   NodeFactory.RetiredStrongKey RETIRED_STRONG_KEY = new NodeFactory.RetiredStrongKey();
   NodeFactory.RetiredWeakKey RETIRED_WEAK_KEY = new NodeFactory.RetiredWeakKey();
   NodeFactory.DeadStrongKey DEAD_STRONG_KEY = new NodeFactory.DeadStrongKey();
   NodeFactory.DeadWeakKey DEAD_WEAK_KEY = new NodeFactory.DeadWeakKey();
   String ACCESS_TIME = "accessTime";
   String WRITE_TIME = "writeTime";
   String VALUE = "value";
   String KEY = "key";

   default boolean weakValues() {
      return false;
   }

   default boolean softValues() {
      return false;
   }

   Node<K, V> newNode(K key, @Nullable ReferenceQueue<K> keyReferenceQueue, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now);

   Node<K, V> newNode(Object keyReference, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now);

   default Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
      return key;
   }

   default Object newLookupKey(Object key) {
      return key;
   }

   static <K, V> NodeFactory<K, V> newFactory(Caffeine<K, V> builder, boolean isAsync) {
      if (builder.interner) {
         return (NodeFactory<K, V>)Interned.FACTORY;
      } else {
         String className = getClassName(builder, isAsync);
         return loadFactory(className);
      }
   }

   static String getClassName(Caffeine<?, ?> builder, boolean isAsync) {
      StringBuilder className = new StringBuilder();
      if (builder.isStrongKeys()) {
         className.append('P');
      } else {
         className.append('F');
      }

      if (builder.isStrongValues()) {
         className.append('S');
      } else if (builder.isWeakValues()) {
         className.append('W');
      } else {
         className.append('D');
      }

      if (builder.expiresVariable()) {
         if (builder.refreshAfterWrite()) {
            className.append('A');
            if (builder.evicts()) {
               className.append('W');
            }
         } else {
            className.append('W');
         }
      } else {
         if (builder.expiresAfterAccess()) {
            className.append('A');
         }

         if (builder.expiresAfterWrite()) {
            className.append('W');
         }
      }

      if (builder.refreshAfterWrite()) {
         className.append('R');
      }

      if (builder.evicts()) {
         className.append('M');
         if (!isAsync && (!builder.isWeighted() || builder.weigher == Weigher.singletonWeigher())) {
            className.append('S');
         } else {
            className.append('W');
         }
      }

      return className.toString();
   }

   static <K, V> NodeFactory<K, V> loadFactory(String className) {
      NodeFactory<Object, Object> factory = FACTORIES.get(className);
      if (factory == null) {
         factory = FACTORIES.computeIfAbsent(className, NodeFactory::newFactory);
      }

      return (NodeFactory<K, V>)factory;
   }

   static NodeFactory<Object, Object> newFactory(String className) {
      try {
         Class<?> clazz = LOOKUP.findClass(Node.class.getPackageName() + "." + className);
         MethodHandle constructor = LOOKUP.findConstructor(clazz, FACTORY);
         return (NodeFactory)constructor.invoke();
      } catch (Error | RuntimeException var3) {
         throw var3;
      } catch (Throwable var4) {
         throw new IllegalStateException(className, var4);
      }
   }

   public static final class DeadStrongKey {
   }

   public static final class DeadWeakKey extends References.WeakKeyReference<Object> {
      DeadWeakKey() {
         super(null, null);
      }
   }

   public static final class RetiredStrongKey {
   }

   public static final class RetiredWeakKey extends References.WeakKeyReference<Object> {
      RetiredWeakKey() {
         super(null, null);
      }
   }
}
