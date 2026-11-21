package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GraveCompassUseEvent;
import dev.cwhead.GravesX.event.GraveParticleEvent;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
   private final Graves plugin;

   public PlayerInteractListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      if (this.isMainHandInteraction(event) && this.isNotSpectatorMode(player)) {
         if (event.getClickedBlock() != null) {
            this.handleBlockInteraction(event, player);
         }

         if (event.getItem() != null) {
            this.handleCompassInteraction(event, player);
         }
      }
   }

   private boolean isMainHandInteraction(PlayerInteractEvent event) {
      return !this.plugin.getVersionManager().hasSecondHand() || event.getHand() != null && event.getHand() == EquipmentSlot.HAND;
   }

   private boolean isNotSpectatorMode(Player player) {
      if (this.plugin.getVersionManager().is_v1_7()) {
         return true;
      } else {
         return this.plugin.getPermissionManager().hasGrantedPermission("graves.spectator.bypass", player.getPlayer())
            ? true
            : Objects.requireNonNull(player.getPlayer()).getGameMode() != GameMode.SPECTATOR;
      }
   }

   private void handleBlockInteraction(PlayerInteractEvent event, Player player) {
      Block block = event.getClickedBlock();
      if (block != null) {
         if (event.useInteractedBlock() != Result.DENY && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            this.handleGraveInteraction(event, player, block);
         }
      }
   }

   private void handleGraveInteraction(PlayerInteractEvent event, Player player, Block block) {
      Grave grave = this.plugin.getCacheManager().getGrave(block);
      if (grave == null) {
         Block blockRelative = block.getRelative(event.getBlockFace());
         if (!blockRelative.getType().isSolid()) {
            grave = this.plugin.getCacheManager().getGrave(blockRelative);
         }
      }

      if (grave != null) {
         event.setCancelled(true);
         Grave finalGrave = grave;

         try {
            this.plugin.getSchedulerManager().runTaskLater(() -> this.plugin.getGraveManager().openGrave(player, block.getLocation(), finalGrave), 1L);
         } catch (Exception var7) {
            this.plugin.getLogger().severe("Failed to open grave at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
            this.plugin.logStackTrace(var7);
         }
      }
   }

   private void handleCompassInteraction(PlayerInteractEvent event, Player player) {
      ItemStack itemStack = event.getItem();
      if (itemStack != null) {
         UUID uuid = this.plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack);
         if (uuid != null) {
            if (!this.plugin.getCacheManager().getGraveMap().containsKey(uuid)) {
               player.getInventory().remove(itemStack);
               event.setCancelled(true);
            } else {
               Grave grave = this.plugin.getCacheManager().getGraveMap().get(uuid);
               List<Location> locationList = this.plugin.getGraveManager().getGraveLocationList(player.getLocation(), grave);
               if (locationList.isEmpty()) {
                  player.getInventory().remove(itemStack);
                  event.setCancelled(true);
               } else {
                  Location location = locationList.get(0);
                  if (event.getClickedBlock() != null
                     && this.plugin.getLocationManager().hasGrave(event.getClickedBlock().getLocation())
                     && player.getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("compass")) {
                     player.getInventory().remove(itemStack);
                     event.setCancelled(true);
                  } else {
                     ItemStack graveCompass = this.plugin.getEntityManager().createGraveCompass(player, location, grave);
                     if (graveCompass != null && graveCompass.getItemMeta() != null && !graveCompass.getItemMeta().getDisplayName().contains("Abandoned")) {
                        GraveCompassUseEvent modernUse = new GraveCompassUseEvent(player, grave);
                        this.plugin.getServer().getPluginManager().callEvent(modernUse);
                        com.ranull.graves.event.GraveCompassUseEvent legacyUse = new com.ranull.graves.event.GraveCompassUseEvent(player, grave);
                        this.plugin.getServer().getPluginManager().callEvent(legacyUse);
                        boolean blockedUse = modernUse.isCancelled() || modernUse.isAddon() || legacyUse.isCancelled() || legacyUse.isAddon();
                        if (!blockedUse) {
                           player.getInventory().setItem(player.getInventory().getHeldItemSlot(), graveCompass);
                           this.plugin
                              .getEntityManager()
                              .runFunction(
                                 player,
                                 Objects.requireNonNull(
                                    this.plugin.getConfigManager().getConfigSection("compass.function", grave).getString("compass.function")
                                 ),
                                 grave
                              );
                           if (this.plugin.getConfigManager().getConfigSection("compass.particles.enabled", grave).getBoolean("compass.particles.enabled")) {
                              GraveParticleEvent modernParticle = new GraveParticleEvent(player, grave);
                              this.plugin.getServer().getPluginManager().callEvent(modernParticle);
                              com.ranull.graves.event.GraveParticleEvent legacyParticle = new com.ranull.graves.event.GraveParticleEvent(player, grave);
                              this.plugin.getServer().getPluginManager().callEvent(legacyParticle);
                              boolean blockedParticle = modernParticle.isCancelled()
                                 || modernParticle.isAddon()
                                 || legacyParticle.isCancelled()
                                 || legacyParticle.isAddon();
                              if (!blockedParticle) {
                                 this.plugin
                                    .getParticleManager()
                                    .startCompassParticleTrail(
                                       player.getLocation(),
                                       grave.getLocationDeath(),
                                       this.plugin
                                          .getVersionManager()
                                          .getParticleForVersion(
                                             Objects.requireNonNull(
                                                   this.plugin
                                                      .getConfigManager()
                                                      .getConfigSection("compass.particles.particle", grave)
                                                      .getString("compass.particles.particle")
                                                )
                                                .toUpperCase()
                                          ),
                                       this.plugin.getConfigManager().getConfigSection("compass.particles.count", grave).getInt("compass.particles.count", 5),
                                       this.plugin
                                          .getConfigManager()
                                          .getConfigSection("compass.particles.speed", grave)
                                          .getDouble("compass.particles.speed", 0.3),
                                       this.plugin
                                          .getConfigManager()
                                          .getConfigSection("compass.particles.duration", grave)
                                          .getInt("compass.particles.duration"),
                                       player.getUniqueId()
                                    );
                              }
                           }
                        }

                        event.setCancelled(true);
                     } else {
                        player.getInventory().remove(itemStack);
                        event.setCancelled(true);
                     }
                  }
               }
            }
         }
      }
   }
}
