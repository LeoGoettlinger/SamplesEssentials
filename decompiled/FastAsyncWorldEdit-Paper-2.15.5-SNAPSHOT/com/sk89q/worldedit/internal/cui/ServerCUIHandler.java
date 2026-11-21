package com.sk89q.worldedit.internal.cui;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;

public class ServerCUIHandler {
   private static final int MAX_DISTANCE = 32;

   private ServerCUIHandler() {
   }

   public static int getMaxServerCuiSize() {
      return 48;
   }

   @Nullable
   public static BaseBlock createStructureBlock(Player player) {
      LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
      RegionSelector regionSelector = session.getRegionSelector(player.getWorld());
      if (regionSelector instanceof CuboidRegionSelector) {
         int posX;
         int posY;
         int posZ;
         int width;
         int height;
         int length;
         if (regionSelector.isDefined()) {
            try {
               CuboidRegion region = ((CuboidRegionSelector)regionSelector).getRegion();
               posX = region.getMinimumPoint().x();
               posY = region.getMinimumPoint().y();
               posZ = region.getMinimumPoint().z();
               width = region.getWidth();
               height = region.getHeight();
               length = region.getLength();
            } catch (IncompleteRegionException var21) {
               var21.printStackTrace();
               return null;
            }
         } else {
            CuboidRegion region = ((CuboidRegionSelector)regionSelector).getIncompleteRegion();
            BlockVector3 point;
            if (region.getPos1() != null) {
               point = region.getPos1();
            } else {
               if (region.getPos2() == null) {
                  return null;
               }

               point = region.getPos2();
            }

            posX = point.x();
            posY = point.y();
            posZ = point.z();
            width = 1;
            height = 1;
            length = 1;
         }

         int maxSize = getMaxServerCuiSize();
         if (width <= maxSize && length <= maxSize && height <= maxSize) {
            Location location = player.getLocation();
            double rotX = location.getYaw();
            double rotY = location.getPitch();
            double xz = Math.cos(Math.toRadians(rotY));
            int x = (int)(location.x() - -xz * Math.sin(Math.toRadians(rotX)) * 12.0);
            int z = (int)(location.z() - xz * Math.cos(Math.toRadians(rotX)) * 12.0);
            int y = Math.max(player.getWorld().getMinY(), Math.min(Math.min(player.getWorld().getMaxY(), posY + 32), posY + 3));
            posX -= x;
            posY -= y;
            posZ -= z;
            if (Math.abs(posX) <= 32 && Math.abs(posY) <= 32 && Math.abs(posZ) <= 32) {
               LinCompoundTag.Builder structureTag = LinCompoundTag.builder();
               structureTag.putString("name", "worldedit:" + player.getName());
               structureTag.putString("author", player.getName());
               structureTag.putString("metadata", "");
               structureTag.putInt("x", x);
               structureTag.putInt("y", y);
               structureTag.putInt("z", z);
               structureTag.putInt("posX", posX);
               structureTag.putInt("posY", posY);
               structureTag.putInt("posZ", posZ);
               structureTag.putInt("sizeX", width);
               structureTag.putInt("sizeY", height);
               structureTag.putInt("sizeZ", length);
               structureTag.putString("rotation", "NONE");
               structureTag.putString("mirror", "NONE");
               structureTag.putString("mode", "SAVE");
               structureTag.putByte("ignoreEntities", (byte)1);
               structureTag.putByte("showboundingbox", (byte)1);
               structureTag.putString("id", BlockTypes.STRUCTURE_BLOCK.id());
               return BlockTypes.STRUCTURE_BLOCK.getDefaultState().toBaseBlock(structureTag.build());
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
