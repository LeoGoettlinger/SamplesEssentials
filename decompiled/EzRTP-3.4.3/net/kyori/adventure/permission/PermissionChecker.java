package net.kyori.adventure.permission;

import java.util.Objects;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.util.TriState;

public interface PermissionChecker extends Predicate<String> {
   Pointer<PermissionChecker> POINTER = Pointer.pointer(PermissionChecker.class, Key.key("adventure", "permission"));

   static PermissionChecker always(final TriState state) {
      Objects.requireNonNull(state);
      if (state == TriState.TRUE) {
         return PermissionCheckers.TRUE;
      } else {
         return state == TriState.FALSE ? PermissionCheckers.FALSE : PermissionCheckers.NOT_SET;
      }
   }

   TriState value(final String permission);

   default boolean test(final String permission) {
      return this.value(permission) == TriState.TRUE;
   }
}
