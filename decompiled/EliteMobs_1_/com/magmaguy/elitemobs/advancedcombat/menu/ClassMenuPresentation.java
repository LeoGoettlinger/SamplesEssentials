package com.magmaguy.elitemobs.advancedcombat.menu;

import java.util.List;
import java.util.Objects;

record ClassMenuPresentation(String title, List<String> bodyLines, int columns, int buttonWidth, List<ClassMenuPresentation.ActionView> actions) {
   ClassMenuPresentation(String title, List<String> bodyLines, int columns, int buttonWidth, List<ClassMenuPresentation.ActionView> actions) {
      Objects.requireNonNull(title, "title");
      bodyLines = List.copyOf(bodyLines);
      if (columns < 1) {
         throw new IllegalArgumentException("columns must be positive");
      } else if (buttonWidth < 1) {
         throw new IllegalArgumentException("buttonWidth must be positive");
      } else {
         actions = List.copyOf(actions);
         this.title = title;
         this.bodyLines = bodyLines;
         this.columns = columns;
         this.buttonWidth = buttonWidth;
         this.actions = actions;
      }
   }

   String bodyText() {
      return String.join("\n", this.bodyLines);
   }

   static enum ActionKind {
      FORM,
      PARENT,
      SELECT,
      CONTROLS,
      OVERVIEW,
      DEACTIVATE;
   }

   record ActionView(ClassMenuPresentation.ActionKind kind, ClassMenuPresentation.Tone tone, String label, String tooltip, ClassMenuAction action) {
      ActionView(ClassMenuPresentation.ActionKind kind, ClassMenuPresentation.Tone tone, String label, String tooltip, ClassMenuAction action) {
         Objects.requireNonNull(kind, "kind");
         Objects.requireNonNull(tone, "tone");
         Objects.requireNonNull(label, "label");
         Objects.requireNonNull(tooltip, "tooltip");
         Objects.requireNonNull(action, "action");
         this.kind = kind;
         this.tone = tone;
         this.label = label;
         this.tooltip = tooltip;
         this.action = action;
      }
   }

   static enum Tone {
      LOCKED,
      ACTIVE,
      UNLOCKED,
      NAVIGATION,
      CONTROL;
   }
}
