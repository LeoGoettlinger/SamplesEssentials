package me.byteful.plugin.leveltools.profile.trigger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public final class TriggerProfileLoader {
   private final Logger logger;

   public TriggerProfileLoader(@NotNull Logger logger) {
      this.logger = logger;
   }

   @NotNull
   public Map<String, TriggerProfile> load(@NotNull FileConfiguration config) {
      Map<String, TriggerProfile> profiles = new HashMap<>();
      ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
      if (profilesSection == null) {
         this.logger.warning("No trigger profiles found in trigger_profiles.yml");
         return profiles;
      } else {
         for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection != null) {
               try {
                  TriggerProfile profile = this.parseProfile(profileId, profileSection);
                  profiles.put(profileId, profile);
               } catch (Exception var8) {
                  this.logger.severe("Failed to load trigger profile '" + profileId + "': " + var8.getMessage());
               }
            }
         }

         this.logger.info("Loaded " + profiles.size() + " trigger profile(s).");
         return profiles;
      }
   }

   @NotNull
   private TriggerProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section) {
      String typeStr = section.getString("type");
      if (typeStr == null) {
         throw new IllegalArgumentException("Missing 'type' field");
      } else {
         String triggerId = TriggerIds.normalize(typeStr);
         if (triggerId.isEmpty()) {
            throw new IllegalArgumentException("Invalid trigger type: " + typeStr);
         } else {
            XpModifierConfig xpModifier = this.parseXpModifier(section.getConfigurationSection("xp_modifier"));
            TriggerFilter filter = this.parseFilter(section.getConfigurationSection("filter"));
            TriggerSlotFilter slotFilter = this.parseSlots(id, section);
            TriggerSettings settings = this.parseSettings(section, triggerId);
            return TriggerProfile.builder(id).triggerId(triggerId).xpModifier(xpModifier).filter(filter).slotFilter(slotFilter).settings(settings).build();
         }
      }
   }

   @NotNull
   private XpModifierConfig parseXpModifier(ConfigurationSection section) {
      if (section == null) {
         return new XpModifierConfig(1.0, 1.0, Collections.emptyMap());
      } else {
         ConfigurationSection defaultSection = section.getConfigurationSection("default");
         double defaultMin = 1.0;
         double defaultMax = 1.0;
         if (defaultSection != null) {
            defaultMin = defaultSection.getDouble("min", 1.0);
            defaultMax = defaultSection.getDouble("max", 1.0);
         }

         Map<String, XpModifierConfig.XpModifierRange> customModifiers = new HashMap<>();
         ConfigurationSection customSection = section.getConfigurationSection("custom");
         if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
               ConfigurationSection modSection = customSection.getConfigurationSection(key);
               if (modSection != null) {
                  double min = modSection.getDouble("min", defaultMin);
                  double max = modSection.getDouble("max", defaultMax);
                  customModifiers.put(key.toUpperCase(), new XpModifierConfig.XpModifierRange(min, max));
               }
            }
         }

         return new XpModifierConfig(defaultMin, defaultMax, customModifiers);
      }
   }

   @NotNull
   private TriggerFilter parseFilter(ConfigurationSection section) {
      if (section == null) {
         return TriggerFilter.empty();
      } else {
         String typeStr = section.getString("type", "BLACKLIST");
         boolean whitelist = typeStr.equalsIgnoreCase("WHITELIST");
         List<String> list = section.getStringList("list");
         Set<String> entries = new HashSet<>();

         for (String entry : list) {
            entries.add(entry.toUpperCase());
         }

         return new TriggerFilter(whitelist, entries);
      }
   }

   @NotNull
   private TriggerSettings parseSettings(@NotNull ConfigurationSection section, @NotNull String triggerId) {
      if (!"right_click".equals(triggerId) && !"left_click".equals(triggerId)) {
         return TriggerSettings.empty();
      } else {
         String clickModeStr = section.getString("click_mode", "ANY");
         TriggerSettings.ClickMode clickMode = TriggerSettings.ClickMode.fromString(clickModeStr);
         return TriggerSettings.forClick(clickMode);
      }
   }

   @NotNull
   private TriggerSlotFilter parseSlots(@NotNull String profileId, @NotNull ConfigurationSection section) {
      if (!section.contains("slots")) {
         return TriggerSlotFilter.all();
      } else {
         List<String> slotValues = section.getStringList("slots");
         if (slotValues.isEmpty()) {
            return TriggerSlotFilter.all();
         } else {
            Set<TriggerSlot> slots = EnumSet.noneOf(TriggerSlot.class);

            for (String slotValue : slotValues) {
               if (slotValue == null || slotValue.trim().isEmpty()) {
                  this.logger.warning("Trigger profile '" + profileId + "' contains an empty slot entry. Ignoring configured slots for this profile.");
                  return TriggerSlotFilter.all();
               }

               TriggerSlot slot = TriggerSlot.fromName(slotValue);
               if (slot == null) {
                  this.logger
                     .warning("Trigger profile '" + profileId + "' contains an invalid slot '" + slotValue + "'. Ignoring configured slots for this profile.");
                  return TriggerSlotFilter.all();
               }

               slots.add(slot);
            }

            return slots.isEmpty() ? TriggerSlotFilter.all() : TriggerSlotFilter.restricted(slots);
         }
      }
   }
}
