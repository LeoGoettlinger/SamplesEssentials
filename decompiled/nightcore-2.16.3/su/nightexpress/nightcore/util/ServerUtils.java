package su.nightexpress.nightcore.util;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.NonNull;

public class ServerUtils {
   @NonNull
   public static <T> Optional<T> serviceProvider(@NonNull Class<T> type) {
      RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(type);
      return provider == null ? Optional.empty() : Optional.of((T)provider.getProvider());
   }
}
