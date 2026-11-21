package net.thenextlvl.protect.masks;

import com.fastasyncworldedit.bukkit.regions.BukkitMaskManager;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.FaweMaskManager.MaskType;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionIntersection;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.GlobalArea;
import net.thenextlvl.protect.area.RegionizedArea;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ProtectMaskManager extends BukkitMaskManager {
   private static final CuboidRegion GLOBAL = new CuboidRegion(
      BlockVector3.at(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE), BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
   );
   private final ProtectPlugin plugin;

   public ProtectMaskManager(ProtectPlugin plugin) {
      super(plugin.getName());
      this.plugin = plugin;
   }

   @Nullable
   public FaweMask getMask(Player player, MaskType type, boolean whitelist) {
      return this.getMask(player, type, whitelist, true);
   }

   @Nullable
   public FaweMask getMask(Player player, MaskType type, boolean whitelist, boolean notify) {
      if (whitelist && Settings.settings().REGION_RESTRICTIONS_OPTIONS.WORLDGUARD_REGION_BLACKLIST) {
         return new FaweMask(GLOBAL);
      } else {
         org.bukkit.entity.Player bukkit = this.plugin.getServer().getPlayer(player.getUniqueId());
         if (bukkit == null) {
            return null;
         } else {
            List<Area> areas = this.getAreas(bukkit, whitelist);
            if (areas.isEmpty()) {
               return null;
            } else {
               ArrayList<Region> regions = new ArrayList<>();

               for (Area area : areas) {
                  if (area instanceof RegionizedArea<?> regionized) {
                     regions.add(regionized.getRegion());
                  } else if (area instanceof GlobalArea) {
                     regions.add(GLOBAL);
                  }
               }

               return new ProtectMaskManager.RegionMask(bukkit, regions, areas, player.getWorld());
            }
         }
      }
   }

   public List<Area> getAreas(org.bukkit.entity.Player player, boolean whitelist) {
      return this.plugin.areaProvider().getAreas(player.getWorld()).filter(area -> whitelist == this.plugin.protectionService().canEdit(player, area)).toList();
   }

   private final class RegionMask extends FaweMask {
      private final org.bukkit.entity.Player player;
      private final Collection<Area> areas;

      public RegionMask(final org.bukkit.entity.Player player, final Collection<Region> regions, final Collection<Area> areas, final World world) {
         super(new RegionIntersection(world, regions));
         this.player = player;
         this.areas = areas;
      }

      public boolean isValid(Player ignored, MaskType type) {
         return this.areas.stream().allMatch(area -> ProtectMaskManager.this.plugin.protectionService().canEdit(this.player, area));
      }
   }
}
