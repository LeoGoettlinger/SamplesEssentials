package net.kyori.adventure.text.event;

import net.kyori.adventure.nbt.api.BinaryTagHolder;

public interface DataComponentValue {
   static DataComponentValue.Removed removed() {
      return RemovedDataComponentValueImpl.REMOVED;
   }

   public interface Removed extends DataComponentValue {
   }

   public interface TagSerializable extends DataComponentValue {
      BinaryTagHolder asBinaryTag();
   }
}
