package vrx.economy.vRXClans.libs.cloud.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface Permission {
   Permission EMPTY = permission("");

   @NonNull
   static Permission permission(@NonNull final String permission) {
      return PermissionImpl.of(permission);
   }

   @NonNull
   static Permission of(@NonNull final String permission) {
      return permission(permission);
   }

   @NonNull
   static Permission empty() {
      return EMPTY;
   }

   @NonNull
   static Permission allOf(@NonNull final Collection<Permission> permissions) {
      Set<Permission> objects = new HashSet<>();

      for (Permission permission : permissions) {
         if (permission instanceof AndPermission) {
            objects.addAll(permission.permissions());
         } else {
            objects.add(permission);
         }
      }

      return new AndPermission(objects);
   }

   @NonNull
   static Permission allOf(@NonNull final Permission... permissions) {
      return allOf(Arrays.asList(permissions));
   }

   @NonNull
   static Permission anyOf(@NonNull final Collection<Permission> permissions) {
      Set<Permission> objects = new HashSet<>();

      for (Permission permission : permissions) {
         if (permission instanceof OrPermission) {
            objects.addAll(permission.permissions());
         } else {
            objects.add(permission);
         }
      }

      return new OrPermission(objects);
   }

   @NonNull
   static Permission anyOf(@NonNull final Permission... permissions) {
      return anyOf(Arrays.asList(permissions));
   }

   @NonNull
   default Collection<Permission> permissions() {
      return Collections.singleton(this);
   }

   @NonNull
   String permissionString();

   @API(
      status = API.Status.STABLE
   )
   default boolean isEmpty() {
      return this.permissionString().isEmpty();
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default Permission or(@NonNull final Permission other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(2);
      permission.add(this);
      permission.add(other);
      return anyOf(permission);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default Permission or(@NonNull final Permission... other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(other.length + 1);
      permission.add(this);
      permission.addAll(Arrays.asList(other));
      return anyOf(permission);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default Permission and(@NonNull final Permission other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(2);
      permission.add(this);
      permission.add(other);
      return allOf(permission);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default Permission and(@NonNull final Permission... other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(other.length + 1);
      permission.add(this);
      permission.addAll(Arrays.asList(other));
      return allOf(permission);
   }
}
