package vrx.economy.vRXClans.libs.cloud.bukkit;

import org.apiguardian.api.API;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class BukkitCommandMeta {
   public static final CloudKey<String> BUKKIT_DESCRIPTION = CloudKey.of("bukkit_description", String.class);

   private BukkitCommandMeta() {
   }
}
