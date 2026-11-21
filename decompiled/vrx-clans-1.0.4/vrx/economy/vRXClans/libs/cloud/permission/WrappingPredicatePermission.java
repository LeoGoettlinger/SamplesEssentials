package vrx.economy.vRXClans.libs.cloud.permission;

import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
)
final class WrappingPredicatePermission<C> implements PredicatePermission<C> {
   private final CloudKey<Void> key;
   private final Predicate<C> predicate;

   WrappingPredicatePermission(@NonNull final CloudKey<Void> key, @NonNull final Predicate<C> predicate) {
      this.key = key;
      this.predicate = predicate;
   }

   @NonNull
   @Override
   public PermissionResult testPermission(@NonNull final C sender) {
      return PermissionResult.of(this.predicate.test(sender), this);
   }

   @NonNull
   @Override
   public CloudKey<Void> key() {
      return this.key;
   }

   @Override
   public String toString() {
      return this.key.name();
   }
}
