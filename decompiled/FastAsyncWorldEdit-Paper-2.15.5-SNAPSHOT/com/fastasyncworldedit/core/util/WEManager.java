package com.fastasyncworldedit.core.util;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.regions.RegionWrapper;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.logging.log4j.Logger;

public class WEManager {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static WEManager INSTANCE;
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static WEManager IMP = weManager();
   private final ArrayDeque<FaweMaskManager> managers = new ArrayDeque<>();

   public static WEManager weManager() {
      if (INSTANCE == null) {
         INSTANCE = new WEManager();
      }

      return INSTANCE;
   }

   public ArrayDeque<FaweMaskManager> getManagers() {
      return this.managers;
   }

   public void addManager(FaweMaskManager manager) {
      if (manager.isExclusive()) {
         this.managers.addFirst(manager);
      } else {
         this.managers.add(manager);
      }
   }

   public void addManagers(Collection<FaweMaskManager> managers) {
      for (FaweMaskManager manager : managers) {
         this.addManager(manager);
      }
   }

   public void cancelEditSafe(AbstractDelegateExtent parent, FaweException reason) throws FaweException {
      Extent currentExtent = parent.getExtent();
      if (!(currentExtent instanceof NullExtent)) {
         parent.extent = new NullExtent(parent.extent, reason);
      }

      throw reason;
   }

   public void cancelEdit(AbstractDelegateExtent parent, FaweException reason) throws WorldEditException {
      this.cancelEditSafe(parent, reason);
   }

   public boolean isIn(int x, int y, int z, Region region) {
      return region.contains(x, y, z);
   }

   public Region[] getMask(Player player) {
      return this.getMask(player, FaweMaskManager.MaskType.getDefaultMaskType(), true);
   }

   public Region[] getMask(Player player, FaweMaskManager.MaskType type, final boolean isWhitelist) {
      if (Settings.settings().REGION_RESTRICTIONS && !player.hasPermission("fawe.bypass.regions")) {
         Location loc = player.getLocation();
         String world = player.getWorld().getName();
         if (!world.equals(player.getMeta("lastMaskWorld"))) {
            player.deleteMeta("lastMaskWorld");
            player.deleteMeta("lastMask");
         }

         player.setMeta("lastMaskWorld", world);
         Set<FaweMask> masks = player.getMeta("lastMask");
         Set<Region> regions = new HashSet<>();
         if (masks != null && isWhitelist) {
            synchronized (masks) {
               boolean removed = false;
               boolean inMask = false;
               if (!masks.isEmpty()) {
                  Iterator<FaweMask> iterator = masks.iterator();

                  while (iterator.hasNext()) {
                     FaweMask mask = iterator.next();
                     if (mask.isValid(player, type, false)) {
                        Region region = mask.getRegion();
                        inMask |= region.contains(loc.toBlockPoint());
                        regions.add(region);
                     } else {
                        if (Settings.settings().ENABLED_COMPONENTS.DEBUG) {
                           player.printDebug(Caption.of("fawe.error.region-mask-invalid", mask.getClass().getSimpleName()));
                        }

                        removed = true;
                        iterator.remove();
                     }
                  }
               }

               if (!removed && inMask) {
                  return regions.toArray(new Region[0]);
               }
            }
         } else {
            masks = new HashSet<>();
         }

         synchronized (masks) {
            for (FaweMaskManager manager : this.managers) {
               if (player.hasPermission("fawe." + manager.getKey())) {
                  try {
                     if (!manager.isExclusive() || masks.isEmpty()) {
                        FaweMask mask = manager.getMask(player, FaweMaskManager.MaskType.getDefaultMaskType(), isWhitelist, masks.isEmpty());
                        if (mask != null) {
                           regions.add(mask.getRegion());
                           masks.add(mask);
                           if (manager.isExclusive()) {
                              break;
                           }
                        }
                     }
                  } catch (Throwable var16) {
                     var16.printStackTrace();
                  }
               } else {
                  player.printError(TextComponent.of("Missing permission fawe." + manager.getKey()));
               }
            }

            if (isWhitelist) {
               if (!masks.isEmpty()) {
                  player.setMeta("lastMask", masks);
               } else {
                  player.deleteMeta("lastMask");
               }
            }
         }

         return regions.toArray(new Region[0]);
      } else {
         return new Region[]{RegionWrapper.GLOBAL()};
      }
   }

   public boolean intersects(Region region1, Region region2) {
      BlockVector3 rg1P1 = region1.getMinimumPoint();
      BlockVector3 rg1P2 = region1.getMaximumPoint();
      BlockVector3 rg2P1 = region2.getMinimumPoint();
      BlockVector3 rg2P2 = region2.getMaximumPoint();
      return rg1P1.x() <= rg2P2.x() && rg1P2.x() >= rg2P1.x() && rg1P1.z() <= rg2P2.z() && rg1P2.z() >= rg2P1.z();
   }

   public boolean regionContains(Region selection, HashSet<Region> mask) {
      for (Region region : mask) {
         if (this.intersects(region, selection)) {
            return true;
         }
      }

      return false;
   }
}
