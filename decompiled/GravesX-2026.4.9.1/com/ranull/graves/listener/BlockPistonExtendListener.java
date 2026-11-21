package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.event.GravePistonExtendEvent;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class BlockPistonExtendListener implements Listener {
   private final Graves plugin;

   public BlockPistonExtendListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      BlockFace direction = event.getDirection();
      Block piston = event.getBlock();
      List<Block> blocks = event.getBlocks();

      for (Block pushedBlock : blocks) {
         Block destination = pushedBlock.getRelative(direction);
         Grave grave = this.plugin.getCacheManager().getGrave(destination);
         if (grave != null) {
            this.handleGravePistonMove(event, grave, piston, direction, blocks);
            return;
         }
      }
   }

   private void handleGravePistonMove(BlockPistonExtendEvent event, Grave grave, Block piston, BlockFace direction, List<Block> blocks) {
      boolean allowPush = this.plugin.getConfigManager().getConfigSection("drop.piston-extend", grave).getBoolean("drop.piston-extend", true);
      this.plugin.debugMessage("allowPush value for grave at " + grave.getLocationDeath() + " is: " + allowPush, 2);
      if (!allowPush) {
         this.plugin.debugMessage("Push is forbidden for grave at " + grave.getLocationDeath() + " with piston at " + piston.getLocation(), 2);
         event.setCancelled(true);
      } else {
         this.plugin.debugMessage("Push is allowed for grave at " + grave.getLocationDeath() + " with piston at " + piston.getLocation(), 2);
         GravePistonExtendEvent modern = new GravePistonExtendEvent(grave, piston.getLocation(), piston, direction, blocks);
         this.plugin
            .debugMessage("Created modern GravePistonExtendEvent for grave at " + grave.getLocationDeath() + " with piston at " + piston.getLocation(), 2);
         this.plugin.getServer().getPluginManager().callEvent(modern);
         com.ranull.graves.event.GravePistonExtendEvent legacy = new com.ranull.graves.event.GravePistonExtendEvent(
            grave, piston.getLocation(), piston, direction, blocks
         );
         this.plugin
            .debugMessage("Created legacy GravePistonExtendEvent for grave at " + grave.getLocationDeath() + " with piston at " + piston.getLocation(), 2);
         this.plugin.getServer().getPluginManager().callEvent(legacy);
         if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
            this.plugin.debugMessage("Piston move allowed for grave at " + grave.getLocationDeath() + ". Breaking grave...", 2);

            try {
               Location loc = grave.getLocationDeath();
               Objects.requireNonNull(loc.getWorld()).spawnParticle(this.plugin.getVersionManager().getParticleForVersion("EXPLOSION"), loc, 1);

               try {
                  loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
               } catch (Exception var11) {
                  loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
               }
            } catch (Exception var12) {
            }

            this.plugin.getGraveManager().breakGrave(grave.getLocationDeath(), grave);
            this.plugin.getGraveManager().closeGrave(grave);
            this.plugin.getGraveManager().playEffect("effect.loot", piston.getLocation(), grave);
            this.plugin.getEntityManager().runCommands("event.command.pistonextend", piston.getType().name(), piston.getLocation(), grave);
         } else {
            this.plugin.debugMessage("Piston move for grave at " + grave.getLocationDeath() + " was cancelled or is an addon. No action taken.", 2);
         }
      }
   }
}
