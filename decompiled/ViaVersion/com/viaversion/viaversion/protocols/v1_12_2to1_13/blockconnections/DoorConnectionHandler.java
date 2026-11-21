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

public class DoorConnectionHandler implements ConnectionHandler {
   private static final Int2ObjectMap<DoorConnectionHandler.DoorData> DOOR_DATA_MAP = new Int2ObjectOpenHashMap<>();
   private static final Int2IntMap CONNECTED_STATES = new Int2IntOpenHashMap();

   static ConnectionData.ConnectorInitAction init() {
      List<String> baseDoors = Arrays.asList(
         "minecraft:oak_door",
         "minecraft:birch_door",
         "minecraft:jungle_door",
         "minecraft:dark_oak_door",
         "minecraft:acacia_door",
         "minecraft:spruce_door",
         "minecraft:iron_door"
      );
      DoorConnectionHandler connectionHandler = new DoorConnectionHandler();
      return blockData -> {
         int type = baseDoors.indexOf(blockData.getMinecraftKey());
         if (type != -1) {
            int id = blockData.getSavedBlockStateId();
            DoorConnectionHandler.DoorData doorData = new DoorConnectionHandler.DoorData(
               blockData.getValue("half").equals("lower"),
               blockData.getValue("hinge").equals("right"),
               blockData.getValue("powered").equals("true"),
               blockData.getValue("open").equals("true"),
               BlockFace.valueOf(blockData.getValue("facing").toUpperCase(Locale.ROOT)),
               type
            );
            DOOR_DATA_MAP.put(id, doorData);
            CONNECTED_STATES.put(getStates(doorData), id);
            ConnectionData.connectionHandlerMap.put(id, connectionHandler);
         }
      };
   }

   private static short getStates(DoorConnectionHandler.DoorData doorData) {
      short s = 0;
      if (doorData.lower()) {
         s = (short)(s | 1);
      }

      if (doorData.open()) {
         s = (short)(s | 2);
      }

      if (doorData.powered()) {
         s = (short)(s | 4);
      }

      if (doorData.rightHinge()) {
         s = (short)(s | 8);
      }

      s = (short)(s | doorData.facing().ordinal() << 4);
      return (short)(s | (doorData.type() & 7) << 6);
   }

   @Override
   public int connect(UserConnection user, BlockPosition position, int blockState) {
      DoorConnectionHandler.DoorData doorData = DOOR_DATA_MAP.get(blockState);
      if (doorData == null) {
         return blockState;
      } else {
         short s = 0;
         s = (short)(s | (doorData.type() & 7) << 6);
         if (doorData.lower()) {
            DoorConnectionHandler.DoorData upperHalf = DOOR_DATA_MAP.get(this.getBlockData(user, position.getRelative(BlockFace.TOP)));
            if (upperHalf == null) {
               return blockState;
            }

            s = (short)(s | 1);
            if (doorData.open()) {
               s = (short)(s | 2);
            }

            if (upperHalf.powered()) {
               s = (short)(s | 4);
            }

            if (upperHalf.rightHinge()) {
               s = (short)(s | 8);
            }

            s = (short)(s | doorData.facing().ordinal() << 4);
         } else {
            DoorConnectionHandler.DoorData lowerHalf = DOOR_DATA_MAP.get(this.getBlockData(user, position.getRelative(BlockFace.BOTTOM)));
            if (lowerHalf == null) {
               return blockState;
            }

            if (lowerHalf.open()) {
               s = (short)(s | 2);
            }

            if (doorData.powered()) {
               s = (short)(s | 4);
            }

            if (doorData.rightHinge()) {
               s = (short)(s | 8);
            }

            s = (short)(s | lowerHalf.facing().ordinal() << 4);
         }

         return CONNECTED_STATES.getOrDefault(s, blockState);
      }
   }

   private record DoorData(boolean lower, boolean rightHinge, boolean powered, boolean open, BlockFace facing, int type) {
   }
}
