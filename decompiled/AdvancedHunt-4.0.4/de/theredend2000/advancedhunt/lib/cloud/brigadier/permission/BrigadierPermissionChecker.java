package de.theredend2000.advancedhunt.lib.cloud.brigadier.permission;

import de.theredend2000.advancedhunt.lib.cloud.permission.Permission;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface BrigadierPermissionChecker<C> {
   boolean hasPermission(@NonNull C sender, @NonNull Permission permission);
}
