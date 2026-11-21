package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LazyValue<T> {
   private final Object synchronizedLock = new Object();
   private final Supplier<T> valueSupplier;
   private Optional<T> value = Optional.empty();

   public LazyValue(@NotNull Supplier<T> valueSupplier) {
      this.valueSupplier = Objects.requireNonNull(valueSupplier, "value supplier cannot be null!");
   }

   public LazyValue(@NotNull T value) {
      Objects.requireNonNull(value, "value cannot be null!");
      this.value = Optional.of(value);
      this.valueSupplier = null;
   }

   public boolean isLoaded() {
      return this.value.isPresent();
   }

   public void eager() {
      if (!this.isLoaded()) {
         synchronized (this.synchronizedLock) {
            if (!this.isLoaded()) {
               T value = this.valueSupplier.get();
               this.value = Optional.ofNullable(value);
            }
         }
      }
   }

   @Nullable
   public T getValue() {
      if (!this.isLoaded()) {
         synchronized (this.synchronizedLock) {
            if (!this.isLoaded()) {
               T value = this.valueSupplier.get();
               this.value = Optional.ofNullable(value);
               return value;
            }
         }
      }

      return this.value.get();
   }

   @NotNull
   public Optional<T> getIfLoaded() {
      return this.value;
   }
}
