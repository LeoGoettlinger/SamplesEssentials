package de.theredend2000.advancedhunt.lib.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public class Quartet<U, V, W, X> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;

   protected Quartet(@NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
   }

   @NonNull
   public static <U, V, W, X> Quartet<U, V, W, X> of(@NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth) {
      return new Quartet<>(first, second, third, fourth);
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

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Quartet<?, ?, ?, ?> quartet = (Quartet<?, ?, ?, ?>)o;
         return Objects.equals(this.first(), quartet.first())
            && Objects.equals(this.second(), quartet.second())
            && Objects.equals(this.third(), quartet.third())
            && Objects.equals(this.fourth(), quartet.fourth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s)", this.first, this.second, this.third, this.fourth);
   }

   @Override
   public final int size() {
      return 4;
   }

   @NonNull
   @Override
   public final Object[] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth};
   }
}
