package de.theredend2000.advancedhunt.lib.cloud.permission;

import de.theredend2000.advancedhunt.lib.cloud.key.CloudKey;
import de.theredend2000.advancedhunt.lib.cloud.key.CloudKeyHolder;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface PredicatePermission<C> extends Permission, CloudKeyHolder<Void> {
   static <C> PredicatePermission<C> of(@NonNull final CloudKey<Void> key, @NonNull final Predicate<C> predicate) {
      return new WrappingPredicatePermission<>(key, predicate);
   }

   static <C> PredicatePermission<C> of(@NonNull final Predicate<C> predicate) {
      return new PredicatePermission<C>() {
         @NonNull
         @Override
         public PermissionResult testPermission(@NonNull final C sender) {
            return PermissionResult.of(predicate.test(sender), this);
         }
      };
   }

   @NonNull
   @Override
   default CloudKey<Void> key() {
      return CloudKey.of(this.getClass().getSimpleName());
   }

   @NonNull
   @Override
   default String permissionString() {
      return this.key().name();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   PermissionResult testPermission(@NonNull C sender);

   @Override
   default boolean isEmpty() {
      return false;
   }
}
