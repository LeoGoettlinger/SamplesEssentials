package de.theredend2000.advancedhunt.lib.cloud.paper;

import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.BukkitBrigadierMapper;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.CraftBukkitReflection;
import de.theredend2000.advancedhunt.lib.cloud.paper.parser.KeyedWorldParser;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
final class PaperBrigadierMappings {
   private PaperBrigadierMappings() {
   }

   static <C> void register(@NonNull final BukkitBrigadierMapper<C> mapper) {
      Class<?> keyed = CraftBukkitReflection.findClass("org.bukkit.Keyed");
      if (keyed != null && keyed.isAssignableFrom(World.class)) {
         mapper.mapSimpleNMS(new TypeToken<KeyedWorldParser<C>>() {}, "resource_location", true);
      }
   }
}
