package ac.grim.grimac.utils.collisions;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.movementtick.MovementTickerStrider;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Attachment;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Axis;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Face;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Part;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Thickness;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Tilt;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.Type;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.VerticalDirection;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.collisions.blocks.DoorHandler;
import ac.grim.grimac.utils.collisions.blocks.DynamicChest;
import ac.grim.grimac.utils.collisions.blocks.DynamicChorusPlant;
import ac.grim.grimac.utils.collisions.blocks.DynamicStair;
import ac.grim.grimac.utils.collisions.blocks.PistonBaseCollision;
import ac.grim.grimac.utils.collisions.blocks.PistonHeadCollision;
import ac.grim.grimac.utils.collisions.blocks.TrapDoorHandler;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicCollisionFence;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicCollisionPane;
import ac.grim.grimac.utils.collisions.blocks.connecting.DynamicCollisionWall;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.CollisionFactory;
import ac.grim.grimac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.DynamicCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.HexCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.HexOffsetCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.packetentity.PacketEntityStrider;
import ac.grim.grimac.utils.nmsutil.Materials;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import com.viaversion.viaversion.api.Via;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public enum CollisionData implements CollisionFactory {
   LAVA(
      (player, version, block, x, y, z) -> (CollisionBox)(MovementTickerStrider.isAbove(player)
            && player.compensatedEntities.self.getRiding() instanceof PacketEntityStrider
            && block.getLevel() == 0
         ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
         : NoCollisionBox.INSTANCE),
      StateTypes.LAVA
   ),
   BREWING_STAND(
      (player, version, block, x, y, z) -> {
         int base = 0;
         int maxIndex = 3;
         if (version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            maxIndex = 2;
            base = 1;
         }

         return new ComplexCollisionBox(
            maxIndex,
            new HexCollisionBox(base, 0.0, base, 16 - base, 2.0, 16 - base),
            new SimpleCollisionBox(0.4375, 0.0, 0.4375, 0.5625, 0.875, 0.5625, false)
         );
      },
      StateTypes.BREWING_STAND
   ),
   BAMBOO(
      (player, version, block, x, y, z) -> (CollisionBox)(version.isOlderThan(ClientVersion.V_1_14)
         ? NoCollisionBox.INSTANCE
         : new HexOffsetCollisionBox(block.getType(), 6.5, 0.0, 6.5, 9.5, 16.0, 9.5)),
      StateTypes.BAMBOO
   ),
   COMPOSTER(
      (player, version, block, x, y, z) -> {
         double height = 0.125;
         if (version.isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
            height = 0.25;
         }

         if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
            height = 0.3125;
         }

         return new ComplexCollisionBox(
            5,
            new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, height, 1.0, false),
            new SimpleCollisionBox(0.0, height, 0.0, 0.125, 1.0, 1.0, false),
            new SimpleCollisionBox(0.875, height, 0.0, 1.0, 1.0, 1.0, false),
            new SimpleCollisionBox(0.0, height, 0.0, 1.0, 1.0, 0.125, false),
            new SimpleCollisionBox(0.0, height, 0.875, 1.0, 1.0, 1.0, false)
         );
      },
      StateTypes.COMPOSTER
   ),
   RAIL(
      new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.125, 0.0, false),
      StateTypes.RAIL,
      StateTypes.ACTIVATOR_RAIL,
      StateTypes.DETECTOR_RAIL,
      StateTypes.POWERED_RAIL
   ),
   ANVIL(
      (player, version, datax, x, y, z) -> {
         BlockFace face = datax.getFacing();
         if (!version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            return face != BlockFace.NORTH && face != BlockFace.SOUTH
               ? new SimpleCollisionBox(0.0, 0.0, 0.125, 1.0, 1.0, 0.875, false)
               : new SimpleCollisionBox(0.125, 0.0, 0.0, 0.875, 1.0, 1.0, false);
         } else {
            ComplexCollisionBox complexAnvil = new ComplexCollisionBox(4);
            complexAnvil.add(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 4.0, 14.0));
            if (face != BlockFace.NORTH && face != BlockFace.SOUTH) {
               complexAnvil.add(new HexCollisionBox(3.0, 4.0, 4.0, 13.0, 5.0, 12.0));
               complexAnvil.add(new HexCollisionBox(4.0, 5.0, 6.0, 12.0, 10.0, 10.0));
               complexAnvil.add(new HexCollisionBox(0.0, 10.0, 3.0, 16.0, 16.0, 13.0));
            } else {
               complexAnvil.add(new HexCollisionBox(4.0, 4.0, 3.0, 12.0, 5.0, 13.0));
               complexAnvil.add(new HexCollisionBox(6.0, 5.0, 4.0, 10.0, 10.0, 12.0));
               complexAnvil.add(new HexCollisionBox(3.0, 10.0, 0.0, 13.0, 16.0, 16.0));
            }

            return complexAnvil;
         }
      },
      BlockTags.ANVIL.getStates().toArray(new StateType[0])
   ),
   WALL(new DynamicCollisionWall(), BlockTags.WALLS.getStates().toArray(new StateType[0])),
   SLAB(
      (player, version, datax, x, y, z) -> {
         Type slabType = datax.getTypeData();
         if (slabType == Type.DOUBLE) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else {
            return slabType == Type.BOTTOM
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.5, 1.0, false)
               : new SimpleCollisionBox(0.0, 0.5, 0.0, 1.0, 1.0, 1.0, false);
         }
      },
      BlockTags.SLABS.getStates().toArray(new StateType[0])
   ),
   SKULL(
      new SimpleCollisionBox(0.25, 0.0, 0.25, 0.75, 0.5, 0.75, false),
      StateTypes.CREEPER_HEAD,
      StateTypes.ZOMBIE_HEAD,
      StateTypes.DRAGON_HEAD,
      StateTypes.PLAYER_HEAD,
      StateTypes.SKELETON_SKULL,
      StateTypes.WITHER_SKELETON_SKULL,
      StateTypes.HEAVY_CORE
   ),
   PIGLIN_HEAD(new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 8.0, 13.0), StateTypes.PIGLIN_HEAD),
   WALL_SKULL(
      (player, version, datax, x, y, z) -> {
         return switch (datax.getFacing()) {
            case SOUTH -> new SimpleCollisionBox(0.25, 0.25, 0.0, 0.75, 0.75, 0.5, false);
            case EAST -> new SimpleCollisionBox(0.0, 0.25, 0.25, 0.5, 0.75, 0.75, false);
            case WEST -> new SimpleCollisionBox(0.5, 0.25, 0.25, 1.0, 0.75, 0.75, false);
            default -> new SimpleCollisionBox(0.25, 0.25, 0.5, 0.75, 0.75, 1.0, false);
         };
      },
      StateTypes.CREEPER_WALL_HEAD,
      StateTypes.DRAGON_WALL_HEAD,
      StateTypes.PLAYER_WALL_HEAD,
      StateTypes.ZOMBIE_WALL_HEAD,
      StateTypes.SKELETON_WALL_SKULL,
      StateTypes.WITHER_SKELETON_WALL_SKULL
   ),
   PIGLIN_WALL_HEAD((player, version, datax, x, y, z) -> {
      return switch (datax.getFacing()) {
         case SOUTH -> new HexCollisionBox(3.0, 4.0, 0.0, 13.0, 12.0, 8.0);
         case EAST -> new HexCollisionBox(0.0, 4.0, 3.0, 8.0, 12.0, 13.0);
         case WEST -> new HexCollisionBox(8.0, 4.0, 3.0, 16.0, 12.0, 13.0);
         default -> new HexCollisionBox(3.0, 4.0, 8.0, 13.0, 12.0, 16.0);
      };
   }, StateTypes.PIGLIN_WALL_HEAD),
   DOOR(new DoorHandler(), BlockTags.DOORS.getStates().toArray(new StateType[0])),
   HOPPER(
      (player, version, datax, x, y, z) -> {
         if (version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            ComplexCollisionBox hopperBox = new ComplexCollisionBox(7);
            switch (datax.getFacing()) {
               case DOWN:
                  hopperBox.add(new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
                  break;
               case NORTH:
                  hopperBox.add(new HexCollisionBox(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
                  break;
               case SOUTH:
                  hopperBox.add(new HexCollisionBox(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
                  break;
               case EAST:
                  hopperBox.add(new HexCollisionBox(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
                  break;
               case WEST:
                  hopperBox.add(new HexCollisionBox(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
            }

            hopperBox.add(new SimpleCollisionBox(0.0, 0.625, 0.0, 1.0, 0.6875, 1.0, false));
            hopperBox.add(new SimpleCollisionBox(0.0, 0.6875, 0.0, 0.125, 1.0, 1.0, false));
            hopperBox.add(new SimpleCollisionBox(0.125, 0.6875, 0.0, 1.0, 1.0, 0.125, false));
            hopperBox.add(new SimpleCollisionBox(0.125, 0.6875, 0.875, 1.0, 1.0, 1.0, false));
            hopperBox.add(new SimpleCollisionBox(0.25, 0.25, 0.25, 0.75, 0.625, 0.75, false));
            hopperBox.add(new SimpleCollisionBox(0.875, 0.6875, 0.125, 1.0, 1.0, 0.875, false));
            return hopperBox;
         } else {
            double height = 0.625;
            return new ComplexCollisionBox(
               5,
               new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, height, 1.0, false),
               new SimpleCollisionBox(0.0, height, 0.0, 0.125, 1.0, 1.0, false),
               new SimpleCollisionBox(0.875, height, 0.0, 1.0, 1.0, 1.0, false),
               new SimpleCollisionBox(0.0, height, 0.0, 1.0, 1.0, 0.125, false),
               new SimpleCollisionBox(0.0, height, 0.875, 1.0, 1.0, 1.0, false)
            );
         }
      },
      StateTypes.HOPPER
   ),
   CAKE((player, version, datax, x, y, z) -> {
      double height = 0.5;
      if (version.isOlderThan(ClientVersion.V_1_8)) {
         height = 0.4375;
      }

      double eatenPosition = (1 + datax.getBites() * 2) / 16.0;
      return new SimpleCollisionBox(eatenPosition, 0.0, 0.0625, 0.9375, height, 0.9375, false);
   }, StateTypes.CAKE),
   COCOA_BEANS((player, version, datax, x, y, z) -> getCocoa(version, datax.getAge(), datax.getFacing()), StateTypes.COCOA),
   STONE_CUTTER(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isOlderThanOrEquals(ClientVersion.V_1_13_2)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 9.0, 16.0)),
      StateTypes.STONECUTTER
   ),
   CORAL_FAN(NoCollisionBox.INSTANCE, BlockTags.CORALS.getStates().toArray(new StateType[0])),
   RAILS(NoCollisionBox.INSTANCE, BlockTags.RAILS.getStates().toArray(new StateType[0])),
   BANNER(NoCollisionBox.INSTANCE, BlockTags.BANNERS.getStates().toArray(new StateType[0])),
   SMALL_FLOWER(NoCollisionBox.INSTANCE, BlockTags.SMALL_FLOWERS.getStates().toArray(new StateType[0])),
   TALL_FLOWER(NoCollisionBox.INSTANCE, BlockTags.TALL_FLOWERS.getStates().toArray(new StateType[0])),
   SAPLING(NoCollisionBox.INSTANCE, BlockTags.SAPLINGS.getStates().toArray(new StateType[0])),
   BUTTON(NoCollisionBox.INSTANCE, BlockTags.BUTTONS.getStates().toArray(new StateType[0])),
   NO_COLLISION(
      NoCollisionBox.INSTANCE,
      StateTypes.TWISTING_VINES_PLANT,
      StateTypes.WEEPING_VINES_PLANT,
      StateTypes.TWISTING_VINES,
      StateTypes.WEEPING_VINES,
      StateTypes.CAVE_VINES,
      StateTypes.CAVE_VINES_PLANT,
      StateTypes.TALL_SEAGRASS,
      StateTypes.SEAGRASS,
      StateTypes.SHORT_GRASS,
      StateTypes.FERN,
      StateTypes.NETHER_SPROUTS,
      StateTypes.DEAD_BUSH,
      StateTypes.SUGAR_CANE,
      StateTypes.SWEET_BERRY_BUSH,
      StateTypes.WARPED_ROOTS,
      StateTypes.CRIMSON_ROOTS,
      StateTypes.TORCHFLOWER_CROP,
      StateTypes.PINK_PETALS,
      StateTypes.TALL_GRASS,
      StateTypes.LARGE_FERN,
      StateTypes.BAMBOO_SAPLING,
      StateTypes.HANGING_ROOTS,
      StateTypes.VINE,
      StateTypes.SMALL_DRIPLEAF,
      StateTypes.END_PORTAL,
      StateTypes.LEVER,
      StateTypes.PUMPKIN_STEM,
      StateTypes.MELON_STEM,
      StateTypes.ATTACHED_MELON_STEM,
      StateTypes.ATTACHED_PUMPKIN_STEM,
      StateTypes.BEETROOTS,
      StateTypes.POTATOES,
      StateTypes.WHEAT,
      StateTypes.CARROTS,
      StateTypes.NETHER_WART,
      StateTypes.MOVING_PISTON,
      StateTypes.AIR,
      StateTypes.CAVE_AIR,
      StateTypes.VOID_AIR,
      StateTypes.LIGHT,
      StateTypes.WATER,
      StateTypes.BUBBLE_COLUMN,
      StateTypes.FIRE,
      StateTypes.SOUL_FIRE
   ),
   KELP(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 9.0, 16.0), StateTypes.KELP),
   BELL(
      (player, version, datax, x, y, z) -> {
         if (version.isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else {
            BlockFace direction = datax.getFacing();
            if (datax.getAttachment() == Attachment.FLOOR) {
               return direction != BlockFace.NORTH && direction != BlockFace.SOUTH
                  ? new HexCollisionBox(4.0, 0.0, 0.0, 12.0, 16.0, 16.0)
                  : new HexCollisionBox(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
            } else {
               ComplexCollisionBox complex = new ComplexCollisionBox(
                  3, new HexCollisionBox(5.0, 6.0, 5.0, 11.0, 13.0, 11.0), new HexCollisionBox(4.0, 4.0, 4.0, 12.0, 6.0, 12.0)
               );
               if (datax.getAttachment() == Attachment.CEILING) {
                  complex.add(new HexCollisionBox(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
               } else if (datax.getAttachment() == Attachment.DOUBLE_WALL) {
                  if (direction != BlockFace.NORTH && direction != BlockFace.SOUTH) {
                     complex.add(new HexCollisionBox(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
                  } else {
                     complex.add(new HexCollisionBox(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
                  }
               } else if (direction == BlockFace.NORTH) {
                  complex.add(new HexCollisionBox(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
               } else if (direction == BlockFace.SOUTH) {
                  complex.add(new HexCollisionBox(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
               } else if (direction == BlockFace.EAST) {
                  complex.add(new HexCollisionBox(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
               } else {
                  complex.add(new HexCollisionBox(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
               }

               return complex;
            }
         }
      },
      StateTypes.BELL
   ),
   SCAFFOLDING(
      (player, version, datax, x, y, z) -> {
         if (version.isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else if (player.lastY > y + 1 - 1.0E-5 && !player.isSneaking) {
            return new ComplexCollisionBox(
               5,
               new HexCollisionBox(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
               new HexCollisionBox(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
               new HexCollisionBox(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
               new HexCollisionBox(0.0, 0.0, 14.0, 2.0, 16.0, 16.0),
               new HexCollisionBox(14.0, 0.0, 14.0, 16.0, 16.0, 16.0)
            );
         } else {
            return (CollisionBox)(datax.getDistance() != 0 && datax.isBottom() && player.lastY > y - 1.0E-5
               ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 2.0, 16.0)
               : NoCollisionBox.INSTANCE);
         }
      },
      StateTypes.SCAFFOLDING
   ),
   LADDER((player, version, datax, x, y, z) -> {
      int width = 3;
      if (version.isOlderThanOrEquals(ClientVersion.V_1_8)) {
         width = 2;
      }
      return switch (datax.getFacing()) {
         case NORTH -> new HexCollisionBox(0.0, 0.0, 16.0 - width, 16.0, 16.0, 16.0);
         case SOUTH -> new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, width);
         default -> new HexCollisionBox(0.0, 0.0, 0.0, width, 16.0, 16.0);
         case WEST -> new HexCollisionBox(16.0 - width, 0.0, 0.0, 16.0, 16.0, 16.0);
      };
   }, StateTypes.LADDER),
   CAMPFIRE((player, version, datax, x, y, z) -> {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
         return (CollisionBox)(datax.isLit() ? NoCollisionBox.INSTANCE : new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0));
      } else {
         return new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
      }
   }, StateTypes.CAMPFIRE, StateTypes.SOUL_CAMPFIRE),
   LANTERN(
      (player, version, datax, x, y, z) -> {
         if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else {
            return datax.isHanging()
               ? new ComplexCollisionBox(2, new HexCollisionBox(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), new HexCollisionBox(6.0, 8.0, 6.0, 10.0, 10.0, 10.0))
               : new ComplexCollisionBox(2, new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), new HexCollisionBox(6.0, 7.0, 6.0, 10.0, 9.0, 10.0));
         }
      },
      BlockTags.LANTERNS.getStates().toArray(new StateType[0])
   ),
   LECTERN(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isOlderThanOrEquals(ClientVersion.V_1_13_2)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new ComplexCollisionBox(2, new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), new HexCollisionBox(4.0, 2.0, 4.0, 12.0, 14.0, 12.0))),
      StateTypes.LECTERN
   ),
   HONEY_BLOCK(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isOlderThanOrEquals(ClientVersion.V_1_14_4)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)),
      StateTypes.HONEY_BLOCK
   ),
   DRAGON_EGG_BLOCK(new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 16.0, 15.0), StateTypes.DRAGON_EGG),
   GRINDSTONE(
      (player, version, datax, x, y, z) -> {
         BlockFace facing = datax.getFacing();
         if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
            return facing != BlockFace.NORTH && facing != BlockFace.SOUTH
               ? new SimpleCollisionBox(0.0, 0.0, 0.125, 1.0, 1.0, 0.875, false)
               : new SimpleCollisionBox(0.125, 0.0, 0.0, 0.875, 1.0, 1.0, false);
         } else if (!version.isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
            Face attachment = datax.getFace();
            if (attachment == Face.FLOOR) {
               return facing != BlockFace.NORTH && facing != BlockFace.SOUTH
                  ? new ComplexCollisionBox(
                     5,
                     new HexCollisionBox(6.0, 0.0, 2.0, 10.0, 7.0, 4.0),
                     new HexCollisionBox(6.0, 0.0, 12.0, 10.0, 7.0, 14.0),
                     new HexCollisionBox(5.0, 7.0, 2.0, 11.0, 13.0, 4.0),
                     new HexCollisionBox(5.0, 7.0, 12.0, 11.0, 13.0, 14.0),
                     new HexCollisionBox(2.0, 4.0, 4.0, 14.0, 16.0, 12.0)
                  )
                  : new ComplexCollisionBox(
                     5,
                     new HexCollisionBox(2.0, 0.0, 6.0, 4.0, 7.0, 10.0),
                     new HexCollisionBox(12.0, 0.0, 6.0, 14.0, 7.0, 10.0),
                     new HexCollisionBox(2.0, 7.0, 5.0, 4.0, 13.0, 11.0),
                     new HexCollisionBox(12.0, 7.0, 5.0, 14.0, 13.0, 11.0),
                     new HexCollisionBox(4.0, 4.0, 2.0, 12.0, 16.0, 14.0)
                  );
            } else if (attachment == Face.WALL) {
               switch (facing) {
                  case NORTH:
                     return new ComplexCollisionBox(
                        5,
                        new HexCollisionBox(2.0, 6.0, 7.0, 4.0, 10.0, 16.0),
                        new HexCollisionBox(12.0, 6.0, 7.0, 14.0, 10.0, 16.0),
                        new HexCollisionBox(2.0, 5.0, 3.0, 4.0, 11.0, 9.0),
                        new HexCollisionBox(12.0, 5.0, 3.0, 14.0, 11.0, 9.0),
                        new HexCollisionBox(4.0, 2.0, 0.0, 12.0, 14.0, 12.0)
                     );
                  case SOUTH:
                     return new ComplexCollisionBox(
                        5,
                        new HexCollisionBox(2.0, 6.0, 0.0, 4.0, 10.0, 7.0),
                        new HexCollisionBox(12.0, 6.0, 0.0, 14.0, 10.0, 7.0),
                        new HexCollisionBox(2.0, 5.0, 7.0, 4.0, 11.0, 13.0),
                        new HexCollisionBox(12.0, 5.0, 7.0, 14.0, 11.0, 13.0),
                        new HexCollisionBox(4.0, 2.0, 4.0, 12.0, 14.0, 16.0)
                     );
                  case EAST:
                     return new ComplexCollisionBox(
                        5,
                        new HexCollisionBox(0.0, 6.0, 2.0, 9.0, 10.0, 4.0),
                        new HexCollisionBox(0.0, 6.0, 12.0, 9.0, 10.0, 14.0),
                        new HexCollisionBox(7.0, 5.0, 2.0, 13.0, 11.0, 4.0),
                        new HexCollisionBox(7.0, 5.0, 12.0, 13.0, 11.0, 14.0),
                        new HexCollisionBox(4.0, 2.0, 4.0, 16.0, 14.0, 12.0)
                     );
                  case WEST:
                     return new ComplexCollisionBox(
                        5,
                        new HexCollisionBox(7.0, 6.0, 2.0, 16.0, 10.0, 4.0),
                        new HexCollisionBox(7.0, 6.0, 12.0, 16.0, 10.0, 14.0),
                        new HexCollisionBox(3.0, 5.0, 2.0, 9.0, 11.0, 4.0),
                        new HexCollisionBox(3.0, 5.0, 12.0, 9.0, 11.0, 14.0),
                        new HexCollisionBox(0.0, 2.0, 4.0, 12.0, 14.0, 12.0)
                     );
                  default:
                     return NoCollisionBox.INSTANCE;
               }
            } else {
               return facing != BlockFace.NORTH && facing != BlockFace.SOUTH
                  ? new ComplexCollisionBox(
                     5,
                     new HexCollisionBox(6.0, 9.0, 2.0, 10.0, 16.0, 4.0),
                     new HexCollisionBox(6.0, 9.0, 12.0, 10.0, 16.0, 14.0),
                     new HexCollisionBox(5.0, 3.0, 2.0, 11.0, 9.0, 4.0),
                     new HexCollisionBox(5.0, 3.0, 12.0, 11.0, 9.0, 14.0),
                     new HexCollisionBox(2.0, 0.0, 4.0, 14.0, 12.0, 12.0)
                  )
                  : new ComplexCollisionBox(
                     5,
                     new HexCollisionBox(2.0, 9.0, 6.0, 4.0, 16.0, 10.0),
                     new HexCollisionBox(12.0, 9.0, 6.0, 14.0, 16.0, 10.0),
                     new HexCollisionBox(2.0, 3.0, 5.0, 4.0, 9.0, 11.0),
                     new HexCollisionBox(12.0, 3.0, 5.0, 14.0, 9.0, 11.0),
                     new HexCollisionBox(4.0, 0.0, 2.0, 12.0, 12.0, 14.0)
                  );
            }
         } else {
            ComplexCollisionBox complexAnvil = new ComplexCollisionBox(4);
            complexAnvil.add(new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 4.0, 14.0));
            if (facing != BlockFace.NORTH && facing != BlockFace.SOUTH) {
               complexAnvil.add(new HexCollisionBox(3.0, 4.0, 4.0, 13.0, 5.0, 12.0));
               complexAnvil.add(new HexCollisionBox(4.0, 5.0, 6.0, 12.0, 10.0, 10.0));
               complexAnvil.add(new HexCollisionBox(0.0, 10.0, 3.0, 16.0, 16.0, 13.0));
            } else {
               complexAnvil.add(new HexCollisionBox(4.0, 4.0, 3.0, 12.0, 5.0, 13.0));
               complexAnvil.add(new HexCollisionBox(6.0, 5.0, 4.0, 10.0, 10.0, 12.0));
               complexAnvil.add(new HexCollisionBox(3.0, 10.0, 0.0, 13.0, 16.0, 16.0));
            }

            return complexAnvil;
         }
      },
      StateTypes.GRINDSTONE
   ),
   PANE(new DynamicCollisionPane(), Materials.getPanes().toArray(new StateType[0])),
   CHAIN_BLOCK((player, version, datax, x, y, z) -> {
      if (version.isOlderThan(ClientVersion.V_1_16)) {
         return PANE.fetch(player, version, datax, x, y, z);
      } else if (datax.getAxis() == Axis.X) {
         return new HexCollisionBox(0.0, 6.5, 6.5, 16.0, 9.5, 9.5);
      } else {
         return datax.getAxis() == Axis.Y ? new HexCollisionBox(6.5, 0.0, 6.5, 9.5, 16.0, 9.5) : new HexCollisionBox(6.5, 6.5, 0.0, 9.5, 9.5, 16.0);
      }
   }, Materials.getChains().toArray(new StateType[0])),
   CHORUS_PLANT(new DynamicChorusPlant(), StateTypes.CHORUS_PLANT),
   FENCE_GATE((player, version, datax, x, y, z) -> {
      if (datax.isOpen()) {
         return NoCollisionBox.INSTANCE;
      } else {
         return (CollisionBox)(switch (datax.getFacing()) {
            case NORTH, SOUTH -> new SimpleCollisionBox(0.0, 0.0, 0.375, 1.0, 1.5, 0.625, false);
            case EAST, WEST -> new SimpleCollisionBox(0.375, 0.0, 0.0, 0.625, 1.5, 1.0, false);
            default -> NoCollisionBox.INSTANCE;
         });
      }
   }, BlockTags.FENCE_GATES.getStates().toArray(new StateType[0])),
   FENCE(new DynamicCollisionFence(), BlockTags.FENCES.getStates().toArray(new StateType[0])),
   SNOW(
      (player, version, datax, x, y, z) -> {
         int layers = datax.getLayers();
         if (layers == 1 && version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13)
               || !ViaVersionUtil.isAvailable
               || !Via.getConfig().isSnowCollisionFix()) {
               return NoCollisionBox.INSTANCE;
            }

            layers++;
         }

         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, (layers - 1) * 0.125, 1.0);
      },
      StateTypes.SNOW
   ),
   STAIR(new DynamicStair(), BlockTags.STAIRS.getStates().toArray(new StateType[0])),
   CHEST(new DynamicChest(), Materials.getChests().toArray(new StateType[0])),
   ENDER_CHEST(new SimpleCollisionBox(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375, false), StateTypes.ENDER_CHEST),
   ENCHANTING_TABLE(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.75, 1.0, false), StateTypes.ENCHANTING_TABLE),
   FRAME((player, version, datax, x, y, z) -> {
      ComplexCollisionBox complexCollisionBox = new ComplexCollisionBox(2, new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 13.0, 16.0));
      if (datax.isEye()) {
         if (version.isNewerThanOrEquals(ClientVersion.V_1_13)) {
            complexCollisionBox.add(new HexCollisionBox(4.0, 13.0, 4.0, 12.0, 16.0, 12.0));
         } else {
            complexCollisionBox.add(new HexCollisionBox(5.0, 13.0, 5.0, 11.0, 16.0, 11.0));
         }
      }

      return complexCollisionBox;
   }, StateTypes.END_PORTAL_FRAME),
   CARPET(
      (player, version, datax, x, y, z) -> version.isOlderThanOrEquals(ClientVersion.V_1_7_10)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.0, 1.0, false)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0, false),
      BlockTags.WOOL_CARPETS.getStates().toArray(new StateType[0])
   ),
   MOSS_CARPET(CARPET, StateTypes.MOSS_CARPET),
   PALE_MOSS_CARPET(
      (player, version, datax, x, y, z) -> {
         if (!datax.isBottom()) {
            return NoCollisionBox.INSTANCE;
         } else {
            return (CollisionBox)(version.isOlderThan(ClientVersion.V_1_21_2)
               ? MOSS_CARPET.fetch(player, version, datax, x, y, z)
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0, false));
         }
      },
      StateTypes.PALE_MOSS_CARPET
   ),
   DAYLIGHT(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.375, 1.0, false), StateTypes.DAYLIGHT_DETECTOR),
   FARMLAND(
      (player, version, datax, x, y, z) -> {
         if (version == ClientVersion.V_1_10) {
            return (CollisionBox)(Math.abs(player.y % 1.0) < 0.001
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               : new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 15.0, 16.0));
         } else {
            return (CollisionBox)(version.isNewerThanOrEquals(ClientVersion.V_1_10)
               ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true));
         }
      },
      StateTypes.FARMLAND
   ),
   GRASS_PATH(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isNewerThanOrEquals(ClientVersion.V_1_9)
         ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)),
      StateTypes.DIRT_PATH,
      StateTypes.GRASS_PATH
   ),
   LILYPAD(
      (player, version, datax, x, y, z) -> {
         if (player.inVehicle() && player.compensatedEntities.self.getRiding().isBoat && version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
            return NoCollisionBox.INSTANCE;
         } else {
            return (CollisionBox)(version.isOlderThan(ClientVersion.V_1_9)
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.015625, 1.0, false)
               : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 1.5, 15.0));
         }
      },
      StateTypes.LILY_PAD
   ),
   BED((player, version, datax, x, y, z) -> {
      if (version.isOlderThan(ClientVersion.V_1_14)) {
         return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.5625, 1.0, false);
      } else {
         ComplexCollisionBox baseBox = new ComplexCollisionBox(3, new HexCollisionBox(0.0, 3.0, 0.0, 16.0, 9.0, 16.0));
         BlockFace facing = datax.getPart() == Part.HEAD ? datax.getFacing() : datax.getFacing().getOppositeFace();
         switch (facing) {
            case NORTH:
               baseBox.add(new HexCollisionBox(0.0, 0.0, 0.0, 3.0, 3.0, 3.0));
               baseBox.add(new HexCollisionBox(13.0, 0.0, 0.0, 16.0, 3.0, 3.0));
               break;
            case SOUTH:
               baseBox.add(new HexCollisionBox(0.0, 0.0, 13.0, 3.0, 3.0, 16.0));
               baseBox.add(new HexCollisionBox(13.0, 0.0, 13.0, 16.0, 3.0, 16.0));
               break;
            case EAST:
               baseBox.add(new HexCollisionBox(13.0, 0.0, 0.0, 16.0, 3.0, 3.0));
               baseBox.add(new HexCollisionBox(13.0, 0.0, 13.0, 16.0, 3.0, 16.0));
               break;
            case WEST:
               baseBox.add(new HexCollisionBox(0.0, 0.0, 0.0, 3.0, 3.0, 3.0));
               baseBox.add(new HexCollisionBox(0.0, 0.0, 13.0, 3.0, 3.0, 16.0));
         }

         return baseBox;
      }
   }, BlockTags.BEDS.getStates().toArray(new StateType[0])),
   TRAPDOOR(new TrapDoorHandler(), BlockTags.TRAPDOORS.getStates().toArray(new StateType[0])),
   DIODES(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.125, 1.0, false), StateTypes.REPEATER, StateTypes.COMPARATOR),
   STRUCTURE_VOID(new SimpleCollisionBox(0.375, 0.375, 0.375, 0.625, 0.625, 0.625, false), StateTypes.STRUCTURE_VOID),
   END_ROD((player, version, datax, x, y, z) -> getEndRod(version, datax.getFacing()), Materials.getRods().toArray(new StateType[0])),
   CAULDRON(
      (player, version, datax, x, y, z) -> {
         if (version.isNewerThan(ClientVersion.V_1_13_2)) {
            return new ComplexCollisionBox(
               15,
               new SimpleCollisionBox(0.0, 0.0, 0.0, 0.125, 1.0, 0.25, false),
               new SimpleCollisionBox(0.0, 0.0, 0.75, 0.125, 1.0, 1.0, false),
               new SimpleCollisionBox(0.125, 0.0, 0.0, 0.25, 1.0, 0.125, false),
               new SimpleCollisionBox(0.125, 0.0, 0.875, 0.25, 1.0, 1.0, false),
               new SimpleCollisionBox(0.75, 0.0, 0.0, 1.0, 1.0, 0.125, false),
               new SimpleCollisionBox(0.75, 0.0, 0.875, 1.0, 1.0, 1.0, false),
               new SimpleCollisionBox(0.875, 0.0, 0.125, 1.0, 1.0, 0.25, false),
               new SimpleCollisionBox(0.875, 0.0, 0.75, 1.0, 1.0, 0.875, false),
               new SimpleCollisionBox(0.0, 0.1875, 0.25, 1.0, 0.25, 0.75, false),
               new SimpleCollisionBox(0.125, 0.1875, 0.125, 0.875, 0.25, 0.25, false),
               new SimpleCollisionBox(0.125, 0.1875, 0.75, 0.875, 0.25, 0.875, false),
               new SimpleCollisionBox(0.25, 0.1875, 0.0, 0.75, 1.0, 0.125, false),
               new SimpleCollisionBox(0.25, 0.1875, 0.875, 0.75, 1.0, 1.0, false),
               new SimpleCollisionBox(0.0, 0.25, 0.25, 0.125, 1.0, 0.75, false),
               new SimpleCollisionBox(0.875, 0.25, 0.25, 1.0, 1.0, 0.75, false)
            );
         } else {
            double height = 0.25;
            if (version.isOlderThan(ClientVersion.V_1_13)) {
               height = 0.3125;
            }

            return new ComplexCollisionBox(
               5,
               new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, height, 1.0, false),
               new SimpleCollisionBox(0.0, height, 0.0, 0.125, 1.0, 1.0, false),
               new SimpleCollisionBox(0.875, height, 0.0, 1.0, 1.0, 1.0, false),
               new SimpleCollisionBox(0.0, height, 0.0, 1.0, 1.0, 0.125, false),
               new SimpleCollisionBox(0.0, height, 0.875, 1.0, 1.0, 1.0, false)
            );
         }
      },
      BlockTags.CAULDRONS.getStates().toArray(new StateType[0])
   ),
   CACTUS(new SimpleCollisionBox(0.0625, 0.0, 0.0625, 0.9375, 0.9375, 0.9375, false), StateTypes.CACTUS),
   PISTON_BASE(new PistonBaseCollision(), StateTypes.PISTON, StateTypes.STICKY_PISTON),
   PISTON_HEAD(new PistonHeadCollision(), StateTypes.PISTON_HEAD),
   SOULSAND(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.875, 1.0, false), StateTypes.SOUL_SAND),
   PICKLE((player, version, datax, x, y, z) -> getPicklesBox(version, datax.getPickles()), StateTypes.SEA_PICKLE),
   TURTLEEGG((player, version, datax, x, y, z) -> {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
         return getCocoa(version, datax.getEggs(), BlockFace.WEST);
      } else {
         return datax.getEggs() == 1 ? new HexCollisionBox(3.0, 0.0, 3.0, 12.0, 7.0, 12.0) : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);
      }
   }, StateTypes.TURTLE_EGG),
   CONDUIT(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isOlderThanOrEquals(ClientVersion.V_1_12_2)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new HexCollisionBox(5.0, 5.0, 5.0, 11.0, 11.0, 11.0)),
      StateTypes.CONDUIT
   ),
   POT(new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 6.0, 11.0), BlockTags.FLOWER_POTS.getStates().toArray(new StateType[0])),
   WALL_SIGN((player, version, datax, x, y, z) -> {
      return (CollisionBox)(switch (datax.getFacing()) {
         case NORTH -> new HexCollisionBox(0.0, 4.5, 14.0, 16.0, 12.5, 16.0);
         case SOUTH -> new HexCollisionBox(0.0, 4.5, 0.0, 16.0, 12.5, 2.0);
         case EAST -> new HexCollisionBox(0.0, 4.5, 0.0, 2.0, 12.5, 16.0);
         case WEST -> new HexCollisionBox(14.0, 4.5, 0.0, 16.0, 12.5, 16.0);
         default -> NoCollisionBox.INSTANCE;
      });
   }, BlockTags.WALL_SIGNS.getStates().toArray(new StateType[0])),
   WALL_FAN((player, version, datax, x, y, z) -> {
      return switch (datax.getFacing()) {
         case NORTH -> new HexCollisionBox(0.0, 4.0, 5.0, 16.0, 12.0, 16.0);
         case SOUTH -> new HexCollisionBox(0.0, 4.0, 0.0, 16.0, 12.0, 11.0);
         default -> new HexCollisionBox(0.0, 4.0, 0.0, 11.0, 12.0, 16.0);
         case WEST -> new HexCollisionBox(5.0, 4.0, 0.0, 16.0, 12.0, 16.0);
      };
   }, BlockTags.WALL_CORALS.getStates().toArray(new StateType[0])),
   CORAL_PLANT(
      (player, version, datax, x, y, z) -> new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 15.0, 14.0),
      Stream.concat(
            Arrays.stream(BlockTags.CORAL_PLANTS.getStates().toArray(new StateType[0])),
            Stream.of(
               StateTypes.DEAD_HORN_CORAL, StateTypes.DEAD_TUBE_CORAL, StateTypes.DEAD_BRAIN_CORAL, StateTypes.DEAD_BUBBLE_CORAL, StateTypes.DEAD_FIRE_CORAL
            )
         )
         .distinct()
         .toArray(StateType[]::new)
   ),
   SIGN(new SimpleCollisionBox(0.25, 0.0, 0.25, 0.75, 1.0, 0.75, false), BlockTags.STANDING_SIGNS.getStates().toArray(new StateType[0])),
   STONE_PRESSURE_PLATE(
      (player, version, datax, x, y, z) -> datax.isPowered()
         ? new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 0.5, 15.0)
         : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
      BlockTags.STONE_PRESSURE_PLATES.getStates().toArray(new StateType[0])
   ),
   WOOD_PRESSURE_PLATE(
      (player, version, datax, x, y, z) -> datax.isPowered()
         ? new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 0.5, 15.0)
         : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
      BlockTags.WOODEN_PRESSURE_PLATES.getStates().toArray(new StateType[0])
   ),
   OTHER_PRESSURE_PLATE(
      (player, version, datax, x, y, z) -> datax.getPower() > 0
         ? new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 0.5, 15.0)
         : new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
      StateTypes.LIGHT_WEIGHTED_PRESSURE_PLATE,
      StateTypes.HEAVY_WEIGHTED_PRESSURE_PLATE
   ),
   TRIPWIRE(
      (player, version, datax, x, y, z) -> datax.isAttached()
         ? new HexCollisionBox(0.0, 1.0, 0.0, 16.0, 2.5, 16.0)
         : new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
      StateTypes.TRIPWIRE
   ),
   TRIPWIRE_HOOK((player, version, datax, x, y, z) -> {
      return switch (datax.getFacing()) {
         case NORTH -> new HexCollisionBox(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
         case SOUTH -> new HexCollisionBox(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
         default -> new HexCollisionBox(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);
         case WEST -> new HexCollisionBox(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
      };
   }, StateTypes.TRIPWIRE_HOOK),
   TORCH(new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 10.0, 10.0), StateTypes.TORCH, StateTypes.REDSTONE_TORCH, StateTypes.COPPER_TORCH, StateTypes.SOUL_TORCH),
   WALL_TORCH((player, version, datax, x, y, z) -> {
      return switch (datax.getFacing()) {
         case NORTH -> new HexCollisionBox(5.5, 3.0, 11.0, 10.5, 13.0, 16.0);
         case SOUTH -> new HexCollisionBox(5.5, 3.0, 0.0, 10.5, 13.0, 5.0);
         case EAST -> new HexCollisionBox(0.0, 3.0, 5.5, 5.0, 13.0, 10.5);
         case WEST -> new HexCollisionBox(11.0, 3.0, 5.5, 16.0, 13.0, 10.5);
         default -> new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);
      };
   }, StateTypes.WALL_TORCH, StateTypes.REDSTONE_WALL_TORCH, StateTypes.COPPER_WALL_TORCH, StateTypes.SOUL_WALL_TORCH),
   CANDLE((player, version, datax, x, y, z) -> {
      if (version.isNewerThanOrEquals(ClientVersion.V_1_17)) {
         return switch (datax.getCandles()) {
            case 1 -> new HexCollisionBox(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
            case 2 -> new HexCollisionBox(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
            case 3 -> new HexCollisionBox(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
            default -> new HexCollisionBox(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);
         };
      } else {
         return getPicklesBox(version, datax.getCandles());
      }
   }, BlockTags.CANDLES.getStates().toArray(new StateType[0])),
   CANDLE_CAKE(
      (player, version, datax, x, y, z) -> {
         SimpleCollisionBox cake = new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
         return (CollisionBox)(version.isOlderThan(ClientVersion.V_1_17)
            ? cake
            : new ComplexCollisionBox(2, cake, new HexCollisionBox(7.0, 8.0, 7.0, 9.0, 14.0, 9.0)));
      },
      BlockTags.CANDLE_CAKES.getStates().toArray(new StateType[0])
   ),
   SCULK_SENSOR(new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), StateTypes.SCULK_SENSOR, StateTypes.CALIBRATED_SCULK_SENSOR),
   DECORATED_POT(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_19_3)
         ? new HexCollisionBox(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)),
      StateTypes.DECORATED_POT
   ),
   BIG_DRIPLEAF((player, version, datax, x, y, z) -> {
      Tilt tilt = datax.getTilt();
      if (version.isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
         return tilt == Tilt.FULL ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.5, 1.0, false) : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
      } else if (tilt == Tilt.NONE || tilt == Tilt.UNSTABLE) {
         return new HexCollisionBox(0.0, 11.0, 0.0, 16.0, 15.0, 16.0);
      } else {
         return (CollisionBox)(tilt == Tilt.PARTIAL ? new HexCollisionBox(0.0, 11.0, 0.0, 16.0, 13.0, 16.0) : NoCollisionBox.INSTANCE);
      }
   }, StateTypes.BIG_DRIPLEAF),
   POINTED_DRIPSTONE((player, version, datax, x, y, z) -> {
      if (version.isOlderThan(ClientVersion.V_1_17)) {
         return getEndRod(version, BlockFace.UP);
      } else {
         SimpleCollisionBox box;
         if (datax.getThickness() == Thickness.TIP_MERGE) {
            box = new HexOffsetCollisionBox(datax.getType(), 5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
         } else if (datax.getThickness() == Thickness.TIP) {
            if (datax.getVerticalDirection() == VerticalDirection.DOWN) {
               box = new HexOffsetCollisionBox(datax.getType(), 5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
            } else {
               box = new HexOffsetCollisionBox(datax.getType(), 5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
            }
         } else if (datax.getThickness() == Thickness.FRUSTUM) {
            box = new HexOffsetCollisionBox(datax.getType(), 4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
         } else if (datax.getThickness() == Thickness.MIDDLE) {
            box = new HexOffsetCollisionBox(datax.getType(), 3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
         } else {
            box = new HexOffsetCollisionBox(datax.getType(), 2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
         }

         return box;
      }
   }, StateTypes.POINTED_DRIPSTONE, StateTypes.SULFUR_SPIKE),
   POWDER_SNOW(
      (player, version, datax, x, y, z) -> {
         if (version.isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else if (player.fallDistance > 2.5) {
            return player.getClientVersion() == ClientVersion.V_1_21_4
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 0.9, 1.0, false);
         } else {
            ItemStack boots = player.inventory.getBoots();
            return (CollisionBox)(player.lastY > y + 1 - 1.0E-5
                  && boots != null
                  && boots.getType() == ItemTypes.LEATHER_BOOTS
                  && !player.isSneaking
                  && !player.inVehicle()
               ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
               : NoCollisionBox.INSTANCE);
         }
      },
      StateTypes.POWDER_SNOW
   ),
   NETHER_PORTAL(
      (player, version, datax, x, y, z) -> datax.getAxis() == Axis.X
         ? new HexCollisionBox(0.0, 0.0, 6.0, 16.0, 16.0, 10.0)
         : new HexCollisionBox(6.0, 0.0, 0.0, 10.0, 16.0, 16.0),
      StateTypes.NETHER_PORTAL
   ),
   AZALEA(
      (player, version, datax, x, y, z) -> new ComplexCollisionBox(
         2, new HexCollisionBox(0.0, 8.0, 0.0, 16.0, 16.0, 16.0), new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 8.0, 10.0)
      ),
      StateTypes.AZALEA,
      StateTypes.FLOWERING_AZALEA
   ),
   AMETHYST_CLUSTER((player, version, datax, x, y, z) -> getAmethystBox(version, datax.getFacing(), 7, 3), StateTypes.AMETHYST_CLUSTER),
   SMALL_AMETHYST_BUD((player, version, datax, x, y, z) -> getAmethystBox(version, datax.getFacing(), 3, 4), StateTypes.SMALL_AMETHYST_BUD),
   MEDIUM_AMETHYST_BUD((player, version, datax, x, y, z) -> getAmethystBox(version, datax.getFacing(), 4, 3), StateTypes.MEDIUM_AMETHYST_BUD),
   LARGE_AMETHYST_BUD((player, version, datax, x, y, z) -> getAmethystBox(version, datax.getFacing(), 5, 3), StateTypes.LARGE_AMETHYST_BUD),
   MUD_BLOCK(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isNewerThanOrEquals(ClientVersion.V_1_19)
         ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)),
      StateTypes.MUD
   ),
   MANGROVE_PROPAGULE_BLOCK((player, version, datax, x, y, z) -> {
      if (!datax.isHanging()) {
         return new HexCollisionBox(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
      } else {
         return switch (datax.getAge()) {
            case 0 -> new HexCollisionBox(7.0, 13.0, 7.0, 9.0, 16.0, 9.0);
            case 1 -> new HexCollisionBox(7.0, 10.0, 7.0, 9.0, 16.0, 9.0);
            case 2 -> new HexCollisionBox(7.0, 7.0, 7.0, 9.0, 16.0, 9.0);
            case 3 -> new HexCollisionBox(7.0, 3.0, 7.0, 9.0, 16.0, 9.0);
            default -> new HexCollisionBox(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
         };
      }
   }, StateTypes.MANGROVE_PROPAGULE),
   SCULK_SHRIKER(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_18_2)
         ? new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)),
      StateTypes.SCULK_SHRIEKER
   ),
   SNIFFER_EGG(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isNewerThan(ClientVersion.V_1_19_4)
         ? new HexCollisionBox(1.0, 0.0, 2.0, 15.0, 16.0, 14.0)
         : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)),
      StateTypes.SNIFFER_EGG
   ),
   PITCHER_CROP((player, version, datax, x, y, z) -> {
      if (version.isNewerThan(ClientVersion.V_1_19_4)) {
         SimpleCollisionBox COLLISION_SHAPE_BULB = new HexCollisionBox(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
         SimpleCollisionBox COLLISION_SHAPE_CROP = new HexCollisionBox(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
         if (datax.getAge() == 0) {
            return COLLISION_SHAPE_BULB;
         } else {
            return (CollisionBox)(datax.getHalf() == Half.LOWER ? COLLISION_SHAPE_CROP : NoCollisionBox.INSTANCE);
         }
      } else {
         return NoCollisionBox.INSTANCE;
      }
   }, StateTypes.PITCHER_CROP),
   WALL_HANGING_SIGNS((player, version, datax, x, y, z) -> {
      return (CollisionBox)(switch (datax.getFacing()) {
         case NORTH, SOUTH -> new HexCollisionBox(0.0, 14.0, 6.0, 16.0, 16.0, 10.0);
         case EAST, WEST -> new HexCollisionBox(6.0, 14.0, 0.0, 10.0, 16.0, 16.0);
         default -> NoCollisionBox.INSTANCE;
      });
   }, BlockTags.WALL_HANGING_SIGNS.getStates().toArray(new StateType[0])),
   DRIED_GHAST(
      (player, version, datax, x, y, z) -> {
         if (player.getClientVersion().isNewerThan(ClientVersion.V_1_21_5)) {
            return new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);
         } else if (player.getClientVersion().isNewerThan(ClientVersion.V_1_12_2)) {
            return new ComplexCollisionBox(
               2, new SimpleCollisionBox(0.1875, 0.1875, 0.1875, 0.8125, 0.8125, 0.8125), new SimpleCollisionBox(0.1875, 0.0, 0.1875, 0.8125, 0.8125, 0.8125)
            );
         } else {
            return player.getClientVersion().isNewerThan(ClientVersion.V_1_8)
               ? new SimpleCollisionBox(0.1875, 0.0, 0.1875, 0.8125, 0.8125, 0.8125)
               : new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         }
      },
      StateTypes.DRIED_GHAST
   ),
   SHELF(
      (player, version, datax, x, y, z) -> {
         if (version.isOlderThan(ClientVersion.V_1_21_9)) {
            return new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true);
         } else {
            return switch (datax.getFacing()) {
               case NORTH -> new ComplexCollisionBox(
                  3,
                  new HexCollisionBox(0.0, 12.0, 11.0, 16.0, 16.0, 13.0),
                  new HexCollisionBox(0.0, 0.0, 13.0, 16.0, 16.0, 16.0),
                  new HexCollisionBox(0.0, 0.0, 11.0, 16.0, 4.0, 13.0)
               );
               case SOUTH -> new ComplexCollisionBox(
                  3,
                  new HexCollisionBox(0.0, 12.0, 3.0, 16.0, 16.0, 5.0),
                  new HexCollisionBox(0.0, 0.0, 0.0, 16.0, 16.0, 3.0),
                  new HexCollisionBox(0.0, 0.0, 3.0, 16.0, 4.0, 5.0)
               );
               case EAST -> new ComplexCollisionBox(
                  3,
                  new HexCollisionBox(3.0, 12.0, 0.0, 5.0, 16.0, 16.0),
                  new HexCollisionBox(0.0, 0.0, 0.0, 3.0, 16.0, 16.0),
                  new HexCollisionBox(3.0, 0.0, 0.0, 5.0, 4.0, 16.0)
               );
               case WEST -> new ComplexCollisionBox(
                  3,
                  new HexCollisionBox(11.0, 12.0, 0.0, 13.0, 16.0, 16.0),
                  new HexCollisionBox(13.0, 0.0, 0.0, 16.0, 16.0, 16.0),
                  new HexCollisionBox(11.0, 0.0, 0.0, 13.0, 4.0, 16.0)
               );
               default -> throw new IllegalStateException("Unexpected value: " + datax.getFacing());
            };
         }
      },
      BlockTags.WOODEN_SHELVES.getStates().toArray(new StateType[0])
   ),
   COPPER_GOLEM_STATUE(
      (player, version, datax, x, y, z) -> (CollisionBox)(version.isOlderThan(ClientVersion.V_1_21_9)
         ? new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true)
         : new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 14.0, 13.0)),
      BlockTags.COPPER_GOLEM_STATUES.getStates().toArray(new StateType[0])
   ),
   DEFAULT(new SimpleCollisionBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, true), StateTypes.STONE);

   private static final Map<StateType, CollisionData> rawLookupMap = new IdentityHashMap<>();
   public final StateType[] materials;
   public CollisionBox box;
   public CollisionFactory dynamic;

   private CollisionData(CollisionBox box, StateType... states) {
      this.box = box;
      Set<StateType> mList = new HashSet<>(Arrays.asList(states));
      mList.remove(null);
      this.materials = mList.toArray(new StateType[0]);
   }

   private CollisionData(CollisionFactory dynamic, StateType... states) {
      this.dynamic = dynamic;
      Set<StateType> mList = new HashSet<>(Arrays.asList(states));
      mList.remove(null);
      this.materials = mList.toArray(new StateType[0]);
   }

   private static CollisionBox getAmethystBox(ClientVersion version, BlockFace facing, int param_0, int param_1) {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
         return NoCollisionBox.INSTANCE;
      } else {
         return switch (facing) {
            case DOWN -> new HexCollisionBox(param_1, 16 - param_0, param_1, 16 - param_1, 16.0, 16 - param_1);
            case NORTH -> new HexCollisionBox(param_1, param_1, 16 - param_0, 16 - param_1, 16 - param_1, 16.0);
            case SOUTH -> new HexCollisionBox(param_1, param_1, 0.0, 16 - param_1, 16 - param_1, param_0);
            case EAST -> new HexCollisionBox(0.0, param_1, param_1, param_0, 16 - param_1, 16 - param_1);
            case WEST -> new HexCollisionBox(16 - param_0, param_1, param_1, 16.0, 16 - param_1, 16 - param_1);
            default -> new HexCollisionBox(param_1, 0.0, param_1, 16 - param_1, param_0, 16 - param_1);
         };
      }
   }

   private static CollisionBox getPicklesBox(ClientVersion version, int pickles) {
      if (version.isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
         return getCocoa(version, pickles, BlockFace.WEST);
      } else {
         return (CollisionBox)(switch (pickles) {
            case 1 -> new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
            case 2 -> new HexCollisionBox(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
            case 3 -> new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 6.0, 14.0);
            case 4 -> new HexCollisionBox(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);
            default -> NoCollisionBox.INSTANCE;
         });
      }
   }

   public static CollisionBox getCocoa(ClientVersion version, int age, BlockFace direction) {
      if (version.isNewerThanOrEquals(ClientVersion.V_1_9_1) && version.isOlderThan(ClientVersion.V_1_11)) {
         age = Math.min(age, 1);
      }

      switch (direction) {
         case EAST:
            switch (age) {
               case 0:
                  return new HexCollisionBox(11.0, 7.0, 6.0, 15.0, 12.0, 10.0);
               case 1:
                  return new HexCollisionBox(9.0, 5.0, 5.0, 15.0, 12.0, 11.0);
               case 2:
                  return new HexCollisionBox(7.0, 3.0, 4.0, 15.0, 12.0, 12.0);
            }
         case WEST:
            switch (age) {
               case 0:
                  return new HexCollisionBox(1.0, 7.0, 6.0, 5.0, 12.0, 10.0);
               case 1:
                  return new HexCollisionBox(1.0, 5.0, 5.0, 7.0, 12.0, 11.0);
               case 2:
                  return new HexCollisionBox(1.0, 3.0, 4.0, 9.0, 12.0, 12.0);
            }
         case NORTH:
            switch (age) {
               case 0:
                  return new HexCollisionBox(6.0, 7.0, 1.0, 10.0, 12.0, 5.0);
               case 1:
                  return new HexCollisionBox(5.0, 5.0, 1.0, 11.0, 12.0, 7.0);
               case 2:
                  return new HexCollisionBox(4.0, 3.0, 1.0, 12.0, 12.0, 9.0);
            }
         case SOUTH:
            switch (age) {
               case 0:
                  return new HexCollisionBox(6.0, 7.0, 11.0, 10.0, 12.0, 15.0);
               case 1:
                  return new HexCollisionBox(5.0, 5.0, 9.0, 11.0, 12.0, 15.0);
               case 2:
                  return new HexCollisionBox(4.0, 3.0, 7.0, 12.0, 12.0, 15.0);
            }
         default:
            return NoCollisionBox.INSTANCE;
      }
   }

   private static CollisionBox getEndRod(ClientVersion version, BlockFace face) {
      if (version.isOlderThan(ClientVersion.V_1_9)) {
         return NoCollisionBox.INSTANCE;
      } else {
         return switch (face) {
            case NORTH, SOUTH -> new HexCollisionBox(6.0, 6.0, 0.0, 10.0, 10.0, 16.0);
            case EAST, WEST -> new HexCollisionBox(0.0, 6.0, 6.0, 16.0, 10.0, 10.0);
            default -> new HexCollisionBox(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
         };
      }
   }

   public static CollisionData getData(StateType state) {
      return !state.isSolid()
            && state != StateTypes.LAVA
            && state != StateTypes.SCAFFOLDING
            && state != StateTypes.PITCHER_CROP
            && state != StateTypes.HEAVY_CORE
            && state != StateTypes.POTTED_GOLDEN_DANDELION
            && state != StateTypes.PALE_MOSS_CARPET
            && !BlockTags.WALL_HANGING_SIGNS.contains(state)
            && !BlockTags.COPPER_GOLEM_STATUES.contains(state)
         ? NO_COLLISION
         : rawLookupMap.getOrDefault(state, DEFAULT);
   }

   public static CollisionData getRawData(StateType state) {
      return rawLookupMap.getOrDefault(state, DEFAULT);
   }

   public CollisionBox getMovementCollisionBox(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      return this.fetch(player, version, block, x, y, z).offset(x, y, z);
   }

   public CollisionBox getMovementCollisionBox(GrimPlayer player, ClientVersion version, WrappedBlockState block) {
      return (CollisionBox)(this.box != null ? this.box.copy() : new DynamicCollisionBox(player, version, this.dynamic, block));
   }

   @Override
   public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
      return (CollisionBox)(this.box != null ? this.box.copy() : new DynamicCollisionBox(player, version, this.dynamic, block));
   }

   static {
      for (CollisionData data : values()) {
         for (StateType type : data.materials) {
            rawLookupMap.put(type, data);
         }
      }
   }
}
