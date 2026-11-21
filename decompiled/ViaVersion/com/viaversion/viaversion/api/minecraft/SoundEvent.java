package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

public record SoundEvent(String identifier, @Nullable Float fixedRange) {
   private static final Holder<SoundEvent> UNKNOWN = Holder.of(new SoundEvent("viaversion:unknown", 0.0F));

   public SoundEvent withIdentifier(String identifier) {
      return new SoundEvent(identifier, this.fixedRange);
   }

   @Nullable
   public static Holder<SoundEvent> rewriteHolder(@Nullable Holder<SoundEvent> holder, Int2IntFunction soundRewriteFunction) {
      return holder == null ? null : holder.updateId(soundRewriteFunction, () -> UNKNOWN);
   }
}
