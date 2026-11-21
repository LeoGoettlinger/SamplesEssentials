package com.lishid.openinv.internal;

import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface IAnySilentContainer {
   boolean activateContainer(@NotNull Player var1, boolean var2, @NotNull Block var3);

   void deactivateContainer(@NotNull Player var1);

   boolean isAnyContainerNeeded(@NotNull Block var1);

   default boolean isShulkerBlocked(@NotNull Block shulkerBox) {
      Directional directional = (Directional)shulkerBox.getBlockData();
      BlockFace facing = directional.getFacing();
      BoundingBox box = new BoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
      box.expand(facing, 0.5);
      box.shift(facing.getOppositeFace().getDirection());
      return shulkerBox.getRelative(facing).getCollisionShape().overlaps(box);
   }

   default boolean isChestBlocked(@NotNull Block chest) {
      Block relative = chest.getRelative(0, 1, 0);
      return relative.getType().isOccluding() || !chest.getWorld().getNearbyEntities(BoundingBox.of(relative), Cat.class::isInstance).isEmpty();
   }

   boolean isAnySilentContainer(@NotNull Block var1);

   default boolean isAnySilentContainer(@NotNull BlockState blockState) {
      return blockState instanceof InventoryHolder holder && this.isAnySilentContainer(holder) || blockState instanceof EnderChest;
   }

   default boolean isAnySilentContainer(@NotNull InventoryHolder holder) {
      return holder instanceof EnderChest
         || holder instanceof Chest
         || holder instanceof DoubleChest
         || holder instanceof ShulkerBox
         || holder instanceof Barrel;
   }

   boolean isAnySilentContainer(@NotNull Inventory var1);
}
