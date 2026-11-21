package com.magmaguy.elitemobs.config.customquests;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.quests.objectives.CustomObjectivesParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;

public class CustomQuestsConfigFields extends CustomConfigFields {
   private static final Set<String> TRANSLATED_OBJECTIVE_DEFAULT_KEYS = Set.of("dialog", "npcName", "location");
   protected Map<String, Map<String, Object>> customObjectives = new HashMap<>();
   List<String> temporaryPermissions = new ArrayList<>();
   List<String> questAcceptDialog = new ArrayList<>();
   private int questLevel = 0;
   private List<String> customRewardsList = new ArrayList<>();
   private String questAcceptPermission = "";
   private List<String> questAcceptPermissions = null;
   private String questLockoutPermission = "";
   private int questLockoutMinutes = -1;
   private String questName = "";
   private List<String> questLore = new ArrayList<>();
   private List<String> questCompleteDialog = new ArrayList<>();
   private List<String> questCompleteCommands = new ArrayList<>();
   private String turnInNPC = "";
   private boolean trackable = true;
   private String questAcceptSound;
   private String questCompleteSound;

   public CustomQuestsConfigFields(
      String filename,
      boolean isEnabled,
      Map<String, Map<String, Object>> customQuestObjective,
      List<String> customQuestReward,
      int questLevel,
      String questName,
      List<String> questLore
   ) {
      super(filename, isEnabled);
      this.customObjectives = customQuestObjective;
      this.customRewardsList = customQuestReward;
      this.questLevel = questLevel;
      this.questName = questName;
      this.questLore = questLore;
   }

   public CustomQuestsConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   public void setQuestLockoutPermission() {
      this.questLockoutPermission = "elitequest." + this.getFilename();
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      if (this.fileConfiguration.contains("customObjectives") && this.fileConfiguration.get("customObjectives") instanceof List) {
         this.updateOldStringFormat(this.fileConfiguration.getStringList("customObjectives"));
      }

      this.customObjectives = this.processQuestObjectives();
      this.customRewardsList = this.processStringList("customRewards", this.customRewardsList, new ArrayList<>(), true);
      this.questAcceptPermission = this.processString("questAcceptPermission", this.questAcceptPermission, null, false);
      this.questAcceptPermissions = this.processStringList("questAcceptPermissions", this.questAcceptPermissions, null, false);
      this.questLockoutPermission = this.processString("questLockoutPermission", this.questLockoutPermission, null, false);
      this.questLockoutMinutes = this.processInt("questLockoutMinutes", this.questLockoutMinutes, -1, false);
      this.questName = this.translatable(this.filename, "name", this.processString("name", this.questName, this.filename, true));
      this.questLore = this.translatable(this.filename, "questLore", this.processStringList("questLore", this.questLore, new ArrayList<>(), false));
      this.temporaryPermissions = this.processStringList("temporaryPermissions", this.temporaryPermissions, new ArrayList<>(), false);
      this.questAcceptDialog = this.translatable(
         this.filename, "questAcceptDialog", this.processStringList("questAcceptDialog", this.questAcceptDialog, new ArrayList<>(), false)
      );
      this.questCompleteDialog = this.translatable(
         this.filename, "questCompleteMessage", this.processStringList("questCompleteMessage", this.questCompleteDialog, new ArrayList<>(), false)
      );
      this.questCompleteCommands = this.processStringList("questCompleteCommands", this.questCompleteCommands, new ArrayList<>(), false);
      this.turnInNPC = this.processString("turnInNPC", this.turnInNPC, "", false);
      this.trackable = this.processBoolean("trackable", this.trackable, true, false);
      this.questLevel = this.processInt("questLevel", this.questLevel, 0, false);
      this.questAcceptSound = this.processString("questAcceptSound", this.questAcceptSound, "", false);
      this.questCompleteSound = this.processString("questCompleteSound", this.questCompleteSound, "", false);
      if (this.isEnabled) {
         try {
            CustomObjectivesParser.processCustomObjectives(this);
         } catch (IllegalArgumentException var2) {
            this.isEnabled = false;
            Logger.warn("Quest " + this.filename + " was disabled because an objective is invalid: " + var2.getMessage());
         }
      }
   }

