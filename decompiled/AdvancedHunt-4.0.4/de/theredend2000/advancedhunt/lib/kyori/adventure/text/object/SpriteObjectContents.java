package de.theredend2000.advancedhunt.lib.kyori.adventure.text.object;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;

public sealed interface SpriteObjectContents extends ObjectContents permits SpriteObjectContentsImpl {
   Key DEFAULT_ATLAS = Key.key("minecraft:blocks");

   Key atlas();

   Key sprite();
}
