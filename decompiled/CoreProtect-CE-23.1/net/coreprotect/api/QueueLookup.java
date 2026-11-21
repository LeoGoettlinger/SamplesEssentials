package net.coreprotect.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.consumer.Queue;
import net.coreprotect.consumer.process.Process;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class QueueLookup extends Queue {
   private QueueLookup() {
      throw new IllegalStateException("API class");
   }

   public static List<String[]> performLookup(Block block) {
      List<String[]> result = new ArrayList<>();
      if (!Config.getGlobal().API_ENABLED) {
         return result;
      } else if (block == null) {
         return result;
      } else {
         try {
            int consumerCount = calculateConsumerCount();
            if (consumerCount == 0) {
               return result;
            }

            int currentConsumer = Consumer.currentConsumer;
            ArrayList<Object[]> consumerData = Consumer.consumer.get(currentConsumer);
            Map<Integer, String[]> users = Consumer.consumerUsers.get(currentConsumer);
            Map<Integer, Object> consumerObject = Consumer.consumerObjects.get(currentConsumer);
            Location blockLocation = block.getLocation();

            for (Object[] data : consumerData) {
               int id = (Integer)data[0];
               int action = (Integer)data[1];
               if (action == 0 || action == 1) {
                  String[] userData = users.get(id);
                  Object objectData = consumerObject.get(id);
                  if (isActionForBlock(userData, objectData, blockLocation)) {
                     Material blockType = (Material)data[2];
                     int legacyData = (Integer)data[3];
                     String blockData = (String)data[7];
                     String user = userData[0];
                     BlockState blockState = (BlockState)objectData;
                     Location location = blockState.getLocation();
                     int worldId = WorldUtils.getWorldId(location.getWorld().getName());
                     int resultType = MaterialUtils.getBlockId(blockType);
                     int time = (int)(System.currentTimeMillis() / 1000L);
                     String[] lookupData = new String[]{
                        String.valueOf(time),
                        user,
                        String.valueOf(location.getBlockX()),
                        String.valueOf(location.getBlockY()),
                        String.valueOf(location.getBlockZ()),
                        String.valueOf(resultType),
                        String.valueOf(legacyData),
                        String.valueOf(action),
                        "0",
                        String.valueOf(worldId),
                        blockData
                     };
                     result.add(StringUtils.toStringArray(lookupData));
                  }
               }
            }

            Collections.reverse(result);
         } catch (Exception var24) {
            var24.printStackTrace();
         }

         return result;
      }
   }

   private static int calculateConsumerCount() {
      int currentConsumerSize = Process.getCurrentConsumerSize();
      if (currentConsumerSize == 0) {
         return Consumer.getConsumerSize(0) + Consumer.getConsumerSize(1);
      } else {
         int consumerId = Consumer.currentConsumer == 1 ? 1 : 0;
         return Consumer.getConsumerSize(consumerId) + currentConsumerSize;
      }
   }

   private static boolean isActionForBlock(String[] userData, Object objectData, Location blockLocation) {
      return userData != null && objectData != null && objectData instanceof BlockState && ((BlockState)objectData).getLocation().equals(blockLocation);
   }
}
