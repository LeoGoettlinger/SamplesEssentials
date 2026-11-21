package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

final class TerrainImpactPlanner {
   static final int MAXIMUM_TILES = 48;

   TerrainImpactPlan plan(TerrainImpactRequest request) {
      Objects.requireNonNull(request, "request");
      Location center = request.center();
      World world = center.getWorld();
      return world == null ? new TerrainImpactPlan(List.of()) : plan(request, new LocalTopSurfaceSampler.BukkitBlockAccess(world));
   }

   static TerrainImpactPlan plan(TerrainImpactRequest request, LocalTopSurfaceSampler.BlockAccess blocks) {
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(blocks, "blocks");
      Location center = request.center();
      List<LocalTopSurfaceSampler.SurfaceTile> surfaces = LocalTopSurfaceSampler.sample(
         center.getX(), center.getY(), center.getZ(), request.radius(), 48, blocks
      );
      if (surfaces.isEmpty()) {
         return new TerrainImpactPlan(List.of());
      } else {
         List<TerrainImpactPlan.Tile> tiles = new ArrayList<>(surfaces.size());

         for (LocalTopSurfaceSampler.SurfaceTile surface : surfaces) {
            TerrainImpactPlan.Transform transform = TerrainImpactTransformPlanner.plan(
               request.seed(), surface.x(), surface.y(), surface.z(), surface.radialFalloff(), request.intensity()
            );
            tiles.add(
               new TerrainImpactPlan.Tile(
                  surface.x(), surface.y(), surface.z(), surface.blockData(), surface.radialDistance(), surface.radialFalloff(), transform
               )
            );
         }

         return new TerrainImpactPlan(tiles);
      }
   }
}
