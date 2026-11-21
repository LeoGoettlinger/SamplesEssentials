package com.earth2me.essentials.libs.kyori.adventure.text.object;

import com.earth2me.essentials.libs.kyori.adventure.key.Key;
import com.earth2me.essentials.libs.kyori.examination.ExaminableProperty;
import java.util.stream.Stream;
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
