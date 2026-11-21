package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SpawnerNameNotices {
   private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

   private SpawnerNameNotices() {
   }

   public static void missing(SmartSpawner plugin, String name, boolean itemSpawner) {
      if (name != null && !name.isBlank()) {
         String file = itemSpawner ? "spawner_items.yml" : "spawner_mobs.yml";
         if (WARNED.add(file + ":" + name)) {
            plugin.getLogger()
               .warning(
                  "Spawners saved as '"
                     + name
                     + "' have no entry in "
                     + file
                     + " and use their base spawner for now. Add the entry back, or list '"
                     + name
                     + "' under the aliases of the spawner it was renamed to."
               );
         }
      }
   }

   public static void reset() {
      WARNED.clear();
   }
}
