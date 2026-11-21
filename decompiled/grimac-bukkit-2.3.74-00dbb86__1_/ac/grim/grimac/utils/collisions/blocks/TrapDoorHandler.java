package ac.grim.grimac.utils.collisions.blocks;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.CollisionFactory;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;

public class TrapDoorHandler implements CollisionFactory {
   @Override
   public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      double var2x = 0.1875;
      if (block.isOpen()) {
         switch (block.getFacing()) {
            case SOUTH:
               return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, var2x, false);
            case NORTH:
               return new SimpleCollisionBox(0.0, 0.0, 1.0 - var2x, 1.0, 1.0, 1.0, false);
            case EAST:
               return new SimpleCollisionBox(0.0, 0.0, 0.0, var2x, 1.0, 1.0, false);
            case WEST:
               return new SimpleCollisionBox(1.0 - var2x, 0.0, 0.0, 1.0, 1.0, 1.0, false);
            default:
               return NoCollisionBox.INSTANCE;
         }
      } else {
         return block.getHalf() == Half.BOTTOM
            ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, var2x, 1.0, false)
            : new SimpleCollisionBox(0.0, 1.0 - var2x, 0.0, 1.0, 1.0, 1.0, false);
      }
   }
}
