package de.theredend2000.advancedhunt.lib.kyori.adventure.text.object;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Contract;

public sealed interface ObjectContents permits SpriteObjectContents, PlayerHeadObjectContents {
   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static SpriteObjectContents sprite(final Key atlas, final Key sprite) {
      return new SpriteObjectContentsImpl(Objects.requireNonNull(atlas, "atlas"), Objects.requireNonNull(sprite, "sprite"));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static SpriteObjectContents sprite(final Key sprite) {
      return new SpriteObjectContentsImpl(SpriteObjectContents.DEFAULT_ATLAS, Objects.requireNonNull(sprite, "sprite"));
   }

   @Contract(
      value = "-> new",
      pure = true
   )
   static PlayerHeadObjectContents.Builder playerHead() {
      return new PlayerHeadObjectContentsImpl.BuilderImpl();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static PlayerHeadObjectContents playerHead(final String name) {
      return new PlayerHeadObjectContentsImpl(name, null, List.of(), true, null);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static PlayerHeadObjectContents playerHead(final UUID id) {
      return new PlayerHeadObjectContentsImpl(null, id, List.of(), true, null);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static PlayerHeadObjectContents playerHead(final PlayerHeadObjectContents.SkinSource skinSource) {
      return playerHead().skin(skinSource).build();
   }
}
