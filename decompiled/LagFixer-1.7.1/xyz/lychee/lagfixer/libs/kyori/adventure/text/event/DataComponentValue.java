package xyz.lychee.lagfixer.libs.kyori.adventure.text.event;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.nbt.api.BinaryTagHolder;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;

public interface DataComponentValue extends Examinable {
   @NotNull
   static DataComponentValue.Removed removed() {
      return RemovedDataComponentValueImpl.REMOVED;
   }

   public interface Removed extends DataComponentValue {
   }

   public interface TagSerializable extends DataComponentValue {
      @NotNull
      BinaryTagHolder asBinaryTag();
   }
}
