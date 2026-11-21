package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.nbt.api.BinaryTagHolder;

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
