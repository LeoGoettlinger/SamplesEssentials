package me.clip.placeholderapi.libs.kyori.adventure.text.format;

import java.util.stream.Stream;
import me.clip.placeholderapi.libs.kyori.examination.Examinable;
import me.clip.placeholderapi.libs.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
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
