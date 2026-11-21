package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public class PeaceBannerConfig extends ConfigurationFile {
   private static boolean enabled;
   private static boolean craftable;
   private static int chunkRadius;
   private static boolean suppressEvents;
   private static List<String> recipeShape;
   private static Map<String, String> recipeIngredients;
   private static String itemName;
   private static List<String> itemLore;
   private static String placedMessage;
   private static String removedMessage;

   public PeaceBannerConfig() {
      super("PeaceBanner.yml");
   }

   @Override
   public void initializeValues() {
      enabled = ConfigurationEngine.setBoolean(List.of("Whether the Peace Banner feature is enabled."), this.fileConfiguration, "enabled", true);
      craftable = ConfigurationEngine.setBoolean(List.of("Whether the Peace Banner can be crafted by players."), this.fileConfiguration, "craftable", true);
      chunkRadius = ConfigurationEngine.setInt(
         List.of("The radius (in chunks) around a placed Peace Banner where elite mobs are suppressed."), this.fileConfiguration, "chunkRadius", 4
      );
      suppressEvents = ConfigurationEngine.setBoolean(
         List.of("Whether placed Peace Banners also suppress EliteMobs events (action events, timed events)."), this.fileConfiguration, "suppressEvents", true
      );
      recipeShape = ConfigurationEngine.setList(
         List.of("The crafting recipe shape. Each string is one row of the crafting grid."),
         this.file,
         this.fileConfiguration,
         "recipeShape",
         List.of("BBB", "BWB", "BBB"),
         false
      );
      Map<String, Object> defaultIngredients = Map.of("B", "BONE", "W", "ANY_BANNER");
      this.fileConfiguration.addDefault("recipeIngredients", defaultIngredients);
      this.fileConfiguration.setComments("recipeIngredients", List.of("Maps recipe characters to materials. Use ANY_BANNER to accept all 16 banner colors."));
      recipeIngredients = new HashMap<>();
      ConfigurationSection section = this.fileConfiguration.getConfigurationSection("recipeIngredients");
      if (section != null) {
         for (String key : section.getKeys(false)) {
            recipeIngredients.put(key, section.getString(key));
         }
      } else {
         recipeIngredients.putAll(Map.of("B", "BONE", "W", "ANY_BANNER"));
      }

      itemName = ConfigurationEngine.setString(
         List.of("The display name of the Peace Banner item."), this.file, this.fileConfiguration, "itemName", "&aPeace Banner", true
      );
      itemLore = ConfigurationEngine.setList(
         List.of("The lore lines shown on the Peace Banner item."),
         this.file,
         this.fileConfiguration,
         "itemLore",
         List.of("&7Place to create a peaceful zone", "&7Prevents elite mob spawning", "&7within a &f4 chunk &7radius"),
         true
      );
      placedMessage = ConfigurationEngine.setString(
         List.of("Message sent to the player when they place a Peace Banner."),
         this.file,
         this.fileConfiguration,
         "placedMessage",
         "&aPeace Banner placed! Elite mobs will not spawn within a &f$radius chunk &aradius.",
         true
      );
      removedMessage = ConfigurationEngine.setString(
         List.of("Message sent to the player when they break a Peace Banner."),
         this.file,
         this.fileConfiguration,
         "removedMessage",
         "&cPeace Banner removed. Elite mobs can now spawn in this area again.",
         true
      );
   }

   @Generated
   public static boolean isEnabled() {
      return enabled;
   }

   @Generated
   public static boolean isCraftable() {
      return craftable;
   }

   @Generated
   public static int getChunkRadius() {
      return chunkRadius;
   }

   @Generated
   public static boolean isSuppressEvents() {
      return suppressEvents;
   }

   @Generated
   public static List<String> getRecipeShape() {
      return recipeShape;
   }

   @Generated
   public static Map<String, String> getRecipeIngredients() {
      return recipeIngredients;
   }

   @Generated
   public static String getItemName() {
      return itemName;
   }

   @Generated
   public static List<String> getItemLore() {
      return itemLore;
   }

   @Generated
   public static String getPlacedMessage() {
      return placedMessage;
   }

   @Generated
   public static String getRemovedMessage() {
      return removedMessage;
   }
}
