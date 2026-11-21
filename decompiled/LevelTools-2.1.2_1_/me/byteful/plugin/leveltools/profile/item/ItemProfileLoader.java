package me.byteful.plugin.leveltools.profile.item;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.libs.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemProfileLoader {
   private final Logger logger;

   public ItemProfileLoader(@NotNull Logger logger) {
      this.logger = logger;
   }

   @NotNull
   public Map<String, ItemProfile> load(@NotNull FileConfiguration config) {
      Map<String, ItemProfile> profiles = new HashMap<>();
      Map<String, ConfigurationSection> rawProfiles = new HashMap<>();
      ConfigurationSection profilesSection = config.getConfigurationSection("profiles");
      if (profilesSection == null) {
         this.logger.warning("No item profiles found in item_profiles.yml");
         return profiles;
      } else {
         for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection != null) {
               rawProfiles.put(profileId, profileSection);
            }
         }

         for (String profileIdx : rawProfiles.keySet()) {
            if (!profiles.containsKey(profileIdx)) {
               try {
                  ItemProfile profile = this.resolveProfile(profileIdx, rawProfiles, profiles, new HashSet<>());
                  if (profile != null) {
                     profiles.put(profileIdx, profile);
                  }
               } catch (Exception var8) {
                  this.logger.severe("Failed to load item profile '" + profileIdx + "': " + var8.getMessage());
               }
            }
         }

         this.logger.info("Loaded " + profiles.size() + " item profile(s).");
         return profiles;
      }
   }

   @Nullable
   private ItemProfile resolveProfile(
      @NotNull String profileId,
      @NotNull Map<String, ConfigurationSection> rawProfiles,
      @NotNull Map<String, ItemProfile> resolvedProfiles,
      @NotNull Set<String> resolvingChain
   ) {
      if (resolvedProfiles.containsKey(profileId)) {
         return resolvedProfiles.get(profileId);
      } else if (resolvingChain.contains(profileId)) {
         throw new IllegalStateException("Circular inheritance detected: " + String.join(" -> ", resolvingChain) + " -> " + profileId);
      } else {
         ConfigurationSection section = rawProfiles.get(profileId);
         if (section == null) {
            this.logger.warning("Item profile '" + profileId + "' not found");
            return null;
         } else {
            resolvingChain.add(profileId);
            String extendsId = section.getString("extends");
            ItemProfile parentProfile = null;
            if (extendsId != null && !extendsId.isEmpty()) {
               parentProfile = this.resolveProfile(extendsId, rawProfiles, resolvedProfiles, resolvingChain);
               if (parentProfile == null) {
                  throw new IllegalStateException("Parent profile '" + extendsId + "' not found for profile '" + profileId + "'");
               }
            }

            ItemProfile profile = this.parseProfile(profileId, section, parentProfile);
            resolvedProfiles.put(profileId, profile);
            resolvingChain.remove(profileId);
            return profile;
         }
      }
   }

   @NotNull
   private ItemProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section, @Nullable ItemProfile parent) {
      Set<Material> materials = this.parseMaterials(section.getStringList("materials"));
      if (materials.isEmpty() && parent != null) {
         materials = parent.getMaterials();
      }

      List<String> triggerProfileIds = this.parseTriggerProfileIds(section, parent);
      if (triggerProfileIds.isEmpty()) {
         throw new IllegalArgumentException("Missing 'trigger_profiles' field");
      } else {
         String rewardProfileId = section.getString("reward_profile");
         if (rewardProfileId == null && parent != null) {
            rewardProfileId = parent.getRewardProfileId();
         }

         if (rewardProfileId == null) {
            throw new IllegalArgumentException("Missing 'reward_profile' field");
         } else {
            String displayProfileId = section.getString("display_profile");
            if (displayProfileId == null && parent != null) {
               displayProfileId = parent.getDisplayProfileId();
            }

            if (displayProfileId == null) {
               displayProfileId = "default";
            }

            int maxLevel = section.getInt("max_level", parent != null ? parent.getMaxLevel() : 100);
            String levelXpFormula = section.getString("level_xp_formula");
            if (levelXpFormula == null && parent != null) {
               levelXpFormula = parent.getLevelXpFormula();
            }

            String extendsProfileId = section.getString("extends");
            return ItemProfile.builder(id)
               .materials(materials)
               .triggerProfiles(triggerProfileIds)
               .rewardProfile(rewardProfileId)
               .displayProfile(displayProfileId)
               .maxLevel(maxLevel)
               .levelXpFormula(levelXpFormula)
               .extendsProfile(extendsProfileId)
               .build();
         }
      }
   }

   @NotNull
   private List<String> parseTriggerProfileIds(@NotNull ConfigurationSection section, @Nullable ItemProfile parent) {
      if (section.contains("trigger_profiles")) {
         return section.getStringList("trigger_profiles");
      } else {
         String singleTriggerProfile = section.getString("trigger_profile");
         if (singleTriggerProfile != null && !singleTriggerProfile.trim().isEmpty()) {
            return Collections.singletonList(singleTriggerProfile);
         } else {
            return parent != null ? parent.getTriggerProfileIds() : Collections.emptyList();
         }
      }
   }

   @NotNull
   private Set<Material> parseMaterials(@NotNull List<String> materialNames) {
      Set<Material> materials = new HashSet<>();

      for (String name : materialNames) {
         try {
            Optional<XMaterial> material = XMaterial.matchXMaterial(name.toUpperCase());
            materials.add(Objects.requireNonNull(material.get().get()));
         } catch (NoSuchElementException | NullPointerException | IllegalArgumentException var6) {
            this.logger.warning("Unknown material: " + name);
         }
      }

      return materials;
   }
}
