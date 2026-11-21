package net.kyori.adventure.text.object;

import net.kyori.adventure.key.Key;

public sealed interface SpriteObjectContents extends ObjectContents permits SpriteObjectContentsImpl {
   Key DEFAULT_ATLAS = Key.key("minecraft:blocks");

   Key atlas();

   Key sprite();
}
