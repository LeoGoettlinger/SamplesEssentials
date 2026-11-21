package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobLevelPlaceholderFormatter {
   private static final String DEFAULT_SCALED_STANDARD_SYMBOL = "「⚔」";
   private static final String DEFAULT_SCALED_HIGH_THREAT_SYMBOL = "☠";
   private static final Pattern DEFAULT_LEVEL_PREFIX_PATTERN = Pattern.compile("Lvl\\s+((?:(?:&|§)[0-9a-fk-orxA-FK-ORX])*)\\$level");

   private MobLevelPlaceholderFormatter() {
   }

   public static String replaceLevelPlaceholders(String template, EliteEntity eliteEntity, int level) {
      if (template == null) {
         return null;
      } else {
         if (eliteEntity != null && eliteEntity.isScaledCombat()) {
            template = collapseDefaultLevelPrefix(template);
         }

         String rawLevelText = String.valueOf(level);
         if (eliteEntity != null && eliteEntity.isScaledCombat()) {
            rawLevelText = eliteEntity.getHealthMultiplier() > 1.0 ? getConfiguredScaledHighThreatSymbol() : getConfiguredScaledStandardSymbol();
         }

         return template.replace("$level", rawLevelText)
            .replace("$normalLevel", "&2[&a" + rawLevelText + "&2]&f")
            .replace("$minibossLevel", "&6〖&e" + rawLevelText + "&6〗&f")
            .replace("$bossLevel", "&4『&c" + rawLevelText + "&4』&f")
            .replace("$reinforcementLevel", "&8〔&7" + rawLevelText + "&8〕&f")
            .replace("$eventBossLevel", "&4「&c" + rawLevelText + "&4」&f");
      }
   }

   private static String collapseDefaultLevelPrefix(String template) {
      Matcher matcher = DEFAULT_LEVEL_PREFIX_PATTERN.matcher(template);
      return !matcher.find() ? template : matcher.replaceFirst(Matcher.quoteReplacement(matcher.group(1) + "$level"));
   }

   private static String getConfiguredScaledStandardSymbol() {
      String configuredSymbol = MobCombatSettingsConfig.getScaledStandardLevelSymbol();
      return configuredSymbol != null && !configuredSymbol.isBlank() ? configuredSymbol : "「⚔」";
   }

   private static String getConfiguredScaledHighThreatSymbol() {
      String configuredSymbol = MobCombatSettingsConfig.getScaledHighThreatLevelSymbol();
      return configuredSymbol != null && !configuredSymbol.isBlank() ? configuredSymbol : "☠";
   }
}
