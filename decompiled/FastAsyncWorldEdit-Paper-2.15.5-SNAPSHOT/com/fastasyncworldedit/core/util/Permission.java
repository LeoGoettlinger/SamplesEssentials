package com.fastasyncworldedit.core.util;

import com.sk89q.worldedit.util.auth.Subject;
import javax.annotation.Nonnull;

public enum Permission {
   ADMIN("fawe.admin", "admin");

   public final String permission;
   public final String cat;

   private Permission(String permission, String category) {
      this.permission = permission;
      this.cat = category;
   }

   public static boolean hasPermission(@Nonnull Subject player, String permission) {
      if (!player.hasPermission(ADMIN.permission) && !player.hasPermission(permission)) {
         String[] nodes = permission.split("\\.");
         StringBuilder n = new StringBuilder();

         for (int i = 0; i < nodes.length - 1; i++) {
            n.append(nodes[i]).append(".");
            if (player.hasPermission(n + "*")) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }
}
