package de.theredend2000.advancedhunt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Collection {
   private final UUID id;
   private String name;
   private boolean enabled;
   private List<ActRule> actRules;
   private String progressResetCron;
   private boolean singlePlayerFind;
   private List<Reward> completionRewards;
   private UUID defaultTreasureRewardPresetId;
   private boolean hideWhenNotAvailable;
   private boolean hideAfterFound;

   public Collection(UUID id, String name, boolean enabled) {
      this.id = Objects.requireNonNull(id, "Collection id cannot be null");
      if (name != null && !name.trim().isEmpty()) {
         this.name = name;
         this.enabled = enabled;
         this.actRules = new ArrayList<>();
         this.completionRewards = new ArrayList<>();
         this.singlePlayerFind = false;
         this.defaultTreasureRewardPresetId = null;
         this.progressResetCron = null;
         this.hideWhenNotAvailable = false;
         this.hideAfterFound = false;
      } else {
         throw new IllegalArgumentException("Collection name cannot be null or empty");
      }
   }

   public UUID getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      if (name != null && !name.trim().isEmpty()) {
         this.name = name;
      } else {
         throw new IllegalArgumentException("Collection name cannot be null or empty");
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public List<ActRule> getActRules() {
      return this.actRules != null ? Collections.unmodifiableList(this.actRules) : Collections.emptyList();
   }

   public void setActRules(List<ActRule> actRules) {
      this.actRules = actRules != null ? new ArrayList<>(actRules) : new ArrayList<>();
   }

   public void addActRule(ActRule rule) {
      if (this.actRules == null) {
         this.actRules = new ArrayList<>();
      }

      this.actRules.add(rule);
   }

   public void removeActRule(UUID ruleId) {
      if (this.actRules != null) {
         this.actRules.removeIf(rule -> rule.getId().equals(ruleId));
      }
   }

   public List<ActRule> getEnabledActRules() {
      return (List<ActRule>)(this.actRules == null ? new ArrayList<>() : this.actRules.stream().filter(ActRule::isEnabled).collect(Collectors.toList()));
   }

   public String getProgressResetCron() {
      return this.progressResetCron;
   }

   public void setProgressResetCron(String progressResetCron) {
      this.progressResetCron = progressResetCron;
   }

   @Deprecated
   public String getResetCron() {
      return this.progressResetCron;
   }

   @Deprecated
   public void setResetCron(String resetCron) {
      this.progressResetCron = resetCron;
   }

   @Deprecated
   public String getActiveStart() {
      return null;
   }

   @Deprecated
   public void setActiveStart(String activeStart) {
   }

   @Deprecated
   public String getActiveEnd() {
      return null;
   }

   @Deprecated
   public void setActiveEnd(String activeEnd) {
   }

   public boolean isSinglePlayerFind() {
      return this.singlePlayerFind;
   }

   public void setSinglePlayerFind(boolean singlePlayerFind) {
      this.singlePlayerFind = singlePlayerFind;
   }

   public List<Reward> getCompletionRewards() {
      return this.completionRewards != null ? Collections.unmodifiableList(this.completionRewards) : Collections.emptyList();
   }

   public void setCompletionRewards(List<Reward> completionRewards) {
      this.completionRewards = completionRewards != null ? new ArrayList<>(completionRewards) : new ArrayList<>();
   }

   public UUID getDefaultTreasureRewardPresetId() {
      return this.defaultTreasureRewardPresetId;
   }

   public void setDefaultTreasureRewardPresetId(UUID defaultTreasureRewardPresetId) {
      this.defaultTreasureRewardPresetId = defaultTreasureRewardPresetId;
   }

   public boolean isHideWhenNotAvailable() {
      return this.hideWhenNotAvailable;
   }

   public void setHideWhenNotAvailable(boolean hideWhenNotAvailable) {
      this.hideWhenNotAvailable = hideWhenNotAvailable;
   }

   public boolean isHideAfterFound() {
      return this.hideAfterFound;
   }

   public void setHideAfterFound(boolean hideAfterFound) {
      this.hideAfterFound = hideAfterFound;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Collection)) {
         return false;
      } else {
         Collection that = (Collection)o;
         return this.id.equals(that.id);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }
}
