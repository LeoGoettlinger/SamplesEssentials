package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.data.ChunkData;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.LocationUtil;
import com.ranull.graves.util.MaterialUtil;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.event.GraveWalkOverEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerMoveListener implements Listener {
   private final Graves plugin;
   private final Map<UUID, Long> compassCheckCooldown = new HashMap<>();

   public PlayerMoveListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerMove(PlayerMoveEvent event) {
      if (!event.isCancelled()) {
         Player player = event.getPlayer();
         if (this.isNotSpectatorMode(player) && this.hasPlayerMoved(event)) {
            Location location = LocationUtil.roundLocation(player.getLocation());
            if (this.isLocationSafe(location)) {
               this.plugin.getLocationManager().setLastSolidLocation(player, location.clone());
            }

            if (this.isLocationContainingGrave(location)) {
               this.handleGraveAutoLootOnWalk(event, player, location);
            }

            this.removeSpecificCompassNearGrave(player, location);
         }
      }
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

   private boolean hasPlayerMoved(PlayerMoveEvent event) {
      return event.getTo() != null
         && (
            event.getTo().getBlockX() != event.getFrom().getBlockX()
               || event.getTo().getBlockY() != event.getFrom().getBlockY()
               || event.getTo().getBlockZ() != event.getFrom().getBlockZ()
         );
   }

   private boolean isLocationSafe(Location location) {
      Material feet = location.getBlock().getType();
      Material head = location.getBlock().getRelative(BlockFace.UP).getType();
      return this.plugin.getLocationManager().isInsideBorder(location)
         && !MaterialUtil.isWater(feet)
         && !MaterialUtil.isWater(head)
         && location.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()
         && this.plugin.getLocationManager().isLocationSafePlayer(location);
   }

   private boolean isLocationContainingGrave(Location location) {
      return location.getWorld() != null && this.plugin.getDataManager().hasChunkData(location);
   }

   private void handleGraveAutoLootOnWalk(PlayerMoveEvent event, Player player, Location location) {
      ChunkData chunkData = this.plugin.getDataManager().getChunkData(location);
      BlockData blockData = this.getBlockDataFromLocation(chunkData, location);
      if (blockData != null) {
         Grave grave = this.plugin.getCacheManager().getGrave(blockData.getGraveUUID());
         if (grave != null) {
            if (this.plugin.getConfigManager().getConfigSection("block.walk-over", grave).getBoolean("block.walk-over")) {
               this.plugin.getGraveManager().cleanupCompasses(player, grave);
               GraveWalkOverEvent modern = new GraveWalkOverEvent(player, location, grave);
               this.plugin.getServer().getPluginManager().callEvent(modern);
               com.ranull.graves.event.GraveWalkOverEvent legacy = new com.ranull.graves.event.GraveWalkOverEvent(player, location, grave);
               this.plugin.getServer().getPluginManager().callEvent(legacy);
               if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
                  this.plugin.getGraveManager().autoLootGrave(player, location, grave);
                  if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
                     if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
                        this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
                     }

                     if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
                        this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
                     }
                  }
               }
            }
         }
      }
   }

   private void removeSpecificCompassNearGrave(Player player, Location location) {
      long now = System.currentTimeMillis();
      UUID playerId = player.getUniqueId();
      Long last = this.compassCheckCooldown.get(playerId);
      if (last == null || now - last >= 1000L) {
         this.compassCheckCooldown.put(playerId, now);
         PlayerInventory inventory = player.getInventory();

         Material recoveryCompass;
         try {
            recoveryCompass = Material.valueOf(String.valueOf(this.plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")));
         } catch (IllegalArgumentException var20) {
            return;
         }

         if (location.getWorld() != null) {
            ItemStack[] items = inventory.getContents();
            if (items != null) {
               for (ItemStack item : items) {
                  if (item != null && item.getType() == recoveryCompass) {
                     ItemMeta meta = item.getItemMeta();
                     if (meta != null && meta.hasDisplayName()) {
                        UUID graveUUID = this.getGraveUUIDFromItemStack(item);
                        if (graveUUID != null) {
                           Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUUID);
                           if (grave != null) {
                              Location graveLocation = this.plugin.getGraveManager().getGraveLocation(location, grave);
                              if (graveLocation != null && location.getWorld().equals(graveLocation.getWorld()) && !(location.distance(graveLocation) > 15.0)) {
                                 String configuredName = this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name");
                                 if (configuredName != null) {
                                    String expectedName = StringUtil.parseString("&f" + configuredName, grave, this.plugin);
                                    if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                                       expectedName = MiniMessage.parseString(expectedName);
                                    }

                                    if (meta.getDisplayName().equals(expectedName)) {
                                       inventory.remove(item);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
      if (!itemStack.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String uuidString = (String)meta.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING);
            return uuidString != null ? UUID.fromString(uuidString) : null;
         }
      }
   }

   private BlockData getBlockDataFromLocation(ChunkData chunkData, Location location) {
      BlockData blockData = null;
      if (chunkData.getBlockDataMap().containsKey(location)) {
         blockData = chunkData.getBlockDataMap().get(location);
      } else if (chunkData.getBlockDataMap().containsKey(location.clone().add(0.0, 1.0, 0.0))) {
         blockData = chunkData.getBlockDataMap().get(location.clone().add(0.0, 1.0, 0.0));
      } else if (chunkData.getBlockDataMap().containsKey(location.clone().subtract(0.0, 1.0, 0.0))) {
         blockData = chunkData.getBlockDataMap().get(location.clone().subtract(0.0, 1.0, 0.0));
      }

      return blockData;
   }
}
