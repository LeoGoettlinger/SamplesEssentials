package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

public class BannerBlockCompatibilityHandler implements NBTCompatibilityHandler {
   private static final Property<Direction> FacingProperty;
   private static final Property<Integer> RotationProperty;

   @Override
   public BaseBlock updateNbt(BaseBlock block) {
      BlockType blockType = block.getBlockType();
      if (blockType != BlockTypes.WHITE_BANNER && blockType != BlockTypes.WHITE_WALL_BANNER) {
         return block;
      } else {
         LinCompoundTag nbt = block.getNbt();
         if (nbt == null) {
            return block;
         } else {
            LinIntTag typeTag = nbt.findTag("Base", LinTagType.intTag());
            if (typeTag != null) {
               boolean isWall = block.getBlockType() == BlockTypes.WHITE_WALL_BANNER;
               String bannerType = convertBannerType(typeTag.valueAsInt(), isWall);
               if (bannerType != null) {
                  BlockType type = BlockTypes.get("minecraft:" + bannerType);
                  if (type != null) {
                     BlockState state = type.getDefaultState();
                     if (isWall) {
                        Property<Direction> facingProp = type.getProperty("facing");
                        state = state.with(facingProp, block.getState(FacingProperty));
                     } else {
                        Property<Integer> rotationProp = type.getProperty("rotation");
                        state = state.with(rotationProp, block.getState(RotationProperty));
                     }

                     LinCompoundTag.Builder nbtBuilder = nbt.toBuilder();
                     nbtBuilder.remove("Base");
                     LinListTag<LinCompoundTag> patternsTag = nbt.findListTag("Patterns", LinTagType.compoundTag());
                     if (patternsTag != null) {
                        LinListTag.Builder<LinCompoundTag> newPatterns = LinListTag.builder(LinTagType.compoundTag());

                        for (LinCompoundTag pattern : patternsTag.value()) {
                           LinIntTag color = pattern.findTag("Color", LinTagType.intTag());
                           if (color != null) {
                              newPatterns.add(pattern.toBuilder().putInt("Color", 15 - color.valueAsInt()).build());
                           } else {
                              newPatterns.add(pattern);
                           }
                        }

                        nbtBuilder.put("Patterns", newPatterns.build());
                     }

                     return state.toBaseBlock(nbtBuilder.build());
                  }
               }
            }

            return block;
         }
      }
   }

   private static String convertBannerType(int oldType, boolean isWall) {
      String color = switch (oldType) {
         case 0 -> "black";
         case 1 -> "red";
         case 2 -> "green";
         case 3 -> "brown";
         case 4 -> "blue";
         case 5 -> "purple";
         case 6 -> "cyan";
         case 7 -> "light_gray";
         case 8 -> "gray";
         case 9 -> "pink";
         case 10 -> "lime";
         case 11 -> "yellow";
         case 12 -> "light_blue";
         case 13 -> "magenta";
         case 14 -> "orange";
         case 15 -> "white";
         default -> null;
      };
      return color == null ? null : color + (isWall ? "_wall_banner" : "_banner");
   }

   static {
      Property<Direction> tempFacing;
      Property<Integer> tempRotation;
      try {
         tempFacing = BlockTypes.WHITE_WALL_BANNER.getProperty("facing");
         tempRotation = BlockTypes.WHITE_BANNER.getProperty("rotation");
      } catch (IllegalArgumentException | ClassCastException | NullPointerException var3) {
         tempFacing = null;
         tempRotation = null;
      }

      FacingProperty = tempFacing;
      RotationProperty = tempRotation;
   }
}
