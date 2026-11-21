package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

public class FlowerPotCompatibilityHandler implements NBTCompatibilityHandler {
   @Override
   public BaseBlock updateNbt(BaseBlock block) {
      if (block.getBlockType() != BlockTypes.FLOWER_POT) {
         return block;
      } else {
         LinCompoundTag tag = block.getNbt();
         if (tag == null) {
            return block;
         } else {
            LinStringTag item = tag.findTag("Item", LinTagType.stringTag());
            if (item == null) {
               return block;
            } else {
               String id = item.value();
               if (id.isEmpty()) {
                  return BlockTypes.FLOWER_POT.getDefaultState().toBaseBlock();
               } else {
                  int data = 0;
                  LinIntTag dataTag = tag.findTag("Data", LinTagType.intTag());
                  if (dataTag != null) {
                     data = dataTag.valueAsInt();
                  }

                  BlockState newState = this.convertLegacyBlockType(id, data);
                  return newState != null ? newState.toBaseBlock() : block;
               }
            }
         }
      }
   }

   private BlockState convertLegacyBlockType(String id, int data) {
      int newId = switch (id) {
         case "minecraft:red_flower" -> 38;
         case "minecraft:yellow_flower" -> 37;
         case "minecraft:sapling" -> 6;
         case "minecraft:deadbush", "minecraft:tallgrass" -> 31;
         default -> 0;
      };
      String plantedName = null;
      if (newId == 0 && id.startsWith("minecraft:")) {
         plantedName = id.substring(10);
      } else {
         BlockState plantedWithData = LegacyMapper.getInstance().getBlockFromLegacy(newId, data);
         if (plantedWithData != null) {
            plantedName = plantedWithData.getBlockType().id().substring(10);
         }
      }

      if (plantedName != null) {
         BlockType potAndPlanted = BlockTypes.get("minecraft:potted_" + plantedName);
         if (potAndPlanted != null) {
            return potAndPlanted.getDefaultState();
         }
      }

      return null;
   }
}
