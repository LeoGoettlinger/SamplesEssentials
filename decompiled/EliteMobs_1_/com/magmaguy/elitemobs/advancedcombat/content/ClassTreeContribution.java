package com.magmaguy.elitemobs.advancedcombat.content;

import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.passives.PassiveProfile;
import com.magmaguy.elitemobs.advancedcombat.resources.ClassResourceDefinition;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record ClassTreeContribution(
   String rootId,
   List<ClassFormDefinition> forms,
   Map<String, FixedAbilitySpec> abilities,
   Map<String, PassiveProfile> passives,
   ClassResourceDefinition resource
) {
   public ClassTreeContribution(
      String rootId,
      List<ClassFormDefinition> forms,
      Map<String, FixedAbilitySpec> abilities,
      Map<String, PassiveProfile> passives,
      ClassResourceDefinition resource
   ) {
      Objects.requireNonNull(rootId, "rootId");
      if (rootId.isBlank()) {
         throw new IllegalArgumentException("rootId must not be blank");
      } else {
         forms = List.copyOf(forms);
         abilities = immutableCopy(abilities, "abilities");
         passives = immutableCopy(passives, "passives");
         resource = Objects.requireNonNull(resource, "resource");
         if (forms.isEmpty()) {
            throw new IllegalArgumentException(rootId + " tree has no forms");
         } else {
            Set<String> formIds = new HashSet<>();
            int roots = 0;
            ClassFormDefinition rootForm = null;

            for (ClassFormDefinition form : forms) {
               if (!formIds.add(form.id())) {
                  throw new IllegalArgumentException("Duplicate form " + form.id() + " in " + rootId);
               }

               if (form.band().isRoot()) {
                  roots++;
                  rootForm = form;
                  if (!form.id().equals(rootId)) {
                     throw new IllegalArgumentException("Contribution " + rootId + " contains root " + form.id());
                  }
               }
            }

            if (roots != 1) {
               throw new IllegalArgumentException(rootId + " must contain exactly one root form");
            } else if (rootForm != null && rootForm.rootKit().resourceType() == resource.type()) {
               for (ClassFormDefinition form : forms) {
                  if (!form.band().isRoot() && !formIds.contains(form.parentId())) {
                     throw new IllegalArgumentException(form.id() + " has a parent outside contribution " + rootId);
                  }
               }

               Set<String> expectedAbilityIds = new HashSet<>();

               for (ClassFormDefinition formx : forms) {
                  expectedAbilityIds.add(formx.signature().id());
                  expectedAbilityIds.add(formx.utility().id());
                  formx.optionalRootKit().ifPresent(kit -> expectedAbilityIds.add(kit.mobility().id()));
               }

               if (!abilities.keySet().equals(expectedAbilityIds)) {
                  throw new IllegalArgumentException(rootId + " ability contribution does not match its forms");
               } else {
                  abilities.forEach((id, spec) -> {
                     if (!id.equals(spec.id())) {
                        throw new IllegalArgumentException(rootId + " ability key does not match spec id " + id);
                     }
                  });
                  if (!passives.keySet().equals(formIds)) {
                     throw new IllegalArgumentException(rootId + " passive contribution does not match its forms");
                  } else {
                     this.rootId = rootId;
                     this.forms = forms;
                     this.abilities = abilities;
                     this.passives = passives;
                     this.resource = resource;
                  }
               }
            } else {
               throw new IllegalArgumentException(rootId + " resource definition does not match its root kit");
            }
         }
      }
   }

   private static <K, V> Map<K, V> immutableCopy(Map<K, V> source, String field) {
      Objects.requireNonNull(source, field);
      LinkedHashMap<K, V> copy = new LinkedHashMap<>();
      source.forEach((key, value) -> {
         Objects.requireNonNull(key, field + " contains a null key");
         Objects.requireNonNull(value, field + " contains a null value");
         if (copy.putIfAbsent((K)key, (V)value) != null) {
            throw new IllegalArgumentException(field + " contains a duplicate key " + key);
         }
      });
      return Collections.unmodifiableMap(copy);
   }
}
