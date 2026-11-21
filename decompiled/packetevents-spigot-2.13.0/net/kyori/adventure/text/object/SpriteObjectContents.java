package net.kyori.adventure.text.object;

import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface SpriteObjectContents extends ObjectContents {
   Key DEFAULT_ATLAS = Key.key("minecraft:blocks");

   @NotNull
   Key atlas();

   @NotNull
   Key sprite();

   @NotNull
   @Override
   default Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("atlas", this.atlas()), ExaminableProperty.of("sprite", this.sprite()));
   }
}
