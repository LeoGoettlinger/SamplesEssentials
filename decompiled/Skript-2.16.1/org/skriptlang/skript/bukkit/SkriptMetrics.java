package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.bstats.bukkit.Metrics;
import ch.njol.skript.bstats.charts.DrilldownPie;
import ch.njol.skript.bstats.charts.SimplePie;
import ch.njol.skript.config.Option;
import ch.njol.skript.localization.Language;
import ch.njol.skript.update.Updater;
import ch.njol.skript.util.Version;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

public class SkriptMetrics {
   public static void setupMetrics(Metrics metrics) {
      setupLegacyMetrics(metrics);
      metrics.addCustomChart(new DrilldownPie("drilldownPluginVersion", () -> {
         Version version = Skript.getVersion();
         Table<String, String, Integer> table = HashBasedTable.create(1, 1);
         table.put(version.getMajor() + "." + version.getMinor(), version.toString(), 1);
         return table.rowMap();
      }));
      metrics.addCustomChart(new DrilldownPie("drilldownMinecraftVersion", () -> {
         Version version = Skript.getMinecraftVersion();
         Table<String, String, Integer> table = HashBasedTable.create(1, 1);
         table.put(version.getMajor() + "." + version.getMinor(), version.toString(), 1);
         return table.rowMap();
      }));
      metrics.addCustomChart(new SimplePie("buildFlavor", () -> {
         Updater updater = Skript.getInstance().getUpdater();
         return updater != null ? updater.getCurrentRelease().flavor : "unknown";
      }));
      metrics.addCustomChart(new DrilldownPie("drilldownPluginLanguage", () -> {
         String lang = Language.getName();
         return isDefaultMap(lang, SkriptConfig.language.defaultValue());
      }));
      metrics.addCustomChart(new DrilldownPie("drilldownUpdateChecker", () -> {
         Table<String, String, Integer> table = HashBasedTable.create(1, 1);
         table.put(SkriptConfig.checkForNewVersion.value().toString(), SkriptConfig.updateCheckInterval.value().toString(), 1);
         return table.rowMap();
      }));
      metrics.addCustomChart(new SimplePie("releaseChannel", SkriptConfig.releaseChannel::value));
      metrics.addCustomChart(new DrilldownPie("drilldownEffectCommands", () -> {
         Table<String, String, Integer> table = HashBasedTable.create(1, 1);
         table.put(SkriptConfig.enableEffectCommands.value().toString(), SkriptConfig.effectCommandToken.value(), 1);
         return table.rowMap();
      }));
      metrics.addCustomChart(new SimplePie("effectCommandsOps", () -> SkriptConfig.allowOpsToUseEffectCommands.value().toString()));
      metrics.addCustomChart(new SimplePie("logEffectCommands", () -> SkriptConfig.logEffectCommands.value().toString()));
      metrics.addCustomChart(new SimplePie("loadDefaultAliases", () -> SkriptConfig.loadDefaultAliases.value().toString()));
      metrics.addCustomChart(new SimplePie("playerVariableFix", () -> SkriptConfig.enablePlayerVariableFix.value().toString()));
      metrics.addCustomChart(new SimplePie("uuidsWithPlayers", () -> SkriptConfig.usePlayerUUIDsInVariableNames.value().toString()));
      metrics.addCustomChart(new DrilldownPie("drilldownDateFormat", () -> {
         String value = ((SimpleDateFormat)SkriptConfig.dateFormat.value()).toPattern();
         String defaultValue = ((SimpleDateFormat)SkriptConfig.dateFormat.defaultValue()).toPattern();
         return isDefaultMap(value, defaultValue, "default");
      }));
      metrics.addCustomChart(new DrilldownPie("drilldownLogVerbosity", () -> {
         String verbosity = SkriptConfig.verbosity.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
         String defaultValue = SkriptConfig.verbosity.defaultValue().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
         return isDefaultMap(verbosity, defaultValue);
      }));
      metrics.addCustomChart(new DrilldownPie("drilldownPluginPriority", () -> {
         String priority = SkriptConfig.defaultEventPriority.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
         String defaultValue = SkriptConfig.defaultEventPriority.defaultValue().name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
         return isDefaultMap(priority, defaultValue);
      }));
      metrics.addCustomChart(new SimplePie("cancelledByDefault", () -> SkriptConfig.listenCancelledByDefault.value().toString()));
      metrics.addCustomChart(new DrilldownPie("drilldownNumberAccuracy", () -> isDefaultMap(SkriptConfig.numberAccuracy)));
      metrics.addCustomChart(new DrilldownPie("drilldownMaxTargetDistance", () -> isDefaultMap(SkriptConfig.maxTargetBlockDistance)));
      metrics.addCustomChart(new SimplePie("caseSensitiveFunctions", () -> SkriptConfig.caseSensitive.value().toString()));
      metrics.addCustomChart(new SimplePie("caseSensitiveVariables", () -> String.valueOf(!SkriptConfig.caseInsensitiveVariables.value())));
      metrics.addCustomChart(new SimplePie("caseSensitiveCommands", () -> String.valueOf(!SkriptConfig.caseInsensitiveCommands.value())));
      metrics.addCustomChart(new SimplePie("disableSaveWarnings", () -> SkriptConfig.disableObjectCannotBeSavedWarnings.value().toString()));
      metrics.addCustomChart(new SimplePie("disableAndOrWarnings", () -> SkriptConfig.disableMissingAndOrWarnings.value().toString()));
      metrics.addCustomChart(new SimplePie("disableStartsWithWarnings", () -> SkriptConfig.disableVariableStartingWithExpressionWarnings.value().toString()));
      metrics.addCustomChart(new SimplePie("softApiExceptions", () -> SkriptConfig.apiSoftExceptions.value().toString()));
      metrics.addCustomChart(
         new SimplePie("timingsStatus", () -> !Skript.classExists("co.aikar.timings.Timings") ? "unsupported" : SkriptConfig.enableTimings.value().toString())
      );
      metrics.addCustomChart(new SimplePie("parseLinks", () -> TextComponentParser.instance().linkParseMode().name().toLowerCase(Locale.ENGLISH)));
      metrics.addCustomChart(new SimplePie("colorResetCodes", () -> SkriptConfig.colorResetCodes.value().toString()));
      metrics.addCustomChart(new SimplePie("keepLastUsage", () -> SkriptConfig.keepLastUsageDates.value().toString()));
      metrics.addCustomChart(new DrilldownPie("drilldownParsetimeWarningThreshold", () -> isDefaultMap(SkriptConfig.longParseTimeWarningThreshold, "disabled")));
   }

