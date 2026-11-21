package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.configurate.reactive.Disposable;
import com.earth2me.essentials.libs.configurate.reactive.Processor;
import com.earth2me.essentials.libs.configurate.reactive.Subscriber;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class DirectoryListenerRegistration implements Subscriber<WatchEvent<?>> {
   private final Lock lock = new ReentrantLock();
   private final AtomicBoolean acceptingRegistrations = new AtomicBoolean(true);
   private final WatchKey key;
   private final ConcurrentMap<Path, Processor<WatchEvent<?>, WatchEvent<?>>> fileListeners = new ConcurrentHashMap<>();
   private final Executor executor;
   private final Processor<WatchEvent<?>, WatchEvent<?>> dirListeners;

   DirectoryListenerRegistration(final WatchKey key, final Executor executor) {
      this.key = Objects.requireNonNull(key, "key");
      this.executor = Objects.requireNonNull(executor, "executor");
      this.dirListeners = Processor.create(executor);
   }

   public WatchKey key() {
      return this.key;
   }

   public void submit(final WatchEvent<?> item) {
      if (this.acceptingRegistrations.get()) {
         Path file = (Path)item.context();
         Processor<WatchEvent<?>, WatchEvent<?>> fileListeners = this.fileListeners
            .computeIfPresent(file, (key, old) -> old.closeIfUnsubscribed() ? null : old);
         this.dirListeners.submit(item);
         if (fileListeners != null) {
            fileListeners.submit(item);
         }
      }
   }

   @Override
   public void onClose() {
      this.lock.lock();

      try {
         try {
            this.dirListeners.onClose();
         } catch (Exception var5) {
            this.dirListeners.onError(var5);
         }

         this.fileListeners.forEach((k, v) -> {
            try {
               v.onClose();
            } catch (Exception var3) {
               v.onError(var3);
            }
         });
         this.fileListeners.clear();
         this.key.cancel();
      } finally {
         this.lock.unlock();
      }
   }

   public Disposable subscribe(final Subscriber<WatchEvent<?>> subscriber) {
      if (!this.acceptingRegistrations.get()) {
         return () -> {};
      } else {
         this.lock.lock();

         Disposable var2;
         try {
            var2 = this.dirListeners.subscribe(subscriber);
         } finally {
            this.lock.unlock();
         }

         return var2;
      }
   }

   public Disposable subscribe(final Path file, final Subscriber<WatchEvent<?>> subscriber) {
      if (!this.acceptingRegistrations.get()) {
         return () -> {};
      } else {
         this.lock.lock();

         Disposable var3;
         try {
            var3 = this.fileListeners.computeIfAbsent(file, f -> Processor.create(this.executor)).subscribe(subscriber);
         } finally {
            this.lock.unlock();
         }

         return var3;
      }
   }

   public boolean hasSubscribers() {
      this.lock.lock();

      boolean var1;
      try {
         var1 = this.dirListeners.hasSubscribers() || !this.fileListeners.isEmpty();
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof DirectoryListenerRegistration)) {
         return false;
      } else {
         DirectoryListenerRegistration that = (DirectoryListenerRegistration)o;
         return this.key().equals(that.key()) && this.fileListeners.equals(that.fileListeners) && this.dirListeners.equals(that.dirListeners);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.key(), this.fileListeners, this.dirListeners);
   }

   public boolean closeIfEmpty() {
      this.lock.lock();

      try {
         if (!this.hasSubscribers()) {
            this.onClose();
            return true;
         }
      } finally {
         this.lock.unlock();
      }

      return false;
   }
}
