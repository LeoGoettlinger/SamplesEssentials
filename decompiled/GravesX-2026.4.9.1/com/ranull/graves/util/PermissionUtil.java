package com.ranull.graves.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionUtil {
   private PermissionUtil() {
   }

   public static int getHighestInt(Player player, String permission) {
      if (player != null && permission != null && !permission.isEmpty()) {
         List<Integer> gravePermissions = new ArrayList<>();

         for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            String permName = perm.getPermission();
            if (permName.startsWith(permission)) {
               try {
                  gravePermissions.add(Integer.parseInt(permName.substring(permission.length())));
               } catch (NumberFormatException var7) {
               }
            }
         }

         return !gravePermissions.isEmpty() ? Collections.max(gravePermissions) : 0;
      } else {
         return 0;
      }
   }

   public static double getHighestDouble(Player player, String permission) {
      if (player != null && permission != null && !permission.isEmpty()) {
         List<Double> gravePermissions = new ArrayList<>();

         for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            String permName = perm.getPermission();
            if (permName.startsWith(permission)) {
               try {
                  gravePermissions.add(Double.parseDouble(permName.substring(permission.length())));
               } catch (NumberFormatException var7) {
               }
            }
         }

         return !gravePermissions.isEmpty() ? Collections.max(gravePermissions) : 0.0;
      } else {
         return 0.0;
      }
   }
}
