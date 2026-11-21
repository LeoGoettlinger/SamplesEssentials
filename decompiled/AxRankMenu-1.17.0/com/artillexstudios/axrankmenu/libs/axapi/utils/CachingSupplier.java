package com.artillexstudios.axrankmenu.libs.axapi.utils;

import java.util.function.Supplier;

public interface CachingSupplier<T> extends Supplier<T> {
   boolean hasValue();

   static <Z> CachingSupplier<Z> create(Supplier<Z> supplier) {
      return new CachingSupplier<Z>() {
         private Z value;

         @Override
         public Z get() {
            if (this.value == null) {
               this.value = supplier.get();
            }

            return this.value;
         }

         @Override
         public boolean hasValue() {
            return this.value != null;
         }
      };
   }
}
