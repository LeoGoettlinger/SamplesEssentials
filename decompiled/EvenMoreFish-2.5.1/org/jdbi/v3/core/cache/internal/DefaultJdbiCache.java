package org.jdbi.v3.core.cache.internal;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jdbi.v3.core.cache.JdbiCache;
import org.jdbi.v3.core.cache.JdbiCacheLoader;
import org.jdbi.v3.core.internal.exceptions.Sneaky;

final class DefaultJdbiCache<K, V> implements JdbiCache<K, V> {
   private final ConcurrentMap<K, DoubleLinkedList.Node<K, CompletableFuture<V>>> cache = new ConcurrentHashMap<>();
   @GuardedBy("expungeQueue")
   private final DoubleLinkedList<K, CompletableFuture<V>> expungeQueue = new DoubleLinkedList<>();
   private final JdbiCacheLoader<K, V> cacheLoader;
   private final int maxSize;

   DefaultJdbiCache(DefaultJdbiCacheBuilder builder, JdbiCacheLoader<K, V> cacheLoader) {
      this.cacheLoader = cacheLoader;
      this.maxSize = builder.getMaxSize();
   }

   @Override
   public V get(K key) {
      Object var2;
      try {
         var2 = this.doGet(key, this.cacheLoader);
      } finally {
         this.expunge();
      }

      return (V)var2;
   }

   @Override
   public V getWithLoader(K key, JdbiCacheLoader<K, V> loader) {
      Object var3;
      try {
         var3 = this.doGet(key, loader);
      } finally {
         this.expunge();
      }

      return (V)var3;
   }

   private V doGet(final K key, final JdbiCacheLoader<K, V> loader) {
      DoubleLinkedList.Node<K, CompletableFuture<V>> node = this.cache.computeIfAbsent(key, k -> DoubleLinkedList.createNode(key, new CompletableFuture<>()));
      if (node.value.isDone()) {
         if (!node.value.isCompletedExceptionally()) {
            this.refresh(node);
         }

         return this.join(node);
      } else {
         synchronized (node) {
            if (!node.value.isDone()) {
               V value;
               try {
                  value = loader == null ? null : loader.create(key);
               } catch (Throwable var10) {
                  node.value.completeExceptionally(var10);
                  this.cache.remove(key, node);
                  throw var10;
               }

               if (this.maxSize > 0) {
                  synchronized (this.expungeQueue) {
                     this.expungeQueue.addHead(node);
                  }
               }

               node.value.complete(value);
            }

            return this.join(node);
         }
      }
   }

   private V join(DoubleLinkedList.Node<K, CompletableFuture<V>> node) {
      try {
         return node.value.join();
      } catch (CompletionException var3) {
         throw Sneaky.throwAnyway(var3.getCause());
      }
   }

   public DefaultJdbiCacheStats getStats() {
      synchronized (this.expungeQueue) {
         return new DefaultJdbiCacheStats(this.expungeQueue.size, this.maxSize);
      }
   }

   private void refresh(DoubleLinkedList.Node<K, CompletableFuture<V>> node) {
      if (this.maxSize > 0) {
         synchronized (this.expungeQueue) {
            DoubleLinkedList.Node<K, CompletableFuture<V>> cacheNode = this.expungeQueue.removeNode(node);
            if (cacheNode != null) {
               this.expungeQueue.addHead(cacheNode);
            }
         }
      }
   }

   private void expunge() {
      List<K> purgeList = new ArrayList<>();
      synchronized (this.expungeQueue) {
         if (this.maxSize <= 0 || this.expungeQueue.size <= this.maxSize) {
            return;
         }

         while (this.expungeQueue.size > this.maxSize) {
            DoubleLinkedList.Node<K, ?> node = this.expungeQueue.removeTail();
            if (node != null) {
               purgeList.add(node.key);
            }
         }
      }

      purgeList.forEach(this.cache::remove);
   }
}
