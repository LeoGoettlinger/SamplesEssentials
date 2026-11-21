package net.kyori.adventure.pointer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

record PointersSupplierImpl<T>(@Nullable PointersSupplier<? super T> parent, Map<Pointer<?>, Function<T, ?>> resolvers) implements PointersSupplier<T> {
   @Override
   public Pointers view(final T instance) {
      return new PointersSupplierImpl.ForwardingPointers<>(instance, this);
   }

   @Override
   public <P> boolean supports(final Pointer<P> pointer) {
      if (this.resolvers.containsKey(Objects.requireNonNull(pointer, "pointer"))) {
         return true;
      } else {
         return this.parent == null ? false : this.parent.supports(pointer);
      }
   }

   @Nullable
   @Override
   public <P> Function<? super T, P> resolver(final Pointer<P> pointer) {
      Function<? super T, ?> resolver = this.resolvers.get(Objects.requireNonNull(pointer, "pointer"));
      if (resolver != null) {
         return (Function<? super T, P>)resolver;
      } else {
         return this.parent == null ? null : this.parent.resolver(pointer);
      }
   }

   static final class BuilderImpl<T> implements PointersSupplier.Builder<T> {
      @Nullable
      private PointersSupplier<? super T> parent = null;
      private final Map<Pointer<?>, Function<T, ?>> resolvers = new HashMap<>();

      @Override
      public PointersSupplier.Builder<T> parent(@Nullable final PointersSupplier<? super T> parent) {
         this.parent = parent;
         return this;
      }

      @Override
      public <P> PointersSupplier.Builder<T> resolving(final Pointer<P> pointer, final Function<T, P> resolver) {
         this.resolvers.put(pointer, resolver);
         return this;
      }

      public PointersSupplier<T> build() {
         return new PointersSupplierImpl<>(this.parent, Map.copyOf(this.resolvers));
      }
   }

   record ForwardingPointers<U>(U instance, PointersSupplierImpl<U> supplier) implements Pointers {
      @Override
      public <T> Optional<T> get(final Pointer<T> pointer) {
         Function<? super U, ?> resolver = this.supplier.resolvers.get(Objects.requireNonNull(pointer, "pointer"));
         if (resolver == null) {
            PointersSupplier<? super U> parent = this.supplier.parent;
            if (parent != null) {
               resolver = parent.resolver(pointer);
            }
         }

         return Optional.ofNullable(resolver).map(r -> (T)r.apply(this.instance));
      }

      @Override
      public <T> boolean supports(final Pointer<T> pointer) {
         return this.supplier.supports(pointer);
      }

      public Pointers.Builder toBuilder() {
         Pointers.Builder builder = this.supplier.parent == null ? Pointers.builder() : this.supplier.parent.view(this.instance).toBuilder();

         for (Entry<Pointer<?>, Function<U, ?>> entry : this.supplier.resolvers.entrySet()) {
            builder.withDynamic(entry.getKey(), () -> entry.getValue().apply(this.instance));
         }

         return builder;
      }
   }
}
