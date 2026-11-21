package de.oliver.cloud.paper;

import de.oliver.cloud.bukkit.internal.BukkitBrigadierMapper;
import de.oliver.cloud.bukkit.internal.CraftBukkitReflection;
import de.oliver.cloud.paper.parser.KeyedWorldParser;
import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL
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
