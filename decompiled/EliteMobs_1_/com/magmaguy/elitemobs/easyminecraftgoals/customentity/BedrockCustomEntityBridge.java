package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import java.util.Map;
import org.bukkit.entity.Player;

public interface BedrockCustomEntityBridge {
   BedrockCustomEntityBridge NO_OP = new BedrockCustomEntityBridge() {
      @Override
      public boolean isAvailable() {
         return false;
      }
   };

   default boolean isAvailable() {
      return true;
   }

   default void registerDefinition(CustomEntityDefinition definition) {
   }

   default void prepareEntitySpawn(Player player, int javaEntityId, CustomEntityDefinition definition) {
   }

   default void sendEntityData(Player player, int javaEntityId, Float height, Float width, Float scale, Integer color, Integer variant) {
   }

   default void sendProperties(Player player, int javaEntityId, Map<String, Object> properties) {
   }
}
