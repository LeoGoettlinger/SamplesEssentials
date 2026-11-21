package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

final class ClassMenuPresenter {
   private static final int BODY_WIDTH = 420;
   private static final int BUTTON_WIDTH = 205;

   private ClassMenuPresenter() {
   }

   static ClassMenuPresentation overview(ClassMenuView view) {
      ClassMenuView.FormView activeForm = view.forms().values().stream().filter(ClassMenuView.FormView::active).findFirst().orElse(null);
      String activeClass = activeForm == null ? "&7None" : ClassMenuStyle.themed(activeForm, activeForm.displayName());
      List<String> body = new ArrayList<>();
      body.add(ClassMenuStyle.section("#B8860B:#F0C040", "Active class:") + " " + activeClass);

      for (ClassMenuView.FormView root : view.roots()) {
         if (root.band() == ClassBand.STARTER) {
            body.add("&7Start as " + root.displayName() + ". Reach level " + root.band().effectiveEnd() + " to challenge the other class instructors.");
         }
      }

      List<ClassMenuPresentation.ActionView> actions = new ArrayList<>();

      for (ClassMenuView.FormView rootx : view.roots()) {
         actions.add(formAction(rootx, ClassMenuPresentation.ActionKind.FORM));
      }

      actions.add(
         new ClassMenuPresentation.ActionView(
            ClassMenuPresentation.ActionKind.CONTROLS,
            ClassMenuPresentation.Tone.CONTROL,
            ClassMenuStyle.section("#267A78:#58B8A9", "Ability Controls"),
            "&7View the controls for your three abilities.",
            new ClassMenuAction.OpenControls()
         )
      );
      if (activeForm != null) {
         actions.add(
            new ClassMenuPresentation.ActionView(
               ClassMenuPresentation.ActionKind.DEACTIVATE,
               ClassMenuPresentation.Tone.CONTROL,
               ClassMenuStyle.section("#7A1F2B:#C2414A", "Deactivate Class"),
               "&7Play without any class. Your levels are kept.",
               new ClassMenuAction.DeactivateClass()
            )
         );
      }

      return new ClassMenuPresentation(ClassMenuStyle.title("Classes"), body, 2, 205, actions);
   }

   static ClassMenuPresentation form(ClassMenuView view, ClassMenuView.FormView form) {
      return form(view, form, true);
   }

   static ClassMenuPresentation form(ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses) {
      List<String> body = new ArrayList<>();
      if (form.lineage().size() > 1) {
         body.add("&7Path &8• " + String.join(" &8> ", form.lineage().stream().map(name -> ClassMenuStyle.themed(form, name)).toList()));
      }

      if (form.unlocked()) {
         body.add(ClassMenuStyle.state(form) + " &8• &fLv " + form.effectiveLevel() + "/" + form.effectiveCap());
         body.add("&7XP &8• &f" + form.xpSummary());
      } else {
         body.add(ClassMenuStyle.state(form));
      }

      body.add(skillsLine(form));
      if (!form.unlocked()) {
         for (ClassMenuView.BlockerView blocker : form.blockers()) {
            body.add(ClassMenuStyle.blockerText(blocker));
         }

         body.addAll(List.of(ClassMenuStyle.trialInstructions(form).split("\n")));
      }

      body.add(ClassMenuStyle.themed(form, form.resourceName()) + " &8• &7" + form.resourceDescription());
      body.add("");
      body.add(abilityLine("#355CA8:#5FA9E8", "Mobility", form.mobility()));
      body.add(abilityLine("#A63D2F:#E97932", "Signature", form.signature()));
      body.add(abilityLine("#2E7D4F:#69C56F", "Utility", form.utility()));

      for (ClassMenuView.PassiveView passive : form.passives()) {
         body.addAll(ClassMenuBonusText.lines(form, passive));
      }

      if (view.runLocked() && !form.selected()) {
         body.add("");
         body.add("&cYour active class is locked for this run.");
      }

      List<ClassMenuPresentation.ActionView> actions = new ArrayList<>();
      if (form.parentId() != null && (showAllClasses || !form.band().isRoot())) {
         ClassMenuView.FormView parent = view.requireForm(form.parentId());
         actions.add(
            new ClassMenuPresentation.ActionView(
               ClassMenuPresentation.ActionKind.PARENT,
               ClassMenuPresentation.Tone.NAVIGATION,
               "← " + ClassMenuStyle.themed(parent, parent.displayName()),
               "&7Back to the previous class.",
               new ClassMenuAction.OpenForm(parent.id(), showAllClasses)
            )
         );
      }

      for (ClassMenuView.FormLink childLink : form.children()) {
         ClassMenuView.FormView child = view.requireForm(childLink.id());
         actions.add(formAction(child, ClassMenuPresentation.ActionKind.FORM, showAllClasses || child.band().isRoot()));
      }

      if (form.unlocked() && !form.selected() && !view.runLocked()) {
         actions.add(
            new ClassMenuPresentation.ActionView(
               ClassMenuPresentation.ActionKind.SELECT,
               ClassMenuPresentation.Tone.CONTROL,
               ClassMenuStyle.section("#2E7D4F:#69C56F", "Activate " + form.displayName()),
               "&7Make this your active class.",
               new ClassMenuAction.SelectForm(form.id(), showAllClasses)
            )
         );
      }

      if (!showAllClasses && form.challengeEligible() && !view.runLocked()) {
         String fee = EconomyHandler.formatCurrency(form.challengeFee());
         actions.add(
            new ClassMenuPresentation.ActionView(
               ClassMenuPresentation.ActionKind.SELECT,
               ClassMenuPresentation.Tone.CONTROL,
               "&6Challenge Instructor · " + fee + " coins",
               "&7Solo trial, level "
                  + form.band().skillUnlockLevel()
                  + ". Costs "
                  + fee
                  + " coins when combat begins. Win to unlock and activate "
                  + form.displayName()
                  + ".",
               new ClassMenuAction.Challenge(form.id(), form.challengeFee())
            )
         );
      }

      if (showAllClasses) {
         actions.add(
            new ClassMenuPresentation.ActionView(
               ClassMenuPresentation.ActionKind.OVERVIEW,
               ClassMenuPresentation.Tone.NAVIGATION,
               "← " + ClassMenuStyle.section("#B8860B:#F0C040", "All Classes"),
               "&7Return to all classes.",
               new ClassMenuAction.OpenOverview()
            )
         );
      }

      return new ClassMenuPresentation(ClassMenuStyle.themed(form, form.displayName()), body, 2, 205, actions);
   }

