package com.magmaguy.elitemobs.config.npcs;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.VersionChecker;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolRoute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager.Profession;

public class NPCsConfigFields extends CustomConfigFields {
   public String noPreviousLocationMessage;
   private String name = "";
   private String role = "";
   private Profession profession = null;
   private String spawnLocation;
   private List<String> greetings = new ArrayList<>();
   private List<String> dialog = new ArrayList<>();
   private List<String> farewell = new ArrayList<>();
   private boolean canTalk = true;
   private double activationRadius = 3.0;
   private NPCInteractions.NPCInteractionType interactionType = NPCInteractions.NPCInteractionType.NONE;
   private double timeout = 0.0;
   private List<String> questFilenames = null;
   private String disguise = null;
   private String customDisguiseData = null;
   private String customModel = null;
   private String arenaFilename;
   private String classRoot;
   private List<String> locations = new ArrayList<>();
   private String command;
   private boolean instanced = false;
   private double scale = 1.0;
   private boolean syncMovement = true;
   private List<String> scripts = new ArrayList<>();
   private List<String> transportRoutes = new ArrayList<>();
   private PatrolRoute patrolRoute;
   private double patrolPauseNearPlayersRadius;
   private boolean patrolFaceNearbyPlayers;

   public NPCsConfigFields(
      String fileName,
      boolean isEnabled,
      String name,
      String role,
      Profession profession,
      String location,
      List<String> greetings,
      List<String> dialog,
      List<String> farewell,
      boolean canTalk,
      double activationRadius,
      NPCInteractions.NPCInteractionType interactionType
   ) {
      super(fileName, isEnabled);
      this.name = name;
      this.role = role;
      this.profession = profession;
      this.spawnLocation = location;
      this.greetings = greetings;
      this.dialog = dialog;
      this.farewell = farewell;
      this.canTalk = canTalk;
      this.activationRadius = activationRadius;
      this.interactionType = interactionType;
   }

   public NPCsConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   public boolean addSpawnLocation(String spawnLocation) {
      if (spawnLocation != null && !spawnLocation.isEmpty()) {
         if (this.locations == null) {
            this.locations = new ArrayList<>();
         }

         if (this.locations.contains(spawnLocation)) {
            return false;
         } else {
            this.locations.add(spawnLocation);
            this.saveSpawnLocations();
            return true;
         }
      } else {
         return false;
      }
   }

   public void setSpawnLocation(String spawnLocation) {
      this.addSpawnLocation(spawnLocation);
   }

   private void saveSpawnLocations() {
      try {
         this.getWritableFileConfiguration().set("spawnLocations", this.locations);
         ConfigurationEngine.fileSaverCustomValues(this.getWritableFileConfiguration(), this.file);
      } catch (Exception var2) {
         Logger.warn("Attempted to update the location status for an NPC with no config file! Did you delete it during runtime?");
      }
   }

   protected void migrateLegacyDefaults() {
   }

   protected final void migrateStringIfExact(String path, String legacyValue, String replacement) {
      if (Objects.equals(this.fileConfiguration.getString(path), legacyValue)) {
         this.fileConfiguration.set(path, replacement);
      }
   }

   protected final void migrateDisguiseAliasIfDefault(String legacyDisguise) {
      if (Objects.equals(this.fileConfiguration.getString("disguise"), legacyDisguise)
         && Objects.equals(this.fileConfiguration.getString("customDisguiseData"), this.customDisguiseData)) {
         this.fileConfiguration.set("disguise", this.disguise);
      }
   }

