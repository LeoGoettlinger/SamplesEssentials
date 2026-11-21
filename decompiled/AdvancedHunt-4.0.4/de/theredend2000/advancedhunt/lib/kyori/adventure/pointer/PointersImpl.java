package de.theredend2000.advancedhunt.lib.kyori.adventure.pointer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

record PointersImpl(Map<Pointer<?>, Supplier<?>> pointers) implements Pointers {
   static final PointersImpl EMPTY = new PointersImpl(Map.of());

   PointersImpl(final PointersImpl.BuilderImpl pointers) {
      this(Map.copyOf(pointers.pointers));
   }

   @Override
   public <T> Optional<T> get(final Pointer<T> pointer) {
      Objects.requireNonNull(pointer, "pointer");
      Supplier<?> supplier = this.pointers.get(pointer);
      return Optional.ofNullable(supplier).map(s -> (T)s.get());
   }

   @Override
   public <T> boolean supports(final Pointer<T> pointer) {
      Objects.requireNonNull(pointer, "pointer");
      return this.pointers.containsKey(pointer);
   }

   public Pointers.Builder toBuilder() {
      return new PointersImpl.BuilderImpl(this);
   }

   static final class BuilderImpl implements Pointers.Builder {
      private final Map<Pointer<?>, Supplier<?>> pointers;

      BuilderImpl() {
         this.pointers = new HashMap<>();
      }

      BuilderImpl(final PointersImpl pointers) {
         this.pointers = new HashMap<>(pointers.pointers);
      }

      @Override
      public <T> Pointers.Builder withDynamic(final Pointer<T> pointer, final Supplier<T> value) {
         this.pointers.put(Objects.requireNonNull(pointer, "pointer"), Objects.requireNonNull(value, "value"));
         return this;
      }

      public Pointers build() {
         return new PointersImpl(this);
      }
   }
}