   private static void setupLegacyMetrics(Metrics metrics) {
      metrics.addCustomChart(new SimplePie("pluginLanguage", Language::getName));
      metrics.addCustomChart(new SimplePie("updateCheckerEnabled", () -> SkriptConfig.checkForNewVersion.value().toString()));
      metrics.addCustomChart(new SimplePie("logVerbosity", () -> SkriptConfig.verbosity.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' ')));
      metrics.addCustomChart(
         new SimplePie("pluginPriority", () -> SkriptConfig.defaultEventPriority.value().name().toLowerCase(Locale.ENGLISH).replace('_', ' '))
      );
      metrics.addCustomChart(new SimplePie("effectCommands", () -> SkriptConfig.enableEffectCommands.value().toString()));
      metrics.addCustomChart(new SimplePie("maxTargetDistance", () -> SkriptConfig.maxTargetBlockDistance.value().toString()));
   }

   private static <T> Map<String, Map<String, Integer>> isDefaultMap(@Nullable T value, T defaultValue) {
      return isDefaultMap(value, defaultValue, defaultValue.toString());
   }

   private static <T> Map<String, Map<String, Integer>> isDefaultMap(@Nullable T value, @Nullable T defaultValue, String defaultLabel) {
      Table<String, String, Integer> table = HashBasedTable.create(1, 1);
      table.put(Objects.equals(value, defaultValue) ? defaultLabel : "other", String.valueOf(value), 1);
      return table.rowMap();
   }

   private static <T> Map<String, Map<String, Integer>> isDefaultMap(Option<T> option) {
      return isDefaultMap(option.value(), option.defaultValue(), option.defaultValue().toString());
   }

   private static <T> Map<String, Map<String, Integer>> isDefaultMap(Option<T> option, String defaultLabel) {
      return isDefaultMap(option.value(), option.defaultValue(), defaultLabel);
   }
}
