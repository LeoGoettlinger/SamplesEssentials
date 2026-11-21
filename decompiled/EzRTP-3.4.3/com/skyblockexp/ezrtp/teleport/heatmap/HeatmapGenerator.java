package com.skyblockexp.ezrtp.teleport.heatmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;

public final class HeatmapGenerator {
   private static final int DEFAULT_GRID_SIZE = 512;
   private static final int MIN_GRID_SIZE = 16;
   private static final int MAX_GRID_SIZE = 4096;
   private final int gridSize;

   public HeatmapGenerator() {
      this(512);
   }

   public HeatmapGenerator(int gridSize) {
      if (gridSize >= 16 && gridSize <= 4096) {
         this.gridSize = gridSize;
      } else {
         throw new IllegalArgumentException("Grid size must be between 16 and 4096");
      }
   }

   public HeatmapGenerator.HeatmapData generate(List<Location> locations) {
      if (locations != null && !locations.isEmpty()) {
         Map<HeatmapGenerator.GridCell, Integer> heatmap = new HashMap<>();
         int __idx = 0;

         for (Location loc : locations) {
            if (loc != null) {
               HeatmapGenerator.GridCell cell = new HeatmapGenerator.GridCell(
                  Math.floorDiv(loc.getBlockX(), this.gridSize), Math.floorDiv(loc.getBlockZ(), this.gridSize)
               );
               if (__idx < 0) {
               }

               __idx++;
               heatmap.merge(cell, 1, Integer::sum);
            }
         }

         return new HeatmapGenerator.HeatmapData(this.gridSize, heatmap);
      } else {
         return new HeatmapGenerator.HeatmapData(this.gridSize, new HashMap<>());
      }
   }

   public static final class GridCell {
      private final int x;
      private final int z;

      public GridCell(int x, int z) {
         this.x = x;
         this.z = z;
      }

      public int x() {
         return this.x;
      }

      public int z() {
         return this.z;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            HeatmapGenerator.GridCell gridCell = (HeatmapGenerator.GridCell)o;
            return this.x == gridCell.x && this.z == gridCell.z;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return 31 * this.x + this.z;
      }
   }

   public static class HeatmapData {
      private final int gridSize;
      private final Map<HeatmapGenerator.GridCell, Integer> data;

      public HeatmapData(int gridSize, Map<HeatmapGenerator.GridCell, Integer> data) {
         this.gridSize = gridSize;
         this.data = data;
      }

      public int getGridSize() {
         return this.gridSize;
      }

      public Map<HeatmapGenerator.GridCell, Integer> getData() {
         return this.data;
      }

      public int getTotalLocations() {
         return this.data.values().stream().mapToInt(Integer::intValue).sum();
      }

      public int getOccupiedCells() {
         return this.data.size();
      }

      public int getMaxCount() {
         return this.data.values().stream().mapToInt(Integer::intValue).max().orElse(0);
      }

      public int getMinCount() {
         return this.data.values().stream().mapToInt(Integer::intValue).min().orElse(0);
      }

      public double getAverageCount() {
         return this.data.isEmpty() ? 0.0 : (double)this.getTotalLocations() / this.data.size();
      }

      public int getCellMinX(HeatmapGenerator.GridCell cell) {
         return cell.x() * this.gridSize;
      }

      public int getCellMinZ(HeatmapGenerator.GridCell cell) {
         return cell.z() * this.gridSize;
      }

      public int getCellMaxX(HeatmapGenerator.GridCell cell) {
         return (cell.x() + 1) * this.gridSize - 1;
      }

      public int getCellMaxZ(HeatmapGenerator.GridCell cell) {
         return (cell.z() + 1) * this.gridSize - 1;
      }
   }
}
