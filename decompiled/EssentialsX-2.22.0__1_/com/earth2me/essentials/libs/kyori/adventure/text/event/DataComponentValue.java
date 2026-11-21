package com.earth2me.essentials.libs.kyori.adventure.text.event;

import com.earth2me.essentials.libs.kyori.adventure.nbt.api.BinaryTagHolder;
import com.earth2me.essentials.libs.kyori.examination.Examinable;
import org.jetbrains.annotations.NotNull;

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
