package de.theredend2000.advancedhunt.lib.cloud.bukkit;

import de.theredend2000.advancedhunt.lib.cloud.key.CloudKey;
import org.apiguardian.api.API;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class BukkitCommandMeta {
   public static final CloudKey<String> BUKKIT_DESCRIPTION = CloudKey.of("bukkit_description", String.class);

   private BukkitCommandMeta() {
   }
}
