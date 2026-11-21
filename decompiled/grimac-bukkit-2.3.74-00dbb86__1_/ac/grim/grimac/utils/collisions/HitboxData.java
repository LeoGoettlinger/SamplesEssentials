package ac.grim.grimac.utils.collisions;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.East;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Face;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Leaves;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.North;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.South;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Tilt;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.West;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicHitboxFence;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicHitboxPane;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicHitboxWall;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.HexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.HexOffsetCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.HitBoxFactory;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.OffsetCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.lists.ArrayUtils;
import ac.grim.grimac.utils.nmsutil.Materials;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum HitboxData implements HitBoxFactory {
   VINE((player, item, version, datax, isTargetBlock, x, y, z) -> {
      ComplexCollisionBox boxes = new ComplexCollisionBox(5);
      if (datax.getWest() == West.TRUE) {
         boxes.add(new HexCollisionBox(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
      }

      if (datax.getEast() == East.TRUE) {
         boxes.add(new HexCollisionBox(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));
      }

      if (datax.getNorth() == North.TRUE) {
         boxes.add(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
      }

      if (datax.getSouth() == South.TRUE) {
         boxes.add(new HexCollisionBox(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
      }

      if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2) && boxes.size() > 1) {
         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
      } else {
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13) && datax.isUp()) {
            boxes.add(new HexCollisionBox(0.0, 15.0, 0.0, 16.0, 16.0, 16.0));
         }

         return boxes;
      }
   }, StateTypes.VINE),
   RAILS(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         return (CollisionBox)(switch (datax.getShape()) {
            case ASCENDING_NORTH, ASCENDING_SOUTH, ASCENDING_EAST, ASCENDING_WEST -> {
               if (version.isOlderThan(ClientVersion.V_1_8)) {
                  StateType railType = datax.getType();
                  yield railType != StateTypes.ACTIVATOR_RAIL && (railType != StateTypes.POWERED_RAIL || !datax.isPowered())
                     ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.625, 1.0, false)
                     : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.125, 1.0, false);
               } else {
                  yield version.isOlderThan(ClientVersion.V_1_9)
                     ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.625, 1.0, false)
                     : (
                        version.isNewerThanOrEquals(ClientVersion.V_1_9) && version.isOlderThan(ClientVersion.V_1_10)
                           ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.15625, 1.0, false)
                           : (
                              version.isOlderThan(ClientVersion.V_1_11)
                                 ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
                                 : new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
                           )
                     );
               }
            }
            default -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
         });
      },
      BlockTags.RAILS.getStates().toArray(new StateType[0])
   ),
   END_PORTAL(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (version.isOlderThan(ClientVersion.V_1_9)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);
         } else {
            return (CollisionBox)(version.isOlderThan(ClientVersion.V_1_17)
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.75, 1.0)
               : new HexCollisionBox(0.0, 6.0, 0.0, 16.0, 12.0, 16.0));
         }
      },
      StateTypes.END_PORTAL
   ),
   FENCE_GATE((player, item, version, datax, isTargetBlock, x, y, z) -> {
      boolean isXAxis = datax.getFacing() == BlockFace.WEST || datax.getFacing() == BlockFace.EAST;
      boolean isInWall;
      if (isXAxis) {
         boolean zPosWall = Materials.isWall(player.compensatedWorld.getBlockType(x, y, z + 1));
         boolean zNegWall = Materials.isWall(player.compensatedWorld.getBlockType(x, y, z - 1));
         isInWall = zPosWall || zNegWall;
      } else {
         boolean xPosWall = Materials.isWall(player.compensatedWorld.getBlockType(x + 1, y, z));
         boolean xNegWall = Materials.isWall(player.compensatedWorld.getBlockType(x - 1, y, z));
         isInWall = xPosWall || xNegWall;
      }

      if (isInWall) {
         return isXAxis ? new HexCollisionBox(6.0, 0.0, 0.0, 10.0, 13.0, 16.0) : new HexCollisionBox(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
      } else {
         return isXAxis ? new HexCollisionBox(6.0, 0.0, 0.0, 10.0, 16.0, 16.0) : new HexCollisionBox(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
      }
   }, BlockTags.FENCE_GATES.getStates().toArray(new StateType[0])),
   FENCE(new DynamicHitboxFence(), BlockTags.FENCES.getStates().toArray(new StateType[0])),
   PANE(new DynamicHitboxPane(), Materials.getPanes().toArray(new StateType[0])),
   LEVER(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         Face face = datax.getFace();
         BlockFace facing = datax.getFacing();
         if (version.isOlderThan(ClientVersion.V_1_13)) {
            double f = 0.1875;
            switch (face) {
               case WALL:
                  switch (facing) {
                     case SOUTH:
                        return new SimpleCollisionBox(0.5 - f, 0.2, 0.0, 0.5 + f, 0.8, f * 2.0, false);
                     case WEST:
                        return new SimpleCollisionBox(1.0 - f * 2.0, 0.2, 0.5 - f, 1.0, 0.8, 0.5 + f, false);
                     case NORTH:
                        return new SimpleCollisionBox(0.5 - f, 0.2, 1.0 - f * 2.0, 0.5 + f, 0.8, 1.0, false);
                     case EAST:
                        return new SimpleCollisionBox(0.0, 0.2, 0.5 - f, f * 2.0, 0.8, 0.5 + f, false);
                  }
               case CEILING:
                  return new SimpleCollisionBox(0.25, 0.4, 0.25, 0.75, 1.0, 0.75, false);
               case FLOOR:
                  return new SimpleCollisionBox(0.25, 0.0, 0.25, 0.75, 0.6, 0.75, false);
            }
         }
         return switch (face) {
            case WALL -> {
               switch (facing) {
                  case SOUTH:
                     yield new SimpleCollisionBox(0.3125, 0.25, 0.0, 0.6875, 0.75, 0.375, false);
                  case WEST:
                     yield new SimpleCollisionBox(0.625, 0.25, 0.3125, 1.0, 0.75, 0.6875, false);
                  case NORTH:
                  default:
                     yield new SimpleCollisionBox(0.3125, 0.25, 0.625, 0.6875, 0.75, 1.0, false);
                  case EAST:
                     yield new SimpleCollisionBox(0.0, 0.25, 0.3125, 0.375, 0.75, 0.6875, false);
               }
            }
            case FLOOR -> facing != BlockFace.EAST && facing != BlockFace.WEST
               ? new SimpleCollisionBox(0.3125, 0.0, 0.25, 0.6875, 0.375, 0.75, false)
               : new SimpleCollisionBox(0.25, 0.0, 0.3125, 0.75, 0.375, 0.6875, false);
            default -> facing != BlockFace.EAST && facing != BlockFace.WEST
               ? new SimpleCollisionBox(0.3125, 0.625, 0.25, 0.6875, 1.0, 0.75, false)
               : new SimpleCollisionBox(0.25, 0.625, 0.3125, 0.75, 1.0, 0.6875, false);
         };
      },
      StateTypes.LEVER
   ),
   BUTTON((player, item, version, datax, isTargetBlock, x, y, z) -> {
      Face face = datax.getFace();
      BlockFace facing = datax.getFacing();
      boolean powered = datax.isPowered();
      if (version.isOlderThan(ClientVersion.V_1_13)) {
         double f2 = (datax.isPowered() ? 1 : 2) / 16.0;
         switch (face) {
            case WALL:
               switch (facing) {
                  case SOUTH:
                     return new SimpleCollisionBox(0.3125, 0.375, 0.0, 0.6875, 0.625, f2, false);
                  case WEST:
                     return new SimpleCollisionBox(1.0 - f2, 0.375, 0.3125, 1.0, 0.625, 0.6875, false);
                  case NORTH:
                     return new SimpleCollisionBox(0.3125, 0.375, 1.0 - f2, 0.6875, 0.625, 1.0, false);
                  case EAST:
                     return new SimpleCollisionBox(0.0, 0.375, 0.3125, f2, 0.625, 0.6875, false);
               }
            case CEILING:
               return new SimpleCollisionBox(0.3125, 1.0 - f2, 0.375, 0.6875, 1.0, 0.625, false);
            case FLOOR:
               return new SimpleCollisionBox(0.3125, 0.0, 0.375, 0.6875, 0.0 + f2, 0.625, false);
         }
      }

      switch (face) {
         case WALL:
            return (CollisionBox)(switch (facing) {
               case SOUTH -> powered ? new HexCollisionBox(5.0, 6.0, 0.0, 11.0, 10.0, 1.0) : new HexCollisionBox(5.0, 6.0, 0.0, 11.0, 10.0, 2.0);
               case WEST -> powered ? new HexCollisionBox(15.0, 6.0, 5.0, 16.0, 10.0, 11.0) : new HexCollisionBox(14.0, 6.0, 5.0, 16.0, 10.0, 11.0);
               case NORTH, UP, DOWN -> powered ? new HexCollisionBox(5.0, 6.0, 15.0, 11.0, 10.0, 16.0) : new HexCollisionBox(5.0, 6.0, 14.0, 11.0, 10.0, 16.0);
               case EAST -> powered ? new HexCollisionBox(0.0, 6.0, 5.0, 1.0, 10.0, 11.0) : new HexCollisionBox(0.0, 6.0, 5.0, 2.0, 10.0, 11.0);
               default -> NoCollisionBox.INSTANCE;
            });
         case CEILING:
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
               return LEVER.dynamic.fetch(player, item, version, datax, isTargetBlock, x, y, z);
            } else {
               if (facing != BlockFace.EAST && facing != BlockFace.WEST) {
                  return powered ? new HexCollisionBox(5.0, 15.0, 6.0, 11.0, 16.0, 10.0) : new HexCollisionBox(5.0, 14.0, 6.0, 11.0, 16.0, 10.0);
               }

               return powered ? new HexCollisionBox(6.0, 15.0, 5.0, 10.0, 16.0, 11.0) : new HexCollisionBox(6.0, 14.0, 5.0, 10.0, 16.0, 11.0);
            }
         case FLOOR:
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
               return LEVER.dynamic.fetch(player, item, version, datax, isTargetBlock, x, y, z);
            } else {
               if (facing != BlockFace.EAST && facing != BlockFace.WEST) {
                  return powered ? new HexCollisionBox(5.0, 0.0, 6.0, 11.0, 1.0, 10.0) : new HexCollisionBox(5.0, 0.0, 6.0, 11.0, 2.0, 10.0);
               }

               return powered ? new HexCollisionBox(6.0, 0.0, 5.0, 10.0, 1.0, 11.0) : new HexCollisionBox(6.0, 0.0, 5.0, 10.0, 2.0, 11.0);
            }
         default:
            throw new IllegalStateException();
      }
   }, BlockTags.BUTTONS.getStates().toArray(new StateType[0])),
   WALL(new DynamicHitboxWall(), BlockTags.WALLS.getStates().toArray(new StateType[0])),
   WALL_SIGN((player, item, version, datax, isTargetBlock, x, y, z) -> {
      return (CollisionBox)(switch (datax.getFacing()) {
         case SOUTH -> new HexCollisionBox(0.0, 4.5, 0.0, 16.0, 12.5, 2.0);
         case WEST -> new HexCollisionBox(14.0, 4.5, 0.0, 16.0, 12.5, 16.0);
         case NORTH -> new HexCollisionBox(0.0, 4.5, 14.0, 16.0, 12.5, 16.0);
         case EAST -> new HexCollisionBox(0.0, 4.5, 0.0, 2.0, 12.5, 16.0);
         default -> NoCollisionBox.INSTANCE;
      });
   }, BlockTags.WALL_SIGNS.getStates().toArray(new StateType[0])),
   CEILING_HANGING_SIGNS((player, item, version, datax, isTargetBlock, x, y, z) -> {
      return switch (datax.getRotation()) {
         case 0, 8 -> new HexCollisionBox(1.0, 0.0, 7.0, 15.0, 10.0, 9.0);
         case 4, 12 -> new HexCollisionBox(7.0, 0.0, 1.0, 9.0, 10.0, 15.0);
         default -> new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
      };
   }, BlockTags.CEILING_HANGING_SIGNS.getStates().toArray(new StateType[0])),
   WALL_HANGING_SIGN(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         return switch (datax.getFacing()) {
            case SOUTH, NORTH -> new ComplexCollisionBox(
               2, new HexCollisionBox(0.0, 14.0, 6.0, 16.0, 16.0, 10.0), new HexCollisionBox(1.0, 0.0, 7.0, 15.0, 10.0, 9.0)
            );
            default -> new ComplexCollisionBox(2, new HexCollisionBox(6.0, 14.0, 0.0, 10.0, 16.0, 16.0), new HexCollisionBox(7.0, 0.0, 1.0, 9.0, 10.0, 15.0));
         };
      },
      BlockTags.WALL_HANGING_SIGNS.getStates().toArray(new StateType[0])
   ),
   STANDING_SIGN(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexCollisionBox(4.0, 0.0, 4.0, 12.0, 16.0, 12.0),
      BlockTags.STANDING_SIGNS.getStates().toArray(new StateType[0])
   ),
   SAPLING(
      new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 12.0, 14.0),
      BlockTags.SAPLINGS.getStates().stream().filter(s -> s != StateTypes.AZALEA && s != StateTypes.FLOWERING_AZALEA).toArray(StateType[]::new)
   ),
   ROOTS(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 13.0, 14.0), StateTypes.WARPED_ROOTS, StateTypes.CRIMSON_ROOTS),
   BANNER(
      new HexCollisionBox(4.0, 0.0, 4.0, 12.0, 16.0, 12.0),
      StateTypes.WHITE_BANNER,
      StateTypes.ORANGE_BANNER,
      StateTypes.MAGENTA_BANNER,
      StateTypes.LIGHT_BLUE_BANNER,
      StateTypes.YELLOW_BANNER,
      StateTypes.LIME_BANNER,
      StateTypes.PINK_BANNER,
      StateTypes.GRAY_BANNER,
      StateTypes.LIGHT_GRAY_BANNER,
      StateTypes.CYAN_BANNER,
      StateTypes.PURPLE_BANNER,
      StateTypes.BLUE_BANNER,
      StateTypes.BROWN_BANNER,
      StateTypes.GREEN_BANNER,
      StateTypes.RED_BANNER,
      StateTypes.BLACK_BANNER
   ),
   WALL_BANNER(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (version.isOlderThan(ClientVersion.V_1_8)) {
            return WALL_SIGN.dynamic.fetch(player, item, version, datax, isTargetBlock, x, y, z);
         } else {
            return switch (datax.getFacing()) {
               case SOUTH -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 12.5, 2.0);
               case WEST -> new HexCollisionBox(14.0, 0.0, 0.0, 16.0, 12.5, 16.0);
               case NORTH -> new HexCollisionBox(0.0, 0.0, 14.0, 16.0, 12.5, 16.0);
               case EAST -> new HexCollisionBox(0.0, 0.0, 0.0, 2.0, 12.5, 16.0);
               default -> throw new IllegalStateException("Impossible Banner Facing State; Something very wrong is going on");
            };
         }
      },
      StateTypes.WHITE_WALL_BANNER,
      StateTypes.ORANGE_WALL_BANNER,
      StateTypes.MAGENTA_WALL_BANNER,
      StateTypes.LIGHT_BLUE_WALL_BANNER,
      StateTypes.YELLOW_WALL_BANNER,
      StateTypes.LIME_WALL_BANNER,
      StateTypes.PINK_WALL_BANNER,
      StateTypes.GRAY_WALL_BANNER,
      StateTypes.LIGHT_GRAY_WALL_BANNER,
      StateTypes.CYAN_WALL_BANNER,
      StateTypes.PURPLE_WALL_BANNER,
      StateTypes.BLUE_WALL_BANNER,
      StateTypes.BROWN_WALL_BANNER,
      StateTypes.GREEN_WALL_BANNER,
      StateTypes.RED_WALL_BANNER,
      StateTypes.BLACK_WALL_BANNER
   ),
   BREWING_STAND(
      (player, item, version, block, isTargetBlock, x, y, z) -> {
         if (version.isOlderThan(ClientVersion.V_1_13)) {
            return (CollisionBox)(isTargetBlock && block.getType() == StateTypes.BREWING_STAND && player.getClientVersion().equals(ClientVersion.V_1_8)
               ? new ComplexCollisionBox(2, new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.125, 1.0), new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true))
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.125, 1.0));
         } else {
            return new ComplexCollisionBox(
               2, new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 2.0, 15.0), new SimpleCollisionBox(0.4375, 0.0, 0.4375, 0.5625, 0.875, 0.5625, false)
            );
         }
      },
      StateTypes.BREWING_STAND
   ),
   SMALL_FLOWER(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(player.getClientVersion().isOlderThan(ClientVersion.V_1_13)
         ? new SimpleCollisionBox(0.3125, 0.0, 0.3125, 0.6875, 0.625, 0.6875)
         : new OffsetCollisionBox(datax.getType(), 0.3125, 0.0, 0.3125, 0.6875, 0.625, 0.6875)),
      BlockTags.SMALL_FLOWERS.getStates().toArray(new StateType[0])
   ),
   TALL_FLOWERS(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), BlockTags.TALL_FLOWERS.getStates().toArray(new StateType[0])),
   FIRE((player, item, version, datax, isTargetBlock, x, y, z) -> {
      if (version.isNewerThanOrEquals(ClientVersion.V_1_16)) {
         ComplexCollisionBox boxes = new ComplexCollisionBox(5);
         if (datax.getWest() == West.TRUE) {
            boxes.add(new HexCollisionBox(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
         }

         if (datax.getEast() == East.TRUE) {
            boxes.add(new HexCollisionBox(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));
         }

         if (datax.getNorth() == North.TRUE) {
            boxes.add(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
         }

         if (datax.getSouth() == South.TRUE) {
            boxes.add(new HexCollisionBox(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
         }

         if (datax.hasProperty(StateValue.UP) && datax.isUp()) {
            boxes.add(new HexCollisionBox(0.0, 15.0, 0.0, 16.0, 16.0, 16.0));
         }

         return (CollisionBox)(boxes.isNull() ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 1.0, 16.0) : boxes);
      } else {
         return NoCollisionBox.INSTANCE;
      }
   }, BlockTags.FIRE.getStates().toArray(new StateType[0])),
   HONEY_BLOCK(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.HONEY_BLOCK),
   POWDER_SNOW(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.POWDER_SNOW),
   SOUL_SAND(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.SOUL_SAND),
   CACTUS(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isOlderThan(ClientVersion.V_1_13)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)),
      StateTypes.CACTUS
   ),
   SNOW((player, item, version, datax, isTargetBlock, x, y, z) -> new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, datax.getLayers() * 0.125, 1.0), StateTypes.SNOW),
   LECTERN_BLOCK(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         ComplexCollisionBox common = new ComplexCollisionBox(
            5, new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), new HexCollisionBox(4.0, 2.0, 4.0, 12.0, 14.0, 12.0)
         );
         if (datax.getFacing() == BlockFace.WEST) {
            common.add(new HexCollisionBox(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0));
            common.add(new HexCollisionBox(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0));
            common.add(new HexCollisionBox(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0));
         } else if (datax.getFacing() == BlockFace.NORTH) {
            common.add(new HexCollisionBox(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333));
            common.add(new HexCollisionBox(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667));
            common.add(new HexCollisionBox(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0));
         } else if (datax.getFacing() == BlockFace.EAST) {
            common.add(new HexCollisionBox(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0));
            common.add(new HexCollisionBox(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0));
            common.add(new HexCollisionBox(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0));
         } else {
            common.add(new HexCollisionBox(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0));
            common.add(new HexCollisionBox(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667));
            common.add(new HexCollisionBox(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333));
         }

         return common;
      },
      StateTypes.LECTERN
   ),
   GLOW_LICHEN_SCULK_VEIN((player, item, version, datax, isTargetBlock, x, y, z) -> {
      if (version.isNewerThan(ClientVersion.V_1_16_4)) {
         ComplexCollisionBox box = new ComplexCollisionBox(6);
         if (datax.isUp()) {
            box.add(new HexCollisionBox(0.0, 15.0, 0.0, 16.0, 16.0, 16.0));
         }

         if (datax.isDown()) {
            box.add(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 1.0, 16.0));
         }

         if (datax.getWest() == West.TRUE) {
            box.add(new HexCollisionBox(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
         }

         if (datax.getEast() == East.TRUE) {
            box.add(new HexCollisionBox(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));
         }

         if (datax.getNorth() == North.TRUE) {
            box.add(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
         }

         if (datax.getSouth() == South.TRUE) {
            box.add(new HexCollisionBox(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
         }

         return box;
      } else {
         return NoCollisionBox.INSTANCE;
      }
   }, StateTypes.GLOW_LICHEN, StateTypes.SCULK_VEIN, StateTypes.RESIN_CLUMP),
   SPORE_BLOSSOM(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_16_4)
         ? new HexCollisionBox(2.0, 13.0, 2.0, 14.0, 16.0, 14.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)),
      StateTypes.SPORE_BLOSSOM
   ),
   PITCHER_CROP(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (version.isNewerThan(ClientVersion.V_1_19_4)) {
            SimpleCollisionBox FULL_UPPER_SHAPE = new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
            SimpleCollisionBox FULL_LOWER_SHAPE = new HexCollisionBox(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
            SimpleCollisionBox COLLISION_SHAPE_BULB = new HexCollisionBox(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
            new HexCollisionBox(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
            SimpleCollisionBox[] UPPER_SHAPE_BY_AGE = new SimpleCollisionBox[]{new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), FULL_UPPER_SHAPE};
            SimpleCollisionBox[] LOWER_SHAPE_BY_AGE = new SimpleCollisionBox[]{
               COLLISION_SHAPE_BULB, new HexCollisionBox(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), FULL_LOWER_SHAPE, FULL_LOWER_SHAPE, FULL_LOWER_SHAPE
            };
            return datax.getHalf() == Half.UPPER
               ? UPPER_SHAPE_BY_AGE[Math.min(Math.abs(4 - (datax.getAge() + 1)), UPPER_SHAPE_BY_AGE.length - 1)]
               : LOWER_SHAPE_BY_AGE[datax.getAge()];
         } else {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         }
      },
      StateTypes.PITCHER_CROP
   ),
   WHEAT_BEETROOTS(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, (datax.getAge() + 1) * 2, 16.0),
      StateTypes.WHEAT,
      StateTypes.BEETROOTS
   ),
   CARROT_POTATOES(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, datax.getAge() + 2, 16.0),
      StateTypes.CARROTS,
      StateTypes.POTATOES
   ),
   NETHER_WART(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 5 + datax.getAge() * 3, 16.0), StateTypes.NETHER_WART
   ),
   ATTACHED_PUMPKIN_STEM((player, item, version, datax, isTargetBlock, x, y, z) -> {
      if (version.isOlderThan(ClientVersion.V_1_13)) {
         return new HexCollisionBox(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
      } else {
         return switch (datax.getFacing()) {
            case SOUTH -> new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 10.0, 16.0);
            case WEST -> new HexCollisionBox(0.0, 0.0, 6.0, 10.0, 10.0, 10.0);
            case NORTH -> new HexCollisionBox(6.0, 0.0, 0.0, 10.0, 10.0, 10.0);
            default -> new HexCollisionBox(6.0, 0.0, 6.0, 16.0, 10.0, 10.0);
         };
      }
   }, StateTypes.ATTACHED_MELON_STEM, StateTypes.ATTACHED_PUMPKIN_STEM),
   PUMPKIN_STEM(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexCollisionBox(7.0, 0.0, 7.0, 9.0, 2 * (datax.getAge() + 1), 9.0),
      StateTypes.PUMPKIN_STEM,
      StateTypes.MELON_STEM
   ),
   COCOA_BEANS((player, item, version, datax, isTargetBlock, x, y, z) -> CollisionData.getCocoa(version, datax.getAge(), datax.getFacing()), StateTypes.COCOA),
   REDSTONE_WIRE(NoCollisionBox.INSTANCE, StateTypes.REDSTONE_WIRE),
   SWEET_BERRY(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (datax.getAge() == 0) {
            return new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
         } else {
            return (CollisionBox)(datax.getAge() < 3
               ? new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true));
         }
      },
      StateTypes.SWEET_BERRY_BUSH
   ),
   CORAL_PLANTS(
      new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 15.0, 14.0),
      ArrayUtils.combine(
         BlockTags.CORAL_PLANTS.getStates(),
         StateTypes.DEAD_TUBE_CORAL,
         StateTypes.DEAD_BRAIN_CORAL,
         StateTypes.DEAD_BUBBLE_CORAL,
         StateTypes.DEAD_FIRE_CORAL,
         StateTypes.DEAD_HORN_CORAL
      )
   ),
   CORAL_FAN(
      new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 4.0, 14.0),
      StateTypes.TUBE_CORAL_FAN,
      StateTypes.BRAIN_CORAL_FAN,
      StateTypes.BUBBLE_CORAL_FAN,
      StateTypes.FIRE_CORAL_FAN,
      StateTypes.HORN_CORAL_FAN,
      StateTypes.DEAD_TUBE_CORAL_FAN,
      StateTypes.DEAD_BRAIN_CORAL_FAN,
      StateTypes.DEAD_BUBBLE_CORAL_FAN,
      StateTypes.DEAD_FIRE_CORAL_FAN,
      StateTypes.DEAD_HORN_CORAL_FAN
   ),
   CORAL_WALL_FAN(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         return switch (datax.getFacing()) {
            case SOUTH -> new HexCollisionBox(0.0, 4.0, 0.0, 16.0, 12.0, 11.0);
            case WEST -> new HexCollisionBox(5.0, 4.0, 0.0, 16.0, 12.0, 16.0);
            case NORTH -> new HexCollisionBox(0.0, 4.0, 5.0, 16.0, 12.0, 16.0);
            case EAST -> new HexCollisionBox(0.0, 4.0, 0.0, 11.0, 12.0, 16.0);
            default -> throw new UnsupportedOperationException();
         };
      },
      ArrayUtils.combine(
         BlockTags.WALL_CORALS.getStates(),
         StateTypes.DEAD_TUBE_CORAL_WALL_FAN,
         StateTypes.DEAD_BRAIN_CORAL_WALL_FAN,
         StateTypes.DEAD_BUBBLE_CORAL_WALL_FAN,
         StateTypes.DEAD_FIRE_CORAL_WALL_FAN,
         StateTypes.DEAD_HORN_CORAL_WALL_FAN
      )
   ),
   TORCHFLOWER_CROP(
      (player, item, version, datax, isTargetBlock, x, y, z) -> datax.getAge() == 0
         ? new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 6.0, 11.0)
         : new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 10.0, 11.0),
      StateTypes.TORCHFLOWER_CROP
   ),
   DEAD_BUSH(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 13.0, 14.0), StateTypes.DEAD_BUSH),
   SUGARCANE(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), StateTypes.SUGAR_CANE),
   NETHER_SPROUTS(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), StateTypes.NETHER_SPROUTS),
   HANGING_ROOTS(new HexCollisionBox(2.0, 10.0, 2.0, 14.0, 16.0, 14.0), StateTypes.HANGING_ROOTS),
   HANGING_MOSS(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isOlderThan(ClientVersion.V_1_21_2)
         ? HANGING_ROOTS.fetch(player, item, version, datax, isTargetBlock, x, y, z)
         : new HexCollisionBox(1.0, datax.isTip() ? 2.0 : 0.0, 1.0, 15.0, 16.0, 15.0)),
      StateTypes.PALE_HANGING_MOSS
   ),
   GRASS_FERN(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isOlderThan(ClientVersion.V_1_13)
         ? new SimpleCollisionBox(0.1F, 0.0, 0.1F, 0.9F, 0.8F, 0.9F)
         : new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)),
      StateTypes.SHORT_GRASS,
      StateTypes.FERN
   ),
   SEA_GRASS(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 12.0, 14.0), StateTypes.SEAGRASS),
   TALL_SEAGRASS(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), StateTypes.TALL_SEAGRASS),
   SMALL_DRIPLEAF(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 13.0, 14.0), StateTypes.SMALL_DRIPLEAF),
   CAVE_VINES(new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 16.0, 15.0), StateTypes.CAVE_VINES, StateTypes.CAVE_VINES_PLANT),
   MUSHROOM(new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 6.0, 11.0), StateTypes.BROWN_MUSHROOM, StateTypes.RED_MUSHROOM),
   FUNGUS(new HexCollisionBox(4.0, 0.0, 4.0, 12.0, 9.0, 12.0), StateTypes.CRIMSON_FUNGUS, StateTypes.WARPED_FUNGUS),
   TWISTING_VINES_BLOCK((player, item, version, datax, isTargetBlock, x, y, z) -> getVineCollisionBox(version, false, true), StateTypes.TWISTING_VINES),
   WEEPING_VINES_BLOCK((player, item, version, datax, isTargetBlock, x, y, z) -> getVineCollisionBox(version, true, true), StateTypes.WEEPING_VINES),
   TWISTING_VINES((player, item, version, datax, isTargetBlock, x, y, z) -> getVineCollisionBox(version, false, false), StateTypes.TWISTING_VINES_PLANT),
   WEEPING_VINES((player, item, version, datax, isTargetBlock, x, y, z) -> getVineCollisionBox(version, true, false), StateTypes.WEEPING_VINES_PLANT),
   TALL_PLANT(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.TALL_GRASS, StateTypes.LARGE_FERN),
   BAMBOO(
      (player, item, version, datax, isTargetBlock, x, y, z) -> datax.getLeaves() == Leaves.LARGE
         ? new HexOffsetCollisionBox(datax.getType(), 3.0, 0.0, 3.0, 13.0, 16.0, 13.0)
         : new HexOffsetCollisionBox(datax.getType(), 5.0, 0.0, 5.0, 11.0, 16.0, 11.0),
      StateTypes.BAMBOO
   ),
   BAMBOO_SAPLING(
      (player, item, version, datax, isTargetBlock, x, y, z) -> new HexOffsetCollisionBox(datax.getType(), 4.0, 0.0, 4.0, 12.0, 12.0, 12.0),
      StateTypes.BAMBOO_SAPLING
   ),
   SCAFFOLDING(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (item != StateTypes.SCAFFOLDING && !version.isOlderThan(ClientVersion.V_1_14)) {
            ComplexCollisionBox box = new ComplexCollisionBox(
               9,
               new HexCollisionBox(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
               new HexCollisionBox(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
               new HexCollisionBox(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
               new HexCollisionBox(0.0, 0.0, 14.0, 2.0, 16.0, 16.0),
               new HexCollisionBox(14.0, 0.0, 14.0, 16.0, 16.0, 16.0)
            );
            if (datax.isBottom()) {
               box.add(new HexCollisionBox(0.0, 0.0, 0.0, 2.0, 2.0, 16.0));
               box.add(new HexCollisionBox(14.0, 0.0, 0.0, 16.0, 2.0, 16.0));
               box.add(new HexCollisionBox(0.0, 0.0, 14.0, 16.0, 2.0, 16.0));
               box.add(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 2.0, 2.0));
            }

            return box;
         } else {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         }
      },
      StateTypes.SCAFFOLDING
   ),
   DRIPLEAF((player, item, version, datax, isTargetBlock, x, y, z) -> {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
      } else {
         ComplexCollisionBox box = new ComplexCollisionBox(2);
         if (datax.getFacing() == BlockFace.NORTH) {
            box.add(new HexCollisionBox(5.0, 0.0, 9.0, 11.0, 15.0, 15.0));
         } else if (datax.getFacing() == BlockFace.SOUTH) {
            box.add(new HexCollisionBox(5.0, 0.0, 1.0, 11.0, 15.0, 7.0));
         } else if (datax.getFacing() == BlockFace.EAST) {
            box.add(new HexCollisionBox(1.0, 0.0, 5.0, 7.0, 15.0, 11.0));
         } else {
            box.add(new HexCollisionBox(9.0, 0.0, 5.0, 15.0, 15.0, 11.0));
         }

         if (datax.getTilt() == Tilt.NONE || datax.getTilt() == Tilt.UNSTABLE) {
            box.add(new HexCollisionBox(0.0, 11.0, 0.0, 16.0, 15.0, 16.0));
         } else if (datax.getTilt() == Tilt.PARTIAL) {
            box.add(new HexCollisionBox(0.0, 11.0, 0.0, 16.0, 13.0, 16.0));
         }

         return box;
      }
   }, StateTypes.BIG_DRIPLEAF),
   PINK_PETALS_BLOCK(
      (player, item, version, datax, isTargetBlock, x, y, z) -> {
         if (version.isNewerThan(ClientVersion.V_1_20_2)) {
            return getSegmentedHitBox(datax.getFlowerAmount(), datax.getFacing(), 3);
         } else if (version.isNewerThan(ClientVersion.V_1_19_3)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0);
         } else {
            return version.isNewerThan(ClientVersion.V_1_12_2)
               ? CORAL_FAN.box.copy()
               : GRASS_FERN.dynamic.fetch(player, item, version, datax, isTargetBlock, x, y, z);
         }
      },
      StateTypes.PINK_PETALS
   ),
   MANGROVE_PROPAGULE(
      (player, item, version, datax, isTargetBlock, x, y, z) -> datax.isHanging()
         ? new HexOffsetCollisionBox(datax.getType(), 7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
         : new HexOffsetCollisionBox(datax.getType(), 7.0, getPropaguleMinHeight(datax.getAge()), 7.0, 9.0, 16.0, 9.0),
      StateTypes.MANGROVE_PROPAGULE
   ),
   FROGSPAWN(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_18_2)
         ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 1.5, 16.0)
         : NoCollisionBox.INSTANCE),
      StateTypes.FROGSPAWN
   ),
   BUSH(
      (player, heldItem, version, block, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0)
         : GRASS_FERN.dynamic.fetch(player, heldItem, version, block, isTargetBlock, x, y, z)),
      StateTypes.BUSH
   ),
   SHORT_DRY_GRASS(
      (player, heldItem, version, block, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? new SimpleCollisionBox(0.125, 0.0, 0.125, 0.875, 0.625, 0.875)
         : GRASS_FERN.dynamic.fetch(player, heldItem, version, block, isTargetBlock, x, y, z)),
      StateTypes.SHORT_DRY_GRASS
   ),
   TALL_DRY_GRASS(
      (player, heldItem, version, block, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? new SimpleCollisionBox(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375)
         : GRASS_FERN.dynamic.fetch(player, heldItem, version, block, isTargetBlock, x, y, z)),
      StateTypes.TALL_DRY_GRASS
   ),
   LEAF_LITTER(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? getSegmentedHitBox(datax.getSegmentAmount(), datax.getFacing(), 1)
         : new HexCollisionBox(0.0, 15.0, 0.0, 16.0, 16.0, 16.0)),
      StateTypes.LEAF_LITTER
   ),
   WILDFLOWERS(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? getSegmentedHitBox(datax.getFlowerAmount(), datax.getFacing(), 3)
         : new HexCollisionBox(0.0, 15.0, 0.0, 16.0, 16.0, 16.0)),
      StateTypes.WILDFLOWERS
   ),
   CACTUS_FLOWER(
      (player, item, version, datax, isTargetBlock, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_21_4)
         ? new SimpleCollisionBox(0.0625, 0.0, 0.0625, 0.9375, 0.75, 0.9375)
         : CORAL_FAN.box.copy()),
      StateTypes.CACTUS_FLOWER
   ),
   SCULK_SHRIEKER(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.SCULK_SHRIEKER);

   private static final Map<StateType, HitboxData> lookup = new HashMap<>();
   private final StateType[] materials;
   private CollisionBox box;
   private HitBoxFactory dynamic;

   private HitboxData(CollisionBox box, StateType... materials) {
      this.box = box;
      Set<StateType> mList = new HashSet<>(Arrays.asList(materials));
      mList.remove(null);
      this.materials = mList.toArray(new StateType[0]);
   }

   private HitboxData(HitBoxFactory dynamic, StateType... materials) {
      this.dynamic = dynamic;
      Set<StateType> mList = new HashSet<>(Arrays.asList(materials));
      mList.remove(null);
      this.materials = mList.toArray(new StateType[0]);
   }

   @Override
   public CollisionBox fetch(GrimPlayer player, StateType heldItem, ClientVersion version, WrappedBlockState block, boolean isTargetBlock, int x, int y, int z) {
      return this.box != null ? this.box.copy() : this.dynamic.fetch(player, heldItem, version, block, isTargetBlock, x, y, z);
   }

   public static HitboxData getData(StateType material) {
      return lookup.get(material);
   }

   public static CollisionBox getBlockHitbox(
      GrimPlayer player, StateType heldItem, ClientVersion version, WrappedBlockState block, boolean isTargetBlock, int x, int y, int z
   ) {
      HitboxData data = getData(block.getType());
      return data == null
         ? CollisionData.getRawData(block.getType()).getMovementCollisionBox(player, version, block, x, y, z)
         : data.fetch(player, heldItem, version, block, isTargetBlock, x, y, z).offset(x, y, z);
   }

   private static int getPropaguleMinHeight(int age) {
      return switch (age) {
         case 0, 1, 2 -> 13 - age * 3;
         case 3, 4 -> (4 - age) * 3;
         default -> throw new IllegalStateException("Impossible Propagule Height");
      };
   }

   private static CollisionBox getVineCollisionBox(ClientVersion version, boolean isWeeping, boolean isBlock) {
      if (version.isNewerThan(ClientVersion.V_1_15_2)) {
         return isWeeping
            ? (isBlock ? new SimpleCollisionBox(0.25, 0.5625, 0.25, 0.75, 1.0, 0.75) : new SimpleCollisionBox(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375))
            : new SimpleCollisionBox(0.25, 0.0, 0.25, 0.75, isBlock ? 0.9375 : 1.0, 0.75);
      } else {
         return new ComplexCollisionBox(
            4,
            new SimpleCollisionBox(0.0, 0.0, 0.0, 0.0625, 1.0, 1.0),
            new SimpleCollisionBox(0.9375, 0.0, 0.0, 1.0, 1.0, 1.0),
            new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.0625),
            new SimpleCollisionBox(0.0, 0.0, 0.9375, 1.0, 1.0, 1.0)
         );
      }
   }

   private static CollisionBox getSegmentedHitBox(int segments, BlockFace facing, int height) {
      return (CollisionBox)(switch (segments) {
         case 0 -> NoCollisionBox.INSTANCE;
         case 1 -> {
            switch (facing) {
               case SOUTH:
                  yield new SimpleCollisionBox(0.5, 0.0, 0.5, 1.0, height / 16.0, 1.0, false);
               case WEST:
                  yield new SimpleCollisionBox(0.5, 0.0, 0.0, 1.0, height / 16.0, 0.5, false);
               case NORTH:
                  yield new SimpleCollisionBox(0.0, 0.0, 0.0, 0.5, height / 16.0, 0.5, false);
               case EAST:
                  yield new SimpleCollisionBox(0.0, 0.0, 0.5, 0.5, height / 16.0, 1.0, false);
               default:
                  throw new IllegalStateException("Unexpected value: " + facing);
            }
         }
         case 2 -> {
            switch (facing) {
               case SOUTH:
                  yield new SimpleCollisionBox(0.5, 0.0, 0.0, 1.0, height / 16.0, 1.0, false);
               case WEST:
                  yield new SimpleCollisionBox(0.0, 0.0, 0.5, 1.0, height / 16.0, 1.0, false);
               case NORTH:
                  yield new SimpleCollisionBox(0.0, 0.0, 0.0, 0.5, height / 16.0, 1.0, false);
               case EAST:
                  yield new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, height / 16.0, 0.5, false);
               default:
                  throw new IllegalStateException("Unexpected value: " + facing);
            }
         }
         case 3, 4 -> new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, height / 16.0, 1.0, false);
         default -> throw new IllegalStateException("Unexpected value: " + segments);
      });
   }

   static {
      for (HitboxData data : values()) {
         for (StateType type : data.materials) {
            lookup.put(type, data);
         }
      }
   }
}
