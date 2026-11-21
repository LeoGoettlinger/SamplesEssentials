package xyz.lychee.lagfixer.libs.kyori.adventure.permission;

import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.pointer.Pointer;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.TriState;

public interface PermissionChecker extends Predicate<String> {
   Pointer<PermissionChecker> POINTER = Pointer.pointer(PermissionChecker.class, Key.key("adventure", "permission"));

   @NotNull
   static PermissionChecker always(@NotNull final TriState state) {
      Objects.requireNonNull(state);
      if (state == TriState.TRUE) {
         return PermissionCheckers.TRUE;
      } else {
         return state == TriState.FALSE ? PermissionCheckers.FALSE : PermissionCheckers.NOT_SET;
      }
   }

   @NotNull
   TriState value(@NotNull final String permission);

   default boolean test(@NotNull final String permission) {
      return this.value(permission) == TriState.TRUE;
   }
}
