package me.byteful.plugin.leveltools.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import me.byteful.plugin.leveltools.api.ProfileType;
import me.byteful.plugin.leveltools.api.RegistrationResult;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.display.DisplayProfileLoader;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfileLoader;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardProfileLoader;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfileLoader;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProfileManager {
   private final Logger logger;
   private final ProfileValidator validator;
   private final TriggerProfileLoader triggerLoader;
   private final RewardProfileLoader rewardLoader;
   private final DisplayProfileLoader displayLoader;
   private final ItemProfileLoader itemLoader;
   private volatile Map<String, TriggerProfile> triggerProfiles = new ConcurrentHashMap<>();
   private volatile Map<String, RewardProfile> rewardProfiles = new ConcurrentHashMap<>();
   private volatile Map<String, DisplayProfile> displayProfiles = new ConcurrentHashMap<>();
   private volatile Map<String, ItemProfile> itemProfiles = new ConcurrentHashMap<>();
   private volatile Map<Material, ItemProfile> materialToProfile = new ConcurrentHashMap<>();
   private final Set<String> externalTriggerProfiles = ConcurrentHashMap.newKeySet();
   private final Set<String> externalRewardProfiles = ConcurrentHashMap.newKeySet();
   private final Set<String> externalDisplayProfiles = ConcurrentHashMap.newKeySet();
   private final Set<String> externalItemProfiles = ConcurrentHashMap.newKeySet();

   public ProfileManager(@NotNull Logger logger) {
      this.logger = logger;
      this.validator = new ProfileValidator(logger);
      this.triggerLoader = new TriggerProfileLoader(logger);
      this.rewardLoader = new RewardProfileLoader(logger);
      this.displayLoader = new DisplayProfileLoader(logger);
      this.itemLoader = new ItemProfileLoader(logger);
   }

   public void load(
      @NotNull FileConfiguration triggerConfig,
      @NotNull FileConfiguration rewardConfig,
      @NotNull FileConfiguration displayConfig,
      @NotNull FileConfiguration itemConfig
   ) {
      Map<String, TriggerProfile> savedExtTrigger = new HashMap<>();
      Map<String, RewardProfile> savedExtReward = new HashMap<>();
      Map<String, DisplayProfile> savedExtDisplay = new HashMap<>();
      Map<String, ItemProfile> savedExtItem = new HashMap<>();

      for (String id : this.externalTriggerProfiles) {
         TriggerProfile profile = this.triggerProfiles.get(id);
         if (profile != null) {
            savedExtTrigger.put(id, profile);
         }
      }

      for (String idx : this.externalRewardProfiles) {
         RewardProfile profile = this.rewardProfiles.get(idx);
         if (profile != null) {
            savedExtReward.put(idx, profile);
         }
      }

      for (String idxx : this.externalDisplayProfiles) {
         DisplayProfile profile = this.displayProfiles.get(idxx);
         if (profile != null) {
            savedExtDisplay.put(idxx, profile);
         }
      }

      for (String idxxx : this.externalItemProfiles) {
         ItemProfile profile = this.itemProfiles.get(idxxx);
         if (profile != null) {
            savedExtItem.put(idxxx, profile);
         }
      }

      Map<String, TriggerProfile> newTriggerProfiles = this.triggerLoader.load(triggerConfig);
      Map<String, RewardProfile> newRewardProfiles = this.rewardLoader.load(rewardConfig);
      Map<String, DisplayProfile> newDisplayProfiles = this.displayLoader.load(displayConfig);
      Map<String, ItemProfile> newItemProfiles = this.itemLoader.load(itemConfig);
      ProfileValidator.ValidationResult result = this.validator.validate(newTriggerProfiles, newRewardProfiles, newDisplayProfiles, newItemProfiles);
      result.logTo(this.logger);
      if (result.hasErrors()) {
         throw new IllegalStateException("Profile validation failed with " + result.getErrors().size() + " error(s). Check the logs above.");
      } else {
         newTriggerProfiles.putAll(savedExtTrigger);
         newRewardProfiles.putAll(savedExtReward);
         newDisplayProfiles.putAll(savedExtDisplay);
         newItemProfiles.putAll(savedExtItem);
         Map<Material, ItemProfile> newMaterialToProfile = this.buildMaterialMapping(newItemProfiles);
         this.triggerProfiles = new ConcurrentHashMap<>(newTriggerProfiles);
         this.rewardProfiles = new ConcurrentHashMap<>(newRewardProfiles);
         this.displayProfiles = new ConcurrentHashMap<>(newDisplayProfiles);
         this.itemProfiles = new ConcurrentHashMap<>(newItemProfiles);
         this.materialToProfile = new ConcurrentHashMap<>(newMaterialToProfile);
         this.logger.info("Profile system initialized successfully.");
      }
   }

   @NotNull
   private Map<Material, ItemProfile> buildMaterialMapping(@NotNull Map<String, ItemProfile> items) {
      Map<Material, ItemProfile> mapping = new HashMap<>();

      for (ItemProfile profile : items.values()) {
         for (Material material : profile.getMaterials()) {
            mapping.put(material, profile);
         }
      }

      this.logger.info("Mapped " + mapping.size() + " material(s) to item profiles.");
      return mapping;
   }

   public void clear() {
      this.triggerProfiles = new ConcurrentHashMap<>();
      this.rewardProfiles = new ConcurrentHashMap<>();
      this.displayProfiles = new ConcurrentHashMap<>();
      this.itemProfiles = new ConcurrentHashMap<>();
      this.materialToProfile = new ConcurrentHashMap<>();
      this.externalTriggerProfiles.clear();
      this.externalRewardProfiles.clear();
      this.externalDisplayProfiles.clear();
      this.externalItemProfiles.clear();
   }

   @NotNull
   public RegistrationResult registerTriggerProfile(@NotNull TriggerProfile profile) {
      String id = profile.getId();
      if (this.triggerProfiles.containsKey(id)) {
         return RegistrationResult.alreadyExists(id, "trigger");
      } else {
         this.triggerProfiles.put(id, profile);
         this.externalTriggerProfiles.add(id);
         this.logger.info("Registered external trigger profile: " + id);
         return RegistrationResult.success(id, "trigger");
      }
   }

   @NotNull
   public RegistrationResult registerRewardProfile(@NotNull RewardProfile profile) {
      String id = profile.getId();
      if (this.rewardProfiles.containsKey(id)) {
         return RegistrationResult.alreadyExists(id, "reward");
      } else {
         this.rewardProfiles.put(id, profile);
         this.externalRewardProfiles.add(id);
         this.logger.info("Registered external reward profile: " + id);
         return RegistrationResult.success(id, "reward");
      }
   }

   @NotNull
   public RegistrationResult registerDisplayProfile(@NotNull DisplayProfile profile) {
      String id = profile.getId();
      if (this.displayProfiles.containsKey(id)) {
         return RegistrationResult.alreadyExists(id, "display");
      } else {
         this.displayProfiles.put(id, profile);
         this.externalDisplayProfiles.add(id);
         this.logger.info("Registered external display profile: " + id);
         return RegistrationResult.success(id, "display");
      }
   }

   @NotNull
   public RegistrationResult registerItemProfile(@NotNull ItemProfile profile) {
      String id = profile.getId();
      if (this.itemProfiles.containsKey(id)) {
         return RegistrationResult.alreadyExists(id, "item");
      } else {
         for (String triggerProfileId : profile.getTriggerProfileIds()) {
            if (!this.triggerProfiles.containsKey(triggerProfileId)) {
               return RegistrationResult.missingReference(id, "trigger", triggerProfileId);
            }
         }

         if (!this.rewardProfiles.containsKey(profile.getRewardProfileId())) {
            return RegistrationResult.missingReference(id, "reward", profile.getRewardProfileId());
         } else if (!this.displayProfiles.containsKey(profile.getDisplayProfileId())) {
            return RegistrationResult.missingReference(id, "display", profile.getDisplayProfileId());
         } else {
            for (Material material : profile.getMaterials()) {
               if (this.materialToProfile.containsKey(material)) {
                  return RegistrationResult.materialConflict(id, material, this.materialToProfile.get(material).getId());
               }
            }

            this.itemProfiles.put(id, profile);

            for (Material materialx : profile.getMaterials()) {
               this.materialToProfile.put(materialx, profile);
            }

            this.externalItemProfiles.add(id);
            this.logger.info("Registered external item profile: " + id);
            return RegistrationResult.success(id, "item");
         }
      }
   }

   public boolean unregisterTriggerProfile(@NotNull String id) {
      if (!this.externalTriggerProfiles.contains(id)) {
         return false;
      } else {
         this.triggerProfiles.remove(id);
         this.externalTriggerProfiles.remove(id);
         this.logger.info("Unregistered external trigger profile: " + id);
         return true;
      }
   }

   public boolean unregisterRewardProfile(@NotNull String id) {
      if (!this.externalRewardProfiles.contains(id)) {
         return false;
      } else {
         this.rewardProfiles.remove(id);
         this.externalRewardProfiles.remove(id);
         this.logger.info("Unregistered external reward profile: " + id);
         return true;
      }
   }

   public boolean unregisterDisplayProfile(@NotNull String id) {
      if (!this.externalDisplayProfiles.contains(id)) {
         return false;
      } else {
         this.displayProfiles.remove(id);
         this.externalDisplayProfiles.remove(id);
         this.logger.info("Unregistered external display profile: " + id);
         return true;
      }
   }

   public boolean unregisterItemProfile(@NotNull String id) {
      if (!this.externalItemProfiles.contains(id)) {
         return false;
      } else {
         ItemProfile profile = this.itemProfiles.remove(id);
         if (profile != null) {
            for (Material material : profile.getMaterials()) {
               this.materialToProfile.remove(material);
            }
         }

         this.externalItemProfiles.remove(id);
         this.logger.info("Unregistered external item profile: " + id);
         return true;
      }
   }

   public boolean isExternalProfile(@NotNull String id, @NotNull ProfileType type) {
      switch (type) {
         case TRIGGER:
            return this.externalTriggerProfiles.contains(id);
         case REWARD:
            return this.externalRewardProfiles.contains(id);
         case DISPLAY:
            return this.externalDisplayProfiles.contains(id);
         case ITEM:
            return this.externalItemProfiles.contains(id);
         default:
            return false;
      }
   }

   @Nullable
   public ItemProfile getProfileForMaterial(@NotNull Material material) {
      return this.materialToProfile.get(material);
   }

   public boolean hasMaterialProfile(@NotNull Material material) {
      return this.materialToProfile.containsKey(material);
   }

   @Nullable
   public TriggerProfile getTriggerProfile(@NotNull String id) {
      return this.triggerProfiles.get(id);
   }

   @Nullable
   public RewardProfile getRewardProfile(@NotNull String id) {
      return this.rewardProfiles.get(id);
   }

   @Nullable
   public DisplayProfile getDisplayProfile(@NotNull String id) {
      return this.displayProfiles.get(id);
   }

   @Nullable
   public ItemProfile getItemProfile(@NotNull String id) {
      return this.itemProfiles.get(id);
   }

   @Deprecated
   @Nullable
   public TriggerProfile getTriggerProfileFor(@NotNull ItemProfile itemProfile) {
      List<TriggerProfile> profiles = this.getTriggerProfilesFor(itemProfile);
      return profiles.isEmpty() ? null : profiles.get(0);
   }

   @NotNull
   public List<TriggerProfile> getTriggerProfilesFor(@NotNull ItemProfile itemProfile) {
      List<TriggerProfile> profiles = new ArrayList<>();

      for (String triggerProfileId : itemProfile.getTriggerProfileIds()) {
         TriggerProfile triggerProfile = this.triggerProfiles.get(triggerProfileId);
         if (triggerProfile != null) {
            profiles.add(triggerProfile);
         }
      }

      return Collections.unmodifiableList(profiles);
   }

   @Nullable
   public RewardProfile getRewardProfileFor(@NotNull ItemProfile itemProfile) {
      return this.rewardProfiles.get(itemProfile.getRewardProfileId());
   }

   @Nullable
   public DisplayProfile getDisplayProfileFor(@NotNull ItemProfile itemProfile) {
      return this.displayProfiles.get(itemProfile.getDisplayProfileId());
   }

   @NotNull
   public Collection<TriggerProfile> getTriggerProfiles() {
      return Collections.unmodifiableCollection(this.triggerProfiles.values());
   }

   @NotNull
   public Collection<RewardProfile> getRewardProfiles() {
      return Collections.unmodifiableCollection(this.rewardProfiles.values());
   }

   @NotNull
   public Collection<DisplayProfile> getDisplayProfiles() {
      return Collections.unmodifiableCollection(this.displayProfiles.values());
   }

   @NotNull
   public Collection<ItemProfile> getItemProfiles() {
      return Collections.unmodifiableCollection(this.itemProfiles.values());
   }

   @NotNull
   public Set<String> getTriggerProfileIds() {
      return Collections.unmodifiableSet(this.triggerProfiles.keySet());
   }

   @NotNull
   public Set<String> getRewardProfileIds() {
      return Collections.unmodifiableSet(this.rewardProfiles.keySet());
   }

   @NotNull
   public Set<String> getDisplayProfileIds() {
      return Collections.unmodifiableSet(this.displayProfiles.keySet());
   }

   @NotNull
   public Set<String> getItemProfileIds() {
      return Collections.unmodifiableSet(this.itemProfiles.keySet());
   }
}
