package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public enum Permissions {
   INVENTORY_OPEN_SELF("inventory.open.self"),
   INVENTORY_OPEN_OTHER("inventory.open.other"),
   INVENTORY_EDIT_SELF("inventory.edit.self"),
   INVENTORY_EDIT_OTHER("inventory.edit.other"),
   INVENTORY_SLOT_HEAD_ANY("inventory.slot.head.any"),
   INVENTORY_SLOT_CHEST_ANY("inventory.slot.chest.any"),
   INVENTORY_SLOT_LEGS_ANY("inventory.slot.legs.any"),
   INVENTORY_SLOT_FEET_ANY("inventory.slot.feet.any"),
   INVENTORY_SLOT_DROP("inventory.slot.drop"),
   ENDERCHEST_OPEN_SELF("enderchest.open.self"),
   ENDERCHEST_OPEN_OTHER("enderchest.open.other"),
   ENDERCHEST_EDIT_SELF("enderchest.edit.self"),
   ENDERCHEST_EDIT_OTHER("enderchest.edit.other"),
   CLEAR_SELF("clear.self"),
   CLEAR_OTHER("clear.other"),
   ACCESS_CROSSWORLD("access.crossworld"),
   ACCESS_OFFLINE("access.offline"),
   ACCESS_ONLINE("access.online"),
   ACCESS_EQUAL_EDIT("access.equal.edit"),
   ACCESS_EQUAL_VIEW("access.equal.view"),
   ACCESS_EQUAL_DENY("access.equal.deny"),
   SPECTATE_CLICK("spectate.click"),
   CONTAINER_ANY("container.any"),
   CONTAINER_ANY_USE("container.any.use"),
   CONTAINER_SILENT("container.silent"),
   CONTAINER_SILENT_USE("container.silent.use"),
   SEARCH_INVENTORY("search.inventory"),
   SEARCH_CONTAINER("search.container");

   private final String permission;

   private Permissions(String permission) {
      this.permission = "openinv." + permission;
   }

   public boolean hasPermission(@NotNull Permissible permissible) {
      return permissible.hasPermission(this.permission);
   }
}