   private void updateOldStringFormat(List<String> oldList) {
      Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();
      int counter = 0;

      for (String questObjectiveEntry : oldList) {
         counter++;
         Map<String, Object> parsedEntry = new HashMap<>();
         String[] individualEntries = questObjectiveEntry.split(":");

         for (String keyAndValue : individualEntries) {
            String[] keyAndValueArray = keyAndValue.split("=");
            String key = keyAndValueArray[0];
            Object value = null;
            if (keyAndValueArray.length > 1) {
               value = keyAndValueArray[1];
            }

            if (value == null) {
               String var15 = key.toUpperCase(Locale.ROOT);
               switch (var15) {
                  case "KILL_CUSTOM":
                  case "FETCH_ITEM":
                  case "DIALOG":
                  case "ARENA":
                     value = key;
                     key = "objectiveType";
                     break;
                  default:
                     Logger.warn("Failed to correctly parse key " + key + " in " + this.filename + " while updating the old quest configuration format!");
               }
            }

            if (key.equalsIgnoreCase("dialog")) {
               value = Arrays.stream(((String)value).split("\\n")).toList();
            }

            parsedEntry.put(key, value);
            Logger.info("Converted quest old entry to " + key + ": " + value);
         }

         parsedObjectives.put("Objective" + counter, parsedEntry);
      }

      this.fileConfiguration.set("customObjectives", parsedObjectives);
      this.customObjectives = parsedObjectives;

      try {
         this.fileConfiguration.save(this.file);
      } catch (Exception var17) {
         Logger.warn("Failed to save new custom objective format!");
      }
   }

   private Map<String, Map<String, Object>> processQuestObjectives() {
      if (this.fileConfiguration.getConfigurationSection("customObjectives") == null && !this.customObjectives.isEmpty()) {
         this.fileConfiguration.set("customObjectives", this.customObjectives);
         new HashMap<>(this.customObjectives);
         Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();

         for (Entry<String, Map<String, Object>> maps : this.customObjectives.entrySet()) {
            parsedObjectives.put(maps.getKey(), this.parseQuestObjective(maps.getKey(), maps.getValue()));
         }

         return parsedObjectives;
      } else {
         ConfigurationSection section = this.fileConfiguration.getConfigurationSection("customObjectives");
         Map<String, Object> rawMap = section == null ? null : section.getValues(false);
         if (rawMap == null) {
            Logger.warn("Failed to parse custom objectives for " + this.filename);
            this.isEnabled = false;
            return new HashMap<>();
         } else {
            Map<String, Map<String, Object>> parsedObjectives = new HashMap<>();

            for (Entry<String, Object> maps : rawMap.entrySet()) {
               Map<String, Object> objectiveValues = new HashMap<>(((ConfigurationSection)maps.getValue()).getValues(false));
               if (!TranslationsConfig.isEnglish()) {
                  objectiveValues = restoreTranslatedObjectiveDefaults(objectiveValues, this.customObjectives.get(maps.getKey()));
               }

               Map<String, Object> parsedMap = this.parseQuestObjective(maps.getKey(), objectiveValues);
               parsedObjectives.put(maps.getKey(), parsedMap);
            }

            return parsedObjectives;
         }
      }
   }

   private Map<String, Object> parseQuestObjective(String objectiveName, Map<String, Object> objectiveValues) {
      Map<String, Object> parsedMap = new HashMap<>();

      for (Entry<String, Object> entry : objectiveValues.entrySet()) {
         if (entry.getKey().equals("dialog")) {
            parsedMap.put(
               entry.getKey(), this.translatable(this.filename, "customObjectives." + objectiveName + "." + entry.getKey(), (List<String>)entry.getValue())
            );
         } else if (!entry.getKey().equals("npcName")
            && !entry.getKey().equals("itemName")
            && !entry.getKey().equals("name")
            && !entry.getKey().equals("location")) {
            parsedMap.put(entry.getKey(), entry.getValue());
         } else {
            parsedMap.put(
               entry.getKey(), this.translatable(this.filename, "customObjectives." + objectiveName + "." + entry.getKey(), (String)entry.getValue())
            );
         }
      }

      return parsedMap;
   }

