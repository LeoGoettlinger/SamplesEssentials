package de.theredend2000.advancedhunt.model;

import java.util.Objects;
import java.util.UUID;

public class ActRule {
   private final UUID id;
   private UUID collectionId;
   private String name;
   private String dateRange;
   private String duration;
   private String cronExpression;
   private boolean enabled;

   public ActRule(UUID id, UUID collectionId, String name) {
      this.id = id;
      this.collectionId = Objects.requireNonNull(collectionId, "collectionId cannot be null");
      this.name = Objects.requireNonNull(name, "name cannot be null");
      this.dateRange = "*";
      this.duration = "*";
      this.cronExpression = "NONE";
      this.enabled = true;
   }

   public UUID getId() {
      return this.id;
   }

   public UUID getCollectionId() {
      return this.collectionId;
   }

   public String getName() {
      return this.name;
   }

   public String getDateRange() {
      return this.dateRange;
   }

   public String getDuration() {
      return this.duration;
   }

   public String getCronExpression() {
      return this.cronExpression;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setCollectionId(UUID collectionId) {
      this.collectionId = collectionId;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDateRange(String dateRange) {
      this.dateRange = dateRange;
   }

   public void setDuration(String duration) {
      this.duration = duration;
   }

   public void setCronExpression(String cronExpression) {
      if (cronExpression == null || cronExpression.trim().isEmpty()) {
         this.cronExpression = "NONE";
      } else if (cronExpression.equalsIgnoreCase("MANUAL")) {
         this.cronExpression = "NONE";
      } else {
         this.cronExpression = cronExpression;
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public String getActFormat() {
      return String.format("[%s] [%s] [%s]", this.dateRange, this.duration, this.cronExpression);
   }

   @Override
   public String toString() {
      return "ActRule{id=" + this.id + ", name='" + this.name + '\'' + ", actFormat=" + this.getActFormat() + ", enabled=" + this.enabled + '}';
   }
}
