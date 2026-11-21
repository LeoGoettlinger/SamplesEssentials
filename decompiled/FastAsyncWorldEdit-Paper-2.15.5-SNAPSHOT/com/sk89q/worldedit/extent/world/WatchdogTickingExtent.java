package com.sk89q.worldedit.extent.world;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.UUID;
import javax.annotation.Nullable;

public class WatchdogTickingExtent extends AbstractDelegateExtent {
   private static final int OPS_PER_TICK = 100;
   private final Watchdog watchdog;
   private boolean enabled;
   private int ops;

   public WatchdogTickingExtent(Extent extent, Watchdog watchdog) {
      super(extent);
      this.watchdog = watchdog;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   private void onOperation() {
      if (this.enabled) {
         this.ops++;
         if (this.ops == 100) {
            this.watchdog.tick();
            this.ops = 0;
         }
      }
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
      this.onOperation();
      return super.setBlock(location, block);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      this.onOperation();
      return super.createEntity(location, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      this.onOperation();
      return super.createEntity(location, entity, uuid);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      this.onOperation();
      return super.setBiome(position, biome);
   }
}
