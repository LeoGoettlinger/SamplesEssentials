package com.github.retrooper.packetevents.protocol.world;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import org.jetbrains.annotations.NotNull;

public class WorldBlockPosition {
   public static final ResourceLocation OVERWORLD_DIMENSION = ResourceLocation.minecraft("overworld");
   private ResourceLocation world;
   private Vector3i blockPosition;

   public WorldBlockPosition(@NotNull ResourceLocation world, @NotNull Vector3i blockPosition) {
      this.world = world;
      this.blockPosition = blockPosition;
   }

   public WorldBlockPosition(@NotNull ResourceLocation world, int x, int y, int z) {
      this.world = world;
      this.blockPosition = new Vector3i(x, y, z);
   }

   @Deprecated
   public WorldBlockPosition(@NotNull Dimension dimension, @NotNull Vector3i blockPosition) {
      this(new ResourceLocation(dimension.getDimensionName()), blockPosition);
   }

   public WorldBlockPosition(@NotNull com.github.retrooper.packetevents.protocol.world.dimension.DimensionType dimensionType, @NotNull Vector3i blockPosition) {
      this(dimensionType.getName(), blockPosition);
   }

   public ResourceLocation getWorld() {
      return this.world;
   }

   public void setWorld(ResourceLocation world) {
      this.world = world;
   }

   public Vector3i getBlockPosition() {
      return this.blockPosition;
   }

   public void setBlockPosition(Vector3i blockPosition) {
      this.blockPosition = blockPosition;
   }
}
