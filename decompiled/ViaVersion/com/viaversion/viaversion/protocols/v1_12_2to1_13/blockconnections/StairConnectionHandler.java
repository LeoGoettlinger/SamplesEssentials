package com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StairConnectionHandler implements ConnectionHandler {
   private static final Int2ObjectMap<StairConnectionHandler.StairData> STAIR_DATA_MAP = new Int2ObjectOpenHashMap<>();
   private static final Int2IntMap CONNECTED_BLOCKS = new Int2IntOpenHashMap();

   static ConnectionData.ConnectorInitAction init() {
      List<String> baseStairs = Arrays.asList(
         "minecraft:oak_stairs",
         "minecraft:cobblestone_stairs",
         "minecraft:brick_stairs",
         "minecraft:stone_brick_stairs",
         "minecraft:nether_brick_stairs",
         "minecraft:sandstone_stairs",
         "minecraft:spruce_stairs",
         "minecraft:birch_stairs",
         "minecraft:jungle_stairs",
         "minecraft:quartz_stairs",
         "minecraft:acacia_stairs",
         "minecraft:dark_oak_stairs",
         "minecraft:red_sandstone_stairs",
         "minecraft:purpur_stairs",
         "minecraft:prismarine_stairs",
         "minecraft:prismarine_brick_stairs",
         "minecraft:dark_prismarine_stairs"
      );
      StairConnectionHandler connectionHandler = new StairConnectionHandler();
      return blockData -> {
         int type = baseStairs.indexOf(blockData.getMinecraftKey());
         if (type != -1) {
            if (!blockData.getValue("waterlogged").equals("true")) {
               String stairData = blockData.getValue("shape");
               byte shape;
               switch (stairData) {
                  case "straight":
                     shape = 0;
                     break;
                  case "inner_left":
                     shape = 1;
                     break;
                  case "inner_right":
                     shape = 2;
                     break;
                  case "outer_left":
                     shape = 3;
                     break;
                  case "outer_right":
                     shape = 4;
                     break;
                  default:
                     return;
               }

               StairConnectionHandler.StairData stairData = new StairConnectionHandler.StairData(
                  blockData.getValue("half").equals("bottom"), shape, (byte)type, BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT))
               );
               STAIR_DATA_MAP.put(blockData.getSavedBlockStateId(), stairData);
               CONNECTED_BLOCKS.put(getStates(stairData), blockData.getSavedBlockStateId());
               ConnectionData.connectionHandlerMap.put(blockData.getSavedBlockStateId(), connectionHandler);
            }
         }
      };
   }

   private static short getStates(StairConnectionHandler.StairData stairData) {
      short s = 0;
      if (stairData.bottom()) {
         s = (short)(s | 1);
      }

      s = (short)(s | stairData.shape() << 1);
      s = (short)(s | stairData.type() << 4);
      return (short)(s | stairData.facing().ordinal() << 9);
   }

   @Override
   public int connect(UserConnection user, BlockPosition position, int blockState) {
      StairConnectionHandler.StairData stairData = STAIR_DATA_MAP.get(blockState);
      if (stairData == null) {
         return blockState;
      } else {
         short s = 0;
         if (stairData.bottom()) {
            s = (short)(s | 1);
         }

         s = (short)(s | this.getShape(user, position, stairData) << 1);
         s = (short)(s | stairData.type() << 4);
         s = (short)(s | stairData.facing().ordinal() << 9);
         return CONNECTED_BLOCKS.getOrDefault(s, blockState);
      }
   }

   private int getShape(UserConnection user, BlockPosition position, StairConnectionHandler.StairData stair) {
      BlockFace facing = stair.facing();
      StairConnectionHandler.StairData relativeStair = STAIR_DATA_MAP.get(this.getBlockData(user, position.getRelative(facing)));
      if (relativeStair != null && relativeStair.bottom() == stair.bottom()) {
         BlockFace facing2 = relativeStair.facing();
         if (facing.axis() != facing2.axis() && this.checkOpposite(user, stair, position, facing2.opposite())) {
            return facing2 == this.rotateAntiClockwise(facing) ? 3 : 4;
         }
      }

      relativeStair = STAIR_DATA_MAP.get(this.getBlockData(user, position.getRelative(facing.opposite())));
      if (relativeStair != null && relativeStair.bottom() == stair.bottom()) {
         BlockFace facing2 = relativeStair.facing();
         if (facing.axis() != facing2.axis() && this.checkOpposite(user, stair, position, facing2)) {
            return facing2 == this.rotateAntiClockwise(facing) ? 1 : 2;
         }
      }

      return 0;
   }

   private boolean checkOpposite(UserConnection user, StairConnectionHandler.StairData stair, BlockPosition position, BlockFace face) {
      StairConnectionHandler.StairData relativeStair = STAIR_DATA_MAP.get(this.getBlockData(user, position.getRelative(face)));
      return relativeStair == null || relativeStair.facing() != stair.facing() || relativeStair.bottom() != stair.bottom();
   }

   private BlockFace rotateAntiClockwise(BlockFace face) {
      return switch (face) {
         case NORTH -> BlockFace.WEST;
         case SOUTH -> BlockFace.EAST;
         case EAST -> BlockFace.NORTH;
         case WEST -> BlockFace.SOUTH;
         default -> face;
      };
   }

   private record StairData(boolean bottom, byte shape, byte type, BlockFace facing) {
   }
}
