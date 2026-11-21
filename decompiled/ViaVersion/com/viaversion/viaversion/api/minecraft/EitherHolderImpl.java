package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.util.EitherImpl;

final class EitherHolderImpl<T> extends EitherImpl<Holder<T>, String> implements EitherHolder<T> {
   EitherHolderImpl(Holder<T> left, String value) {
      super(left, value);
   }

   @Override
   public boolean hasHolder() {
      return this.isLeft();
   }

   @Override
   public boolean hasKey() {
      return this.isRight();
   }

   @Override
   public Holder<T> holder() {
      Preconditions.checkArgument(this.hasHolder(), "Either does not have a holder");
      return this.left();
   }

   @Override
   public String key() {
      Preconditions.checkArgument(this.hasKey(), "Either does not have a key");
      return this.right();
   }
}
