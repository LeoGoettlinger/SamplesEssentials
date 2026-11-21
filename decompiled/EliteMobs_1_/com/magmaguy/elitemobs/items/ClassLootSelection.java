package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.progression.ProfileSnapshot;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;

public final class ClassLootSelection {
   private ClassLootSelection() {
   }

   public static ClassFormDefinition activeClass(Player player) {
      if (player != null && AdvancedCombatModule.isInitialized()) {
         AdvancedCombatModule module = AdvancedCombatModule.get();
         return module.profile(player.getUniqueId())
            .flatMap(ProfileSnapshot::optionalActiveLineage)
            .map(active -> module.catalog().require(active.activeFormId()))
            .orElse(null);
      } else {
         return null;
      }
   }

   public static Map<ClassLootFamily, Double> probabilities(List<ClassLootFamily> available, ClassFormDefinition form) {
      List<ClassLootFamily> enabled = available.stream().filter(family -> ClassLootSettingsConfig.categoryWeight(family.category()) > 0.0).toList();
      Map<ClassLootFamily, Double> result = new EnumMap<>(ClassLootFamily.class);
      if (form != null && ClassLootSettingsConfig.classBiasEnabled()) {
         ClassLootPreferences preference = ClassLootSettingsConfig.preferences(form.id());
         List<ClassLootFamily> relevant = enabled.stream().filter(family -> preference.relevant(form, family)).toList();
         List<ClassLootFamily> other = enabled.stream().filter(family -> !preference.relevant(form, family)).toList();
         double relevantChance = other.isEmpty() ? 1.0 : (relevant.isEmpty() ? 0.0 : ClassLootSettingsConfig.relevantChance());
         addBucket(result, relevant, relevantChance);
         addBucket(result, other, 1.0 - relevantChance);
         return Map.copyOf(result);
      } else {
         addBucket(result, enabled, 1.0);
         return Map.copyOf(result);
      }
   }

   private static void addBucket(Map<ClassLootFamily, Double> result, List<ClassLootFamily> families, double probability) {
      if (!families.isEmpty() && !(probability <= 0.0)) {
         Map<ClassLootFamily.Category, Long> counts = new EnumMap<>(ClassLootFamily.Category.class);
         families.forEach(familyx -> counts.merge(familyx.category(), 1L, Long::sum));
         double total = counts.keySet().stream().mapToDouble(ClassLootSettingsConfig::categoryWeight).sum();

         for (ClassLootFamily family : families) {
            result.put(family, probability * ClassLootSettingsConfig.categoryWeight(family.category()) / total / counts.get(family.category()).longValue());
         }
      }
   }

   public static ClassLootFamily select(List<ClassLootFamily> available, Player player) {
      Map<ClassLootFamily, Double> probabilities = probabilities(available, activeClass(player));
      double roll = ThreadLocalRandom.current().nextDouble();
      ClassLootFamily last = null;

      for (ClassLootFamily family : ClassLootFamily.values()) {
         double chance = probabilities.getOrDefault(family, 0.0);
         if (!(chance <= 0.0)) {
            last = family;
            roll -= chance;
            if (roll < 0.0) {
               return family;
            }
         }
      }

      return last;
   }

   public static EliteCustomLootEntry selectEntry(List<EliteCustomLootEntry> eligible, Player player) {
      EnumMap<ClassLootFamily, List<EliteCustomLootEntry>> byFamily = new EnumMap<>(ClassLootFamily.class);

      for (EliteCustomLootEntry entry : eligible) {
         CustomItem item = CustomItem.getCustomItem(entry.getFilename());
         if (item != null && item.getCustomItemsConfigFields().getClassLootFamily() != null) {
            byFamily.computeIfAbsent(item.getCustomItemsConfigFields().getClassLootFamily(), ignored -> new ArrayList<>()).add(entry);
         }
      }

      ClassLootFamily family = select(List.copyOf(byFamily.keySet()), player);
      if (family == null) {
         return null;
      } else {
         List<EliteCustomLootEntry> choices = byFamily.get(family);
         return choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
      }
   }
}
