package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilityParticleEnum;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.event.GraveAutoLootEvent;
import dev.cwhead.GravesX.event.GraveBreakEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
   private final Graves plugin;

   public BlockBreakListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      Grave grave = this.plugin.getCacheManager().getGrave(block);
      if (grave != null) {
         if (this.isGraveBreakAllowed(grave)) {
            if (this.plugin.getEntityManager().canOpenGrave(player, grave)) {
               this.handleGraveBreak(event, player, block, grave);
            } else {
               this.plugin.getEntityManager().sendMessage("message.protection", player, player.getLocation(), grave);
               event.setCancelled(true);
            }
         } else {
            event.setCancelled(true);
         }
      } else if (this.plugin.getGraveManager().isNearGrave(block.getLocation(), player)) {
         event.setCancelled(true);
      }
   }

   private boolean isGraveBreakAllowed(Grave grave) {
      return this.plugin.getConfigManager().getConfigSection("grave.break", grave).getBoolean("grave.break");
   }

   private void handleGraveBreak(BlockBreakEvent event, Player player, Block block, Grave grave) {
      boolean cfgDrop = this.plugin.getConfigManager().getConfigSection("drop.break", grave).getBoolean("drop.break");
      GraveBreakEvent modern = new GraveBreakEvent(block, player, grave);
      modern.setDropItems(cfgDrop);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveBreakEvent legacy = new com.ranull.graves.event.GraveBreakEvent(block, player, grave);
      legacy.setDropItems(cfgDrop);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      boolean cancelled = modern.isCancelled() || legacy.isCancelled();
      boolean addon = modern.isAddon() || legacy.isAddon();
      event.setDropItems(false);
      if (!cancelled && !addon) {
         boolean dropItemsFinal = modern.isDropItems() && legacy.isDropItems();
         modern.setDropItems(dropItemsFinal);
         if (this.plugin.getConfigManager().getConfigSection("drop.auto-loot.enabled", grave).getBoolean("drop.auto-loot.enabled")
            && this.plugin.getConfigManager().getConfigSection("drop.auto-loot.break", grave).getBoolean("drop.auto-loot.break")) {
            if (player.isSneaking()) {
               this.handleAutoLoot(event, player, block, grave, modern);
            } else {
               this.explodeEffectGrave(grave);
               this.plugin.getGraveManager().breakGrave(block.getLocation(), grave);
            }
         } else if (dropItemsFinal) {
            this.explodeEffectGrave(grave);
            this.plugin.getGraveManager().breakGrave(block.getLocation(), grave);
         } else {
            this.explodeEffectGrave(grave);
            this.plugin.getGraveManager().removeGrave(grave);
         }

         this.finalizeGraveBreak(player, block, grave);
      } else if (cancelled && !addon) {
         event.setCancelled(true);
      }
   }

   private void explodeEffectGrave(Grave grave) {
      try {
         Location loc = grave.getLocationDeath();
         Objects.requireNonNull(loc.getWorld()).spawnParticle(CompatibilityParticleEnum.valueOf("EXPLOSION"), loc, 1);

         try {
            loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
         } catch (Exception var4) {
            loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
         }
      } catch (Exception var5) {
      }
   }

   private void handleAutoLoot(BlockBreakEvent event, Player player, Block block, Grave grave, GraveBreakEvent graveBreakEvent) {
      Location hitLoc = block.getLocation();
      GraveAutoLootEvent modern = new GraveAutoLootEvent(player, hitLoc, grave);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveAutoLootEvent legacy = new com.ranull.graves.event.GraveAutoLootEvent(player, hitLoc, grave);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         if (graveBreakEvent.isDropItems()) {
            try {
               Location loc = grave.getLocationDeath();
               Objects.requireNonNull(loc.getWorld()).spawnParticle(CompatibilityParticleEnum.valueOf("EXPLOSION"), loc, 1);

               try {
                  loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
               } catch (Exception var11) {
                  loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
               }
            } catch (Exception var12) {
            }

            this.plugin.getGraveManager().autoLootGrave(player, hitLoc, grave);
            if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
               if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                  this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
               }

               if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                  this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   private void finalizeGraveBreak(Player player, Block block, Grave grave) {
      this.plugin.getGraveManager().closeGrave(grave);
      this.plugin.getGraveManager().playEffect("effect.loot", block.getLocation(), grave);
      this.plugin.getEntityManager().spawnZombie(block.getLocation(), player, player, grave);
      this.plugin.getEntityManager().runCommands("event.command.break", player, block.getLocation(), grave);
   }
}
