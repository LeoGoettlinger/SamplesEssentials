package com.fastasyncworldedit.core.wrappers;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.PlayerProxy;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TargetBlock;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;

public class AsyncPlayer extends PlayerProxy {
   public AsyncPlayer(Player parent) {
      super(parent);
   }

   public static AsyncPlayer wrap(Player parent) {
      return parent instanceof AsyncPlayer ? (AsyncPlayer)parent : new AsyncPlayer(parent);
   }

   @Override
   public World getWorld() {
      return WorldWrapper.wrap(super.getWorld());
   }

   @Override
   public void findFreePosition(Location searchPos) {
      TaskManager.taskManager().sync(new RunnableVal<Boolean>() {
         public void run(Boolean value) {
            AsyncPlayer.this.getBasePlayer().findFreePosition(searchPos);
         }
      });
   }

   @Override
   public void setOnGround(Location searchPos) {
      TaskManager.taskManager().sync(new RunnableVal<Boolean>() {
         public void run(Boolean value) {
            AsyncPlayer.this.getBasePlayer().setOnGround(searchPos);
         }
      });
   }

   @Override
   public void findFreePosition() {
      TaskManager.taskManager().sync(new RunnableVal<Boolean>() {
         public void run(Boolean value) {
            AsyncPlayer.this.getBasePlayer().findFreePosition();
         }
      });
   }

   @Override
   public boolean ascendLevel() {
      return TaskManager.taskManager().sync(() -> this.getBasePlayer().ascendLevel());
   }

   @Override
   public boolean descendLevel() {
      return TaskManager.taskManager().sync(() -> this.getBasePlayer().descendLevel());
   }

   @Override
   public boolean ascendToCeiling(int clearance) {
      return this.ascendToCeiling(clearance, true);
   }

   @Override
   public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int initialY = Math.max(this.getWorld().getMinY(), pos.getBlockY());
      int y = Math.max(this.getWorld().getMinY(), pos.getBlockY() + 2);
      int z = pos.getBlockZ();
      Extent world = this.getLocation().getExtent();
      MutableBlockVector3 mutable = new MutableBlockVector3();
      if (!world.getBlock(mutable.setComponents(x, y, z)).getBlockType().getMaterial().isAir()) {
         return false;
      } else {
         while (y <= world.getMaximumPoint().y()) {
            if (world.getBlock(mutable.mutY(y)).getBlockType().getMaterial().isMovementBlocker()) {
               int platformY = Math.max(initialY, y - 3 - clearance);
               this.floatAt(x, platformY + 1, z, alwaysGlass);
               return true;
            }

            y++;
         }

         return false;
      }
   }

   @Override
   public boolean ascendUpwards(int distance) {
      return this.ascendUpwards(distance, true);
   }

   @Override
   public boolean ascendUpwards(int distance, boolean alwaysGlass) {
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int initialY = Math.max(this.getWorld().getMinY(), pos.getBlockY());
      int y = Math.max(this.getWorld().getMinY(), pos.getBlockY() + 1);
      int z = pos.getBlockZ();
      int maxY = Math.min(this.getWorld().getMaxY() + 1, initialY + distance);
      Extent world = this.getLocation().getExtent();

      for (MutableBlockVector3 mutable = new MutableBlockVector3(x, y, z);
         y <= world.getMaxY() + 2 && !world.getBlock(mutable.mutY(y)).getBlockType().getMaterial().isMovementBlocker() && y <= maxY + 1;
         y++
      ) {
         if (y == maxY + 1) {
            this.floatAt(x, y - 1, z, alwaysGlass);
            return true;
         }
      }

      return false;
   }

   @Override
   public void floatAt(int x, int y, int z, boolean alwaysGlass) {
      if (!alwaysGlass && this.isAllowedToFly()) {
         this.setFlying(true);
      } else {
         RuntimeException caught = null;

         try {
            EditSession edit = WorldEdit.getInstance()
               .newEditSessionBuilder()
               .world(WorldWrapper.unwrap(this.getWorld()))
               .actor(unwrap(this.getBasePlayer()))
               .build();
            edit.setBlock(BlockVector3.at(x, y - 1, z), BlockTypes.GLASS);
            edit.flushQueue();
            LocalSession session = Fawe.instance().getWorldEdit().getSessionManager().get(this);
            if (session != null) {
               session.remember(edit, true, this.getBasePlayer().getLimit().MAX_HISTORY);
            }
         } catch (RuntimeException var8) {
            caught = var8;
         }

         if (caught != null) {
            throw caught;
         }
      }

      this.trySetPosition(Vector3.at(x + 0.5, y, z + 0.5));
   }

   @Override
   public void setPosition(Vector3 pos, float pitch, float yaw) {
      Fawe.instance().getQueueHandler().sync(() -> super.setPosition(pos, pitch, yaw));
   }

   @Override
   public Location getBlockTrace(int range, boolean useLastBlock) {
      return TaskManager.taskManager().sync(() -> {
         TargetBlock tb = new TargetBlock(this, range, 0.2);
         return useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock();
      });
   }

   @Override
   public Location getBlockTraceFace(int range, boolean useLastBlock) {
      return TaskManager.taskManager().sync(() -> {
         TargetBlock tb = new TargetBlock(this, range, 0.2);
         return useLastBlock ? tb.getAnyTargetBlockFace() : tb.getTargetBlockFace();
      });
   }

   @Override
   public Location getSolidBlockTrace(int range) {
      return TaskManager.taskManager().sync(() -> {
         TargetBlock tb = new TargetBlock(this, range, 0.2);
         return tb.getSolidTargetBlock();
      });
   }

   @Override
   public Direction getCardinalDirection() {
      return this.getBasePlayer().getCardinalDirection();
   }

   @Override
   public boolean passThroughForwardWall(int range) {
      return TaskManager.taskManager()
         .sync(
            () -> {
               int searchDist = 0;
               TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
               Extent world = this.getLocation().getExtent();
               boolean firstBlock = true;
               int freeToFind = 2;
               boolean inFree = false;
               MutableBlockVector3 mutable = new MutableBlockVector3();

               Location block;
               while ((block = hitBlox.getNextBlock()) != null) {
                  boolean free = !world.getBlock(mutable.setComponents(block.getBlockX(), block.getBlockY(), block.getBlockZ()))
                     .getBlockType()
                     .getMaterial()
                     .isMovementBlocker();
                  if (firstBlock) {
                     firstBlock = false;
                     if (!free) {
                        freeToFind--;
                        continue;
                     }
                  }

                  if (++searchDist > 20) {
                     return false;
                  }

                  if (inFree != free && free) {
                     freeToFind--;
                  }

                  if (freeToFind == 0) {
                     this.setOnGround(block);
                     return true;
                  }

                  inFree = free;
               }

               return false;
            }
         );
   }
}
