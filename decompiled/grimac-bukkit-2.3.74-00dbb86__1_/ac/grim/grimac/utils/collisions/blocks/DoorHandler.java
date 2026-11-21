package ac.grim.grimac.utils.collisions.blocks;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Hinge;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.CollisionFactory;
import ac.grim.grimac.utils.collisions.datatypes.HexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;

public class DoorHandler implements CollisionFactory {
   protected static final CollisionBox SOUTH_AABB = new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
   protected static final CollisionBox NORTH_AABB = new HexCollisionBox(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
   protected static final CollisionBox WEST_AABB = new HexCollisionBox(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   protected static final CollisionBox EAST_AABB = new HexCollisionBox(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

   @Override
   public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      return (CollisionBox)(switch (this.fetchDirection(player, version, block, x, y, z)) {
         case NORTH -> NORTH_AABB.copy();
         case SOUTH -> SOUTH_AABB.copy();
         case EAST -> EAST_AABB.copy();
         case WEST -> WEST_AABB.copy();
         default -> NoCollisionBox.INSTANCE;
      });
   }

   public BlockFace fetchDirection(GrimPlayer player, ClientVersion version, WrappedBlockState door, int x, int y, int z) {
      BlockFace facingDirection;
      boolean isClosed;
      boolean isRightHinge;
      if (!PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_12_2)
         && !version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
         facingDirection = door.getFacing();
         isClosed = !door.isOpen();
         isRightHinge = door.getHinge() == Hinge.RIGHT;
      } else if (door.getHalf() == Half.LOWER) {
         WrappedBlockState above = player.compensatedWorld.getBlock(x, y + 1, z);
         facingDirection = door.getFacing();
         isClosed = !door.isOpen();
         if (above.getType() == door.getType()) {
            isRightHinge = above.getHinge() == Hinge.RIGHT;
         } else {
            isRightHinge = false;
         }
      } else {
         WrappedBlockState below = player.compensatedWorld.getBlock(x, y - 1, z);
         if (below.getType() == door.getType() && below.getHalf() == Half.LOWER) {
            isClosed = !below.isOpen();
            facingDirection = below.getFacing();
            isRightHinge = door.getHinge() == Hinge.RIGHT;
         } else {
            facingDirection = BlockFace.EAST;
            isClosed = true;
            isRightHinge = false;
         }
      }
      return switch (facingDirection) {
         case NORTH -> isClosed ? BlockFace.NORTH : (isRightHinge ? BlockFace.WEST : BlockFace.EAST);
         case SOUTH -> isClosed ? BlockFace.SOUTH : (isRightHinge ? BlockFace.EAST : BlockFace.WEST);
         default -> isClosed ? BlockFace.EAST : (isRightHinge ? BlockFace.NORTH : BlockFace.SOUTH);
         case WEST -> isClosed ? BlockFace.WEST : (isRightHinge ? BlockFace.SOUTH : BlockFace.NORTH);
      };
   }
}
