package com.viaversion.viabackwards.protocol.v1_13to1_12_2.block_entity_handlers;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viabackwards.protocol.v1_13to1_12_2.provider.BackwardsBlockEntityProvider;
import java.util.StringJoiner;
import java.util.Map.Entry;

public class PistonHandler implements BackwardsBlockEntityProvider.BackwardsBlockEntityHandler {
   @Override
   public CompoundTag transform(int blockId, CompoundTag tag) {
      CompoundTag blockState = tag.getCompoundTag("blockState");
      if (blockState == null) {
         return tag;
      } else {
         String dataFromTag = this.getDataFromTag(blockState);
         if (dataFromTag == null) {
            return tag;
         } else {
            int id = Protocol1_13To1_12_2.MAPPINGS.getPistonIds().getInt(dataFromTag);
            if (id == -1) {
               return tag;
            } else {
               tag.putInt("blockId", id >> 4);
               tag.putInt("blockData", id & 15);
               return tag;
            }
         }
      }
   }

   private String getDataFromTag(CompoundTag tag) {
      StringTag name = tag.getStringTag("Name");
      if (name == null) {
         return null;
      } else {
         CompoundTag properties = tag.getCompoundTag("Properties");
         if (properties == null) {
            return name.getValue();
         } else {
            StringJoiner joiner = new StringJoiner(",", name.getValue() + "[", "]");

            for (Entry<String, Tag> entry : properties) {
               if (entry.getValue() instanceof StringTag) {
                  joiner.add(entry.getKey() + "=" + ((StringTag)entry.getValue()).getValue());
               }
            }

            return joiner.toString();
         }
      }
   }
}
