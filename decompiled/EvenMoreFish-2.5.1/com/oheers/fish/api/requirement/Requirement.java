package com.oheers.fish.api.requirement;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.registry.EMFRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Requirement {
   private final Map<String, List<String>> checkMap = new HashMap<>();

   public Requirement() {
   }

   public Requirement(@NonNull String identifier, @NonNull List<String> values) {
      this.add(identifier, values);
   }

   public Requirement(@NonNull Map<String, List<String>> requirements) {
      this.add(requirements);
   }

   public Requirement(@Nullable Section section) {
      this.add(section);
   }

   public Requirement add(@NonNull String identifier, @NonNull List<String> values) {
      this.processRequirement(identifier, values);
      return this;
   }

   public Requirement add(@NonNull Map<String, List<String>> requirements) {
      requirements.forEach(this::processRequirement);
      return this;
   }

   public Requirement add(@Nullable Section section) {
      if (section == null) {
         return this;
      } else {
         section.getRoutesAsStrings(false).forEach(requirementString -> {
            if (section.isList(requirementString)) {
               this.processRequirement(requirementString, section.getStringList(requirementString));
            } else {
               String value = section.getString(requirementString);
               if (value == null) {
                  return;
               }

               this.processRequirement(requirementString, List.of(value));
            }
         });
         return this;
      }
   }

   private void processRequirement(@NonNull String identifier, @NonNull List<String> values) {
      this.checkMap.put(identifier, values);
   }

   public boolean check(@NonNull Player player) {
      return this.check(RequirementContext.player(player));
   }

   public boolean check(@NonNull RequirementContext context) {
      for (Entry<String, List<String>> entry : this.checkMap.entrySet()) {
         String key = entry.getKey().toUpperCase();
         List<String> value = entry.getValue();
         if (!key.isEmpty() && !value.isEmpty()) {
            RequirementType requirementType = EMFRegistry.REQUIREMENT_TYPE.get(key);
            if (requirementType == null) {
               Logging.warn("Invalid requirement. Possible typo?: " + key);
            } else if (!requirementType.checkRequirement(context, value)) {
               Logging.debug("Requirement " + requirementType.getIdentifier() + " failed with value: " + value);
               return false;
            }
         } else {
            Logging.warn("Attempted to process an invalid Requirement. Please check for earlier warnings.");
         }
      }

      return true;
   }

   @Deprecated(
      since = "2.4.5"
   )
   public boolean meetsRequirements(@NonNull RequirementContext context) {
      return this.check(context);
   }
}
