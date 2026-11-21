package com.fastasyncworldedit.core.extent.transform;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.UUID;
import javax.annotation.Nullable;

public class ScaleTransform extends ResettableExtent {
   private final double dx;
   private final double dy;
   private final double dz;
   private transient MutableVector3 mutable = new MutableVector3();
   private transient int minY;
   private transient int maxy;
   private transient BlockVector3 min;

   public ScaleTransform(Extent parent, double dx, double dy, double dz) {
      super(parent);
      this.dx = dx;
      this.dy = dy;
      this.dz = dz;
      this.minY = parent.getMinY();
      this.maxy = parent.getMaxY();
   }

   @Override
   public ResettableExtent setExtent(Extent extent) {
      this.min = null;
      this.mutable = new MutableVector3();
      this.minY = extent.getMinY();
      this.maxy = extent.getMaxY();
      return super.setExtent(extent);
   }

   private MutableVector3 getPos(BlockVector3 pos) {
      if (this.min == null) {
         this.min = pos;
      }

      this.mutable.mutX(this.min.x() + (pos.x() - this.min.x()) * this.dx);
      this.mutable.mutY(this.min.y() + (pos.y() - this.min.y()) * this.dy);
      this.mutable.mutZ(this.min.z() + (pos.z() - this.min.z()) * this.dz);
      return new MutableVector3(this.mutable);
   }

   private MutableVector3 getPos(int x, int y, int z) {
      if (this.min == null) {
         this.min = BlockVector3.at(x, y, z);
      }

      this.mutable.mutX(this.min.x() + (x - this.min.x()) * this.dx);
      this.mutable.mutY(this.min.y() + (y - this.min.y()) * this.dy);
      this.mutable.mutZ(this.min.z() + (z - this.min.z()) * this.dz);
      return new MutableVector3(this.mutable);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      boolean result = false;
      MutableVector3 vector3 = this.getPos(location);
      MutableBlockVector3 pos = new MutableBlockVector3();
      double sx = vector3.x();
      double sy = vector3.y();
      double sz = vector3.z();
      double ex = sx + this.dx;
      double ey = Math.clamp(sy + this.dy, (double)this.minY, (double)this.maxy);
      double ez = sz + this.dz;
      pos.mutY(sy);

      while (pos.y() < ey) {
         pos.mutZ(sz);

         while (pos.z() < ez) {
            pos.mutX(sx);

            for (; pos.x() < ex; pos.mutX(pos.x() + 1)) {
               if (this.getExtent().contains(pos)) {
                  result |= super.setBlock(pos, block);
               }
            }

            pos.mutZ(pos.z() + 1);
         }

         pos.mutY(pos.y() + 1);
      }

      return result;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      boolean result = false;
      MutableVector3 vector3 = this.getPos(position);
      MutableBlockVector3 pos = new MutableBlockVector3();
      double sx = vector3.x();
      double sy = vector3.y();
      double sz = vector3.z();
      double ex = sx + this.dx;
      double ey = Math.clamp(sy + this.dy, (double)this.minY, (double)this.maxy);
      double ez = sz + this.dz;
      pos.mutY(sy);

      while (pos.y() < ey) {
         pos.mutZ(sz);

         while (pos.z() < ez) {
            pos.mutX(sx);

            for (; pos.x() < ex; pos.mutX(pos.x() + 1)) {
               if (this.getExtent().contains(pos)) {
                  result |= super.setBiome(pos, biome);
               }
            }

            pos.mutZ(pos.z() + 1);
         }

         pos.mutY(pos.y() + 1);
      }

      return result;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x1, int y1, int z1, B block) throws WorldEditException {
      boolean result = false;
      MutableVector3 vector3 = this.getPos(x1, y1, z1);
      MutableBlockVector3 pos = new MutableBlockVector3();
      double sx = vector3.x();
      double sy = vector3.y();
      double sz = vector3.z();
      double ex = vector3.x() + this.dx;
      double ey = Math.min((double)this.maxy, sy + this.dy);
      double ez = vector3.z() + this.dz;
      pos.mutY(sy);

      while (pos.y() < ey) {
         pos.mutZ(sz);

         while (pos.z() < ez) {
            pos.mutX(sx);

            for (; pos.x() < ex; pos.mutX(pos.x() + 1)) {
               if (this.getExtent().contains(pos)) {
                  result |= super.setBlock(pos, block);
               }
            }

            pos.mutZ(pos.z() + 1);
         }

         pos.mutY(pos.y() + 1);
      }

      return result;
   }

   @Override
   public boolean setBiome(int x1, int y1, int z1, BiomeType biome) {
      boolean result = false;
      MutableVector3 vector3 = this.getPos(x1, y1, z1);
      MutableBlockVector3 pos = new MutableBlockVector3();
      double sx = vector3.x();
      double sy = vector3.y();
      double sz = vector3.z();
      double ex = sx + this.dx;
      double ey = Math.clamp(sy + this.dy, (double)this.minY, (double)this.maxy);
      double ez = sz + this.dz;
      pos.mutY(sy);

      while (pos.y() < ey) {
         pos.mutZ(sz);

         while (pos.z() < ez) {
            pos.mutX(sx);

            for (; pos.x() < ex; pos.mutX(pos.x() + 1)) {
               if (this.getExtent().contains(pos)) {
                  result |= super.setBiome(pos, biome);
               }
            }

            pos.mutZ(pos.z() + 1);
         }

         pos.mutY(pos.y() + 1);
      }

      return result;
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      Location newLoc = new Location(
         location.getExtent(), this.getPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getYaw(), location.getPitch()
      );
      return !this.getExtent().contains(newLoc.toBlockPoint()) ? null : super.createEntity(newLoc, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      Location newLoc = new Location(
         location.getExtent(), this.getPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getYaw(), location.getPitch()
      );
      return !this.getExtent().contains(newLoc.toBlockPoint()) ? null : super.createEntity(newLoc, entity, uuid);
   }
}
