package com.earth2me.essentials.libs.configurate.reactive;

import com.earth2me.essentials.libs.checkerframework.checker.interning.qual.InternedDistinct;

final class NoOpDisposable implements Disposable {
   @InternedDistinct
   static final NoOpDisposable INSTANCE = new NoOpDisposable();

   private NoOpDisposable() {
   }

   @Override
   public void dispose() {
   }
}
