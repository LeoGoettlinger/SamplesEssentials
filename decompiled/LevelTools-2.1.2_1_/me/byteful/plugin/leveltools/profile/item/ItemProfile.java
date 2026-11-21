package me.byteful.plugin.leveltools.profile.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemProfile {
   private final String id;
   private final Set<Material> materials;
   private final List<String> triggerProfileIds;
   private final String rewardProfileId;
   private final String displayProfileId;
   private final int maxLevel;
   private final String levelXpFormula;
   private final String extendsProfileId;

   @Deprecated
   public ItemProfile(
      @NotNull String id,
      @NotNull Set<Material> materials,
      @NotNull String triggerProfileId,
      @NotNull String rewardProfileId,
      @NotNull String displayProfileId,
      int maxLevel,
      @Nullable String levelXpFormula,
      @Nullable String extendsProfileId
   ) {
      this(id, materials, Collections.singletonList(triggerProfileId), rewardProfileId, displayProfileId, maxLevel, levelXpFormula, extendsProfileId);
   }

   public ItemProfile(
      @NotNull String id,
      @NotNull Set<Material> materials,
      @NotNull List<String> triggerProfileIds,
      @NotNull String rewardProfileId,
      @NotNull String displayProfileId,
      int maxLevel,
      @Nullable String levelXpFormula,
      @Nullable String extendsProfileId
   ) {
      this.id = id;
      this.materials = Collections.unmodifiableSet(materials);
      this.triggerProfileIds = Collections.unmodifiableList(sanitizeTriggerProfileIds(triggerProfileIds));
      this.rewardProfileId = rewardProfileId;
      this.displayProfileId = displayProfileId;
      this.maxLevel = maxLevel;
      this.levelXpFormula = levelXpFormula;
      this.extendsProfileId = extendsProfileId;
   }

   public static ItemProfile.Builder builder(@NotNull String id) {
      return new ItemProfile.Builder(id);
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @NotNull
   public Set<Material> getMaterials() {
      return this.materials;
   }

   @NotNull
   public List<String> getTriggerProfileIds() {
      return this.triggerProfileIds;
   }

   @Deprecated
   @NotNull
   public String getTriggerProfileId() {
      return this.triggerProfileIds.get(0);
   }

   @NotNull
   public String getRewardProfileId() {
      return this.rewardProfileId;
   }

   @NotNull
   public String getDisplayProfileId() {
      return this.displayProfileId;
   }

   public int getMaxLevel() {
      return this.maxLevel;
   }

   @Nullable
   public String getLevelXpFormula() {
      return this.levelXpFormula;
   }

   @Nullable
   public String getExtendsProfileId() {
      return this.extendsProfileId;
   }

   public boolean hasCustomXpFormula() {
      return this.levelXpFormula != null && !this.levelXpFormula.isEmpty();
   }

   public boolean extendsProfile() {
      return this.extendsProfileId != null && !this.extendsProfileId.isEmpty();
   }

   public boolean matchesMaterial(@NotNull Material material) {
      return this.materials.contains(material);
   }

   @NotNull
   private static List<String> sanitizeTriggerProfileIds(@NotNull List<String> triggerProfileIds) {
      LinkedHashSet<String> uniqueIds = new LinkedHashSet<>();

      for (String triggerProfileId : triggerProfileIds) {
         if (triggerProfileId != null) {
            String trimmed = triggerProfileId.trim();
            if (!trimmed.isEmpty()) {
               uniqueIds.add(trimmed);
            }
         }
      }

      return new ArrayList<>(uniqueIds);
   }

   public static final class Builder {
      private final String id;
      private Set<Material> materials = Collections.emptySet();
      private List<String> triggerProfileIds = Collections.emptyList();
      private String rewardProfileId;
      private String displayProfileId = "default";
      private int maxLevel = 100;
      private String levelXpFormula;
      private String extendsProfileId;

      private Builder(@NotNull String id) {
         this.id = id;
      }

      public ItemProfile.Builder materials(@NotNull Set<Material> materials) {
         this.materials = materials;
         return this;
      }

      public ItemProfile.Builder triggerProfiles(@NotNull List<String> triggerProfileIds) {
         this.triggerProfileIds = triggerProfileIds;
         return this;
      }

      @Deprecated
      public ItemProfile.Builder triggerProfile(@NotNull String triggerProfileId) {
         this.triggerProfileIds = Collections.singletonList(triggerProfileId);
         return this;
      }

      public ItemProfile.Builder rewardProfile(@NotNull String rewardProfileId) {
         this.rewardProfileId = rewardProfileId;
         return this;
      }

      public ItemProfile.Builder displayProfile(@NotNull String displayProfileId) {
         this.displayProfileId = displayProfileId;
         return this;
      }

      public ItemProfile.Builder maxLevel(int maxLevel) {
         this.maxLevel = maxLevel;
         return this;
      }

      public ItemProfile.Builder levelXpFormula(@Nullable String levelXpFormula) {
         this.levelXpFormula = levelXpFormula;
         return this;
      }

      public ItemProfile.Builder extendsProfile(@Nullable String extendsProfileId) {
         this.extendsProfileId = extendsProfileId;
         return this;
      }

      public ItemProfile build() {
         return new ItemProfile(
            this.id,
            this.materials,
            this.triggerProfileIds,
            this.rewardProfileId,
            this.displayProfileId,
            this.maxLevel,
            this.levelXpFormula,
            this.extendsProfileId
         );
      }
   }
}
