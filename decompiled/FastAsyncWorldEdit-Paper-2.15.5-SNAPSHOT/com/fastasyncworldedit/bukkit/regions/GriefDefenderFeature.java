package com.fastasyncworldedit.bukkit.regions;

import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class GriefDefenderFeature extends BukkitMaskManager implements Listener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public GriefDefenderFeature(final Plugin plugin) {
      super(plugin.getName());
      LOGGER.info("Plugin 'GriefDefender' found. Using it now.");
   }

   public boolean isAllowed(Player player, Claim claim, FaweMaskManager.MaskType type) {
      return type == FaweMaskManager.MaskType.MEMBER && claim.isUserTrusted(player.getUniqueId(), TrustTypes.BUILDER);
   }

   @Override
   public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, FaweMaskManager.MaskType type, boolean isWhitelist) {
      final Player player = BukkitAdapter.adapt(wePlayer);
      final Claim claim = GriefDefender.getCore().getClaimAt(player.getLocation());
      if (claim != null && !claim.isWilderness() && this.isAllowed(player, claim, type)) {
         final BlockVector3 pos1 = BlockVector3.at(
            claim.getLesserBoundaryCorner().getX(), claim.getLesserBoundaryCorner().getY(), claim.getLesserBoundaryCorner().getZ()
         );
         final BlockVector3 pos2 = BlockVector3.at(
            claim.getGreaterBoundaryCorner().getX(), claim.getGreaterBoundaryCorner().getY(), claim.getGreaterBoundaryCorner().getZ()
         );
         return new FaweMask(new CuboidRegion(pos1, pos2)) {
            private final int[] bounds = new int[]{pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z()};

            @Override
            public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, FaweMaskManager.MaskType typex) {
               return GriefDefenderFeature.validateClaimAgainstCache(claim, this.bounds) && GriefDefenderFeature.this.isAllowed(player, claim, typex);
            }
         };
      } else {
         return null;
      }
   }

   private static boolean validateClaimAgainstCache(Claim claim, int[] bounds) {
      Vector3i min = claim.getLesserBoundaryCorner();
      Vector3i max = claim.getGreaterBoundaryCorner();
      if (min.getX() != bounds[0] || min.getY() != bounds[1] || min.getZ() != bounds[2]) {
         return false;
      } else if (max.getX() == bounds[3] && max.getY() == bounds[4] && max.getZ() == bounds[5]) {
         ClaimManager manager = GriefDefender.getCore().getClaimManager(claim.getWorldUniqueId());
         return manager == null ? false : manager.getClaimByUUID(claim.getUniqueId()) != null;
      } else {
         return false;
      }
   }
}
