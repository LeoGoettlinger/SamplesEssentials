package de.oliver.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public class Sextet<U, V, W, X, Y, Z> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;
   private final Y fifth;
   private final Z sixth;

   protected Sextet(
      @NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth, @NonNull final Y fifth, @NonNull final Z sixth
   ) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
      this.sixth = sixth;
   }

   @NonNull
   public static <U, V, W, X, Y, Z> Sextet<U, V, W, X, Y, Z> of(
      @NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth, @NonNull final Y fifth, @NonNull final Z sixth
   ) {
      return new Sextet<>(first, second, third, fourth, fifth, sixth);
   }

   @NonNull
   public final U first() {
      return this.first;
   }

   @NonNull
   public final V second() {
      return this.second;
   }

   @NonNull
   public final W third() {
      return this.third;
   }

   @NonNull
   public final X fourth() {
      return this.fourth;
   }

   @NonNull
   public final Y fifth() {
      return this.fifth;
   }

   @NonNull
   public final Z sixth() {
      return this.sixth;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Sextet<?, ?, ?, ?, ?, ?> sextet = (Sextet<?, ?, ?, ?, ?, ?>)o;
         return Objects.equals(this.first(), sextet.first())
            && Objects.equals(this.second(), sextet.second())
            && Objects.equals(this.third(), sextet.third())
            && Objects.equals(this.fourth(), sextet.fourth())
            && Objects.equals(this.fifth(), sextet.fifth())
            && Objects.equals(this.sixth(), sextet.sixth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth(), this.fifth(), this.sixth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s, %s, %s)", this.first, this.second, this.third, this.fourth, this.fifth, this.sixth);
   }

   @Override
   public final int size() {
      return 6;
   }

   @NonNull
   @Override
   public final Object[] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth, this.fifth, this.sixth};
   }
}
