package com.magmaguy.elitemobs.config.customevents;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;

public class CustomEventsConfigFields extends CustomConfigFields {
   private CustomEvent.EventType eventType = CustomEvent.EventType.DEFAULT;
   private List<String> bossFilenames = new ArrayList<>();
   private String startMessage;
   private String endMessage;
   private List<String> eventStartCommands = new ArrayList<>();
   private List<String> eventEndCommands = new ArrayList<>();
   private int announcementPriority = 0;
   private double chance = 0.0;
   private List<Material> breakableMaterials = new ArrayList<>();
   private double localCooldown = 0.0;
   private double globalCooldown = 0.0;
   private double weight = 0.0;
   private double eventDuration = 0.0;
   private int eventEndTime = -1;
   private boolean endEventWithBossDeath = true;
   private String spawnType = "";
   private int minimumPlayerCount = 1;

   public CustomEventsConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.eventType = this.processEnum("eventType", this.eventType, CustomEvent.EventType.DEFAULT, CustomEvent.EventType.class, true);
      if (this.eventType == CustomEvent.EventType.DEFAULT) {
         Logger.warn("Failed to determine a valid event type for " + this.filename + " ! This event will not be registered.");
      } else {
         this.bossFilenames = this.processStringList("bossFilenames", this.bossFilenames, new ArrayList<>(), true);
         if (this.bossFilenames != null) {
            this.startMessage = this.translatable(this.filename, "startMessage", this.processString("startMessage", this.startMessage, null, false));
            this.endMessage = this.translatable(this.filename, "endMessage", this.processString("endMessage", this.endMessage, null, false));
            this.eventStartCommands = this.processStringList("eventStartCommands", this.eventStartCommands, new ArrayList<>(), false);
            this.eventEndCommands = this.processStringList("eventEndCommands", this.eventEndCommands, new ArrayList<>(), false);
            this.announcementPriority = this.processInt("announcementPriority", this.announcementPriority, 0, false);
            this.chance = this.processDouble("chance", this.chance, 0.0, false);
            this.breakableMaterials = this.processEnumList("breakableMaterials", this.breakableMaterials, new ArrayList(), Material.class, false);
            this.localCooldown = this.processDouble("localCooldown", this.localCooldown, 0.0, false);
            this.globalCooldown = this.processDouble("globalCooldown", this.globalCooldown, 0.0, false);
            this.weight = this.processDouble("weight", this.weight, 0.0, true);
            this.eventDuration = this.processDouble("eventDuration", this.eventDuration, 0.0, false);
            this.eventEndTime = this.processInt("eventEndTime", this.eventEndTime, -1, false);
            this.endEventWithBossDeath = this.processBoolean("endEventWithBossDeath", this.endEventWithBossDeath, true, false);
            this.spawnType = this.processString("spawnType", this.spawnType, "", false);
            this.minimumPlayerCount = this.processInt("minimumPlayerCount", this.minimumPlayerCount, 1, false);
         }
      }
   }

   @Generated
   public CustomEvent.EventType getEventType() {
      return this.eventType;
   }

   @Generated
   public void setEventType(CustomEvent.EventType eventType) {
      this.eventType = eventType;
   }

   @Generated
   public List<String> getBossFilenames() {
      return this.bossFilenames;
   }

   @Generated
   public void setBossFilenames(List<String> bossFilenames) {
      this.bossFilenames = bossFilenames;
   }

   @Generated
   public String getStartMessage() {
      return this.startMessage;
   }

   @Generated
   public void setStartMessage(String startMessage) {
      this.startMessage = startMessage;
   }

   @Generated
   public String getEndMessage() {
      return this.endMessage;
   }

   @Generated
   public void setEndMessage(String endMessage) {
      this.endMessage = endMessage;
   }

   @Generated
   public List<String> getEventStartCommands() {
      return this.eventStartCommands;
   }

   @Generated
   public void setEventStartCommands(List<String> eventStartCommands) {
      this.eventStartCommands = eventStartCommands;
   }

   @Generated
   public List<String> getEventEndCommands() {
      return this.eventEndCommands;
   }

   @Generated
   public void setEventEndCommands(List<String> eventEndCommands) {
      this.eventEndCommands = eventEndCommands;
   }

   @Generated
   public int getAnnouncementPriority() {
      return this.announcementPriority;
   }

   @Generated
   public void setAnnouncementPriority(int announcementPriority) {
      this.announcementPriority = announcementPriority;
   }

   @Generated
   public double getChance() {
      return this.chance;
   }

   @Generated
   public void setChance(double chance) {
      this.chance = chance;
   }

   @Generated
   public List<Material> getBreakableMaterials() {
      return this.breakableMaterials;
   }

   @Generated
   public void setBreakableMaterials(List<Material> breakableMaterials) {
      this.breakableMaterials = breakableMaterials;
   }

   @Generated
   public double getLocalCooldown() {
      return this.localCooldown;
   }

   @Generated
   public void setLocalCooldown(double localCooldown) {
      this.localCooldown = localCooldown;
   }

   @Generated
   public double getGlobalCooldown() {
      return this.globalCooldown;
   }

   @Generated
   public void setGlobalCooldown(double globalCooldown) {
      this.globalCooldown = globalCooldown;
   }

   @Generated
   public double getWeight() {
      return this.weight;
   }

   @Generated
   public void setWeight(double weight) {
      this.weight = weight;
   }

   @Generated
   public double getEventDuration() {
      return this.eventDuration;
   }

   @Generated
   public void setEventDuration(double eventDuration) {
      this.eventDuration = eventDuration;
   }

   @Generated
   public int getEventEndTime() {
      return this.eventEndTime;
   }

   @Generated
   public void setEventEndTime(int eventEndTime) {
      this.eventEndTime = eventEndTime;
   }

   @Generated
   public boolean isEndEventWithBossDeath() {
      return this.endEventWithBossDeath;
   }

   @Generated
   public void setEndEventWithBossDeath(boolean endEventWithBossDeath) {
      this.endEventWithBossDeath = endEventWithBossDeath;
   }

   @Generated
   public String getSpawnType() {
      return this.spawnType;
   }

   @Generated
   public void setSpawnType(String spawnType) {
      this.spawnType = spawnType;
   }

   @Generated
   public int getMinimumPlayerCount() {
      return this.minimumPlayerCount;
   }

   @Generated
   public void setMinimumPlayerCount(int minimumPlayerCount) {
      this.minimumPlayerCount = minimumPlayerCount;
   }
}
