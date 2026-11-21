package de.oliver.cloud.annotations;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public interface BuilderDecorator<C> {
   @NonNull
   static <C> BuilderDecorator<C> defaultDescription(@NonNull final de.oliver.cloud.description.CommandDescription description) {
      return builder -> builder.commandDescription(description);
   }

   @NonNull
   static <C> BuilderDecorator<C> defaultPermission(@NonNull final de.oliver.cloud.permission.Permission permission) {
      return builder -> builder.permission(permission);
   }

   @NonNull
   static <C> BuilderDecorator<C> applicable(@NonNull final de.oliver.cloud.Command.Builder.Applicable<C> applicable) {
      return builder -> builder.apply(applicable);
   }

   @NonNull
   de.oliver.cloud.Command.Builder<C> decorate(@NonNull de.oliver.cloud.Command.Builder<C> builder);
}
