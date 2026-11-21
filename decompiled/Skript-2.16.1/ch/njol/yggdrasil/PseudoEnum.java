package ch.njol.yggdrasil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.ThreadSafe;
import org.jetbrains.annotations.Nullable;

@ThreadSafe
public abstract class PseudoEnum<T extends PseudoEnum<T>> {
   private final String name;
   private final int ordinal;
   private final PseudoEnum.Info<T> info;
   private static final Map<Class<? extends PseudoEnum<?>>, PseudoEnum.Info<?>> infos = new HashMap<>();

   protected PseudoEnum(String name) throws IllegalArgumentException {
      this.name = name;
      this.info = getInfo((Class<T>)this.getClass());
      this.info.writeLock.lock();

      try {
         if (this.info.map.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate name '" + name + "'");
         }

         this.ordinal = this.info.values.size();
         this.info.values.add((T)this);
         this.info.map.put(name, (T)this);
      } finally {
         this.info.writeLock.unlock();
      }
   }

   public final String name() {
      return this.name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public final int ordinal() {
      return this.ordinal;
   }

   @Override
   public final int hashCode() {
      return this.ordinal;
   }

   @Override
   public final boolean equals(@Nullable Object object) {
      return object == this;
   }

   @Override
   protected final Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public final Class<T> getDeclaringClass() {
      return (Class<T>)getDeclaringClass((Class<T>)this.getClass());
   }

   public static <T extends PseudoEnum<T>> Class<? super T> getDeclaringClass(Class<T> type) {
      Class<? super T> superClass = type;

      while (superClass.isAnonymousClass()) {
         superClass = superClass.getSuperclass();
      }

      return superClass;
   }

   public final List<T> values() {
      return values(this.getDeclaringClass());
   }

   public static <T extends PseudoEnum<T>> List<T> values(Class<T> type) throws IllegalArgumentException {
      if (type != getDeclaringClass(type)) {
         throw new IllegalArgumentException(type + " != " + getDeclaringClass(type));
      } else {
         return values(getInfo(type));
      }
   }

   private static <T extends PseudoEnum<T>> List<T> values(PseudoEnum.Info<T> info) {
      info.readLock.lock();

      ArrayList var1;
      try {
         var1 = new ArrayList<>(info.values);
      } finally {
         info.readLock.unlock();
      }

      return var1;
   }

   public final T getConstant(int id) throws IndexOutOfBoundsException {
      this.info.readLock.lock();

      PseudoEnum var2;
      try {
         var2 = this.info.values.get(id);
      } finally {
         this.info.readLock.unlock();
      }

      return (T)var2;
   }

   public final int numConstants() {
      this.info.readLock.lock();

      int var1;
      try {
         var1 = this.info.values.size();
      } finally {
         this.info.readLock.unlock();
      }

      return var1;
   }

   @Nullable
   public final T valueOf(String name) {
      this.info.readLock.lock();

      PseudoEnum var2;
      try {
         var2 = this.info.map.get(name);
      } finally {
         this.info.readLock.unlock();
      }

      return (T)var2;
   }

   @Nullable
   public static <T extends PseudoEnum<T>> T valueOf(Class<T> type, String name) {
      PseudoEnum.Info<T> info = getInfo(type);
      info.readLock.lock();

      PseudoEnum var3;
      try {
         var3 = info.map.get(name);
      } finally {
         info.readLock.unlock();
      }

      return (T)var3;
   }

   private static <T extends PseudoEnum<T>> PseudoEnum.Info<T> getInfo(Class<T> type) {
      synchronized (infos) {
         PseudoEnum.Info<T> info = (PseudoEnum.Info<T>)infos.get(getDeclaringClass(type));
         if (info == null) {
            infos.put(type, info = new PseudoEnum.Info<>());
         }

         return info;
      }
   }

   private static final class Info<T extends PseudoEnum<T>> {
      final List<T> values = new ArrayList<>();
      final Map<String, T> map = new HashMap<>();
      final ReadWriteLock lock = new ReentrantReadWriteLock(true);
      final Lock readLock = this.lock.readLock();
      final Lock writeLock = this.lock.writeLock();
   }
}
