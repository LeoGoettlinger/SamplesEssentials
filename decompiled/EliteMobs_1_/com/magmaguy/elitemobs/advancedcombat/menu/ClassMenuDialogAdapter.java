package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.magmacore.dialog.DialogManager;
import org.bukkit.entity.Player;

final class ClassMenuDialogAdapter implements ClassMenuRenderer {
   private static final int BODY_WIDTH = 420;
   private final PlayerActionTokenRegistry tokens;

   ClassMenuDialogAdapter(PlayerActionTokenRegistry tokens) {
      this.tokens = tokens;
   }

   @Override
   public void showOverview(Player player, ClassMenuView view) {
      this.show(player, ClassMenuPresenter.overview(view));
   }

   @Override
   public void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses) {
      this.show(player, ClassMenuPresenter.form(view, form, showAllClasses));
   }

   @Override
   public void showControls(Player player, ClassMenuView view) {
      this.show(player, ClassMenuPresenter.controls(view));
   }

   private void show(Player player, ClassMenuPresentation page) {
      DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder()
         .title(page.title())
         .columns(page.columns())
         .afterAction(DialogManager.AfterAction.CLOSE);
      if (!page.bodyLines().isEmpty()) {
         builder.addBody(DialogManager.PlainMessageBody.of(page.bodyText()).width(420));
      }

      for (ClassMenuPresentation.ActionView action : page.actions()) {
         builder.addAction(this.button(action, page.buttonWidth(), player));
      }

      DialogManager.sendDialog(player, builder);
   }

   private DialogManager.ActionButton button(ClassMenuPresentation.ActionView action, int width, Player player) {
      String token = this.tokens.issue(player.getUniqueId(), action.action());
      return DialogManager.ActionButton.of(action.label(), new DialogManager.RunCommandAction("/elitemobs _classmenu " + token))
         .tooltip(action.tooltip())
         .width(width);
   }
}
