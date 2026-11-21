package ac.grim.grimac.utils.collisions.blocks;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.CollisionFactory;
import ac.grim.grimac.utils.collisions.datatypes.HexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;

public class PistonBaseCollision implements CollisionFactory {
   @Override
   public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      if (!block.isExtended()) {
         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
      } else {
         return switch (block.getFacing()) {
            case UP -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
            case NORTH -> new HexCollisionBox(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
            case SOUTH -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
            case WEST -> new HexCollisionBox(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
            case EAST -> new HexCollisionBox(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
            default -> new HexCollisionBox(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
         };
      }
   }
}