   @Override
   public void processConfigFields() {
      this.migrateLegacyDefaults();
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.name = this.translatable(this.filename, "name", this.processString("name", this.name, "", true));
      this.role = this.translatable(this.filename, "role", this.processString("role", this.role, "", true));

      try {
         if (Bukkit.getServer() == null) {
            this.processString("profession", this.professionConfigName(), "nitwit", false);
         } else if (VersionChecker.serverVersionOlderThan(21, 4)) {
            this.profession = Profession.valueOf(this.processString("profession", this.professionConfigName(), "NITWIT", false).toUpperCase(Locale.ROOT));
         } else {
            String professionString = this.processString("profession", this.professionConfigName(), "nitwit", false);

            for (Profession value : Profession.values()) {
               if (value.getKey().getKey().toLowerCase(Locale.ROOT).equals(professionString.toLowerCase(Locale.ROOT))) {
                  this.profession = value;
                  break;
               }
            }
         }
      } catch (IncompatibleClassChangeError var7) {
      } catch (Exception var8) {
         Logger.warn("NPC in configuration file " + this.filename + " has an invalid profession!");
         var8.printStackTrace();
      }

      this.spawnLocation = this.processString("spawnLocation", this.spawnLocation, null, true);
      this.locations = this.processStringList("spawnLocations", this.locations, null, false);
      this.greetings = this.translatable(this.filename, "greetings", this.processStringList("greetings", this.greetings, new ArrayList<>(), true));
      this.dialog = this.translatable(this.filename, "dialog", this.processStringList("dialog", this.dialog, new ArrayList<>(), true));
      this.farewell = this.translatable(this.filename, "farewell", this.processStringList("farewell", this.farewell, new ArrayList<>(), true));
      this.canTalk = this.processBoolean("canTalk", this.canTalk, true, true);
      this.activationRadius = this.processDouble("activationRadius", this.activationRadius, 3.0, true);
      this.interactionType = this.processEnum(
         "interactionType", this.interactionType, NPCInteractions.NPCInteractionType.NONE, NPCInteractions.NPCInteractionType.class, true
      );
      this.timeout = this.processDouble("timeout", this.timeout, 0.0, false);
      this.noPreviousLocationMessage = this.translatable(
         this.filename, "noPreviousLocationMessage", this.processString("noPreviousLocationMessage", this.noPreviousLocationMessage, "", false)
      );
      this.questFilenames = this.processStringList("questFileName", this.questFilenames, new ArrayList<>(), false);
      this.disguise = this.processString("disguise", this.disguise, null, false);
      this.customDisguiseData = this.processString("customDisguiseData", this.customDisguiseData, null, false);
      this.customModel = this.processString("customModel", this.customModel, null, false);
      this.arenaFilename = this.processString("arena", this.arenaFilename, null, false);
      this.classRoot = this.processString("classRoot", this.classRoot, null, false);
      this.command = this.processString("command", this.command, null, false);
      this.instanced = this.processBoolean("instanced", this.instanced, false, false);
      this.scale = this.processDouble("scale", this.scale, 1.0, false);
      this.syncMovement = this.processBoolean("syncMovement", this.syncMovement, false, true);
      this.scripts = this.processStringList("scripts", this.scripts, new ArrayList<>(), false);
      this.transportRoutes = this.processStringList("transportRoutes", this.transportRoutes, new ArrayList<>(), false);
      this.patrolPauseNearPlayersRadius = this.processDouble("patrol.pauseNearPlayersRadius", this.patrolPauseNearPlayersRadius, 0.0, false);
      this.patrolFaceNearbyPlayers = this.processBoolean("patrol.faceNearbyPlayers", this.patrolFaceNearbyPlayers, false, false);
      if (!Double.isFinite(this.patrolPauseNearPlayersRadius) || this.patrolPauseNearPlayersRadius < 0.0) {
         Logger.warn("Invalid patrol.pauseNearPlayersRadius in " + this.filename + ": expected a finite, non-negative radius. Disabling proximity pauses.");
         this.patrolPauseNearPlayersRadius = 0.0;
      }

      try {
         this.patrolRoute = PatrolRoute.parse(this.fileConfiguration);
      } catch (IllegalArgumentException var6) {
         this.patrolRoute = null;
         Logger.warn("Invalid patrol in " + this.filename + ": " + var6.getMessage());
      }
   }

   public Profession getProfession() {
      return this.profession == null ? Profession.NITWIT : this.profession;
   }

   private String professionConfigName() {
      return this.profession == null ? "nitwit" : this.profession.getKey().getKey();
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.isEnabled = enabled;
      this.getWritableFileConfiguration().set("isEnabled", enabled);

      try {
         ConfigurationEngine.fileSaverCustomValues(this.getWritableFileConfiguration(), this.file);
      } catch (Exception var3) {
         Logger.warn("Attempted to update the enabled status for an NPC with no config file! Did you delete it during runtime?");
      }
   }

   public void removeNPC(String locationString) {
      if (this.locations != null) {
         this.locations.removeIf(entry -> Objects.equals(entry, locationString));
         this.saveSpawnLocations();
      }
   }

   public void setPatrolRoute(PatrolRoute route) {
      this.patrolRoute = route;
   }

   public boolean reloadPatrolRoute() {
      try {
         this.patrolRoute = PatrolRoute.parse(this.getWritableFileConfiguration());
         return this.patrolRoute != null;
      } catch (IllegalArgumentException var2) {
         this.patrolRoute = null;
         Logger.warn("Invalid patrol in " + this.filename + ": " + var2.getMessage());
         return false;
      }
   }

   @Generated
   public String getNoPreviousLocationMessage() {
      return this.noPreviousLocationMessage;
   }

   @Generated
   public void setNoPreviousLocationMessage(String noPreviousLocationMessage) {
      this.noPreviousLocationMessage = noPreviousLocationMessage;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
   }

