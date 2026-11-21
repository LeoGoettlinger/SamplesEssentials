package su.nightexpress.nightcore.integration.permission;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface PermissionProvider {
   @NonNull
   String getName();

   @Nullable
   String getPrimaryGroup(@NonNull Player var1);

   @NonNull
   Set<String> getPermissionGroups(@NonNull Player var1);

   @Nullable
   String getPrefix(@NonNull Player var1);

   @Nullable
   String getSuffix(@NonNull Player var1);

   @NonNull
   CompletableFuture<String> getPrimaryGroup(@NonNull UUID var1);

   @NonNull
   CompletableFuture<Set<String>> getPermissionGroups(@NonNull UUID var1);

   @NonNull
   CompletableFuture<String> getPrefix(@NonNull UUID var1);

   @NonNull
   CompletableFuture<String> getSuffix(@NonNull UUID var1);
}
