package com.lishid.openinv.internal;

import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AnySilentContainerBase implements IAnySilentContainer {
   @Override
   public boolean isAnyContainerNeeded(@NotNull Block block) {
      BlockState blockState = this.getState(block);
      if (blockState instanceof Barrel) {
         return false;
      } else if (blockState instanceof EnderChest) {
         return block.getRelative(0, 1, 0).getType().isOccluding();
      } else if (blockState instanceof ShulkerBox) {
         return this.isShulkerBlocked(block);
      } else if (!(blockState instanceof Chest)) {
         return false;
      } else if (this.isChestBlocked(block)) {
         return true;
      } else if (block.getBlockData() instanceof org.bukkit.block.data.type.Chest chest && chest.getType() != Type.SINGLE) {
         BlockFace relativeFace = switch (chest.getFacing()) {
            case NORTH -> chest.getType() == Type.RIGHT ? BlockFace.WEST : BlockFace.EAST;
            case EAST -> chest.getType() == Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
            case SOUTH -> chest.getType() == Type.RIGHT ? BlockFace.EAST : BlockFace.WEST;
            case WEST -> chest.getType() == Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
            default -> BlockFace.SELF;
         };
         Block relative = block.getRelative(relativeFace);
         if (relative.getType() != block.getType()) {
            return false;
         } else if (!(relative.getBlockData() instanceof org.bukkit.block.data.type.Chest relativeChest)) {
            return false;
         } else {
            return relativeChest.getFacing() == chest.getFacing() && relativeChest.getType() == (chest.getType() == Type.RIGHT ? Type.LEFT : Type.RIGHT)
               ? this.isChestBlocked(relative)
               : false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isAnySilentContainer(@NotNull Block block) {
      return this.isAnySilentContainer(this.getState(block));
   }

   @Override
   public boolean isAnySilentContainer(@NotNull Inventory inventory) {
      return this.isAnySilentContainer(this.getHolder(inventory));
   }

   protected abstract BlockState getState(@NotNull Block var1);

   protected abstract InventoryHolder getHolder(@NotNull Inventory var1);
}
