package vrx.economy.vRXClans.libs.cloud.type.tuple;

import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public class Quintet<U, V, W, X, Y> implements Tuple {
   private final U first;
   private final V second;
   private final W third;
   private final X fourth;
   private final Y fifth;

   protected Quintet(@NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth, @NonNull final Y fifth) {
      this.first = first;
      this.second = second;
      this.third = third;
      this.fourth = fourth;
      this.fifth = fifth;
   }

   @NonNull
   public static <U, V, W, X, Y> Quintet<U, V, W, X, Y> of(
      @NonNull final U first, @NonNull final V second, @NonNull final W third, @NonNull final X fourth, @NonNull final Y fifth
   ) {
      return new Quintet<>(first, second, third, fourth, fifth);
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

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Quintet<?, ?, ?, ?, ?> quintet = (Quintet<?, ?, ?, ?, ?>)o;
         return Objects.equals(this.first(), quintet.first())
            && Objects.equals(this.second(), quintet.second())
            && Objects.equals(this.third(), quintet.third())
            && Objects.equals(this.fourth(), quintet.fourth())
            && Objects.equals(this.fifth(), quintet.fifth());
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.first(), this.second(), this.third(), this.fourth(), this.fifth());
   }

   @Override
   public final String toString() {
      return String.format("(%s, %s, %s, %s, %s)", this.first, this.second, this.third, this.fourth, this.fifth);
   }

   @Override
   public final int size() {
      return 5;
   }

   @NonNull
   @Override
   public final Object[] toArray() {
      return new Object[]{this.first, this.second, this.third, this.fourth, this.fifth};
   }
}