   static ClassMenuPresentation controls(ClassMenuView view) {
      ClassMenuView.FormView active = view.forms().values().stream().filter(ClassMenuView.FormView::active).findFirst().orElse(null);
      String mobility = active == null ? "Mobility" : active.mobility().displayName();
      String signature = active == null ? "Signature" : active.signature().displayName();
      String utility = active == null ? "Utility" : active.utility().displayName();
      List<String> body = List.of(
         ClassMenuStyle.section("#355CA8:#5FA9E8", "F, F") + " &8• &f" + mobility,
         ClassMenuStyle.section("#A63D2F:#E97932", "F + LMB") + " &8• &f" + signature,
         ClassMenuStyle.section("#2E7D4F:#69C56F", "F + RMB") + " &8• &f" + utility
      );
      List<ClassMenuPresentation.ActionView> actions = List.of(
         new ClassMenuPresentation.ActionView(
            ClassMenuPresentation.ActionKind.OVERVIEW,
            ClassMenuPresentation.Tone.NAVIGATION,
            "← " + ClassMenuStyle.section("#B8860B:#F0C040", "All Classes"),
            "&7Return to all classes.",
            new ClassMenuAction.OpenOverview()
         )
      );
      return new ClassMenuPresentation(ClassMenuStyle.title("Ability Controls"), body, 1, 420, actions);
   }

   private static ClassMenuPresentation.ActionView formAction(ClassMenuView.FormView form, ClassMenuPresentation.ActionKind kind) {
      return formAction(form, kind, true);
   }

   private static ClassMenuPresentation.ActionView formAction(ClassMenuView.FormView form, ClassMenuPresentation.ActionKind kind, boolean showAllClasses) {
      ClassMenuPresentation.Tone tone;
      if (!form.unlocked()) {
         tone = ClassMenuPresentation.Tone.LOCKED;
      } else if (form.active()) {
         tone = ClassMenuPresentation.Tone.ACTIVE;
      } else {
         tone = ClassMenuPresentation.Tone.UNLOCKED;
      }

      return new ClassMenuPresentation.ActionView(
         kind, tone, ClassMenuStyle.formButtonLabel(form), ClassMenuStyle.formTooltip(form), new ClassMenuAction.OpenForm(form.id(), showAllClasses)
      );
   }

   private static String skillsLine(ClassMenuView.FormView form) {
      StringJoiner skills = new StringJoiner(" &8• ");

      for (ClassMenuView.FoundationRequirement requirement : form.requirements()) {
         skills.add((requirement.met() ? "&a" : "&c") + requirement.displayName() + " " + requirement.currentLevel() + "&8/&f" + requirement.requiredLevel());
      }

      return ClassMenuStyle.section("#B8860B:#F0C040", "Skills") + " &8• " + skills;
   }

   private static String abilityLine(String colors, String slotName, ClassMenuView.AbilityView ability) {
      return ClassMenuStyle.section(colors, slotName + " " + ability.displayName()) + " &8• &7" + ability.description();
   }
}
