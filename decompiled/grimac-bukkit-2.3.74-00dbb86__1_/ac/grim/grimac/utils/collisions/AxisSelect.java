package ac.grim.grimac.utils.collisions;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;

public enum AxisSelect {
   EAST {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.maxX = 1.0;
         return box;
      }
   },
   WEST {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.minX = 0.0;
         return box;
      }
   },
   NORTH {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.minZ = 0.0;
         return box;
      }
   },
   SOUTH {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.maxZ = 1.0;
         return box;
      }
   },
   UP {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.minY = 0.0;
         return box;
      }
   },
   DOWN {
      @Override
      public SimpleCollisionBox modify(SimpleCollisionBox box) {
         box.maxY = 1.0;
         return box;
      }
   };

   public abstract SimpleCollisionBox modify(SimpleCollisionBox var1);

   @Contract(
      pure = true
   )
   public static AxisSelect byFace(@NotNull BlockFace face) {
      return switch (face) {
         case EAST -> EAST;
         case WEST -> WEST;
         case NORTH -> NORTH;
         case SOUTH -> SOUTH;
         case UP -> UP;
         default -> DOWN;
      };
   }
}
