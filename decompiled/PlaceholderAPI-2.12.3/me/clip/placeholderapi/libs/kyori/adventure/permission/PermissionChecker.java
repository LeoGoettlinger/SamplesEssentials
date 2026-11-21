package me.clip.placeholderapi.libs.kyori.adventure.permission;

import java.util.Objects;
import java.util.function.Predicate;
import me.clip.placeholderapi.libs.kyori.adventure.key.Key;
import me.clip.placeholderapi.libs.kyori.adventure.pointer.Pointer;
import me.clip.placeholderapi.libs.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

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
