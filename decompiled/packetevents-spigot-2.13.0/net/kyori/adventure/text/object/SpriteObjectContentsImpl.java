package net.kyori.adventure.text.object;

import java.util.Objects;
import net.kyori.adventure.internal.Internals;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SpriteObjectContentsImpl implements SpriteObjectContents {
   private final Key atlas;
   private final Key sprite;

   SpriteObjectContentsImpl(@NotNull final Key atlas, @NotNull final Key sprite) {
      this.atlas = atlas;
      this.sprite = sprite;
   }

   @NotNull
   @Override
   public Key atlas() {
      return this.atlas;
   }

   @NotNull
   @Override
   public Key sprite() {
      return this.sprite;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof SpriteObjectContents)) {
         return false;
      } else {
         SpriteObjectContentsImpl that = (SpriteObjectContentsImpl)other;
         return Objects.equals(this.atlas, that.atlas()) && Objects.equals(this.sprite, that.sprite());
      }
   }

   @Override
   public int hashCode() {
      int result = this.atlas.hashCode();
      return 31 * result + this.sprite.hashCode();
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }
}
