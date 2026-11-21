package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;

final class LocalTopSurfaceSampler {
   private static final int SCAN_ABOVE_IMPACT = 2;
   private static final int SCAN_BELOW_IMPACT = 4;
   private static final int MAXIMUM_CONNECTED_STEP = 1;
   private static final int[][] CARDINAL_NEIGHBORS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
   private static final Comparator<LocalTopSurfaceSampler.SurfaceTile> SURFACE_ORDER = Comparator.comparingDouble(
         LocalTopSurfaceSampler.SurfaceTile::radialDistance
      )
      .thenComparingInt(LocalTopSurfaceSampler.SurfaceTile::x)
      .thenComparingInt(LocalTopSurfaceSampler.SurfaceTile::z)
      .thenComparingInt(LocalTopSurfaceSampler.SurfaceTile::y);

   private LocalTopSurfaceSampler() {
   }

   static List<LocalTopSurfaceSampler.SurfaceTile> sample(
      double centerX, double centerY, double centerZ, double radius, int maximumTiles, LocalTopSurfaceSampler.BlockAccess blocks
   ) {
      Objects.requireNonNull(blocks, "blocks");
      if (Double.isFinite(centerX) && Double.isFinite(centerY) && Double.isFinite(centerZ) && Double.isFinite(radius) && !(radius <= 0.0) && maximumTiles >= 1) {
         int minimumX = floor(centerX - radius - 0.5);
         int maximumX = floor(centerX + radius + 0.5);
         int minimumZ = floor(centerZ - radius - 0.5);
         int maximumZ = floor(centerZ + radius + 0.5);
         double radiusSquared = radius * radius;
         Map<LocalTopSurfaceSampler.Column, LocalTopSurfaceSampler.SurfaceTile> candidates = new HashMap<>();

         for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
               double deltaX = x + 0.5 - centerX;
               double deltaZ = z + 0.5 - centerZ;
               double radialDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
               if (!(radialDistanceSquared > radiusSquared)) {
                  double radialDistance = Math.sqrt(radialDistanceSquared);
                  Optional<LocalTopSurfaceSampler.SurfaceTile> tile = scanColumn(x, floor(centerY), z, radialDistance, radius, blocks);
                  if (tile.isPresent()) {
                     candidates.put(new LocalTopSurfaceSampler.Column(x, z), tile.orElseThrow());
                  }
               }
            }
         }

