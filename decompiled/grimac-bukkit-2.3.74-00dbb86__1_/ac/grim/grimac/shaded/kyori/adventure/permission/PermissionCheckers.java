package ac.grim.grimac.shaded.kyori.adventure.permission;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.util.TriState;

final class PermissionCheckers {
   static final PermissionChecker NOT_SET = new PermissionCheckers.Always(TriState.NOT_SET);
   static final PermissionChecker FALSE = new PermissionCheckers.Always(TriState.FALSE);
   static final PermissionChecker TRUE = new PermissionCheckers.Always(TriState.TRUE);

   private PermissionCheckers() {
   }

   private static final class Always implements PermissionChecker {
      private final TriState value;

      private Always(final TriState value) {
         this.value = value;
      }

      @NotNull
      @Override
      public TriState value(@NotNull final String permission) {
         return this.value;
      }

      @Override
      public String toString() {
         return PermissionChecker.class.getSimpleName() + ".always(" + this.value + ")";
      }

      @Override
      public boolean equals(@Nullable final Object other) {
         if (this == other) {
            return true;
         } else if (other != null && this.getClass() == other.getClass()) {
            PermissionCheckers.Always always = (PermissionCheckers.Always)other;
            return this.value == always.value;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }
}
