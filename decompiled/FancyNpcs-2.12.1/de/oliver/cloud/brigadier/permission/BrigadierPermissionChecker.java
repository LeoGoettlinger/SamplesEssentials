package de.oliver.cloud.brigadier.permission;

import de.oliver.cloud.permission.Permission;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.INTERNAL,
   since = "2.0.0"
)
public interface BrigadierPermissionChecker<C> {
   boolean hasPermission(@NonNull C sender, @NonNull Permission permission);
}
