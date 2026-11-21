package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

final class ReviveBanner {
   private final Match match;
   private final MatchPlayer dead;
   private final ReviveMarker marker;
   private Block bannerBlock;
   private Location deathLocation;
   private TemporaryBlockManager.OwnedBlock lease;
   private BukkitTask watchdog;
   private boolean markerShown;
   private boolean cleared;

   ReviveBanner(Match match, MatchPlayer dead, ReviveMarker marker) {
      this.match = match;
      this.dead = dead;
      this.marker = marker;
      if (dead.lives < 1) {
         this.cleared = true;
      } else if (this.relocate(dead.getPlayer().getLocation())) {
         this.watch();
      }
   }

   MatchPlayer dead() {
      return this.dead;
   }

   boolean isPlaced() {
      return !this.cleared && this.bannerBlock != null;
   }

   Location respawnLocation() {
      return this.bannerBlock.getLocation().clone().add(0.5, 0.0, 0.5);
   }

   private Block findBannerLocation(Location location) {
      Location start = this.match.startDestination();
      if (location.getWorld() != null && location.getY() < location.getWorld().getMinHeight() && start != null) {
         location = start;
      }

      if (location.getWorld() == null) {
         return null;
      } else {
         int x = location.getBlockX();
         int z = location.getBlockZ();
         if (!location.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
            return null;
         } else {
            for (int y = Math.max(location.getBlockY(), location.getWorld().getMinHeight()); y < location.getWorld().getMaxHeight(); y++) {
               Block candidate = location.getWorld().getBlockAt(x, y, z);
               if (candidate.getType().isAir() && !this.match.reviveBanners.containsKey(candidate) && TemporaryBlockManager.canOwn(candidate)) {
                  return candidate;
               }
            }

            return null;
         }
      }
   }

   private boolean relocate(Location location) {
      this.releasePlacement();
      Block candidate = this.findBannerLocation(location.clone());
      if (candidate == null) {
         Logger.warn("Could not place a revive banner for " + this.dead.getPlayer().getName() + ": no free loaded block below world height.");
         this.clear(false);
         return false;
      } else {
         try {
            this.lease = TemporaryBlockManager.replaceOwned(candidate, Material.RED_BANNER.createBlockData(), MatchCore.plugin());
            if (this.lease == null) {
               this.clear(false);
               return false;
            } else {
               this.bannerBlock = candidate;
               this.deathLocation = candidate.getLocation();
               this.match.reviveBanners.put(candidate, this);
               this.match.guard("revive marker", () -> this.marker.show(candidate, this.dead, this.dead.lives));
               this.markerShown = true;
               return true;
            }
         } catch (LinkageError | RuntimeException var4) {
            this.clear(false);
            throw var4;
         }
      }
   }

   void clear(boolean revive) {
      if (!this.cleared) {
         this.cleared = true;
         if (this.watchdog != null) {
            this.watchdog.cancel();
            this.watchdog = null;
         }

         this.releasePlacement();
         if (revive && this.bannerBlock != null) {
            this.match.revive(this);
         }
      }
   }

   private void releasePlacement() {
      if (this.bannerBlock != null) {
         this.match.reviveBanners.remove(this.bannerBlock, this);
      }

      if (this.lease != null) {
         this.lease.close();
         this.lease = null;
      }

      if (this.markerShown) {
         this.markerShown = false;
         this.match.guard("revive marker", this.marker::hide);
      }
   }

   private void watch() {
      if (!this.cleared && this.watchdog == null) {
         this.watchdog = Bukkit.getScheduler().runTaskTimer(MatchCore.plugin(), () -> {
            if (this.match.reviveBanners.get(this.bannerBlock) != this) {
               this.clear(false);
            } else if (this.bannerBlock.getType() != Material.RED_BANNER) {
               this.relocate(this.deathLocation);
            }
         }, 5L, 5L);
      }
   }
}
