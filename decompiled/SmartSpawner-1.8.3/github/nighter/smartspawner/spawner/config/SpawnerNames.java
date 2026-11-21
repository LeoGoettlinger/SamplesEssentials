package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import java.util.Set;
import java.util.TreeSet;

public final class SpawnerNames {
   public static final Set<String> RESERVED = Set.of("vanilla", "smart_spawner", "item_spawner", "vanilla_spawner");

   private SpawnerNames() {
   }

   public static SpawnerNames.Resolved resolve(SmartSpawner plugin, String input) {
      SpawnerSettingsConfig.MobDefinition mob = plugin.getSpawnerSettingsConfig().resolve(input);
      if (mob != null) {
         return new SpawnerNames.Resolved(mob, null);
      } else {
         ItemSpawnerSettingsConfig.ItemDefinition item = plugin.getItemSpawnerSettingsConfig().resolve(input);
         return item != null ? new SpawnerNames.Resolved(null, item) : null;
      }
   }

   public static Set<String> suggestionNames(SmartSpawner plugin) {
      Set<String> names = new TreeSet<>(plugin.getSpawnerSettingsConfig().getDefinitionNames());
      names.addAll(plugin.getItemSpawnerSettingsConfig().getDefinitionNames());
      return names;
   }

   public static void warnConflicts(SmartSpawner plugin) {
      Set<String> mobNames = plugin.getSpawnerSettingsConfig().getDefinitionNames();
      Set<String> itemNames = plugin.getItemSpawnerSettingsConfig().getDefinitionNames();

      for (String name : itemNames) {
         if (mobNames.contains(name)) {
            plugin.getLogger()
               .warning(
                  "Spawner name '"
                     + name
                     + "' is in both spawner_mobs.yml and spawner_items.yml. Commands use the mob spawner. Rename one of them to keep them apart."
               );
         }
      }

      for (String namex : RESERVED) {
         if (mobNames.contains(namex) || itemNames.contains(namex)) {
            plugin.getLogger().warning("Spawner name '" + namex + "' is a reserved /ss give keyword and cannot be given by name. Rename it.");
         }
      }
   }

   public record Resolved(SpawnerSettingsConfig.MobDefinition mob, ItemSpawnerSettingsConfig.ItemDefinition item) {
   }
}
