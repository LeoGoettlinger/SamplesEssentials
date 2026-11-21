package vrx.economy.vRXClans.libs.cloud.brigadier.permission;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.permission.Permission;

@FunctionalInterface
@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface BrigadierPermissionChecker<C> {
   boolean hasPermission(@NonNull C sender, @NonNull Permission permission);
}
