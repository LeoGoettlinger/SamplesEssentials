package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CommandLootTable;
import com.magmaguy.elitemobs.items.customloottable.CurrencyCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.VanillaCustomLootEntry;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MobLootPreview {
   private MobLootPreview() {
   }

   public static void run(
      Player player,
      CustomBossesConfigFields fields,
      int level,
      ClassLootSettingsConfig.Difficulty difficulty,
      ClassLootSettingsConfig.Rank rank,
      ClassLootFamily selectedFamily,
      boolean giveAll
   ) {
      ClassFormDefinition form = ClassLootSelection.activeClass(player);
      List<ClassLootFamily> available = ClassLootCoverage.availableFamilies(fields, difficulty, rank, player);
      Map<ClassLootFamily, Double> probabilities = ClassLootSelection.probabilities(available, form);
      List<ItemStack> samples = new ArrayList<>();
      List<String> failures = new ArrayList<>();
      tell(player, "&6Loot review: " + fields.getFilename() + " | level " + level + " | " + difficulty + " | " + rank);
      tell(
         player,
         "&7Class: "
            + (form == null ? "none (unbiased)" : form.displayName())
            + "; boss roll chance: "
            + percent(ClassLootSettingsConfig.dropChance(rank))
            + "; family percentages below are conditional on a successful roll."
      );
      if (!ClassLootSettingsConfig.enabled() || !fields.isClassLoot() || fields.isReinforcement()) {
         tell(player, "&eAutomatic drops are disabled for this configuration. Review samples deliberately bypass that switch.");
      }

      for (ClassLootFamily family : selectedFamily == null ? Arrays.asList(ClassLootFamily.values()) : List.of(selectedFamily)) {
         if (!available.contains(family)) {
            tell(player, "&e" + family + ": unavailable on this server (material, profile or magic service).");
         } else {
            ClassLootProfile profile = ClassLootSettingsConfig.profile(difficulty, rank, family);
            tell(
               player,
               "&f"
                  + family
                  + " &7"
                  + percent(probabilities.getOrDefault(family, 0.0))
                  + " | primary "
                  + profile.primary()
                  + " | "
                  + rules(profile.enchantments())
                  + (profile.rareEnchantments().isEmpty() ? "" : " | rare " + rules(profile.rareEnchantments()))
                  + (profile.potionEffects().isEmpty() ? "" : " | effects " + profile.potionEffects())
            );
            if (giveAll || selectedFamily != null) {
               try {
                  ItemStack sample = ClassLootCoverage.preview(fields, level, difficulty, rank, family, player);
                  if (sample == null) {
                     failures.add(family + " could not be generated");
                  } else {
                     samples.add(sample);
                  }
               } catch (RuntimeException var19) {
                  failures.add(family + ": " + var19.getMessage());
               }
            }
         }
      }

      if (selectedFamily == null) {
         tell(
            player,
            "&6Authored table: all configured variants, including other difficulties. The selected tier above applies to automatic equipment; conditions below are reported, not rolled."
         );
         int index = 0;

         for (CustomLootEntry entry : fields.getCustomLootTable().getEntries()) {
            String label = label(entry);
            tell(
               player,
               "&7#"
                  + ++index
                  + " "
                  + label
                  + " | chance "
                  + percent(entry.getChance())
                  + " | amount "
                  + entry.getAmount()
                  + (entry.getPermission().isEmpty() ? "" : " | permission " + entry.getPermission())
                  + (entry.getWave() < 0 ? "" : " | wave " + entry.getWave())
            );
            if (giveAll
               && (entry instanceof EliteCustomLootEntry || entry instanceof VanillaCustomLootEntry)
               && (!(entry instanceof EliteCustomLootEntry classEntry) || !classEntry.isClassLoot())) {
               try {
                  ItemStack sample = entry instanceof EliteCustomLootEntry elite ? elite.previewDropAtLevel(level, player) : entry.previewDrop(level, player);
                  if (sample != null && !sample.getType().isAir()) {
                     samples.add(sample);
                  } else {
                     failures.add(label + " could not be generated");
                  }
               } catch (RuntimeException var20) {
                  failures.add(label + ": " + var20.getMessage());
               }
            }
         }
      }

      int overflow = 0;

      for (ItemStack sample : samples) {
         for (ItemStack extra : player.getInventory().addItem(new ItemStack[]{sample}).values()) {
            player.getWorld().dropItem(player.getLocation(), extra);
            overflow += extra.getAmount();
         }
      }

      for (String failure : failures) {
         tell(player, "&cSkipped: " + failure);
         Logger.warn("[Loot preview] " + fields.getFilename() + ": " + failure);
      }

      tell(
         player,
         "&7Generated "
            + samples.size()
            + " item samples; "
            + failures.size()
            + " failures."
            + (overflow == 0 ? "" : " " + overflow + " overflow items dropped at your feet.")
      );
      if (giveAll || selectedFamily != null) {
         tell(player, "&7Samples use normal random enchantment rolls. Commands and currency were not awarded; boss drop chances were bypassed.");
      }
   }

   private static String label(CustomLootEntry entry) {
      if (entry instanceof EliteCustomLootEntry elite) {
         return elite.getFilename() + (elite.getDifficultyIDs() == null ? "" : " | difficulty " + elite.getDifficultyIDs());
      } else if (entry instanceof VanillaCustomLootEntry vanilla) {
         return String.valueOf(vanilla.getMaterial());
      } else if (entry instanceof CurrencyCustomLootEntry currency) {
         return "currency " + currency.getCurrencyAmount() + " (report only)";
      } else {
         return entry instanceof CommandLootTable command
            ? "command " + command.getCommand() + " (report only)"
            : entry.getClass().getSimpleName() + " (report only)";
      }
   }

   private static String rules(List<ClassLootProfile.Rule> rules) {
      return String.join(", ", rules.stream().map(rule -> rule.key() + " " + rule.level() + " @ " + percent(rule.chance())).toList());
   }

   private static String percent(double value) {
      return String.format(Locale.ROOT, "%.2f%%", value * 100.0);
   }

   private static void tell(Player player, String message) {
      Logger.sendMessage(player, message);
   }
}
