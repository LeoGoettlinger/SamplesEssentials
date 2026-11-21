package org.apache.commons.lang3.concurrent.locks;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.AbstractSupplier;
import org.apache.commons.lang3.function.Failable;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.Suppliers;

public class LockingVisitors {
   public static <O> LockingVisitors.ReadWriteLockVisitor<O> create(O object, ReadWriteLock readWriteLock) {
      return new LockingVisitors.ReadWriteLockVisitor<>(object, readWriteLock);
   }

   public static <O> LockingVisitors.ReentrantLockVisitor<O> create(O object, ReentrantLock reentrantLock) {
      return new LockingVisitors.ReentrantLockVisitor<>(object, reentrantLock);
   }

   public static <O> LockingVisitors.ReentrantLockVisitor<O> reentrantLockVisitor(O object) {
      return create(object, new ReentrantLock());
   }

   public static <O> LockingVisitors.ReadWriteLockVisitor<O> reentrantReadWriteLockVisitor(O object) {
      return create(object, new ReentrantReadWriteLock());
   }

   public static <O> LockingVisitors.StampedLockVisitor<O> stampedLockVisitor(O object) {
      return new LockingVisitors.StampedLockVisitor<>(object, new StampedLock());
   }

   public static class LockVisitor<O, L> {
      private final L lock;
      private final O object;
      private final Supplier<Lock> readLockSupplier;
      private final Supplier<Lock> writeLockSupplier;

      private LockVisitor(LockingVisitors.LockVisitor.LVBuilder<O, L, ?> builder) {
         this.object = Objects.requireNonNull(builder.object, "object");
         this.lock = Objects.requireNonNull(builder.lock, "lock");
         this.readLockSupplier = Objects.requireNonNull(builder.readLockSupplier, "readLockSupplier");
         this.writeLockSupplier = Objects.requireNonNull(builder.writeLockSupplier, "writeLockSupplier");
      }

      protected LockVisitor(O object, L lock, Supplier<Lock> readLockSupplier, Supplier<Lock> writeLockSupplier) {
         this.object = Objects.requireNonNull(object, "object");
         this.lock = Objects.requireNonNull(lock, "lock");
         this.readLockSupplier = Objects.requireNonNull(readLockSupplier, "readLockSupplier");
         this.writeLockSupplier = Objects.requireNonNull(writeLockSupplier, "writeLockSupplier");
      }

      public void acceptReadLocked(FailableConsumer<O, ?> consumer) {
         this.lockAcceptUnlock(this.readLockSupplier, consumer);
      }

      public void acceptWriteLocked(FailableConsumer<O, ?> consumer) {
         this.lockAcceptUnlock(this.writeLockSupplier, consumer);
      }

      public <T> T applyReadLocked(FailableFunction<O, T, ?> function) {
         return this.lockApplyUnlock(this.readLockSupplier, function);
      }

      public <T> T applyWriteLocked(FailableFunction<O, T, ?> function) {
         return this.lockApplyUnlock(this.writeLockSupplier, function);
      }

      public L getLock() {
         return this.lock;
      }

      public O getObject() {
         return this.object;
      }

      protected void lockAcceptUnlock(Supplier<Lock> lockSupplier, FailableConsumer<O, ?> consumer) {
         Lock lock = Objects.requireNonNull(Suppliers.get(lockSupplier), "lock");
         lock.lock();

         try {
            Failable.accept(consumer, this.object);
         } finally {
            lock.unlock();
         }
      }

      protected <T> T lockApplyUnlock(Supplier<Lock> lockSupplier, FailableFunction<O, T, ?> function) {
         Lock lock = Objects.requireNonNull(Suppliers.get(lockSupplier), "lock");
         lock.lock();

         Object var4;
         try {
            var4 = Failable.apply(function, this.object);
         } finally {
            lock.unlock();
         }

         return (T)var4;
      }

      public static class LVBuilder<O, L, B extends LockingVisitors.LockVisitor.LVBuilder<O, L, B>>
         extends AbstractSupplier<LockingVisitors.LockVisitor<O, L>, B, RuntimeException> {
         L lock;
         O object;
         private Supplier<Lock> readLockSupplier;
         private Supplier<Lock> writeLockSupplier;

         public LockingVisitors.LockVisitor<O, L> get() {
            return new LockingVisitors.LockVisitor<>(this);
         }

         Supplier<Lock> getReadLockSupplier() {
            return this.readLockSupplier;
         }

         Supplier<Lock> getWriteLockSupplier() {
            return this.writeLockSupplier;
         }

         public B setLock(L lock) {
            this.lock = lock;
            return this.asThis();
         }

         public B setObject(O object) {
            this.object = object;
            return this.asThis();
         }

         public B setReadLockSupplier(Supplier<Lock> readLockSupplier) {
            this.readLockSupplier = readLockSupplier;
            return this.asThis();
         }

         public B setWriteLockSupplier(Supplier<Lock> writeLockSupplier) {
            this.writeLockSupplier = writeLockSupplier;
            return this.asThis();
         }
      }
   }

