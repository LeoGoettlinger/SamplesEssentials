package de.oliver.cloud.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   consumers = {"de.oliver.cloud.*"}
)
public final class OrPermission implements Permission {
   private final Set<Permission> permissions;

   OrPermission(@NonNull final Set<Permission> permissions) {
      if (permissions.isEmpty()) {
         throw new IllegalArgumentException("OrPermission may not have an empty set of permissions");
      } else {
         this.permissions = Collections.unmodifiableSet(permissions);
      }
   }

   @NonNull
   @Override
   public Collection<Permission> permissions() {
      return this.permissions;
   }

   @Override
   public boolean isEmpty() {
      return false;
   }

   @NonNull
   @Override
   public String permissionString() {
      StringBuilder stringBuilder = new StringBuilder();
      Iterator<Permission> iterator = this.permissions.iterator();

      while (iterator.hasNext()) {
         Permission permission = iterator.next();
         stringBuilder.append('(').append(permission.permissionString()).append(')');
         if (iterator.hasNext()) {
            stringBuilder.append('|');
         }
      }

      return stringBuilder.toString();
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         OrPermission that = (OrPermission)o;
         return this.permissions.equals(that.permissions);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.permissions());
   }

   @NonNull
   @Override
   public String toString() {
      return this.permissionString();
   }
}
