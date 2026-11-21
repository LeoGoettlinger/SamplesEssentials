package com.fastasyncworldedit.bukkit.regions.plotsquared;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.Settings.Done;
import com.plotsquared.core.configuration.Settings.Enabled_Components;
import com.plotsquared.core.configuration.Settings.FAWE_Components;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.NoWorldeditFlag;
import com.plotsquared.core.util.WEManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.world.World;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlotSquaredFeature extends FaweMaskManager {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public PlotSquaredFeature() {
      super("PlotSquared");
      LOGGER.info("Optimizing PlotSquared");
      if (FAWE_Components.FAWE_HOOK) {
         Enabled_Components.WORLDEDIT_RESTRICTIONS = false;
         if (Settings.PLATFORM.toLowerCase(Locale.ROOT).startsWith("bukkit")) {
         }
      }
   }

   public boolean isAllowed(com.sk89q.worldedit.entity.Player player, Plot plot, FaweMaskManager.MaskType type, boolean notify) {
      if (plot == null) {
         return false;
      } else {
         UUID uid = player.getUniqueId();
         if ((Boolean)plot.getFlag(NoWorldeditFlag.class)) {
            if (notify) {
               player.print(Caption.of("fawe.cancel.reason.no.region.reason", Caption.of("fawe.cancel.reason.no.region.plot.noworldeditflag")));
            }

            return false;
         } else if (plot.isOwner(uid) || player.hasPermission("fawe.plotsquared.admin")) {
            return true;
         } else if (type != FaweMaskManager.MaskType.MEMBER) {
            if (notify) {
               player.print(Caption.of("fawe.cancel.reason.no.region.reason", Caption.of("fawe.cancel.reason.no.region.plot.owner.only")));
            }

            return false;
         } else if (plot.getTrusted().contains(uid) || plot.getTrusted().contains(DBFunc.EVERYONE)) {
            return true;
         } else if (!plot.getMembers().contains(uid) && !plot.getMembers().contains(DBFunc.EVERYONE)) {
            if (notify) {
               player.print(Caption.of("fawe.cancel.reason.no.region.reason", Caption.of("fawe.cancel.reason.no.region.not.added")));
            }

            return false;
         } else if (!player.hasPermission("fawe.plotsquared.member")) {
            if (notify) {
               player.print(Caption.of("fawe.cancel.reason.no.region.reason", Caption.of("fawe.error.no-perm", "fawe.plotsquared.member")));
            }

            return false;
         } else if (!plot.getOwners().isEmpty() && plot.getOwners().stream().anyMatch(this::playerOnline)) {
            return true;
         } else {
            if (notify) {
               player.print(Caption.of("fawe.cancel.reason.no.region.reason", Caption.of("fawe.cancel.reason.no.region.plot.owner.offline")));
            }

            return false;
         }
      }
   }

   private boolean playerOnline(UUID uuid) {
      if (uuid == null) {
         return false;
      } else {
         Player player = Bukkit.getPlayer(uuid);
         return player != null && player.isOnline();
      }
   }

   @Override
   public FaweMask getMask(final com.sk89q.worldedit.entity.Player player, final FaweMaskManager.MaskType type, final boolean isWhitelist) {
      return this.getMask(player, type, isWhitelist, true);
   }

   @Override
   public FaweMask getMask(com.sk89q.worldedit.entity.Player player, FaweMaskManager.MaskType type, boolean isWhitelist, boolean notify) {
      PlotPlayer<Player> pp = PlotPlayer.from(BukkitAdapter.adapt(player));
      if (pp == null) {
         return null;
      } else {
         Plot plot = pp.getCurrentPlot();
         Set<CuboidRegion> regions;
         if (this.isAllowed(player, plot, type, notify)) {
            regions = plot.getRegions();
         } else {
            plot = null;
            regions = WEManager.getMask(pp);
            if (regions.size() == 1) {
               CuboidRegion region = regions.iterator().next();
               if (region.getMinimumPoint().x() == Integer.MIN_VALUE && region.getMaximumPoint().x() == Integer.MAX_VALUE) {
                  regions.clear();
               }
            }
         }

         if (regions.isEmpty()) {
            return null;
         } else {
            PlotArea area = pp.getApplicablePlotArea();
            if ((!Done.RESTRICT_BUILDING || !DoneFlag.isDone(plot)) && !regions.isEmpty()) {
               World world = player.getWorld();
               int min = area != null ? area.getMinBuildHeight() : world.getMinY();
               int max = area != null ? Math.min(world.getMaxY(), area.getMaxBuildHeight() - 1) : world.getMaxY();
               Region maskedRegion;
               if (regions.size() == 1) {
                  CuboidRegion region = regions.iterator().next();
                  BlockVector3 pos1 = BlockVector3.at(region.getMinimumX(), min, region.getMinimumZ());
                  BlockVector3 pos2 = BlockVector3.at(region.getMaximumX(), max, region.getMaximumZ());
                  maskedRegion = new CuboidRegion(pos1, pos2);
               } else {
                  List<Region> weRegions = regions.stream()
                     .map(
                        r -> new CuboidRegion(
                           world, BlockVector3.at(r.getMinimumX(), min, r.getMinimumZ()), BlockVector3.at(r.getMaximumX(), max, r.getMaximumZ())
                        )
                     )
                     .collect(Collectors.toList());
                  maskedRegion = new RegionIntersection(world, weRegions);
               }

               return (FaweMask)(plot == null ? new FaweMask(maskedRegion) : new PlotSquaredFeature.PlotSquaredMask(maskedRegion, plot));
            } else {
               return null;
            }
         }
      }
   }

   private final class PlotSquaredMask extends FaweMask {
      private final Plot plot;
      private final WeakReference<Set<Plot>> connectedPlots;
      private final boolean singlePlot;

      private PlotSquaredMask(@Nonnull Region region, @Nonnull Plot plot) {
         super(region);
         this.plot = Objects.requireNonNull(plot);
         Set<Plot> connected = plot.getConnectedPlots();
         this.connectedPlots = new WeakReference<>(connected);
         this.singlePlot = connected.size() == 1;
      }

      @Override
      public boolean isValid(com.sk89q.worldedit.entity.Player player, FaweMaskManager.MaskType type, boolean notify) {
         return (this.connectedPlots.refersTo(this.plot.getConnectedPlots()) || this.singlePlot && this.plot.getConnectedPlots().size() <= 1)
               && (!Done.RESTRICT_BUILDING || !DoneFlag.isDone(this.plot))
            ? PlotSquaredFeature.this.isAllowed(player, this.plot, type, notify)
            : false;
      }
   }
}