   public static class ReadWriteLockVisitor<O> extends LockingVisitors.LockVisitor<O, ReadWriteLock> {
      public static <O> LockingVisitors.ReadWriteLockVisitor.Builder<O> builder() {
         return new LockingVisitors.ReadWriteLockVisitor.Builder<>();
      }

      private ReadWriteLockVisitor(LockingVisitors.ReadWriteLockVisitor.Builder<O> builder) {
         super(builder);
      }

      protected ReadWriteLockVisitor(O object, ReadWriteLock readWriteLock) {
         super(object, readWriteLock, readWriteLock::readLock, readWriteLock::writeLock);
      }

      public static class Builder<O> extends LockingVisitors.LockVisitor.LVBuilder<O, ReadWriteLock, LockingVisitors.ReadWriteLockVisitor.Builder<O>> {
         public LockingVisitors.ReadWriteLockVisitor<O> get() {
            return new LockingVisitors.ReadWriteLockVisitor<>(this);
         }

         public LockingVisitors.ReadWriteLockVisitor.Builder<O> setLock(ReadWriteLock readWriteLock) {
            this.setReadLockSupplier(readWriteLock::readLock);
            this.setWriteLockSupplier(readWriteLock::writeLock);
            return (LockingVisitors.ReadWriteLockVisitor.Builder<O>)super.setLock(readWriteLock);
         }
      }
   }

   public static class ReentrantLockVisitor<O> extends LockingVisitors.LockVisitor<O, ReentrantLock> {
      public static <O> LockingVisitors.ReentrantLockVisitor.Builder<O> builder() {
         return new LockingVisitors.ReentrantLockVisitor.Builder<>();
      }

      private ReentrantLockVisitor(LockingVisitors.ReentrantLockVisitor.Builder<O> builder) {
         super(builder);
      }

      protected ReentrantLockVisitor(O object, ReentrantLock reentrantLock) {
         super(object, reentrantLock, () -> reentrantLock, () -> reentrantLock);
      }

      public static class Builder<O> extends LockingVisitors.LockVisitor.LVBuilder<O, ReentrantLock, LockingVisitors.ReentrantLockVisitor.Builder<O>> {
         public LockingVisitors.ReentrantLockVisitor<O> get() {
            return new LockingVisitors.ReentrantLockVisitor<>(this);
         }

         public LockingVisitors.ReentrantLockVisitor.Builder<O> setLock(ReentrantLock reentrantLock) {
            this.setReadLockSupplier(() -> reentrantLock);
            this.setWriteLockSupplier(() -> reentrantLock);
            return (LockingVisitors.ReentrantLockVisitor.Builder<O>)super.setLock(reentrantLock);
         }
      }
   }

   public static class StampedLockVisitor<O> extends LockingVisitors.LockVisitor<O, StampedLock> {
      public static <O> LockingVisitors.StampedLockVisitor.Builder<O> builder() {
         return new LockingVisitors.StampedLockVisitor.Builder<>();
      }

      private StampedLockVisitor(LockingVisitors.StampedLockVisitor.Builder<O> builder) {
         super(builder);
      }

      protected StampedLockVisitor(O object, StampedLock stampedLock) {
         super(object, stampedLock, stampedLock::asReadLock, stampedLock::asWriteLock);
      }

      public static class Builder<O> extends LockingVisitors.LockVisitor.LVBuilder<O, StampedLock, LockingVisitors.StampedLockVisitor.Builder<O>> {
         public LockingVisitors.StampedLockVisitor<O> get() {
            return new LockingVisitors.StampedLockVisitor<>(this);
         }

         public LockingVisitors.StampedLockVisitor.Builder<O> setLock(StampedLock stampedLock) {
            this.setReadLockSupplier(stampedLock::asReadLock);
            this.setWriteLockSupplier(stampedLock::asWriteLock);
            return (LockingVisitors.StampedLockVisitor.Builder<O>)super.setLock(stampedLock);
         }
      }
   }
}
