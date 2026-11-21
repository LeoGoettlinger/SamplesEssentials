package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.format.Style;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

final class CallbackStylingTagImpl extends AbstractTag implements Inserting {
   private final Consumer<Style.Builder> styles;

   CallbackStylingTagImpl(final Consumer<Style.Builder> styles) {
      this.styles = styles;
   }

   @NotNull
   @Override
   public Component value() {
      return Component.text("", Style.style(this.styles));
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.styles);
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof CallbackStylingTagImpl)) {
         return false;
      } else {
         CallbackStylingTagImpl that = (CallbackStylingTagImpl)other;
         return Objects.equals(this.styles, that.styles);
      }
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("styles", this.styles));
   }
}
