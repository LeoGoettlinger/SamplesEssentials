package com.magmaguy.elitemobs.advancedcombat.constructs;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public record PacketConstructDefinition(String id, double viewRange, List<PacketConstructDefinition.BlockVisual> blocks) {
   public PacketConstructDefinition(String id, double viewRange, List<PacketConstructDefinition.BlockVisual> blocks) {
      if (id != null && !id.isBlank()) {
         if (Double.isFinite(viewRange) && !(viewRange <= 0.0)) {
            blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
            if (blocks.isEmpty()) {
               throw new IllegalArgumentException("Construct needs at least one visual block");
            } else {
               Set<PacketConstructDefinition.Offset> offsets = new HashSet<>();

               for (PacketConstructDefinition.BlockVisual block : blocks) {
                  PacketConstructDefinition.Offset offset = new PacketConstructDefinition.Offset(block.relativeX(), block.relativeY(), block.relativeZ());
                  if (!offsets.add(offset)) {
                     throw new IllegalArgumentException("Duplicate construct block offset " + offset + " in " + id);
                  }
               }

               this.id = id;
               this.viewRange = viewRange;
               this.blocks = blocks;
            }
         } else {
            throw new IllegalArgumentException("Construct view range must be positive");
         }
      } else {
         throw new IllegalArgumentException("Construct id is required");
      }
   }

   public static PacketConstructDefinition.BlockVisual block(int relativeX, int relativeY, int relativeZ, Material material) {
      Objects.requireNonNull(material, "material");
      if (!material.isBlock()) {
         throw new IllegalArgumentException(material + " is not a block");
      } else {
         return block(relativeX, relativeY, relativeZ, material.createBlockData());
      }
   }

   public static PacketConstructDefinition.BlockVisual block(int relativeX, int relativeY, int relativeZ, BlockData blockData) {
      Objects.requireNonNull(blockData, "blockData");
      return new PacketConstructDefinition.BlockVisual(relativeX, relativeY, relativeZ, blockData.getAsString());
   }

   public record BlockVisual(int relativeX, int relativeY, int relativeZ, String serializedBlockData) {
      public BlockVisual(int relativeX, int relativeY, int relativeZ, String serializedBlockData) {
         if (serializedBlockData != null && !serializedBlockData.isBlank()) {
            this.relativeX = relativeX;
            this.relativeY = relativeY;
            this.relativeZ = relativeZ;
            this.serializedBlockData = serializedBlockData;
         } else {
            throw new IllegalArgumentException("Construct block data is required");
         }
      }
   }

   private record Offset(int x, int y, int z) {
   }
}
