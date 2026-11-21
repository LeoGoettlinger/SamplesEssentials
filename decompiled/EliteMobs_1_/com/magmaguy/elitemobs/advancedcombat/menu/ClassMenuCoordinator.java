package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.ClassWeaponAffinity;
import com.magmaguy.elitemobs.advancedcombat.challenges.ClassChallengeInstance;
import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.advancedcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.advancedcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.Optional;
import org.bukkit.entity.Player;

final class ClassMenuCoordinator {
   private final PlayerActionTokenRegistry tokens;
   private final ClassMenuRenderer dialogs;
   private final ClassMenuRenderer inventories;

   ClassMenuCoordinator(PlayerActionTokenRegistry tokens, ClassMenuRenderer dialogs, ClassMenuRenderer inventories) {
      this.tokens = tokens;
      this.dialogs = dialogs;
      this.inventories = inventories;
   }

   void open(Player player) {
      this.project(player).ifPresent(view -> {
         this.tokens.beginPage(player.getUniqueId());
         this.renderer(player).showOverview(player, view);
      });
   }

   void openForm(Player player, String formId) {
      this.openForm(player, formId, true);
   }

   void openForm(Player player, String formId, boolean showAllClasses) {
      this.project(player).ifPresent(view -> {
         ClassMenuView.FormView form = view.forms().get(formId);
         if (form == null) {
            send(player, "&cThat class form no longer exists. The class list has been refreshed.");
            this.open(player);
         } else {
            this.tokens.beginPage(player.getUniqueId());
            this.renderer(player).showForm(player, view, form, showAllClasses);
         }
      });
   }

   void openControls(Player player) {
      this.project(player).ifPresent(view -> {
         this.tokens.beginPage(player.getUniqueId());
         this.renderer(player).showControls(player, view);
      });
   }

   void dispatch(Player player, String token) {
      Optional<ClassMenuAction> optionalAction = this.tokens.consume(player.getUniqueId(), token);
      if (optionalAction.isEmpty()) {
         send(player, "&eThat class-menu action expired. A fresh menu has been opened.");
         this.open(player);
      } else {
         ClassMenuAction action = optionalAction.get();
         switch (action) {
            case ClassMenuAction.OpenOverview ignored:
               this.open(player);
               break;
            case ClassMenuAction.OpenControls ignoredx:
               this.openControls(player);
               break;
            case ClassMenuAction.OpenForm openForm:
               this.openForm(player, openForm.formId(), openForm.showAllClasses());
               break;
            case ClassMenuAction.SelectForm selectForm:
               this.selectForm(player, selectForm.formId(), selectForm.showAllClasses());
               break;
            case ClassMenuAction.Challenge challenge:
               ClassChallengeInstance.admit(player, challenge.formId(), challenge.quotedFee());
               break;
            case ClassMenuAction.DeactivateClass ignoredxx:
               this.deactivateClass(player);
               break;
            default:
               throw new MatchException(null, null);
         }
      }
   }

   private void deactivateClass(Player player) {
      if (!AdvancedCombatModule.isInitialized()) {
         this.open(player);
      } else {
         SelectionResult result = AdvancedCombatModule.get().clearSelectedForm(player);
         switch (result.status()) {
            case APPLIED:
               send(player, "&aClass deactivated. Your levels are kept; pick a class any time with &f/em class&a.");
               break;
            case UNCHANGED:
               send(player, "&7You have no active class.");
               break;
            case NOT_READY:
               send(player, "&eYour class profile is still loading.");
               break;
            case LOCKED_FORM:
               send(player, "&cYour class is locked until this dungeon run ends.");
               break;
            default:
               send(player, "&cYour class could not be deactivated.");
         }

         this.open(player);
      }
   }

   private Optional<ClassMenuView> project(Player player) {
      if (!AdvancedCombatModule.isInitialized()) {
         send(player, "&c[Alpha] Advanced Combat System is disabled on this server.");
         return Optional.empty();
      } else {
         AdvancedCombatModule module = AdvancedCombatModule.get();
         ProfileSnapshot profile = module.profile(player.getUniqueId()).orElse(null);
         if (profile == null) {
            send(player, "&eYour class profile is still loading. Try again in a moment.");
            return Optional.empty();
         } else {
            return Optional.of(ClassMenuProjector.project(module.catalog(), profile, skill -> PlayerData.getSkillLevel(player.getUniqueId(), skill)));
         }
      }
   }

   private void selectForm(Player player, String formId, boolean showAllClasses) {
      if (!AdvancedCombatModule.isInitialized()) {
         this.open(player);
      } else {
         SelectionResult result = AdvancedCombatModule.get().selectForm(player, formId);
         switch (result.status()) {
            case APPLIED:
               send(
                  player,
                  ClassPresentationTheme.gradient("#2E7D4F:#69C56F", "Class active:")
                     + " &f"
                     + AdvancedCombatModule.get().catalog().require(formId).displayName()
               );
               send(
                  player,
                  ClassPresentationTheme.gradient("#B8860B:#F0C040", "How to play:") + " &fF,F&7: Mobility | &fF+LMB&7: Signature | &fF+RMB&7: Utility."
               );
               send(
                  player,
                  ClassPresentationTheme.gradient("#7A1F2B:#C2414A", "Weapons:")
                     + " "
                     + ClassWeaponAffinity.description(AdvancedCombatModule.get().catalog().require(formId))
               );
               break;
            case UNCHANGED:
               send(player, "&7That is already your active class.");
               break;
            case NOT_READY:
               send(player, "&eYour class profile is still loading.");
               break;
            case LOCKED_FORM:
               send(
                  player,
                  result.snapshot() != null && result.snapshot().lockedRunSelection() != null
                     ? "&cYour class is locked until this dungeon run ends."
                     : "&cThat class form is still locked."
               );
               break;
            case UNKNOWN_FORM:
               send(player, "&cThat class form no longer exists.");
         }

         this.openForm(player, formId, showAllClasses);
      }
   }

   private ClassMenuRenderer renderer(Player player) {
      return ClassSelectionMenu.supportsDialogs(player) ? this.dialogs : this.inventories;
   }

   private static void send(Player player, String message) {
      player.sendMessage(ChatColorConverter.convert(message));
   }
}
