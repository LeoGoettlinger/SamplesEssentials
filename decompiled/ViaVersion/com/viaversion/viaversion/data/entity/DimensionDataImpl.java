package com.viaversion.viaversion.data.entity;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.data.entity.DimensionData;

public final class DimensionDataImpl implements DimensionData {
   private final int id;
   private final int minY;
   private final int height;

   public DimensionDataImpl(int id, int minY, int height) {
      this.id = id;
      this.minY = minY;
      this.height = height;
   }

   public DimensionDataImpl(int id, CompoundTag dimensionData) {
      this.id = id;
      NumberTag height = dimensionData.getNumberTag("height");
      if (height == null) {
         throw new IllegalArgumentException("height missing in dimension data: " + dimensionData);
      } else {
         this.height = height.asInt();
         NumberTag minY = dimensionData.getNumberTag("min_y");
         if (minY == null) {
            throw new IllegalArgumentException("min_y missing in dimension data: " + dimensionData);
         } else {
            this.minY = minY.asInt();
         }
      }
   }

   public static DimensionData withDefaultsFor(String key, int id) {
      return switch (key) {
         case "overworld", "overworld_caves" -> new DimensionDataImpl(id, -64, 384);
         case "the_nether", "the_end" -> new DimensionDataImpl(id, 0, 256);
         default -> throw new IllegalArgumentException("Missing registry data for unknown dimension: " + key);
      };
   }

   @Override
   public int id() {
      return this.id;
   }

   @Override
   public int minY() {
      return this.minY;
   }

   @Override
   public int height() {
      return this.height;
   }

   @Override
   public String toString() {
      return "DimensionDataImpl{id=" + this.id + ", minY=" + this.minY + ", height=" + this.height + "}";
   }
}