   static Map<String, Object> restoreTranslatedObjectiveDefaults(Map<String, Object> objectiveValues, Map<String, Object> defaultObjectiveValues) {
      if (defaultObjectiveValues != null && !defaultObjectiveValues.isEmpty()) {
         Map<String, Object> restoredObjectiveValues = new HashMap<>(objectiveValues);

         for (String defaultKey : TRANSLATED_OBJECTIVE_DEFAULT_KEYS) {
            if (!restoredObjectiveValues.containsKey(defaultKey) && defaultObjectiveValues.containsKey(defaultKey)) {
               restoredObjectiveValues.put(defaultKey, defaultObjectiveValues.get(defaultKey));
            }
         }

         return restoredObjectiveValues;
      } else {
         return objectiveValues;
      }
   }

   @Generated
   public Map<String, Map<String, Object>> getCustomObjectives() {
      return this.customObjectives;
   }

   @Generated
   public void setCustomObjectives(Map<String, Map<String, Object>> customObjectives) {
      this.customObjectives = customObjectives;
   }

   @Generated
   public List<String> getTemporaryPermissions() {
      return this.temporaryPermissions;
   }

   @Generated
   public void setTemporaryPermissions(List<String> temporaryPermissions) {
      this.temporaryPermissions = temporaryPermissions;
   }

   @Generated
   public List<String> getQuestAcceptDialog() {
      return this.questAcceptDialog;
   }

   @Generated
   public void setQuestAcceptDialog(List<String> questAcceptDialog) {
      this.questAcceptDialog = questAcceptDialog;
   }

   @Generated
   public int getQuestLevel() {
      return this.questLevel;
   }

   @Generated
   public void setQuestLevel(int questLevel) {
      this.questLevel = questLevel;
   }

   @Generated
   public List<String> getCustomRewardsList() {
      return this.customRewardsList;
   }

   @Generated
   public void setCustomRewardsList(List<String> customRewardsList) {
      this.customRewardsList = customRewardsList;
   }

   @Generated
   public String getQuestAcceptPermission() {
      return this.questAcceptPermission;
   }

   @Generated
   public void setQuestAcceptPermission(String questAcceptPermission) {
      this.questAcceptPermission = questAcceptPermission;
   }

   @Generated
   public List<String> getQuestAcceptPermissions() {
      return this.questAcceptPermissions;
   }

   @Generated
   public void setQuestAcceptPermissions(List<String> questAcceptPermissions) {
      this.questAcceptPermissions = questAcceptPermissions;
   }

   @Generated
   public String getQuestLockoutPermission() {
      return this.questLockoutPermission;
   }

   @Generated
   public int getQuestLockoutMinutes() {
      return this.questLockoutMinutes;
   }

   @Generated
   public void setQuestLockoutMinutes(int questLockoutMinutes) {
      this.questLockoutMinutes = questLockoutMinutes;
   }

   @Generated
   public String getQuestName() {
      return this.questName;
   }

   @Generated
   public void setQuestName(String questName) {
      this.questName = questName;
   }

   @Generated
   public List<String> getQuestLore() {
      return this.questLore;
   }

   @Generated
   public void setQuestLore(List<String> questLore) {
      this.questLore = questLore;
   }

   @Generated
   public List<String> getQuestCompleteDialog() {
      return this.questCompleteDialog;
   }

   @Generated
   public void setQuestCompleteDialog(List<String> questCompleteDialog) {
      this.questCompleteDialog = questCompleteDialog;
   }

   @Generated
   public List<String> getQuestCompleteCommands() {
      return this.questCompleteCommands;
   }

   @Generated
   public void setQuestCompleteCommands(List<String> questCompleteCommands) {
      this.questCompleteCommands = questCompleteCommands;
   }

   @Generated
   public String getTurnInNPC() {
      return this.turnInNPC;
   }

   @Generated
   public void setTurnInNPC(String turnInNPC) {
      this.turnInNPC = turnInNPC;
   }

   @Generated
   public boolean isTrackable() {
      return this.trackable;
   }

   @Generated
   public void setTrackable(boolean trackable) {
      this.trackable = trackable;
   }

   @Generated
   public String getQuestAcceptSound() {
      return this.questAcceptSound;
   }

   @Generated
   public void setQuestAcceptSound(String questAcceptSound) {
      this.questAcceptSound = questAcceptSound;
   }

   @Generated
   public String getQuestCompleteSound() {
      return this.questCompleteSound;
   }

   @Generated
   public void setQuestCompleteSound(String questCompleteSound) {
      this.questCompleteSound = questCompleteSound;
   }
}
