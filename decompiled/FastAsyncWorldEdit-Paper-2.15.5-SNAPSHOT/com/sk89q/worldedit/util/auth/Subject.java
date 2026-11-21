package com.sk89q.worldedit.util.auth;

public interface Subject {
   String[] getGroups();

   void checkPermission(String permission) throws AuthorizationException;

   boolean hasPermission(String permission);

   default boolean togglePermission(String permission) {
      if (this.hasPermission(permission)) {
         this.setPermission(permission, false);
         return false;
      } else {
         this.setPermission(permission, true);
         return true;
      }
   }

   void setPermission(String permission, boolean value);
}
