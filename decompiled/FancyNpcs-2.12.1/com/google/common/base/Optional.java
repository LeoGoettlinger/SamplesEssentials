package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.CheckForNull;

@DoNotMock("Use Optional.of(value) or Optional.absent()")
@ElementTypesAreNonnullByDefault
@GwtCompatible(
   serializable = true
)
public abstract class Optional<T> implements Serializable {
   private static final long serialVersionUID = 0L;

   public static <T> Optional<T> absent() {
      return Absent.withType();
   }

   public static <T> Optional<T> of(T reference) {
      return new Present<>(Preconditions.checkNotNull(reference));
   }

   public static <T> Optional<T> fromNullable(@CheckForNull T nullableReference) {
      return (Optional<T>)(nullableReference == null ? absent() : new Present<>(nullableReference));
   }

   @CheckForNull
   public static <T> Optional<T> fromJavaUtil(@CheckForNull java.util.Optional<T> javaUtilOptional) {
      return javaUtilOptional == null ? null : fromNullable(javaUtilOptional.orElse(null));
   }

   @CheckForNull
   public static <T> java.util.Optional<T> toJavaUtil(@CheckForNull Optional<T> googleOptional) {
      return googleOptional == null ? null : googleOptional.toJavaUtil();
   }

   public java.util.Optional<T> toJavaUtil() {
      return java.util.Optional.ofNullable(this.orNull());
   }

   Optional() {
   }

   public abstract boolean isPresent();

   public abstract T get();

   public abstract T or(T defaultValue);

   public abstract Optional<T> or(Optional<? extends T> secondChoice);

   public abstract T or(Supplier<? extends T> supplier);

   @CheckForNull
   public abstract T orNull();

   public abstract Set<T> asSet();

   public abstract <V> Optional<V> transform(Function<? super T, V> function);

   @Override
   public abstract boolean equals(@CheckForNull Object object);

   @Override
   public abstract int hashCode();

   @Override
   public abstract String toString();

   public static <T> Iterable<T> presentInstances(final Iterable<? extends Optional<? extends T>> optionals) {
      Preconditions.checkNotNull(optionals);
      return new Iterable<T>() {
         @Override
         public Iterator<T> iterator() {
            return new AbstractIterator<T>() {
               private final Iterator<? extends Optional<? extends T>> iterator = Preconditions.checkNotNull(optionals.iterator());

               @CheckForNull
               @Override
               protected T computeNext() {
                  while (this.iterator.hasNext()) {
                     Optional<? extends T> optional = (Optional<? extends T>)this.iterator.next();
                     if (optional.isPresent()) {
                        return (T)optional.get();
                     }
                  }

                  return (T)this.endOfData();
               }
            };
         }
      };
   }
}
