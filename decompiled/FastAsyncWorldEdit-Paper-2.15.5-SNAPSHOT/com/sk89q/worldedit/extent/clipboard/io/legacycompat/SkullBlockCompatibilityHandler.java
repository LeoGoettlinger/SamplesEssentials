package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

public class SkullBlockCompatibilityHandler implements NBTCompatibilityHandler {
   private static final Property<Direction> FACING_PROPERTY;

   @Override
   public BaseBlock updateNbt(BaseBlock block) {
      BlockType blockType = block.getBlockType();
      boolean isWall = blockType == BlockTypes.SKELETON_WALL_SKULL;
      if (blockType != BlockTypes.SKELETON_SKULL && !isWall) {
         return block;
      } else if (FACING_PROPERTY == null) {
         return block;
      } else {
         LinCompoundTag tag = block.getNbt();
         if (tag == null) {
            return block;
         } else {
            LinByteTag typeTag = tag.findTag("SkullType", LinTagType.byteTag());
            if (typeTag == null) {
               return block;
            } else {
               String skullType = this.convertSkullType(typeTag.valueAsByte(), isWall);
               if (skullType == null) {
                  return block;
               } else {
                  BlockType type = BlockTypes.get("minecraft:" + skullType);
                  if (type == null) {
                     return block;
                  } else {
                     BlockState state = type.getDefaultState();
                     if (isWall) {
                        Property<Direction> newProp = type.getProperty("facing");
                        state = state.with(newProp, block.getState(FACING_PROPERTY));
                     } else {
                        LinByteTag rotTag = tag.findTag("Rot", LinTagType.byteTag());
                        if (rotTag != null) {
                           Property<Integer> newProp = type.getProperty("rotation");
                           state = state.with(newProp, Integer.valueOf(rotTag.valueAsByte()));
                        }
                     }

                     LinCompoundTag newTag = tag.toBuilder().remove("SkullType").remove("Rot").build();
                     return state.toBaseBlock(newTag);
                  }
               }
            }
         }
      }
   }

   private String convertSkullType(byte oldType, boolean isWall) {
      record SkullData(String kind, String suffix) {
      }


      SkullData skullData = switch (oldType) {
         case 0 -> new SkullData("skeleton", "skull");
         case 1 -> new SkullData("wither_skeleton", "skull");
         case 2 -> new SkullData("zombie", "head");
         case 3 -> new SkullData("player", "head");
         case 4 -> new SkullData("creeper", "head");
         case 5 -> new SkullData("dragon", "head");
         default -> null;
      };
      return skullData == null ? null : skullData.kind + (isWall ? "_wall" : "") + "_" + skullData.suffix;
   }

   static {
      Property<Direction> tempFacing;
      try {
         tempFacing = BlockTypes.SKELETON_WALL_SKULL.getProperty("facing");
      } catch (IllegalArgumentException | ClassCastException | NullPointerException var2) {
         tempFacing = null;
      }

      FACING_PROPERTY = tempFacing;
   }
}
