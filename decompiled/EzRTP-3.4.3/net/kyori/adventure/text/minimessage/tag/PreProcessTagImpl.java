package net.kyori.adventure.text.minimessage.tag;

import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PreProcessTagImpl extends AbstractTag implements PreProcess {
   private final String value;

   PreProcessTagImpl(final String value) {
      this.value = value;
   }

   @NotNull
   @Override
   public String value() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof PreProcessTagImpl)) {
         return false;
      } else {
         PreProcessTagImpl that = (PreProcessTagImpl)other;
         return Objects.equals(this.value, that.value);
      }
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("value", this.value));
   }
}
