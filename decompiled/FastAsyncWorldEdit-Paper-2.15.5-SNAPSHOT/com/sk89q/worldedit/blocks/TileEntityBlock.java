package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.NbtValued;

public interface TileEntityBlock extends NbtValued {
   default String getNbtId() {
      CompoundTag nbtData = this.getNbtData();
      if (nbtData == null) {
         return "";
      } else {
         Tag idTag = nbtData.getValue().get("id");
         return idTag instanceof StringTag ? ((StringTag)idTag).getValue() : "";
      }
   }
}
