package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.extent.clipboard.CPUOptimizedClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Arrays;

public class ErodeBrush implements Brush {
   private static final BlockVector3[] FACES_TO_CHECK = Direction.valuesOf(Direction.Flag.CARDINAL)
      .stream()
      .map(Direction::toBlockVector)
      .toArray(BlockVector3[]::new);
   private final int erodeFaces;
   private final int erodeRecursion;
   private final int fillFaces;
   private final int fillRecursion;

   public ErodeBrush() {
      this(2, 1, 5, 1);
   }

   public ErodeBrush(int erodeFaces, int erodeRecursion, int fillFaces, int fillRecursion) {
      this.erodeFaces = erodeFaces;
      this.erodeRecursion = erodeRecursion;
      this.fillFaces = fillFaces;
      this.fillRecursion = fillRecursion;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      this.erosion(editSession, this.erodeFaces, this.erodeRecursion, this.fillFaces, this.fillRecursion, position, size);
   }

   public void erosion(final EditSession es, int erodeFaces, int erodeRecursion, int fillFaces, int fillRecursion, BlockVector3 target, double size) {
      int brushSize = (int)size;
      int brushSizeSquared = (int)(size * size);
      Location min = new Location(es.getWorld(), target.toVector3().subtract(size, size, size));
      Location max = new Location(es.getWorld(), target.toVector3().add(size, size, size));
      Region region = new CuboidRegion(es.getWorld(), min.toBlockPoint(), max.toBlockPoint());
      Clipboard buffer1 = new CPUOptimizedClipboard(region);
      Clipboard buffer2 = new CPUOptimizedClipboard(region);
      int bx = target.x();
      int by = target.y();
      int bz = target.z();
      int x = -brushSize;

      label86:
      for (int relx = 0; x <= brushSize && relx < buffer1.getWidth(); relx++) {
         int x0 = x + bx;
         int y = -brushSize;
         int rely = 0;

         while (y <= brushSize && rely < buffer1.getHeight()) {
            int y0 = y + by;
            int z = -brushSize;

            for (int relz = 0; z <= brushSize && relz < buffer1.getLength(); relz++) {
               int z0 = z + bz;
               BlockState state = es.getBlock(x0, y0, z0);
               buffer1.setBlock(relx, rely, relz, state);
               buffer2.setBlock(relx, rely, relz, state);
               z++;
            }

            y++;
            rely++;
            continue;

            x++;
            continue label86;
         }
         break;
      }

      x = 0;

      for (int i = 0; i < erodeRecursion; i++) {
         this.erosionIteration(brushSize, brushSizeSquared, erodeFaces, x % 2 == 0 ? buffer1 : buffer2, x % 2 == 1 ? buffer1 : buffer2);
         x++;
      }

      for (int i = 0; i < fillRecursion; i++) {
         this.fillIteration(brushSize, brushSizeSquared, fillFaces, x % 2 == 0 ? buffer1 : buffer2, x % 2 == 1 ? buffer1 : buffer2);
         x++;
      }

      Clipboard finalBuffer = x % 2 == 0 ? buffer1 : buffer2;

      for (BlockVector3 pos : finalBuffer) {
         BlockState block = pos.getBlock(finalBuffer);
         es.setBlock(pos.x() + bx - brushSize, pos.y() + by - brushSize, pos.z() + bz - brushSize, block);
      }
   }

   private void fillIteration(int brushSize, int brushSizeSquared, int fillFaces, Clipboard current, Clipboard target) {
      int[] frequency = null;
      int x = -brushSize;

      label65:
      for (int relx = 0; x <= brushSize && relx < target.getWidth(); relx++) {
         int x2 = x * x;
         int z = -brushSize;
         int relz = 0;

         while (z <= brushSize && relz < target.getLength()) {
            int x2y2 = x2 + z * z;
            int y = -brushSize;

            for (int rely = 0; y <= brushSize && rely < target.getHeight(); rely++) {
               int cube = x2y2 + y * y;
               target.setBlock(relx, rely, relz, current.getBlock(relx, rely, relz));
               if (cube < brushSizeSquared) {
                  BaseBlock state = current.getFullBlock(relx, rely, relz);
                  if (!state.getBlockType().getMaterial().isMovementBlocker()) {
                     BaseBlock highestState = state;
                     if (frequency == null) {
                        frequency = new int[BlockTypes.size()];
                     } else {
                        Arrays.fill(frequency, 0);
                     }

                     int total = 0;
                     int highest = 1;

                     for (BlockVector3 offs : FACES_TO_CHECK) {
                        BaseBlock next = current.getFullBlock(relx + offs.x(), rely + offs.y(), relz + offs.z());
                        if (next.getBlockType().getMaterial().isMovementBlocker()) {
                           total++;
                           int count = ++frequency[next.getInternalBlockTypeId()];
                           if (count >= highest) {
                              highest = count;
                              highestState = next;
                           }
                        }
                     }

                     if (total >= fillFaces) {
                        target.setBlock(relx, rely, relz, highestState);
                     }
                  }
               }

               y++;
            }

            z++;
            relz++;
            continue;

            x++;
            continue label65;
         }
         break;
      }
   }

   private void erosionIteration(int brushSize, int brushSizeSquared, int erodeFaces, Clipboard current, Clipboard target) {
      int[] frequency = null;
      int x = -brushSize;

      label65:
      for (int relx = 0; x <= brushSize && relx < target.getWidth(); relx++) {
         int x2 = x * x;
         int z = -brushSize;
         int relz = 0;

         while (z <= brushSize && relz < target.getLength()) {
            int x2y2 = x2 + z * z;
            int y = -brushSize;

            for (int rely = 0; y <= brushSize && rely < target.getHeight(); rely++) {
               int cube = x2y2 + y * y;
               target.setBlock(relx, rely, relz, current.getBlock(relx, rely, relz));
               if (cube < brushSizeSquared) {
                  BaseBlock state = current.getFullBlock(relx, rely, relz);
                  if (state.getMaterial().isMovementBlocker()) {
                     BaseBlock highestState = state;
                     if (frequency == null) {
                        frequency = new int[BlockTypes.size()];
                     } else {
                        Arrays.fill(frequency, 0);
                     }

                     int highest = 1;
                     int total = 0;

                     for (BlockVector3 offs : FACES_TO_CHECK) {
                        BaseBlock next = current.getFullBlock(relx + offs.x(), rely + offs.y(), relz + offs.z());
                        if (!next.getMaterial().isMovementBlocker()) {
                           total++;
                           int count = ++frequency[next.getInternalBlockTypeId()];
                           if (count > highest) {
                              highest = count;
                              highestState = next;
                           }
                        }
                     }

                     if (total >= erodeFaces) {
                        target.setBlock(relx, rely, relz, highestState);
                     }
                  }
               }

               y++;
            }

            z++;
            relz++;
            continue;

            x++;
            continue label65;
         }
         break;
      }
   }
}
