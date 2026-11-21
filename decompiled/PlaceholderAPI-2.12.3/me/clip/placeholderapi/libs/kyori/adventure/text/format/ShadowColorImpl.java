package me.clip.placeholderapi.libs.kyori.adventure.text.format;

import java.util.stream.Stream;
import me.clip.placeholderapi.libs.kyori.adventure.internal.Internals;
import me.clip.placeholderapi.libs.kyori.examination.Examinable;
import me.clip.placeholderapi.libs.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ShadowColorImpl implements ShadowColor, Examinable {
   static final int NONE_VALUE = 0;
   static final ShadowColorImpl NONE = new ShadowColorImpl(0);
   private final int value;

   ShadowColorImpl(final int value) {
      this.value = value;
   }

   @Override
   public int value() {
      return this.value;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (!(other instanceof ShadowColorImpl)) {
         return false;
      } else {
         ShadowColorImpl that = (ShadowColorImpl)other;
         return this.value == that.value;
      }
   }

   @Override
   public int hashCode() {
      return Integer.hashCode(this.value);
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
