package xyz.lychee.lagfixer.libs.kyori.adventure.text.object;

import java.util.stream.Stream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

@ApiStatus.NonExtendable
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
