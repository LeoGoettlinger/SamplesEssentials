package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ClassLineage(List<ClassFormDefinition> forms) {
   public ClassLineage(List<ClassFormDefinition> forms) {
      Objects.requireNonNull(forms, "forms");
      forms = List.copyOf(forms);
      if (forms.isEmpty()) {
         throw new IllegalArgumentException("A class lineage must contain a root");
      } else if (!forms.getFirst().band().isRoot()) {
         throw new IllegalArgumentException("A class lineage must start at a root");
      } else {
         for (int index = 0; index < forms.size(); index++) {
            ClassFormDefinition form = forms.get(index);
            if (form.band().depth() != forms.getFirst().band().depth() + index) {
               throw new IllegalArgumentException("Lineage skips class band at " + form.id());
            }

            if (index != 0) {
               if (form.rootKit() != null) {
                  throw new IllegalArgumentException("Combat lineage crosses into another root kit at " + form.id());
               }

               ClassFormDefinition parent = forms.get(index - 1);
               if (!form.parentId().equals(parent.id())) {
                  throw new IllegalArgumentException(form.id() + " is not a child of " + parent.id());
               }
            }
         }

         this.forms = forms;
      }
   }

   public ClassFormDefinition root() {
      return this.forms.getFirst();
   }

   public ClassFormDefinition activeForm() {
      return this.forms.getLast();
   }

   public RootClassKit rootKit() {
      return this.root().rootKit();
   }

   public ClassResourceType resourceType() {
      return this.rootKit().resourceType();
   }

   public AbilityDefinition mobility() {
      return this.rootKit().mobility();
   }

   public AbilityDefinition signature() {
      return this.activeForm().signature();
   }

   public AbilityDefinition utility() {
      return this.activeForm().utility();
   }

   public List<PassiveDefinition> passives() {
      return this.forms.stream().map(ClassFormDefinition::passive).toList();
   }

   public List<String> formIds() {
      return this.forms.stream().map(ClassFormDefinition::id).toList();
   }

   public boolean contains(String formId) {
      return this.forms.stream().anyMatch(form -> form.id().equals(formId));
   }

   public Optional<ClassFormDefinition> formAt(ClassBand band) {
      Objects.requireNonNull(band, "band");
      return this.forms.stream().filter(form -> form.band() == band).findFirst();
   }

   public Map<String, Integer> passiveContributionLevels(int activeEffectiveLevel) {
      if (!this.activeForm().band().containsEffectiveLevel(activeEffectiveLevel)) {
         throw new IllegalArgumentException("Effective level " + activeEffectiveLevel + " is outside active form " + this.activeForm().displayName());
      } else {
         Map<String, Integer> contributionLevels = new LinkedHashMap<>();

         for (ClassFormDefinition form : this.forms) {
            int contributionLevel = activeEffectiveLevel - form.band().effectiveStart() + 1;
            contributionLevels.put(form.id(), contributionLevel);
         }

         return Collections.unmodifiableMap(contributionLevels);
      }
   }
}
