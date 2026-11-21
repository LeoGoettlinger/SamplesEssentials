package com.oheers.fish.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ConfigUtils {
   @NonNull
   public static ConfigurationSection getOrCreateSection(@NonNull ConfigurationSection section, @NonNull String path) {
      ConfigurationSection subSection = section.getConfigurationSection(path);
      if (subSection == null) {
         subSection = section.createSection(path);
      }

      return subSection;
   }

   @Nullable
   public static ConfigurationSection getSectionOfMany(@NonNull ConfigurationSection section, String... paths) {
      for (String path : paths) {
         ConfigurationSection sub = section.getConfigurationSection(path);
         if (sub != null) {
            return sub;
         }
      }

      return null;
   }

   @NonNull
   public static Section getOrCreateSection(@NonNull Section section, @NonNull String path) {
      Section subSection = section.getSection(path);
      if (subSection == null) {
         subSection = section.createSection(path);
      }

      return subSection;
   }

   @Nullable
   public static Section getSectionOfMany(@NonNull Section section, String... paths) {
      for (String path : paths) {
         Section sub = section.getSection(path);
         if (sub != null) {
            return sub;
         }
      }

      return null;
   }
}