   @Generated
   public String getRole() {
      return this.role;
   }

   @Generated
   public void setRole(String role) {
      this.role = role;
   }

   @Generated
   public void setProfession(Profession profession) {
      this.profession = profession;
   }

   @Generated
   public String getSpawnLocation() {
      return this.spawnLocation;
   }

   @Generated
   public List<String> getGreetings() {
      return this.greetings;
   }

   @Generated
   public void setGreetings(List<String> greetings) {
      this.greetings = greetings;
   }

   @Generated
   public List<String> getDialog() {
      return this.dialog;
   }

   @Generated
   public void setDialog(List<String> dialog) {
      this.dialog = dialog;
   }

   @Generated
   public List<String> getFarewell() {
      return this.farewell;
   }

   @Generated
   public void setFarewell(List<String> farewell) {
      this.farewell = farewell;
   }

   @Generated
   public boolean isCanTalk() {
      return this.canTalk;
   }

   @Generated
   public void setCanTalk(boolean canTalk) {
      this.canTalk = canTalk;
   }

   @Generated
   public double getActivationRadius() {
      return this.activationRadius;
   }

   @Generated
   public void setActivationRadius(double activationRadius) {
      this.activationRadius = activationRadius;
   }

   @Generated
   public NPCInteractions.NPCInteractionType getInteractionType() {
      return this.interactionType;
   }

   @Generated
   public void setInteractionType(NPCInteractions.NPCInteractionType interactionType) {
      this.interactionType = interactionType;
   }

   @Generated
   public double getTimeout() {
      return this.timeout;
   }

   @Generated
   public void setTimeout(double timeout) {
      this.timeout = timeout;
   }

   @Generated
   public List<String> getQuestFilenames() {
      return this.questFilenames;
   }

   @Generated
   public void setQuestFilenames(List<String> questFilenames) {
      this.questFilenames = questFilenames;
   }

   @Generated
   public String getDisguise() {
      return this.disguise;
   }

   @Generated
   public void setDisguise(String disguise) {
      this.disguise = disguise;
   }

   @Generated
   public String getCustomDisguiseData() {
      return this.customDisguiseData;
   }

   @Generated
   public void setCustomDisguiseData(String customDisguiseData) {
      this.customDisguiseData = customDisguiseData;
   }

   @Generated
   public String getCustomModel() {
      return this.customModel;
   }

   @Generated
   public void setCustomModel(String customModel) {
      this.customModel = customModel;
   }

   @Generated
   public String getArenaFilename() {
      return this.arenaFilename;
   }

   @Generated
   public void setArenaFilename(String arenaFilename) {
      this.arenaFilename = arenaFilename;
   }

   @Generated
   public String getClassRoot() {
      return this.classRoot;
   }

   @Generated
   public void setClassRoot(String classRoot) {
      this.classRoot = classRoot;
   }

   @Generated
   public List<String> getLocations() {
      return this.locations;
   }

   @Generated
   public void setLocations(List<String> locations) {
      this.locations = locations;
   }

   @Generated
   public String getCommand() {
      return this.command;
   }

   @Generated
   public void setCommand(String command) {
      this.command = command;
   }

   @Generated
   public boolean isInstanced() {
      return this.instanced;
   }

   @Generated
   public void setInstanced(boolean instanced) {
      this.instanced = instanced;
   }

   @Generated
   public double getScale() {
      return this.scale;
   }

   @Generated
   public void setScale(double scale) {
      this.scale = scale;
   }

   @Generated
   public boolean isSyncMovement() {
      return this.syncMovement;
   }

   @Generated
   public void setSyncMovement(boolean syncMovement) {
      this.syncMovement = syncMovement;
   }

   @Generated
   public List<String> getScripts() {
      return this.scripts;
   }

   @Generated
   public void setScripts(List<String> scripts) {
      this.scripts = scripts;
   }

   @Generated
   public List<String> getTransportRoutes() {
      return this.transportRoutes;
   }

   @Generated
   public PatrolRoute getPatrolRoute() {
      return this.patrolRoute;
   }

   @Generated
   public double getPatrolPauseNearPlayersRadius() {
      return this.patrolPauseNearPlayersRadius;
   }

   @Generated
   public void setPatrolPauseNearPlayersRadius(double patrolPauseNearPlayersRadius) {
      this.patrolPauseNearPlayersRadius = patrolPauseNearPlayersRadius;
   }

   @Generated
   public boolean isPatrolFaceNearbyPlayers() {
      return this.patrolFaceNearbyPlayers;
   }

   @Generated
   public void setPatrolFaceNearbyPlayers(boolean patrolFaceNearbyPlayers) {
      this.patrolFaceNearbyPlayers = patrolFaceNearbyPlayers;
   }
}
