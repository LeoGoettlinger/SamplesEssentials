package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class LegacyFormat {
   static final LegacyFormat RESET = new LegacyFormat(true);
   @Nullable
   private final NamedTextColor color;
   @Nullable
   private final TextDecoration decoration;
   private final boolean reset;

   LegacyFormat(@Nullable final NamedTextColor color) {
      this.color = color;
      this.decoration = null;
      this.reset = false;
   }

   LegacyFormat(@Nullable final TextDecoration decoration) {
      this.color = null;
      this.decoration = decoration;
      this.reset = false;
   }

   private LegacyFormat(final boolean reset) {
      this.color = null;
      this.decoration = null;
      this.reset = reset;
   }

   @Nullable
   public TextColor color() {
      return this.color;
   }

   @Nullable
   public TextDecoration decoration() {
      return this.decoration;
   }

   public boolean reset() {
      return this.reset;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         LegacyFormat that = (LegacyFormat)other;
         return this.color == that.color && this.decoration == that.decoration && this.reset == that.reset;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.color);
      result = 31 * result + Objects.hashCode(this.decoration);
      return 31 * result + Boolean.hashCode(this.reset);
   }

   @Override
   public String toString() {
      return "LegacyFormat{color=" + this.color + ", decoration=" + this.decoration + ", reset=" + this.reset + "}";
   }
}
