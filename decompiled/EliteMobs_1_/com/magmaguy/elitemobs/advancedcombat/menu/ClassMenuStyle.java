package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.advancedcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.economy.EconomyHandler;

final class ClassMenuStyle {
   static final String ELITE = "#8B0000:#CC4400:#DAA520";
   static final String GOLD = "#B8860B:#F0C040";
   static final String ORANGE = "#A63D2F:#E97932";
   static final String BLUE = "#355CA8:#5FA9E8";
   static final String TEAL = "#267A78:#58B8A9";
   static final String GREEN = "#2E7D4F:#69C56F";
   static final String RED = "#7A1F2B:#C2414A";
   static final String PURPLE = "#6D3AA8:#A855F7";

   private ClassMenuStyle() {
   }

   static String title(String text) {
      return gradient("#8B0000:#CC4400:#DAA520", text);
   }

   static String themed(ClassMenuView.FormView form, String text) {
      return gradient(theme(form.resourceName()), text);
   }

   static String themedResource(String resourceName, String text) {
      return gradient(theme(resourceName), text);
   }

   static String formButtonLabel(ClassMenuView.FormView form) {
      if (!form.unlocked()) {
         return gradient("#7A1F2B:#C2414A", form.displayName()) + " &8• " + gradient("#7A1F2B:#C2414A", "Locked");
      } else {
         String status;
         if (form.active()) {
            status = gradient("#B8860B:#F0C040", "Active");
         } else {
            status = gradient("#267A78:#58B8A9", "Lv " + form.effectiveLevel());
         }

         return themed(form, form.displayName()) + " &8• " + status;
      }
   }

   static String formTooltip(ClassMenuView.FormView form) {
      if (!form.unlocked()) {
         return lockTooltip(form);
      } else {
         ClassMenuView.PassiveView ownPassive = form.passives().get(form.passives().size() - 1);
         return "&7" + ownPassive.description();
      }
   }

   static String lockTooltip(ClassMenuView.FormView form) {
      StringBuilder tooltip = new StringBuilder(gradient("#7A1F2B:#C2414A", "Locked"));

      for (ClassMenuView.BlockerView blocker : form.blockers()) {
         tooltip.append('\n').append(blockerText(blocker));
      }

      tooltip.append('\n').append(trialInstructions(form));
      return tooltip.toString();
   }

   static String trialInstructions(ClassMenuView.FormView form) {
      String fee = EconomyHandler.formatCurrency(form.challengeFee());
      return "&eVisit the "
         + form.trainerName()
         + " trainer in the Adventurer's Guild.\n&7Meet the training requirements, then select Challenge Instructor.\n&7Each solo attempt costs "
         + fee
         + " coins when combat begins.\n&7Defeat the instructor to unlock and activate "
         + form.displayName()
         + ".";
   }

   static String blockerText(ClassMenuView.BlockerView blocker) {
      if (blocker.contentRequirement()) {
         return "&c" + blocker.reason();
      } else {
         String name = blocker.classRequirement() ? themedResource(blocker.classResourceName(), blocker.displayName()) : "&f" + blocker.displayName();
         return blocker.classRequirement()
            ? "&cYou need to be level &f" + blocker.requiredLevel() + " &cin " + name + "&c. &7You are level &f" + blocker.currentLevel() + "&7."
            : "&cYou need level &f" + blocker.requiredLevel() + " &cin " + name + "&c. &7You are level &f" + blocker.currentLevel() + "&7.";
      }
   }

   static String state(ClassMenuView.FormView form) {
      if (!form.unlocked()) {
         return gradient("#7A1F2B:#C2414A", "Locked");
      } else {
         return form.active() ? gradient("#B8860B:#F0C040", "Active") : gradient("#267A78:#58B8A9", "Unlocked");
      }
   }

   static String section(String colors, String text) {
      return gradient(colors, text);
   }

   static String gradient(String colors, String text) {
      return ClassPresentationTheme.gradient(colors, text);
   }

   static String theme(String resourceName) {
      return switch (resourceName) {
         case "Resolve" -> "#B8860B:#F0C040";
         case "Fury" -> "#A63D2F:#E97932";
         case "Focus" -> "#2E7D4F:#69C56F";
         case "Grace" -> "#355CA8:#5FA9E8";
         case "Mana" -> "#6D3AA8:#A855F7";
         default -> "#8B0000:#CC4400:#DAA520";
      };
   }
}
