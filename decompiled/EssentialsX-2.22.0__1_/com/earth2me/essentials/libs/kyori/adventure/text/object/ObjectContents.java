package com.earth2me.essentials.libs.kyori.adventure.text.object;

import com.earth2me.essentials.libs.kyori.adventure.key.Key;
import com.earth2me.essentials.libs.kyori.examination.Examinable;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ObjectContents extends Examinable {
   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   @NotNull
   static SpriteObjectContents sprite(@NotNull final Key atlas, @NotNull final Key sprite) {
      return new SpriteObjectContentsImpl(Objects.requireNonNull(atlas, "atlas"), Objects.requireNonNull(sprite, "sprite"));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static SpriteObjectContents sprite(@NotNull final Key sprite) {
      return new SpriteObjectContentsImpl(SpriteObjectContents.DEFAULT_ATLAS, Objects.requireNonNull(sprite, "sprite"));
   }

   @Contract(
      value = "-> new",
      pure = true
   )
   @NotNull
   static PlayerHeadObjectContents.Builder playerHead() {
      return new PlayerHeadObjectContentsImpl.BuilderImpl();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static PlayerHeadObjectContents playerHead(@NotNull final String name) {
      return new PlayerHeadObjectContentsImpl(name, null, Collections.emptyList(), true, null);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static PlayerHeadObjectContents playerHead(@NotNull final UUID id) {
      return new PlayerHeadObjectContentsImpl(null, id, Collections.emptyList(), true, null);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   @NotNull
   static PlayerHeadObjectContents playerHead(@NotNull final PlayerHeadObjectContents.SkinSource skinSource) {
      return playerHead().skin(skinSource).build();
   }
}
