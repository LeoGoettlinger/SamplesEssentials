package de.oliver.cloud;

import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class SenderMapperImpl<A, B> implements SenderMapper<A, B> {
   static final SenderMapper<?, ?> IDENTITY = new SenderMapperImpl((Function<A, B>)Function.identity(), (Function<B, A>)Function.identity());
   @NonNull
   private final Function<A, B> map;
   @NonNull
   private final Function<B, A> reverse;

   SenderMapperImpl(@NonNull final Function<A, B> map, @NonNull final Function<B, A> reverse) {
      this.map = Objects.requireNonNull(map, "map function");
      this.reverse = Objects.requireNonNull(reverse, "reverse function");
   }

   @NonNull
   @Override
   public B map(@NonNull final A base) {
      return this.map.apply(base);
   }

   @NonNull
   @Override
   public A reverse(@NonNull final B mapped) {
      return this.reverse.apply(mapped);
   }

   @Override
   public boolean equals(@Nullable final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SenderMapperImpl<?, ?> that = (SenderMapperImpl<?, ?>)o;
         return Objects.equals(this.map, that.map) && Objects.equals(this.reverse, that.reverse);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.map, this.reverse);
   }

   @Override
   public String toString() {
      return "SenderMapperImpl{map=" + this.map + ", reverse=" + this.reverse + '}';
   }
}
