package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.ClassLootPreferences;
import com.magmaguy.elitemobs.items.ClassLootProfile;
import com.magmaguy.elitemobs.items.potioneffects.ElitePotionEffect;
import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ClassLootSettingsConfig extends ConfigurationFile {
   private static Map<ClassLootSettingsConfig.Key, ClassLootProfile> profiles = Map.of();
   private static Map<String, ClassLootSettingsConfig.Difficulty> difficultyIds = Map.of();
   private static Map<ClassLootSettingsConfig.Rank, Double> chances = Map.of();
   private static ClassLootSettingsConfig.Difficulty defaultDifficulty = ClassLootSettingsConfig.Difficulty.NORMAL;
   private static boolean enabled;
   private static double budgetFraction = 0.5;
   private static int minimumPrimaryLevel = 1;
   private static boolean classBiasEnabled;
   private static double relevantChance = 0.8;
   private static Map<ClassLootFamily.Category, Double> categoryWeights = Map.of();
   private static Map<String, ClassLootPreferences> classPreferences = Map.of();

   public ClassLootSettingsConfig() {
      super("ClassLootSettings.yml");
   }

   @Override
   public void initializeValues() {
      YamlConfiguration defaults;
      try (InputStreamReader reader = new InputStreamReader(
            Objects.requireNonNull(this.getClass().getResourceAsStream("/classloot/defaults.yml")), StandardCharsets.UTF_8
         )) {
         defaults = YamlConfiguration.loadConfiguration(reader);
      } catch (Exception var22) {
         throw new IllegalStateException("Could not load bundled class-loot profiles", var22);
      }

      this.fileConfiguration.setDefaults(defaults);
      this.fileConfiguration.options().copyDefaults(true);
      enabled = this.fileConfiguration.getBoolean("enabled", true);
      defaultDifficulty = difficulty(this.fileConfiguration.getString("defaultDifficulty"), ClassLootSettingsConfig.Difficulty.NORMAL);
      budgetFraction = number(this.fileConfiguration, "enchantmentBudgetFraction", 0.5, 0.0, 1.0);
      minimumPrimaryLevel = (int)number(this.fileConfiguration, "minimumPrimaryLevel", 1.0, 0.0, 100.0);
      this.initializeSelection();
      Map<String, ClassLootSettingsConfig.Difficulty> ids = new HashMap<>();
      ConfigurationSection idSection = this.fileConfiguration.getConfigurationSection("difficultyIds");
      if (idSection != null) {
         for (String id : idSection.getKeys(false)) {
            ids.put(id, difficulty(idSection.getString(id), defaultDifficulty));
         }
      }

      difficultyIds = Map.copyOf(ids);
      Map<ClassLootSettingsConfig.Rank, Double> dropChances = new HashMap<>();
      Map<ClassLootSettingsConfig.Key, ClassLootProfile> parsed = new HashMap<>();

      for (ClassLootSettingsConfig.Rank rank : ClassLootSettingsConfig.Rank.values()) {
         dropChances.put(rank, number(this.fileConfiguration, "dropChance." + rank, defaults.getDouble("dropChance." + rank), 0.0, 1.0));

         for (ClassLootSettingsConfig.Difficulty difficulty : ClassLootSettingsConfig.Difficulty.values()) {
            for (ClassLootFamily family : ClassLootFamily.values()) {
               String path = "profiles." + difficulty + "." + rank + "." + family;
               String primary = Objects.toString(this.fileConfiguration.get(path + ".primaryEnchantment"), "").toLowerCase(Locale.ROOT);
               if (!ClassLootProfile.supports(family, primary) || ClassLootProfile.nativeEnchantment(primary) == null) {
                  Logger.warn("Invalid class-loot primary at " + path + "; using bundled primary.");
                  primary = defaults.getString(path + ".primaryEnchantment").toLowerCase(Locale.ROOT);
               }

               parsed.put(
                  new ClassLootSettingsConfig.Key(difficulty, rank, family),
                  new ClassLootProfile(
                     primary,
                     this.rules(path + ".enchantments", family),
                     this.rules(path + ".rareEnchantments", family),
                     this.potionEffects(path + ".potionEffects")
                  )
               );
            }
         }
      }

      profiles = Map.copyOf(parsed);
      chances = Map.copyOf(dropChances);
      Logger.info("Loaded " + profiles.size() + " class-loot profiles (" + (enabled ? "enabled" : "disabled") + ").");
      this.fileConfiguration
         .setComments(
            "enchantmentBudgetFraction",
            List.of(
               "Native enchantment units drawn without replacement: ceil(total levels * fraction).",
               "0.5 matches existing scalable DLC loot. Custom enchantment levels are not budgeted."
            )
         );
      this.fileConfiguration
         .setComments(
            "minimumPrimaryLevel",
            List.of(
               "Reserve this many primary levels inside the budget, bounded by its configured ceiling.",
               "Disabled enchantments and zero budgets remain disabled."
            )
         );
   }

   private List<ClassLootProfile.Rule> rules(String path, ClassLootFamily family) {
      ConfigurationSection section = this.fileConfiguration.getConfigurationSection(path);
      ArrayList<ClassLootProfile.Rule> result = new ArrayList<>();
      if (section == null) {
         return result;
      } else {
         for (String name : section.getKeys(false)) {
            String key = name.toLowerCase(Locale.ROOT);
            if (!ClassLootProfile.supports(family, key)) {
               Logger.warn("Unsupported class-loot enchantment " + path + "." + name + "; skipped.");
            } else {
               int level = (int)number(section, name + ".level", 1.0, 0.0, 100.0);
               double chance = number(section, name + ".chance", 0.0, 0.0, 1.0);
               result.add(new ClassLootProfile.Rule(key, level, chance));
            }
         }

         return result;
      }
   }

   private List<String> potionEffects(String path) {
      ArrayList<String> result = new ArrayList<>();

      for (String raw : this.fileConfiguration.getStringList(path)) {
         try {
            String[] fields = raw.split(",");
            if (fields.length != 4) {
               throw new IllegalArgumentException("expected EFFECT,amplifier,target,method");
            }

            for (int i = 0; i < fields.length; i++) {
               fields[i] = fields[i].strip().toUpperCase(Locale.ROOT);
            }

            fields[0] = LegacyValueConverter.parsePotionEffect(fields[0]);
            if (Registry.EFFECT.get(NamespacedKey.minecraft(fields[0].toLowerCase(Locale.ROOT))) == null) {
               throw new IllegalArgumentException("unknown potion effect");
            }

            int amplifier = Integer.parseInt(fields[1]);
            if (amplifier < 0 || amplifier > 255) {
               throw new IllegalArgumentException("amplifier must be 0..255");
            }

            ElitePotionEffect.Target.valueOf(fields[2]);
            ElitePotionEffect.ApplicationMethod.valueOf(fields[3]);
            result.add(String.join(",", fields));
         } catch (RuntimeException var7) {
            Logger.warn("Invalid ClassLootSettings " + path + " entry '" + raw + "': " + var7.getMessage() + "; skipped.");
         }
      }

      return List.copyOf(result);
   }

   private void initializeSelection() {
      this.fileConfiguration.addDefault("selection.classBiasEnabled", true);
      this.fileConfiguration.addDefault("selection.relevantChance", 0.8);
      classBiasEnabled = this.fileConfiguration.getBoolean("selection.classBiasEnabled", true);
      relevantChance = number(this.fileConfiguration, "selection.relevantChance", 0.8, 0.0, 1.0);
      Map<ClassLootFamily.Category, Double> weights = new EnumMap<>(ClassLootFamily.Category.class);

      for (ClassLootFamily.Category category : ClassLootFamily.Category.values()) {
         double fallback = switch (category) {
            case WEAPONS -> 50.0;
            case ARMOR -> 45.0;
            case SHIELDS -> 5.0;
         };
         String path = "selection.categoryWeights." + category;
         this.fileConfiguration.addDefault(path, fallback);
         weights.put(category, number(this.fileConfiguration, path, fallback, 0.0, 10000.0));
      }

      if (weights.values().stream().allMatch(weight -> weight == 0.0)) {
         Logger.warn("ClassLootSettings: all category weights are zero; automatic equipment rolls are disabled.");
      }

      categoryWeights = Map.copyOf(weights);
      Map<String, ClassLootPreferences> preferences = new HashMap<>();
      ClassCatalog catalog = BuiltInClassContent.catalog();

      for (ClassFormDefinition form : catalog.forms()) {
         ClassLootPreferences fallback = ClassLootPreferences.defaults(form.id());
         String path = "selection.classes." + form.id();
         this.fileConfiguration.addDefault(path + ".armor", fallback.armor().name());
         this.fileConfiguration.addDefault(path + ".shields", fallback.shields());
         ClassLootPreferences.Armor armor = fallback.armor();

         try {
            armor = ClassLootPreferences.Armor.valueOf(this.fileConfiguration.getString(path + ".armor").strip().toUpperCase(Locale.ROOT));
         } catch (RuntimeException var10) {
            Logger.warn("Invalid " + path + ".armor; expected DPS, TANK or BOTH. Using " + armor);
         }

         if (!this.fileConfiguration.isBoolean(path + ".shields")) {
            Logger.warn("Invalid " + path + ".shields; expected true or false. Using " + fallback.shields());
         }

         preferences.put(form.id(), new ClassLootPreferences(armor, this.fileConfiguration.getBoolean(path + ".shields", fallback.shields())));
      }

      ConfigurationSection configured = this.fileConfiguration.getConfigurationSection("selection.classes");
      if (configured != null) {
         for (String id : configured.getKeys(false)) {
            if (catalog.find(id).isEmpty()) {
               Logger.warn("Unknown class in ClassLootSettings selection.classes: " + id);
            }
         }
      }

      classPreferences = Map.copyOf(preferences);
      this.fileConfiguration
         .setComments(
            "selection",
            List.of(
               "Split relevant/off-class first, then choose a category by weight, then a family equally.",
               "Empty buckets fall back to available equipment. No active class uses unbiased category weights.",
               "Weapons reuse each specialization's weapon affinities. Armor/shield preferences are editable below."
            )
         );
   }

   private static double number(ConfigurationSection section, String path, double fallback, double min, double max) {
      if (section.get(path) instanceof Number number && Double.isFinite(number.doubleValue()) && !(number.doubleValue() < min) && !(number.doubleValue() > max)
         )
       {
         return number.doubleValue();
      } else {
         Logger.warn("Invalid ClassLootSettings value at " + section.getCurrentPath() + "." + path + "; using " + fallback);
         return fallback;
      }
   }

   public static ClassLootSettingsConfig.Difficulty difficulty(String value, ClassLootSettingsConfig.Difficulty fallback) {
      try {
         return ClassLootSettingsConfig.Difficulty.valueOf(value.toUpperCase(Locale.ROOT));
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   @Override
   public void saveDefaults() {
      ConfigurationEngine.fileSaverCustomValues(this.fileConfiguration, this.file);
   }

   public static boolean enabled() {
      return enabled;
   }

   public static ClassLootSettingsConfig.Difficulty defaultDifficulty() {
      return defaultDifficulty;
   }

   public static ClassLootSettingsConfig.Difficulty forDifficultyId(Object id) {
      return difficultyIds.getOrDefault(String.valueOf(id), defaultDifficulty);
   }

   public static ClassLootSettingsConfig.Difficulty forDifficultyId(Object id, Object resolvedId) {
      return difficultyIds.getOrDefault(String.valueOf(id), forDifficultyId(resolvedId));
   }

   public static double dropChance(ClassLootSettingsConfig.Rank rank) {
      return chances.getOrDefault(rank, 0.0);
   }

   public static ClassLootProfile profile(ClassLootSettingsConfig.Difficulty difficulty, ClassLootSettingsConfig.Rank rank, ClassLootFamily family) {
      return profiles.get(new ClassLootSettingsConfig.Key(difficulty, rank, family));
   }

   public static double budgetFraction() {
      return budgetFraction;
   }

   public static int minimumPrimaryLevel() {
      return minimumPrimaryLevel;
   }

   public static boolean classBiasEnabled() {
      return classBiasEnabled;
   }

   public static double relevantChance() {
      return relevantChance;
   }

   public static double categoryWeight(ClassLootFamily.Category category) {
      return categoryWeights.getOrDefault(category, 0.0);
   }

   public static ClassLootPreferences preferences(String classId) {
      return classPreferences.getOrDefault(classId, ClassLootPreferences.defaults(classId));
   }

   public static enum Difficulty {
      NORMAL,
      HARD,
      MYTHIC;
   }

   private record Key(ClassLootSettingsConfig.Difficulty difficulty, ClassLootSettingsConfig.Rank rank, ClassLootFamily family) {
   }

   public static enum Rank {
      TRASH,
      MINIBOSS,
      BOSS;
   }
}
