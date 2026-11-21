package de.theredend2000.advancedhunt.lib.kyori.adventure.permission;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.pointer.Pointer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.TriState;
import java.util.Objects;
import java.util.function.Predicate;

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
