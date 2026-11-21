package su.nightexpress.nightcore.integration.placeholder;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface Expansion {
   boolean register();

   boolean unregister();

   @NonNull
   String getIdentifier();

   @NonNull
   String getAuthor();

   @NonNull
   String getVersion();

   boolean persist();

   @Nullable
   String onPlaceholderRequest(Player var1, @NonNull String var2);
}
