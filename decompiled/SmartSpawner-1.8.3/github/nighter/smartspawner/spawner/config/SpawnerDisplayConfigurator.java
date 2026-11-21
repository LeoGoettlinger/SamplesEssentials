package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class SpawnerDisplayConfigurator {
   private SpawnerDisplayConfigurator() {
   }

   public static void applyMob(SmartSpawner plugin, CreatureSpawner spawner, EntityType entityType) {
      applyMob(plugin, spawner, null, entityType);
   }

   public static void applyMob(SmartSpawner plugin, CreatureSpawner spawner, String configName, EntityType entityType) {
      EntitySnapshot snapshot = plugin.getSpawnerSettingsConfig().getEntityDisplaySnapshot(configName, entityType);
      if (snapshot != null) {
         spawner.setSpawnedEntity(snapshot);
      } else {
         spawner.setSpawnedType(entityType);
      }
   }

   public static void applyItem(SmartSpawner plugin, CreatureSpawner spawner, Material material) {
      applyItem(plugin, spawner, null, material);
   }

   public static void applyItem(SmartSpawner plugin, CreatureSpawner spawner, String configName, Material material) {
      ItemStack displayItem = plugin.getItemSpawnerSettingsConfig().getDisplayItem(configName, material);
      spawner.setSpawnedType(EntityType.ITEM);
      if (spawner.isPlaced()) {
         spawner.setSpawnedItem(displayItem);
      }
   }
}
