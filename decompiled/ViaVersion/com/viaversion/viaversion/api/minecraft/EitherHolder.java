package com.viaversion.viaversion.api.minecraft;

public interface EitherHolder<T> {
   static <T> EitherHolder<T> of(Holder<T> value) {
      return new EitherHolderImpl<>(value, null);
   }

   static <T> EitherHolder<T> of(String key) {
      return new EitherHolderImpl<>(null, key);
   }

   boolean hasHolder();

   boolean hasKey();

   Holder<T> holder();

   String key();
}
