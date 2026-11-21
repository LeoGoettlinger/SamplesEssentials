package ac.grim.grimac.utils.collisions.blocks;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.East;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.North;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.South;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.West;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.CollisionFactory;
import ac.grim.grimac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import java.util.HashSet;
import java.util.Set;

public class DynamicChorusPlant implements CollisionFactory {
   private static final BlockFace[] directions = new BlockFace[]{
      BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
   };
   private static final CollisionBox[] modernShapes = makeShapes();

   private static CollisionBox[] makeShapes() {
      float f = 0.1875F;
      float f1 = 0.8125F;
      SimpleCollisionBox baseShape = new SimpleCollisionBox(f, f, f, f1, f1, f1, false);
      SimpleCollisionBox[] avoxelshape = new SimpleCollisionBox[directions.length];

      for (int i = 0; i < directions.length; i++) {
         BlockFace direction = directions[i];
         avoxelshape[i] = new SimpleCollisionBox(
            0.5 + Math.min(-0.3125, direction.getModX() * 0.5),
            0.5 + Math.min(-0.3125, direction.getModY() * 0.5),
            0.5 + Math.min(-0.3125, direction.getModZ() * 0.5),
            0.5 + Math.max(0.3125, direction.getModX() * 0.5),
            0.5 + Math.max(0.3125, direction.getModY() * 0.5),
            0.5 + Math.max(0.3125, direction.getModZ() * 0.5),
            false
         );
      }

      CollisionBox[] avoxelshape1 = new CollisionBox[64];

      for (int k = 0; k < 64; k++) {
         ComplexCollisionBox directionalShape = new ComplexCollisionBox(7, baseShape);

         for (int j = 0; j < directions.length; j++) {
            if ((k & 1 << j) != 0) {
               directionalShape.add(avoxelshape[j]);
            }
         }

         avoxelshape1[k] = directionalShape;
      }

      return avoxelshape1;
   }

   @Override
   public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_8)) {
         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
      } else if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
         return this.getLegacyBoundingBox(player, version, x, y, z);
      } else {
         Set<BlockFace> directions;
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            directions = new HashSet<>();
            if (block.getWest() == West.TRUE) {
               directions.add(BlockFace.WEST);
            }

            if (block.getEast() == East.TRUE) {
               directions.add(BlockFace.EAST);
            }

            if (block.getNorth() == North.TRUE) {
               directions.add(BlockFace.NORTH);
            }

            if (block.getSouth() == South.TRUE) {
               directions.add(BlockFace.SOUTH);
            }

            if (block.isUp()) {
               directions.add(BlockFace.UP);
            }

            if (block.isDown()) {
               directions.add(BlockFace.DOWN);
            }
         } else {
            directions = this.getLegacyStates(player, version, x, y, z);
         }

         return modernShapes[this.getAABBIndex(directions)].copy();
      }
   }

   public CollisionBox getLegacyBoundingBox(GrimPlayer player, ClientVersion version, int x, int y, int z) {
      Set<BlockFace> faces = this.getLegacyStates(player, version, x, y, z);
      float f1 = faces.contains(BlockFace.WEST) ? 0.0F : 0.1875F;
      float f2 = faces.contains(BlockFace.DOWN) ? 0.0F : 0.1875F;
      float f3 = faces.contains(BlockFace.NORTH) ? 0.0F : 0.1875F;
      float f4 = faces.contains(BlockFace.EAST) ? 1.0F : 0.8125F;
      float f5 = faces.contains(BlockFace.UP) ? 1.0F : 0.8125F;
      float f6 = faces.contains(BlockFace.SOUTH) ? 1.0F : 0.8125F;
      return new SimpleCollisionBox(f1, f2, f3, f4, f5, f6);
   }

   public Set<BlockFace> getLegacyStates(GrimPlayer player, ClientVersion version, int x, int y, int z) {
      Set<BlockFace> faces = new HashSet<>();
      StateType versionFlower = version.isOlderThanOrEquals(ClientVersion.V_1_12_2) ? StateTypes.CHORUS_FLOWER : null;
      StateType downBlock = player.compensatedWorld.getBlockType(x, y - 1, z);
      StateType upBlock = player.compensatedWorld.getBlockType(x, y + 1, z);
      StateType northBlock = player.compensatedWorld.getBlockType(x, y, z - 1);
      StateType eastBlock = player.compensatedWorld.getBlockType(x + 1, y, z);
      StateType southBlock = player.compensatedWorld.getBlockType(x, y, z + 1);
      StateType westBlock = player.compensatedWorld.getBlockType(x - 1, y, z);
      if (downBlock == StateTypes.CHORUS_PLANT || downBlock == versionFlower || downBlock == StateTypes.END_STONE) {
         faces.add(BlockFace.DOWN);
      }

      if (upBlock == StateTypes.CHORUS_PLANT || upBlock == versionFlower) {
         faces.add(BlockFace.UP);
      }

      if (northBlock == StateTypes.CHORUS_PLANT || northBlock == versionFlower) {
         faces.add(BlockFace.EAST);
      }

      if (eastBlock == StateTypes.CHORUS_PLANT || eastBlock == versionFlower) {
         faces.add(BlockFace.EAST);
      }

      if (southBlock == StateTypes.CHORUS_PLANT || southBlock == versionFlower) {
         faces.add(BlockFace.NORTH);
      }

      if (westBlock == StateTypes.CHORUS_PLANT || westBlock == versionFlower) {
         faces.add(BlockFace.NORTH);
      }

      return faces;
   }

   protected int getAABBIndex(Set<BlockFace> p_196486_1_) {
      int i = 0;

      for (int j = 0; j < directions.length; j++) {
         if (p_196486_1_.contains(directions[j])) {
            i |= 1 << j;
         }
      }

      return i;
   }
}
