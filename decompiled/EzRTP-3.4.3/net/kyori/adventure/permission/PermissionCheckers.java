package net.kyori.adventure.permission;

import net.kyori.adventure.util.TriState;

final class PermissionCheckers {
   static final PermissionChecker NOT_SET = new PermissionCheckers.Always(TriState.NOT_SET);
   static final PermissionChecker FALSE = new PermissionCheckers.Always(TriState.FALSE);
   static final PermissionChecker TRUE = new PermissionCheckers.Always(TriState.TRUE);

   private PermissionCheckers() {
   }

   private record Always(TriState value) implements PermissionChecker {
      @Override
      public String toString() {
         return PermissionChecker.class.getSimpleName() + ".always(" + this.value + ")";
      }
   }
}
