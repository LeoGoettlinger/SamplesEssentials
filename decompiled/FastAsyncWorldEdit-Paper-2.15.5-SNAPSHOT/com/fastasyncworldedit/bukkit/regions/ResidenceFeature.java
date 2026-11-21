package com.fastasyncworldedit.bukkit.regions;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ResidenceFeature extends BukkitMaskManager implements Listener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public ResidenceFeature(final Plugin residencePlugin) {
      super(residencePlugin.getName());
      LOGGER.info("Plugin 'Residence' found. Using it now.");
   }

   public boolean isAllowed(Player player, ClaimedResidence residence, FaweMaskManager.MaskType type) {
      return residence != null
         && (
            residence.getOwner().equals(player.getName())
               || residence.getOwner().equals(player.getUniqueId().toString())
               || type == FaweMaskManager.MaskType.MEMBER && TaskManager.taskManager().sync(() -> residence.getPermissions().playerHas(player, "build", false))
         );
   }

   @Override
   public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, final FaweMaskManager.MaskType type, boolean isWhitelist) {
      Player player = BukkitAdapter.adapt(wePlayer);
      Location location = player.getLocation();
      ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);
      if (residence != null) {
         boolean isAllowed;
         while (!(isAllowed = this.isAllowed(player, residence, type)) && residence != null) {
            residence = residence.getSubzoneByLoc(location);
         }

         if (isAllowed) {
            CuboidArea area = residence.getAreaArray()[0];
            Location pos1 = area.getLowLoc();
            Location pos2 = area.getHighLoc();
            final ClaimedResidence finalResidence = residence;
            return new FaweMask(new CuboidRegion(BukkitAdapter.asBlockVector(pos1), BukkitAdapter.asBlockVector(pos2))) {
               @Override
               public boolean isValid(com.sk89q.worldedit.entity.Player player, FaweMaskManager.MaskType typex) {
                  return ResidenceFeature.this.isAllowed(BukkitAdapter.adapt(player), finalResidence, typex);
               }
            };
         }
      }

      return null;
   }
}