         if (candidates.isEmpty()) {
            return List.of();
         } else {
            LocalTopSurfaceSampler.SurfaceTile seed = candidates.values().stream().min(SURFACE_ORDER).orElseThrow();
            LocalTopSurfaceSampler.Column seedColumn = new LocalTopSurfaceSampler.Column(seed.x(), seed.z());
            Set<LocalTopSurfaceSampler.Column> connected = new LinkedHashSet<>();
            ArrayDeque<LocalTopSurfaceSampler.Column> pending = new ArrayDeque<>();
            connected.add(seedColumn);
            pending.add(seedColumn);

            while (!pending.isEmpty()) {
               LocalTopSurfaceSampler.Column currentColumn = pending.removeFirst();
               LocalTopSurfaceSampler.SurfaceTile current = candidates.get(currentColumn);

               for (int[] offset : CARDINAL_NEIGHBORS) {
                  LocalTopSurfaceSampler.Column neighborColumn = new LocalTopSurfaceSampler.Column(currentColumn.x() + offset[0], currentColumn.z() + offset[1]);
                  if (!connected.contains(neighborColumn)) {
                     LocalTopSurfaceSampler.SurfaceTile neighbor = candidates.get(neighborColumn);
                     if (neighbor != null && Math.abs(neighbor.y() - current.y()) <= 1) {
                        connected.add(neighborColumn);
                        pending.addLast(neighborColumn);
                     }
                  }
               }
            }

            List<LocalTopSurfaceSampler.SurfaceTile> ordered = new ArrayList<>(connected.size());

            for (LocalTopSurfaceSampler.Column column : connected) {
               ordered.add(candidates.get(column));
            }

            ordered.sort(SURFACE_ORDER);
            if (ordered.size() > maximumTiles) {
               ordered = ordered.subList(0, maximumTiles);
            }

            return List.copyOf(ordered);
         }
      } else {
         return List.of();
      }
   }

   private static Optional<LocalTopSurfaceSampler.SurfaceTile> scanColumn(
      int x, int impactY, int z, double radialDistance, double radius, LocalTopSurfaceSampler.BlockAccess blocks
   ) {
      if (!blocks.isColumnLoaded(x, z)) {
         return Optional.empty();
      } else {
         int top = Math.min(blocks.maximumHeight() - 1, impactY + 2);
         int bottom = Math.max(blocks.minimumHeight(), impactY - 4);
         if (top < bottom) {
            return Optional.empty();
         } else {
            for (int y = top; y >= bottom; y--) {
               LocalTopSurfaceSampler.BlockSnapshot candidate = blocks.blockAt(x, y, z);
               TerrainSurfaceEligibility.MaterialTraits material = candidate.material();
               if (!material.air()) {
                  if (material.liquid()) {
                     return Optional.empty();
                  }

                  if (!TerrainSurfaceEligibility.isEligibleSurface(material)) {
                     return Optional.empty();
                  }

                  if (y + 1 >= blocks.maximumHeight()) {
                     return Optional.empty();
                  }

                  LocalTopSurfaceSampler.BlockSnapshot clearance = blocks.blockAt(x, y + 1, z);
                  if (!TerrainSurfaceEligibility.hasClearance(clearance.material())) {
                     return Optional.empty();
                  }

                  double falloff = Math.max(0.0, 1.0 - radialDistance / radius);
                  return Optional.of(new LocalTopSurfaceSampler.SurfaceTile(x, y, z, candidate.blockData(), radialDistance, falloff));
               }
            }

            return Optional.empty();
         }
      }
   }

   private static int floor(double value) {
      return (int)Math.floor(value);
   }

   interface BlockAccess {
      int minimumHeight();

      int maximumHeight();

      boolean isColumnLoaded(int var1, int var2);

      LocalTopSurfaceSampler.BlockSnapshot blockAt(int var1, int var2, int var3);
   }

   record BlockSnapshot(BlockData blockData, TerrainSurfaceEligibility.MaterialTraits material) {
      BlockSnapshot(BlockData blockData, TerrainSurfaceEligibility.MaterialTraits material) {
         blockData = Objects.requireNonNull(blockData, "blockData").clone();
         material = Objects.requireNonNull(material, "material");
         this.blockData = blockData;
         this.material = material;
      }

      public BlockData blockData() {
         return this.blockData.clone();
      }
   }

   static final class BukkitBlockAccess implements LocalTopSurfaceSampler.BlockAccess {
      private final World world;

      BukkitBlockAccess(World world) {
         this.world = Objects.requireNonNull(world, "world");
      }

      @Override
      public int minimumHeight() {
         return this.world.getMinHeight();
      }

      @Override
      public int maximumHeight() {
         return this.world.getMaxHeight();
      }

      @Override
      public boolean isColumnLoaded(int x, int z) {
         return this.world.isChunkLoaded(x >> 4, z >> 4);
      }

      @Override
      public LocalTopSurfaceSampler.BlockSnapshot blockAt(int x, int y, int z) {
         Block block = this.world.getBlockAt(x, y, z);
         Material material = block.getType();
         boolean air = material.isAir();
         boolean liquid = block.isLiquid();
         boolean solid = material.isSolid();
         boolean occluding = material.isOccluding();
         boolean interactable = material.isInteractable();
         boolean tileState = !air && !liquid && block.getState() instanceof TileState;
         TerrainSurfaceEligibility.MaterialTraits traits = new TerrainSurfaceEligibility.MaterialTraits(
            air, liquid, solid, occluding, interactable, tileState, block.isPassable()
         );
         return new LocalTopSurfaceSampler.BlockSnapshot(block.getBlockData(), traits);
      }
   }

   private record Column(int x, int z) {
   }

   record SurfaceTile(int x, int y, int z, BlockData blockData, double radialDistance, double radialFalloff) {
      SurfaceTile(int x, int y, int z, BlockData blockData, double radialDistance, double radialFalloff) {
         blockData = Objects.requireNonNull(blockData, "blockData").clone();
         this.x = x;
         this.y = y;
         this.z = z;
         this.blockData = blockData;
         this.radialDistance = radialDistance;
         this.radialFalloff = radialFalloff;
      }

      public BlockData blockData() {
         return this.blockData.clone();
      }
   }
}
