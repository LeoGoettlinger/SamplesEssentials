package vrx.economy.vRXClans.libs.cloud.paper;

import io.leangen.geantyref.TypeToken;
import org.apiguardian.api.API;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.BukkitBrigadierMapper;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.paper.parser.KeyedWorldParser;

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
