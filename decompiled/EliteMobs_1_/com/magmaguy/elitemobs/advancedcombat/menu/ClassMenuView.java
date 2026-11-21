package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.advancedcombat.progression.InputProfile;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

record ClassMenuView(
   String activeSummary,
   String selectedSummary,
   boolean runLocked,
   String runLockSummary,
   InputProfile selectedInput,
   InputProfile activeInput,
   List<ClassMenuView.FormView> roots,
   Map<String, ClassMenuView.FormView> forms
) {
   ClassMenuView(
      String activeSummary,
      String selectedSummary,
      boolean runLocked,
      String runLockSummary,
      InputProfile selectedInput,
      InputProfile activeInput,
      List<ClassMenuView.FormView> roots,
      Map<String, ClassMenuView.FormView> forms
   ) {
      Objects.requireNonNull(activeSummary, "activeSummary");
      Objects.requireNonNull(selectedSummary, "selectedSummary");
      Objects.requireNonNull(runLockSummary, "runLockSummary");
      Objects.requireNonNull(selectedInput, "selectedInput");
      Objects.requireNonNull(activeInput, "activeInput");
      roots = List.copyOf(roots);
      forms = Map.copyOf(new LinkedHashMap<>(forms));
      this.activeSummary = activeSummary;
      this.selectedSummary = selectedSummary;
      this.runLocked = runLocked;
      this.runLockSummary = runLockSummary;
      this.selectedInput = selectedInput;
      this.activeInput = activeInput;
      this.roots = roots;
      this.forms = forms;
   }

   ClassMenuView.FormView requireForm(String formId) {
      ClassMenuView.FormView form = this.forms.get(formId);
      if (form == null) {
         throw new IllegalArgumentException("Unknown projected form: " + formId);
      } else {
         return form;
      }
   }

   record AbilityView(String displayName, String description) {
   }

   record BlockerView(String displayName, int requiredLevel, int currentLevel, String classResourceName, String reason) {
      BlockerView(String displayName, int requiredLevel, int currentLevel, String classResourceName) {
         this(displayName, requiredLevel, currentLevel, classResourceName, null);
      }

      BlockerView(String displayName, int requiredLevel, int currentLevel, String classResourceName, String reason) {
         Objects.requireNonNull(displayName, "displayName");
         if (requiredLevel >= 0 && currentLevel >= 0) {
            this.displayName = displayName;
            this.requiredLevel = requiredLevel;
            this.currentLevel = currentLevel;
            this.classResourceName = classResourceName;
            this.reason = reason;
         } else {
            throw new IllegalArgumentException("Blocker levels must be non-negative");
         }
      }

      boolean classRequirement() {
         return this.classResourceName != null;
      }

      boolean contentRequirement() {
         return this.reason != null;
      }
   }

   record FormLink(String id, String displayName, boolean unlocked, int effectiveLevel) {
   }

   record FormView(
      String id,
      String displayName,
      ClassBand band,
      boolean unlocked,
      boolean challengeEligible,
      double challengeFee,
      boolean selected,
      boolean active,
      String trainerName,
      List<String> lineage,
      String parentId,
      List<ClassMenuView.FormLink> children,
      int localLevel,
      int effectiveLevel,
      int localCap,
      int effectiveCap,
      String xpSummary,
      List<ClassMenuView.FoundationRequirement> requirements,
      List<ClassMenuView.BlockerView> blockers,
      String resourceName,
      String resourceDescription,
      ClassMenuView.AbilityView mobility,
      ClassMenuView.AbilityView signature,
      ClassMenuView.AbilityView utility,
      List<ClassMenuView.PassiveView> passives
   ) {
      FormView(
         String id,
         String displayName,
         ClassBand band,
         boolean unlocked,
         boolean challengeEligible,
         double challengeFee,
         boolean selected,
         boolean active,
         String trainerName,
         List<String> lineage,
         String parentId,
         List<ClassMenuView.FormLink> children,
         int localLevel,
         int effectiveLevel,
         int localCap,
         int effectiveCap,
         String xpSummary,
         List<ClassMenuView.FoundationRequirement> requirements,
         List<ClassMenuView.BlockerView> blockers,
         String resourceName,
         String resourceDescription,
         ClassMenuView.AbilityView mobility,
         ClassMenuView.AbilityView signature,
         ClassMenuView.AbilityView utility,
         List<ClassMenuView.PassiveView> passives
      ) {
         Objects.requireNonNull(id, "id");
         Objects.requireNonNull(displayName, "displayName");
         Objects.requireNonNull(band, "band");
         Objects.requireNonNull(trainerName, "trainerName");
         lineage = List.copyOf(lineage);
         children = List.copyOf(children);
         Objects.requireNonNull(xpSummary, "xpSummary");
         requirements = List.copyOf(requirements);
         blockers = List.copyOf(blockers);
         Objects.requireNonNull(resourceName, "resourceName");
         Objects.requireNonNull(resourceDescription, "resourceDescription");
         Objects.requireNonNull(mobility, "mobility");
         Objects.requireNonNull(signature, "signature");
         Objects.requireNonNull(utility, "utility");
         passives = List.copyOf(passives);
         this.id = id;
         this.displayName = displayName;
         this.band = band;
         this.unlocked = unlocked;
         this.challengeEligible = challengeEligible;
         this.challengeFee = challengeFee;
         this.selected = selected;
         this.active = active;
         this.trainerName = trainerName;
         this.lineage = lineage;
         this.parentId = parentId;
         this.children = children;
         this.localLevel = localLevel;
         this.effectiveLevel = effectiveLevel;
         this.localCap = localCap;
         this.effectiveCap = effectiveCap;
         this.xpSummary = xpSummary;
         this.requirements = requirements;
         this.blockers = blockers;
         this.resourceName = resourceName;
         this.resourceDescription = resourceDescription;
         this.mobility = mobility;
         this.signature = signature;
         this.utility = utility;
         this.passives = passives;
      }

      String statusLabel() {
         if (!this.unlocked) {
            return "Locked";
         } else {
            return this.active ? "Active · Lv " + this.effectiveLevel : "Unlocked · Lv " + this.effectiveLevel;
         }
      }
   }

   record FoundationRequirement(String displayName, int currentLevel, int requiredLevel) {
      boolean met() {
         return this.currentLevel >= this.requiredLevel;
      }
   }

   record PassiveStat(String label, double fraction) {
      PassiveStat(String label, double fraction) {
         Objects.requireNonNull(label, "label");
         if (!Double.isFinite(fraction)) {
            throw new IllegalArgumentException("Passive stat must be finite");
         } else {
            this.label = label;
            this.fraction = fraction;
         }
      }
   }

   record PassiveView(String sourceFormName, String description, int contributionLevel, boolean preview, List<ClassMenuView.PassiveStat> stats) {
      PassiveView(String sourceFormName, String description, int contributionLevel, boolean preview, List<ClassMenuView.PassiveStat> stats) {
         Objects.requireNonNull(sourceFormName, "sourceFormName");
         Objects.requireNonNull(description, "description");
         if (contributionLevel < 1) {
            throw new IllegalArgumentException("Passive contribution level must be positive");
         } else {
            stats = List.copyOf(stats);
            this.sourceFormName = sourceFormName;
            this.description = description;
            this.contributionLevel = contributionLevel;
            this.preview = preview;
            this.stats = stats;
         }
      }
   }
}
