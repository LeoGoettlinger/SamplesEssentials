package com.magmaguy.elitemobs.advancedcombat.menu;

import org.bukkit.entity.Player;

interface ClassMenuRenderer {
   void showOverview(Player var1, ClassMenuView var2);

   default void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form) {
      this.showForm(player, view, form, true);
   }

   void showForm(Player var1, ClassMenuView var2, ClassMenuView.FormView var3, boolean var4);

   void showControls(Player var1, ClassMenuView var2);
}
