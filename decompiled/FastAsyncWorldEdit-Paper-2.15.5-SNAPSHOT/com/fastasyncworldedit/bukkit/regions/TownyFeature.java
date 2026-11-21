package com.fastasyncworldedit.bukkit.regions;

import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class TownyFeature extends BukkitMaskManager implements Listener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public TownyFeature(Plugin townyPlugin) {
      super(townyPlugin.getName());
      LOGGER.info("Plugin 'Towny' found. Using it now.");
   }

   public boolean isAllowed(Player player, TownBlock block) {
      if (block == null) {
         return false;
      } else {
         Resident resident = TownyAPI.getInstance().getResident(player);
         if (resident == null) {
            return false;
         } else if (!block.hasResident(resident) && !block.hasTrustedResident(resident)) {
            Town town = block.getTownOrNull();
            if (!town.isMayor(resident) && !town.hasTrustedResident(resident)) {
               if (!town.hasResident(resident)) {
                  return false;
               } else if (player.hasPermission("fawe.towny.*")) {
                  return true;
               } else {
                  for (String rank : resident.getTownRanks()) {
                     if (player.hasPermission("fawe.towny." + rank)) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, FaweMaskManager.MaskType type, boolean isWhitelist) {
      Player player = BukkitAdapter.adapt(wePlayer);
      Location location = player.getLocation();
      WorldCoord mycoord = WorldCoord.parseWorldCoord(location);
      if (mycoord.isWilderness()) {
         return null;
      } else {
         final TownBlock myplot = mycoord.getTownBlockOrNull();
         boolean isMember = this.isAllowed(player, myplot);
         if (isMember) {
            Location loc1 = mycoord.getLowerMostCornerLocation();
            Location loc2 = mycoord.getUpperMostCornerLocation();
            BlockVector3 pos1 = BlockVector3.at(loc1.getX(), loc1.getY(), loc1.getZ());
            BlockVector3 pos2 = BlockVector3.at(loc2.getX() - 1.0, loc2.getY(), loc2.getZ() - 1.0);
            return new FaweMask(new CuboidRegion(pos1, pos2)) {
               @Override
               public boolean isValid(com.sk89q.worldedit.entity.Player player, FaweMaskManager.MaskType typex) {
                  return TownyFeature.this.isAllowed(BukkitAdapter.adapt(player), myplot);
               }
            };
         } else {
            return null;
         }
      }
   }
}
