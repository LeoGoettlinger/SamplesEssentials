package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class NightbreakPluginCatalog {
   private static final Map<String, NightbreakPluginCatalog.Entry> ENTRIES = new LinkedHashMap<>();
   private static final Map<String, List<NightbreakPluginCatalog.Recommendation>> RECOMMENDATIONS = new LinkedHashMap<>();

   private NightbreakPluginCatalog() {
   }

   public static List<NightbreakPluginCatalog.Entry> entries() {
      return List.copyOf(ENTRIES.values());
   }

   public static Optional<NightbreakPluginCatalog.Entry> find(String slug) {
      return slug == null ? Optional.empty() : Optional.ofNullable(ENTRIES.get(normalize(slug)));
   }

   public static Optional<NightbreakPluginCatalog.Entry> forSpec(NightbreakPluginSpec spec) {
      if (spec == null) {
         return Optional.empty();
      } else {
         Optional<NightbreakPluginCatalog.Entry> bySlug = find(spec.pluginSlug());
         return bySlug.isPresent() ? bySlug : ENTRIES.values().stream().filter(entry -> entry.rootCommand().equalsIgnoreCase(spec.rootCommand())).findFirst();
      }
   }

   public static List<NightbreakPluginCatalog.Recommendation> recommendationsFor(String slug) {
      return slug == null ? List.of() : RECOMMENDATIONS.getOrDefault(normalize(slug), List.of());
   }

   public static boolean isInstalled(NightbreakPluginCatalog.Entry entry) {
      return installedPlugin(entry).isPresent();
   }

   public static Optional<Plugin> installedPlugin(NightbreakPluginCatalog.Entry entry) {
      if (entry != null && entry.pluginName() != null && !entry.pluginName().isBlank()) {
         for (String pluginName : entry.allPluginNames()) {
            Plugin exactMatch = Bukkit.getPluginManager().getPlugin(pluginName);
            if (exactMatch != null) {
               return Optional.of(exactMatch);
            }
         }

         for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            for (String pluginNamex : entry.allPluginNames()) {
               if (plugin.getName().equalsIgnoreCase(pluginNamex)) {
                  return Optional.of(plugin);
               }
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public static String setupCommand(NightbreakPluginCatalog.Entry entry) {
      return entry != null && !entry.rootCommand().isBlank() ? "/" + entry.rootCommand() + " setup" : "";
   }

   public static String recommendedPluginsCommand(NightbreakPluginCatalog.Entry entry) {
      return entry != null && !entry.rootCommand().isBlank() ? "/" + entry.rootCommand() + " recommendedplugins" : "";
   }

   private static void add(NightbreakPluginCatalog.Entry entry) {
      ENTRIES.put(normalize(entry.slug()), entry);
   }

   private static NightbreakPluginCatalog.Recommendation rec(
      String slug, NightbreakPluginCatalog.RelationType relationType, NightbreakPluginCatalog.Recommendation... dependencyNotes
   ) {
      return new NightbreakPluginCatalog.Recommendation(slug, relationType, List.of(dependencyNotes));
   }

   private static void recommend(String ownerSlug, NightbreakPluginCatalog.Recommendation... recommendations) {
      RECOMMENDATIONS.put(normalize(ownerSlug), List.of(recommendations));
   }

   private static String normalize(String slug) {
      return slug.toLowerCase(Locale.ROOT).replace("_", "-").trim();
   }

   static {
      add(
         new NightbreakPluginCatalog.Entry(
            "elitemobs",
            "EliteMobs",
            "EliteMobs",
            "em",
            "EliteMobs.jar",
            "https://nightbreak.io/plugin/elitemobs/",
            "https://nightbreak.io/plugin/elitemobs/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Dynamic bosses, dungeons, quests, loot, arenas, and server RPG progression."),
            "40090"
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "betterstructures",
            "BetterStructures",
            "BetterStructures",
            "bs",
            "BetterStructures.jar",
            "https://nightbreak.io/plugin/betterstructures/",
            "https://nightbreak.io/plugin/betterstructures/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Random structures, schematics, modular generation, and world exploration content."),
            "103241"
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "freeminecraftmodels",
            "FreeMinecraftModels",
            "FreeMinecraftModels",
            "fmm",
            "FreeMinecraftModels.jar",
            "https://nightbreak.io/plugin/freeminecraftmodels/",
            "https://nightbreak.io/plugin/freeminecraftmodels/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Free custom model engine for mobs, props, furniture, and Bedrock-friendly assets."),
            "111660"
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "resourcepackmanager",
            "ResourcePackManager",
            "ResourcePackManager",
            "rspm",
            "ResourcePackManager.jar",
            "https://nightbreak.io/plugin/resourcepackmanager/",
            "https://nightbreak.io/plugin/resourcepackmanager/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Builds, hosts, merges, and delivers resource packs across supported plugins."),
            "118574"
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "eternaltd",
            "EternalTD",
            "EternalTD",
            "etd",
            "EternalTD.jar",
            "https://nightbreak.io/plugin/eternaltd/",
            "https://nightbreak.io/plugin/eternaltd/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Tower-defense minigame maps, waves, NPCs, towers, and installable content."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "resurrectionchest",
            "ResurrectionChest",
            "ResurrectionChest",
            "resurrectionchest",
            "ResurrectionChest.jar",
            "https://nightbreak.io/plugin/resurrectionchest/",
            "https://nightbreak.io/plugin/resurrectionchest/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Death chests that preserve items, with optional custom chest models."),
            "57541"
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "cannonrtp",
            "CannonRTP",
            "CannonRTP",
            "wc",
            "CannonRTP.jar",
            "https://nightbreak.io/plugin/cannonrtp/",
            "https://nightbreak.io/plugin/cannonrtp/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Cannon-based random teleporting with configurable landing and protection checks."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "betterfood",
            "BetterFood",
            "BetterFood",
            "betterfood",
            "BetterFood.jar",
            "https://nightbreak.io/plugin/betterfood/",
            "https://nightbreak.io/plugin/betterfood/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Quality-of-life eating controls for crafted and cooked food."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "extractioncraft",
            "ExtractionCraft",
            "Extractioncraft",
            "extractioncraft",
            "Extractioncraft.jar",
            "https://nightbreak.io/plugin/extractioncraft/",
            "https://nightbreak.io/plugin/extractioncraft/download/",
            NightbreakPluginCatalog.SourceType.NIGHTBREAK,
            List.of("&7Extraction match gameplay that pairs with structure and RPG content."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "worldedit",
            "WorldEdit",
            "WorldEdit",
            "",
            "",
            "https://modrinth.com/plugin/worldedit",
            "https://modrinth.com/plugin/worldedit",
            NightbreakPluginCatalog.SourceType.EXTERNAL,
            List.of("&7Required structure editing library used to load and paste schematics."),
            "",
            List.of("FastAsyncWorldEdit")
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "worldguard",
            "WorldGuard",
            "WorldGuard",
            "",
            "",
            "https://modrinth.com/plugin/worldguard",
            "https://modrinth.com/plugin/worldguard",
            NightbreakPluginCatalog.SourceType.EXTERNAL,
            List.of("&7Region protection layer used by BetterStructures guided setup."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "libsdisguises",
            "LibsDisguises Free",
            "LibsDisguises",
            "",
            "",
            "https://www.spigotmc.org/resources/libs-disguises-free.81/",
            "https://www.spigotmc.org/resources/libs-disguises-free.81/",
            NightbreakPluginCatalog.SourceType.EXTERNAL,
            List.of("&7Adds disguise support for custom NPC and player appearances."),
            ""
         )
      );
      add(
         new NightbreakPluginCatalog.Entry(
            "packetevents",
            "PacketEvents",
            "packetevents",
            "",
            "",
            "https://modrinth.com/plugin/packetevents",
            "https://modrinth.com/plugin/packetevents",
            NightbreakPluginCatalog.SourceType.EXTERNAL,
            List.of("&7Required packet library used by current LibsDisguises builds."),
            ""
         )
      );
      recommend(
         "elitemobs",
         rec("betterstructures", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("libsdisguises", NightbreakPluginCatalog.RelationType.RECOMMENDED, rec("packetevents", NightbreakPluginCatalog.RelationType.REQUIRED))
      );
      recommend(
         "betterstructures",
         rec("worldedit", NightbreakPluginCatalog.RelationType.REQUIRED),
         rec("worldguard", NightbreakPluginCatalog.RelationType.REQUIRED),
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("elitemobs", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED)
      );
      recommend("resourcepackmanager", rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.ENCOURAGED));
      recommend(
         "freeminecraftmodels",
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("elitemobs", NightbreakPluginCatalog.RelationType.ENCOURAGED),
         rec("betterstructures", NightbreakPluginCatalog.RelationType.ENCOURAGED)
      );
      recommend(
         "eternaltd",
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("libsdisguises", NightbreakPluginCatalog.RelationType.ENCOURAGED, rec("packetevents", NightbreakPluginCatalog.RelationType.REQUIRED))
      );
      recommend(
         "extractioncraft",
         rec("elitemobs", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("betterstructures", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED)
      );
      recommend(
         "resurrectionchest",
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED)
      );
      recommend(
         "cannonrtp",
         rec("freeminecraftmodels", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("resourcepackmanager", NightbreakPluginCatalog.RelationType.RECOMMENDED),
         rec("worldguard", NightbreakPluginCatalog.RelationType.ENCOURAGED)
      );
      recommend("betterfood");
   }

   public record Entry(
      String slug,
      String displayName,
      String pluginName,
      String rootCommand,
      String jarFileName,
      String publicPageUrl,
      String downloadPageUrl,
      NightbreakPluginCatalog.SourceType sourceType,
      List<String> summaryLore,
      String spigotResourceId,
      List<String> pluginAliases
   ) {
      public Entry(
         String slug,
         String displayName,
         String pluginName,
         String rootCommand,
         String jarFileName,
         String publicPageUrl,
         String downloadPageUrl,
         NightbreakPluginCatalog.SourceType sourceType,
         List<String> summaryLore,
         String spigotResourceId
      ) {
         this(slug, displayName, pluginName, rootCommand, jarFileName, publicPageUrl, downloadPageUrl, sourceType, summaryLore, spigotResourceId, List.of());
      }

      public Entry(
         String slug,
         String displayName,
         String pluginName,
         String rootCommand,
         String jarFileName,
         String publicPageUrl,
         String downloadPageUrl,
         NightbreakPluginCatalog.SourceType sourceType,
         List<String> summaryLore,
         String spigotResourceId,
         List<String> pluginAliases
      ) {
         summaryLore = summaryLore == null ? List.of() : List.copyOf(summaryLore);
         rootCommand = rootCommand == null ? "" : rootCommand;
         jarFileName = jarFileName == null ? "" : jarFileName;
         spigotResourceId = spigotResourceId == null ? "" : spigotResourceId;
         pluginAliases = pluginAliases == null ? List.of() : List.copyOf(pluginAliases);
         this.slug = slug;
         this.displayName = displayName;
         this.pluginName = pluginName;
         this.rootCommand = rootCommand;
         this.jarFileName = jarFileName;
         this.publicPageUrl = publicPageUrl;
         this.downloadPageUrl = downloadPageUrl;
         this.sourceType = sourceType;
         this.summaryLore = summaryLore;
         this.spigotResourceId = spigotResourceId;
         this.pluginAliases = pluginAliases;
      }

      public List<String> allPluginNames() {
         List<String> pluginNames = new ArrayList<>();
         if (this.pluginName != null && !this.pluginName.isBlank()) {
            pluginNames.add(this.pluginName);
         }

         pluginNames.addAll(this.pluginAliases);
         return pluginNames.stream().filter(name -> name != null && !name.isBlank()).distinct().toList();
      }
   }

   public record Recommendation(String slug, NightbreakPluginCatalog.RelationType relationType, List<NightbreakPluginCatalog.Recommendation> dependencyNotes) {
      public Recommendation(String slug, NightbreakPluginCatalog.RelationType relationType, List<NightbreakPluginCatalog.Recommendation> dependencyNotes) {
         dependencyNotes = dependencyNotes == null ? List.of() : List.copyOf(dependencyNotes);
         this.slug = slug;
         this.relationType = relationType;
         this.dependencyNotes = dependencyNotes;
      }

      public Optional<NightbreakPluginCatalog.Entry> entry() {
         return NightbreakPluginCatalog.find(this.slug);
      }
   }

   public static enum RelationType {
      REQUIRED("&cRequired", "&7This plugin is needed for the guided setup."),
      RECOMMENDED("&aRecommended", "&7This plugin adds useful functionality here."),
      ENCOURAGED("&eEncouraged", "&7This plugin is useful when you want this extra feature.");

      private final String displayName;
      private final String description;

      private RelationType(String displayName, String description) {
         this.displayName = displayName;
         this.description = description;
      }

      public String displayName() {
         return this.displayName;
      }

      public String description() {
         return this.description;
      }
   }

   public static enum SourceType {
      NIGHTBREAK,
      EXTERNAL;
   }
}
