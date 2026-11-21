package dev.demora.demoralegendaryweapons.weapons.voidstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PortalManager {
   private final DemoraLegendaryWeapons plugin;
   private final Map<UUID, PortalManager.PortalPair> playerPortals;
   private final Map<Location, UUID> portalLocations;
   private final Set<UUID> playersInPortal;

   public PortalManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.playerPortals = new HashMap<>();
      this.portalLocations = new HashMap<>();
      this.playersInPortal = new HashSet<>();
   }

   public boolean hasFirstPortal(Player player) {
      PortalManager.PortalPair pair = this.playerPortals.get(player.getUniqueId());
      return pair != null && pair.getPortal1() != null;
   }

   public boolean createPortal(Player player, Location location) {
      UUID playerId = player.getUniqueId();
      PortalManager.PortalPair existingPair = this.playerPortals.get(playerId);
      if (existingPair == null) {
         Location portal1 = this.placePortalBlock(location);
         if (portal1 != null) {
            PortalManager.PortalPair newPair = new PortalManager.PortalPair(portal1, null, playerId);
            this.playerPortals.put(playerId, newPair);
            this.portalLocations.put(portal1, playerId);
            this.startPortalEffects(portal1);
            return false;
         }
      } else {
         if (existingPair.getPortal2() != null) {
            player.sendMessage(DemoraLegendaryWeapons.getInstance().getMessageWithPrefix("weapon-messages.portal-already-exists"));
            return false;
         }

         Location portal2 = this.placePortalBlock(location);
         if (portal2 != null) {
            existingPair.setPortal2(portal2);
            this.portalLocations.put(portal2, playerId);
            this.startPortalEffects(portal2);
            this.schedulePortalRemoval(playerId);
            return true;
         }
      }

      return false;
   }

   private Location placePortalBlock(Location location) {
      Block block = location.getBlock();
      if (this.plugin.isRegionProtected(location)) {
         return null;
      } else if (block.getType() == Material.AIR) {
         block.setType(Material.END_GATEWAY);
         return block.getLocation();
      } else {
         return null;
      }
   }

   private void startPortalEffects(Location location) {
      (new BukkitRunnable() {
         public void run() {
            if (location.getBlock().getType() != Material.END_GATEWAY) {
               this.cancel();
            } else {
               location.getWorld().spawnParticle(Particle.PORTAL, location.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
               location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location.clone().add(0.5, 1.5, 0.5), 5, 0.2, 0.2, 0.2, 0.05);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 10L);
   }

   private void schedulePortalRemoval(UUID playerId) {
      (new BukkitRunnable() {
         public void run() {
            PortalManager.this.removePortals(playerId);
         }
      }).runTaskLater(this.plugin, 900L);
   }

   public void removePortals(UUID playerId) {
      PortalManager.PortalPair pair = this.playerPortals.remove(playerId);
      if (pair != null) {
         if (pair.getPortal1() != null) {
            pair.getPortal1().getBlock().setType(Material.AIR);
            this.portalLocations.remove(pair.getPortal1());
         }

         if (pair.getPortal2() != null) {
            pair.getPortal2().getBlock().setType(Material.AIR);
            this.portalLocations.remove(pair.getPortal2());
         }

         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player != null) {
            player.sendMessage(DemoraLegendaryWeapons.getInstance().getMessageWithPrefix("weapon-messages.portal-removed"));
         }
      }
   }

   public void handlePortalTouch(Player player, Location portalLocation) {
      final UUID playerId = player.getUniqueId();
      if (!this.playersInPortal.contains(playerId)) {
         UUID portalOwner = this.portalLocations.get(portalLocation);
         if (portalOwner != null) {
            PortalManager.PortalPair pair = this.playerPortals.get(portalOwner);
            if (pair != null && pair.getPortal2() != null) {
               Location destination;
               if (portalLocation.equals(pair.getPortal1())) {
                  destination = pair.getPortal2().clone().add(0.5, 1.0, 0.5);
               } else {
                  if (!portalLocation.equals(pair.getPortal2())) {
                     return;
                  }

                  destination = pair.getPortal1().clone().add(0.5, 1.0, 0.5);
               }

               this.playersInPortal.add(playerId);
               player.teleport(destination);
               player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
               player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1.0, 0.5, 0.1);
               (new BukkitRunnable() {
                  public void run() {
                     PortalManager.this.playersInPortal.remove(playerId);
                  }
               }).runTaskLater(this.plugin, 20L);
            }
         }
      }
   }

   public boolean isPortalBlock(Location location) {
      return this.portalLocations.containsKey(location);
   }

   private static class PortalPair {
      private final Location portal1;
      private Location portal2;
      private final UUID owner;

      public PortalPair(Location portal1, Location portal2, UUID owner) {
         this.portal1 = portal1;
         this.portal2 = portal2;
         this.owner = owner;
      }

      public Location getPortal1() {
         return this.portal1;
      }

      public Location getPortal2() {
         return this.portal2;
      }

      public void setPortal2(Location portal2) {
         this.portal2 = portal2;
      }

      public UUID getOwner() {
         return this.owner;
      }
   }
}
