package xyz.lychee.lagfixer.libs.kyori.adventure.text.format;

import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

@ApiStatus.NonExtendable
public interface TextDecorationAndState extends Examinable, StyleBuilderApplicable {
   @NotNull
   TextDecoration decoration();

   @NotNull
   TextDecoration.State state();

   @Override
   default void styleApply(@NotNull final Style.Builder style) {
      style.decoration(this.decoration(), this.state());
   }

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("decoration", this.decoration()), ExaminableProperty.of("state", this.state()));
   }
}
