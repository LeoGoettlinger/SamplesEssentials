package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class ScriptBlockActions {
   private ScriptBlockActions() {
   }

   public static boolean placeBlock(Player player, Block block, Material expected, BlockData replacement, ItemStack source, EquipmentSlot hand) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Script placement requires the server thread");
      } else if (player != null
         && player.isOnline()
         && player.isValid()
         && !player.isDead()
         && block != null
         && player.getWorld().equals(block.getWorld())
         && expected != null
         && replacement != null
         && block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
         && block.getY() > block.getWorld().getMinHeight()
         && block.getY() < block.getWorld().getMaxHeight()
         && block.getType() == expected
         && !(block.getState() instanceof TileState)
         && LocationQueryRegistry.canBuild(player, block.getLocation())) {
         BlockState original = block.getState();
         String originalData = original.getBlockData().getAsString();
         String placedData = replacement.getAsString();
         if (originalData.equals(placedData)) {
            return false;
         } else {
            block.setBlockData(replacement, false);
            boolean accepted = false;

            boolean var11;
            try {
               ScriptBlockActions.ScriptPlaceEvent event = new ScriptBlockActions.ScriptPlaceEvent(
                  block, original, block.getRelative(BlockFace.DOWN), source == null ? new ItemStack(Material.AIR) : source.clone(), player, hand
               );
               Bukkit.getPluginManager().callEvent(event);
               accepted = !event.isCancelled()
                  && event.canBuild()
                  && player.isOnline()
                  && player.isValid()
                  && !player.isDead()
                  && player.getWorld().equals(block.getWorld())
                  && block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
                  && placedData.equals(block.getBlockData().getAsString())
                  && LocationQueryRegistry.canBuild(player, block.getLocation());
               var11 = accepted;
            } finally {
               if (!accepted && block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4) && placedData.equals(block.getBlockData().getAsString())) {
                  original.update(true, false);
               }
            }

            return var11;
         }
      } else {
         return false;
      }
   }

   public static boolean breakBlock(Player player, Block block, Material expected, ItemStack drop) {
      return breakBlock(player, block, expected, drop, null, true);
   }

   public static boolean breakNaturally(Player player, Block block, Material expected, ItemStack tool, boolean dropItems) {
      return breakBlock(player, block, expected, null, Objects.requireNonNull(tool, "tool").clone(), dropItems);
   }

   private static boolean breakBlock(Player player, Block block, Material expected, ItemStack drop, ItemStack tool, boolean dropItems) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Script mining requires the server thread");
      } else if (player != null
         && player.isOnline()
         && player.isValid()
         && !player.isDead()
         && block != null
         && player.getWorld().equals(block.getWorld())
         && expected != null
         && !expected.isAir()
         && block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
         && block.getY() >= block.getWorld().getMinHeight()
         && block.getY() < block.getWorld().getMaxHeight()
         && block.getType() == expected
         && !(block.getState() instanceof TileState)
         && LocationQueryRegistry.canBuild(player, block.getLocation())) {
         ItemStack capturedDrop = drop == null ? null : drop.clone();
         if (capturedDrop == null || capturedDrop.getAmount() >= 1 && !capturedDrop.getType().isAir() && capturedDrop.getType().isItem()) {
            String original = block.getBlockData().getAsString();
            BlockBreakEvent event = new ScriptBlockActions.ScriptBreakEvent(block, player);
            event.setDropItems(dropItems && (capturedDrop != null || tool != null));
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()
               && player.isOnline()
               && player.isValid()
               && !player.isDead()
               && player.getWorld().equals(block.getWorld())
               && block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
               && original.equals(block.getBlockData().getAsString())
               && LocationQueryRegistry.canBuild(player, block.getLocation())) {
               List<ItemStack> drops = !dropItems || !event.isDropItems()
                  ? List.of()
                  : (
                     tool != null
                        ? block.getDrops(tool, player).stream().<ItemStack>map(ItemStack::clone).toList()
                        : (capturedDrop == null ? List.of() : List.of(capturedDrop))
                  );
               block.setType(Material.AIR);
               if (block.getType() != Material.AIR) {
                  return false;
               } else {
                  for (ItemStack result : drops) {
                     block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), result);
                  }

                  return true;
               }
            } else {
               return false;
            }
         } else {
            throw new IllegalArgumentException("Invalid authored mining drop");
         }
      } else {
         return false;
      }
   }

   private static final class ScriptBreakEvent extends BlockBreakEvent {
      private ScriptBreakEvent(Block block, Player player) {
         super(block, player);
      }
   }

   private static final class ScriptPlaceEvent extends BlockPlaceEvent {
      private ScriptPlaceEvent(Block block, BlockState original, Block against, ItemStack item, Player player, EquipmentSlot hand) {
         super(block, original, against, item, player, true, hand);
      }
   }
}
