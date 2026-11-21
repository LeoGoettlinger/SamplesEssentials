package su.nightexpress.nightcore.bridge.text.contents;

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.common.NightKey;
import su.nightexpress.nightcore.bridge.text.adapter.ObjectContentsAdapter;

public class NightSpriteObjectContents implements NightObjectContents {
   public static final NightKey DEFAULT_ATLAS = NightKey.key("minecraft:blocks");
   private final NightKey atlas;
   private final NightKey sprite;

   NightSpriteObjectContents(@NonNull NightKey atlas, @NonNull NightKey sprite) {
      this.atlas = atlas;
      this.sprite = sprite;
   }

   @NonNull
   @Override
   public <T> T adapt(@NonNull ObjectContentsAdapter<T> adapter) {
      return adapter.adaptContents(this);
   }

   @NonNull
   public NightKey atlas() {
      return this.atlas;
   }

   @NonNull
   public NightKey sprite() {
      return this.sprite;
   }

   @Override
   public boolean equals(Object o) {
      return !(o instanceof NightSpriteObjectContents that) ? false : Objects.equals(this.atlas, that.atlas) && Objects.equals(this.sprite, that.sprite);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.atlas, this.sprite);
   }

   @Override
   public String toString() {
      return "NightSpriteObjectContents{atlas=" + this.atlas + ", sprite=" + this.sprite + "}";
   }
}
