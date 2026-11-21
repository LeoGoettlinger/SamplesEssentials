package com.magmaguy.elitemobs.npcs;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.NPCEntityRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.StackedText;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.npcs.chatter.NPCChatBubble;
import com.magmaguy.elitemobs.npcs.scripts.NPCScriptManager;
import com.magmaguy.elitemobs.npcs.scripts.ScriptableNPC;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NPCEntity implements PersistentObject, PersistentMovingEntity {
   private static final float NAMEPLATE_SCALE = 1.0F;
   private static final double NAMEPLATE_LINE_GAP = 0.06;
   private static final ArrayListMultimap<String, NPCEntity.InstancedNPCContainer> instancedNPCEntities = ArrayListMultimap.create();
   public NPCsConfigFields npCsConfigFields;
   private final UUID uuid = UUID.randomUUID();
   private boolean isInstancedDuplicate = false;
   private PersistentObjectHandler persistentObjectHandler;
   private Villager villager = null;
   private Location spawnLocation;
   private boolean isTalking = false;
   private StackedText nameplate;
   private List<String> nameplateLines;
   private int nameplateLineCount;
   private static BukkitTask nameplateTask;
   private boolean isDisguised = false;
   private String locationString;
   private CustomModel customModel = null;
   private final List<ScriptInstance> scriptInstances = new ArrayList<>();
   private ScriptableNPC scriptableNPC;

   public NPCEntity(NPCsConfigFields npCsConfigFields, String locationString) {
      this.npCsConfigFields = npCsConfigFields;
      this.locationString = locationString;
      if (npCsConfigFields.isEnabled()) {
         if (npCsConfigFields.isInstanced()) {
            Location location = ConfigurationLocation.serialize(locationString);
            String blueprintWorldName = locationString.split(",")[0];
            instancedNPCEntities.put(blueprintWorldName, new NPCEntity.InstancedNPCContainer(location, this));
         } else if (locationString != null && !locationString.equalsIgnoreCase("null")) {
            this.setSpawnLocation();
            this.spawn();
            this.persistentObjectHandler = new PersistentObjectHandler(this);
         }
      }
   }

   public NPCEntity(NPCsConfigFields npCsConfigFields, Location location) {
      this.npCsConfigFields = npCsConfigFields;
      this.isInstancedDuplicate = true;
      if (npCsConfigFields.isEnabled()) {
         this.spawnLocation = location;
         this.spawn();
         this.persistentObjectHandler = new PersistentObjectHandler(this);
      }
   }

   public NPCEntity(Location location) {
      this(location, "travelling_merchant.yml");
   }

   public NPCEntity(Location location, String filename) {
      this.npCsConfigFields = NPCsConfig.getNpcEntities().get(filename);
      if (this.npCsConfigFields != null
         && this.npCsConfigFields.isEnabled()
         && location.getWorld() != null
         && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
         Location potentialLocation = location.clone();
         potentialLocation.add(potentialLocation.getDirection().normalize()).setY(location.getY());
         if (location.getWorld().isChunkLoaded(potentialLocation.getBlockX() >> 4, potentialLocation.getBlockZ() >> 4)
            && NonSolidBlockTypes.isPassthrough(potentialLocation.getBlock().getType())) {
            this.spawnLocation = potentialLocation;
         } else {
            this.spawnLocation = location.clone();
         }

         this.spawnLocation.setDirection(this.spawnLocation.getDirection().multiply(-1));
         this.spawn();
      }
   }

   public static void shutdown() {
      if (nameplateTask != null) {
         nameplateTask.cancel();
      }

      nameplateTask = null;
      EntityTracker.getNpcEntities().values().forEach(NPCEntity::removeNameplate);
      instancedNPCEntities.clear();
   }

   public static void initializeInstancedNPCs(String blueprintWorldName, World newWorld, int playerCount, DungeonInstance dungeonInstance) {
      for (NPCEntity.InstancedNPCContainer instancedNPCContainer : instancedNPCEntities.get(blueprintWorldName)) {
         Location newLocation = instancedNPCContainer.getLocation().clone();
         newLocation.setWorld(newWorld);
         new NPCEntity(instancedNPCContainer.getNpcEntity().getNPCsConfigFields(), newLocation);
      }
   }

   public static void initializeNPCs(NPCsConfigFields npCsConfigFields) {
      if (npCsConfigFields.getLocations() != null && !npCsConfigFields.getLocations().isEmpty()) {
         for (String locationString : npCsConfigFields.getLocations()) {
            new NPCEntity(npCsConfigFields, locationString);
         }
      } else if (npCsConfigFields.getSpawnLocation() != null && !npCsConfigFields.getSpawnLocation().isEmpty()) {
         new NPCEntity(npCsConfigFields, npCsConfigFields.getSpawnLocation());
      }
   }

   public static boolean hasLoadedNPC(NPCsConfigFields npCsConfigFields, String locationString) {
      if (locationString == null) {
         return false;
      } else {
         for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values()) {
            if (npcEntity.getNPCsConfigFields() == npCsConfigFields && locationString.equals(npcEntity.locationString) && npcEntity.isValid()) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean referencesWorld(World world) {
      if (world == null) {
         return false;
      } else {
         return this.villager != null && world.equals(this.villager.getWorld())
            ? true
            : this.spawnLocation != null && world.equals(this.spawnLocation.getWorld());
      }
   }

   public void remove(RemovalReason removalReason) {
      String removedLocationString = this.locationString;
      NPCEntityRemoveEvent npcEntityRemoveEvent = new NPCEntityRemoveEvent(this.villager, this, removalReason);
      if (this.villager != null) {
         new EventCaller(npcEntityRemoveEvent);
      }

      this.runScripts(ScriptableNPC.ON_REMOVE, npcEntityRemoveEvent, null);
      this.shutdownScriptInstances();
      this.removeNameplate();
      if (this.villager != null) {
         if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            DisguiseEntity.undisguise(this.villager);
         }

         this.villager.remove();
         this.villager = null;
      }

      EntityTracker.getNpcEntities().remove(this.uuid);
      if (removalReason.equals(RemovalReason.WORLD_UNLOAD) && this.isInstancedDuplicate) {
         this.persistentObjectHandler.remove();
      } else {
         if (removalReason.equals(RemovalReason.REMOVE_COMMAND)) {
            if (this.npCsConfigFields.getLocations() != null && !this.npCsConfigFields.getLocations().isEmpty()) {
               this.npCsConfigFields.removeNPC(removedLocationString);
               this.locationString = null;
               this.spawnLocation = null;
            } else {
               this.npCsConfigFields.setEnabled(false);
               this.spawnLocation = null;
            }

            this.removeLoadedDuplicates(removedLocationString);
            if (this.persistentObjectHandler != null) {
               this.persistentObjectHandler.remove();
            }
         } else if (this.persistentObjectHandler != null) {
            if (!removalReason.equals(RemovalReason.SHUTDOWN)) {
               this.persistentObjectHandler.updatePersistentLocation(this.getPersistentLocation());
            } else {
               this.persistentObjectHandler.remove();
            }
         }
      }
   }

   private void removeLoadedDuplicates(String removedLocationString) {
      if (removedLocationString != null) {
         for (NPCEntity npcEntity : new ArrayList<>(EntityTracker.getNpcEntities().values())) {
            if (npcEntity != this && npcEntity.getNPCsConfigFields() == this.npCsConfigFields && removedLocationString.equals(npcEntity.locationString)) {
               npcEntity.removeDuplicateInstance();
            }
         }
      }
   }

   private void removeDuplicateInstance() {
      NPCEntityRemoveEvent npcEntityRemoveEvent = new NPCEntityRemoveEvent(this.villager, this, RemovalReason.REMOVE_COMMAND);
      if (this.villager != null) {
         new EventCaller(npcEntityRemoveEvent);
      }

      this.runScripts(ScriptableNPC.ON_REMOVE, npcEntityRemoveEvent, null);
      this.shutdownScriptInstances();
      this.removeNameplate();
      if (this.villager != null) {
         this.villager.remove();
         this.villager = null;
      }

      EntityTracker.getNpcEntities().remove(this.uuid);
      if (this.persistentObjectHandler != null) {
         this.persistentObjectHandler.remove();
         this.persistentObjectHandler = null;
      }

      this.locationString = null;
      this.spawnLocation = null;
   }

   private void spawn() {
      PatrolService.registerPendingNpc(this);
      Location effectiveSpawnLocation = PatrolService.materializationLocation(this).orElse(this.spawnLocation);
      if (effectiveSpawnLocation != null && effectiveSpawnLocation.getWorld() != null && PatrolService.canMaterialize(this, effectiveSpawnLocation)) {
         if (this.villager == null || !this.villager.isValid()) {
            if (!this.npCsConfigFields.getInteractionType().equals(NPCInteractions.NPCInteractionType.SCROLL_APPLIER)
               || ItemSettingsConfig.isUseEliteItemScrolls()) {
               if (EliteMobs.worldGuardIsEnabled) {
                  WorldGuardSpawnEventBypasser.forceSpawn();
               }

               String displayName = ChatColorConverter.convert(this.npCsConfigFields.getName());
               boolean disguiseQueued = this.queueDisguise(displayName);
               this.villager = (Villager)effectiveSpawnLocation.getWorld().spawn(effectiveSpawnLocation, Villager.class, villagerInstance -> {
                  PersistentTagger.tagNPC(villagerInstance, this.uuid);
                  villagerInstance.setAI(false);
                  villagerInstance.setCollidable(false);
                  villagerInstance.setPersistent(false);
                  villagerInstance.setRemoveWhenFarAway(false);
                  villagerInstance.setCustomName(displayName);
                  villagerInstance.setCustomNameVisible(true);
                  villagerInstance.setProfession(this.npCsConfigFields.getProfession());
                  AttributeManager.setAttribute(villagerInstance, "generic_scale", this.npCsConfigFields.getScale());
               });
               if (CustomModel.customModelsEnabled()
                  && this.getNPCsConfigFields().getCustomModel() != null
                  && !this.getNPCsConfigFields().getCustomModel().isEmpty()
                  && CustomModel.modelExists(this.getNPCsConfigFields().getCustomModel())) {
                  this.setCustomModel(this.villager);
               } else if (disguiseQueued) {
                  DisguiseEntity.setDisguiseNameVisibility(true, this.villager, displayName);
                  this.isDisguised = true;
               } else {
                  this.setDisguise(this.villager);
               }

               EntityTracker.registerNPCEntity(this);
               if (this.villager != null && this.villager.isValid()) {
                  this.initializeScripts();
                  this.runScripts(ScriptHook.ON_SPAWN, null, null);
                  this.initializeNameplate();
                  this.setTimeout();
               }
            }
         }
      }
   }

   private void initializeScripts() {
      this.shutdownScriptInstances();
      if (this.npCsConfigFields.getScripts() != null && !this.npCsConfigFields.getScripts().isEmpty()) {
         List<ScriptDefinition> definitions = new ArrayList<>();

         for (String scriptFileName : this.npCsConfigFields.getScripts()) {
            if (scriptFileName != null && !scriptFileName.isBlank()) {
               String fileName = scriptFileName.toLowerCase(Locale.ROOT).endsWith(".lua") ? scriptFileName : scriptFileName + ".lua";
               ScriptDefinition definition = NPCScriptManager.getDefinition(fileName);
               if (definition == null) {
                  Logger.warn("NPC " + this.npCsConfigFields.getFilename() + " references missing NPC Lua script " + scriptFileName + ".");
               } else {
                  definitions.add(definition);
               }
            }
         }

         definitions.sort(Comparator.comparingInt(ScriptDefinition::getPriority));
         this.scriptableNPC = new ScriptableNPC(this);

         for (ScriptDefinition definition : definitions) {
            try {
               this.scriptInstances.add(new ScriptInstance(definition, this.scriptableNPC));
            } catch (Exception var6) {
               Logger.warn("Failed to initialize NPC Lua script " + definition.getFileName() + " for NPC " + this.npCsConfigFields.getFilename() + ".");
               var6.printStackTrace();
            }
         }
      }
   }

   private void shutdownScriptInstances() {
      for (ScriptInstance scriptInstance : new ArrayList<>(this.scriptInstances)) {
         scriptInstance.shutdown();
      }

      this.scriptInstances.clear();
      this.scriptableNPC = null;
   }

   public void runScripts(ScriptHook hook, Event event, LivingEntity actor) {
      for (ScriptInstance scriptInstance : new ArrayList<>(this.scriptInstances)) {
         if (scriptInstance.isClosed()) {
            this.scriptInstances.remove(scriptInstance);
         } else {
            scriptInstance.handleEvent(hook, event, null, actor);
         }
      }
   }

   private void setDisguise(LivingEntity livingEntity) {
      if (this.npCsConfigFields.getDisguise() != null) {
         if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            try {
               DisguiseEntity.disguise(
                  this.npCsConfigFields.getDisguise(), livingEntity, this.npCsConfigFields.getCustomDisguiseData(), this.npCsConfigFields.getFilename()
               );
               DisguiseEntity.setDisguiseNameVisibility(true, livingEntity, ChatColorConverter.convert(this.npCsConfigFields.getName()));
               livingEntity.setCustomNameVisible(true);
               this.isDisguised = true;
            } catch (Exception var3) {
               Logger.warn("Failed to load LibsDisguises disguise correctly!");
               var3.printStackTrace();
            }
         }
      }
   }

   private boolean queueDisguise(String displayName) {
      if (this.npCsConfigFields.getDisguise() == null) {
         return false;
      } else if (CustomModel.customModelsEnabled()
         && this.getNPCsConfigFields().getCustomModel() != null
         && !this.getNPCsConfigFields().getCustomModel().isEmpty()
         && CustomModel.modelExists(this.getNPCsConfigFields().getCustomModel())) {
         return false;
      } else {
         return !Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")
            ? false
            : DisguiseEntity.disguiseNext(
               this.npCsConfigFields.getDisguise(), displayName, true, this.npCsConfigFields.getCustomDisguiseData(), this.npCsConfigFields.getFilename()
            );
      }
   }

   private void setCustomModel(LivingEntity livingEntity) {
      this.customModel = CustomModel.generateCustomModel(
         livingEntity,
         this.getNPCsConfigFields().getCustomModel(),
         ChatColorConverter.convert(this.getNPCsConfigFields().getName()),
         (player, modeledEntity) -> NPCInteractions.handleNPCInteraction(player, this),
         (player, modeledEntity) -> NPCInteractions.handleNPCInteraction(player, this)
      );
      if (this.customModel != null) {
         this.customModel.setSyncMovement(this.getNPCsConfigFields().isSyncMovement() || this.getNPCsConfigFields().getPatrolRoute() != null);
      }
   }

   public void refreshCustomModel() {
      if (this.villager != null && this.villager.isValid()) {
         if (CustomModel.customModelsEnabled()) {
            if (this.getNPCsConfigFields().getCustomModel() != null && !this.getNPCsConfigFields().getCustomModel().isEmpty()) {
               if (CustomModel.modelExists(this.getNPCsConfigFields().getCustomModel())) {
                  this.customModel = null;
                  this.setCustomModel(this.villager);
                  this.initializeNameplate();
               }
            }
         }
      }
   }

   public Villager getVillager() {
      return this.villager;
   }

   public boolean isValid() {
      return this.villager == null ? false : this.villager.isValid();
   }

   public NPCsConfigFields getNPCsConfigFields() {
      return this.npCsConfigFields;
   }

   public String getLocationString() {
      return this.locationString;
   }

   public void rebindPatrolConfig(NPCsConfigFields fields) {
      Objects.requireNonNull(fields, "fields");
      if (fields.getPatrolRoute() != null) {
         this.enablePatrolMovementSync();
      }

      this.npCsConfigFields = fields;
   }

   public void enablePatrolMovementSync() {
      if (this.customModel != null) {
         this.customModel.setSyncMovement(true);
      }
   }

   public Location getSpawnLocation() {
      return this.spawnLocation;
   }

   public boolean setSpawnLocation() {
      Location location = ConfigurationLocation.serialize(this.locationString);
      if (location == null) {
         return false;
      } else {
         this.spawnLocation = location;
         return true;
      }
   }

   public boolean getIsTalking() {
      return this.isTalking;
   }

   @Override
   public void chunkLoad() {
      this.spawn();
   }

   @Override
   public void chunkUnload() {
      this.remove(RemovalReason.CHUNK_UNLOAD);
   }

   @Override
   public void worldLoad(World world) {
      this.setSpawnLocation();
      if (PatrolService.hasConfiguredPatrol(this)) {
         this.spawn();
      }
   }

   @Override
   public void worldUnload() {
      this.spawnLocation.setWorld(null);
      this.remove(RemovalReason.WORLD_UNLOAD);
   }

   @Override
   public Location getPersistentLocation() {
      return PatrolService.materializationLocation(this).orElse(this.getSpawnLocation());
   }

   public void updatePatrolPersistentLocation(Location location) {
      if (location != null && this.persistentObjectHandler != null) {
         this.persistentObjectHandler.updatePersistentLocation(location.clone());
      }
   }

   public void syncPatrolVisuals() {
      if (this.villager != null && this.villager.isValid()) {
         this.updateNameplateLocation();
      }
   }

   @Override
   public String getWorldName() {
      if (this.getSpawnLocation() != null && this.getSpawnLocation().getWorld() != null) {
         return this.getSpawnLocation().getWorld().getName();
      } else {
         String world = null;
         if (this.locationString != null && !this.locationString.isEmpty()) {
            world = this.locationString.split(",")[0];
         }

         return world;
      }
   }

   private void initializeNameplate() {
      this.removeNameplate();
      this.refreshNameplate();
   }

   private boolean isGamblingDenOwner() {
      return "gambling_den_owner.yml".equals(this.npCsConfigFields.getFilename());
   }

   private void refreshNameplate() {
      List<String> lines = new ArrayList<>();
      if (this.isGamblingDenOwner() && GamblingConfig.isGamblingEnabled()) {
         lines.add(ChatColorConverter.convert(GamblingConfig.getHouseEarningsLabel() + GamblingEconomyHandler.getFormattedHouseEarnings()));
      }

      String role = ChatColorConverter.convert(this.npCsConfigFields.getRole());
      String name = ChatColorConverter.convert(this.npCsConfigFields.getName());
      if (!role.isBlank()) {
         lines.add(role);
      }

      if (!name.isBlank()) {
         lines.add(name);
      }

      if (!lines.equals(this.nameplateLines)) {
         this.nameplateLineCount = lines.stream().mapToInt(line -> line.split("\\R", -1).length).sum();
         if (this.customModel != null && this.customModel.setNpcNameLines(lines, 1.0F, 0.06)) {
            this.villager.setCustomNameVisible(false);
            this.villager.setCustomName(null);
            this.nameplateLines = List.copyOf(lines);
         } else {
            List<String> completeLines = List.copyOf(lines);
            if (this.customModel != null) {
               if (!name.isBlank()) {
                  lines.remove(lines.size() - 1);
               }
            } else {
               this.villager.setCustomNameVisible(false);
               this.villager.setCustomName(null);
               if (this.isDisguised) {
                  DisguiseEntity.setDisguiseNameVisibility(false, this.villager, name);
               }
            }

            if (this.nameplate == null) {
               this.nameplate = new StackedText();
               this.nameplate.setScale(1.0F);
               this.nameplate.setLineGap(0.06);
            }

            this.nameplate.setLines(lines);
            this.nameplateLines = completeLines;
            this.updateNameplateLocation();
         }
      }
   }

   private void updateNameplateLocation() {
      if (this.nameplate != null && this.villager != null && this.villager.isValid()) {
         this.nameplate.move(this.getNameplateLocation());
      }
   }

   private Location getNameplateLocation() {
      if (this.customModel != null) {
         return this.villager.getLocation().add(0.0, 2.3, 0.0);
      } else {
         double height = this.isDisguised ? DisguiseEntity.getDisguiseHeight(this.villager, this.npCsConfigFields.getScale()) : this.villager.getHeight();
         return this.villager.getLocation().add(0.0, height + 0.08, 0.0);
      }
   }

   public Location getQuestIndicatorLocation() {
      double stackHeight = (this.nameplateLineCount * 0.225 + Math.max(0, this.nameplateLineCount - 1) * 0.06) * 1.0;
      return this.getNameplateLocation().add(0.0, stackHeight - 0.08, 0.0);
   }

   private void removeNameplate() {
      if (this.nameplate != null) {
         this.nameplate.remove();
      }

      this.nameplate = null;
      this.nameplateLines = null;
   }

   public static void startNameplates() {
      if (nameplateTask == null) {
         nameplateTask = (new BukkitRunnable() {
               private int refreshCounter;

               public void run() {
                  boolean refreshEarnings = ++this.refreshCounter >= 10;
                  if (refreshEarnings) {
                     this.refreshCounter = 0;
                  }

                  List<? extends Player> players = List.copyOf(Bukkit.getOnlinePlayers());

                  for (NPCEntity npc : EntityTracker.getNpcEntities().values()) {
                     if (npc.isValid()) {
                        if (refreshEarnings && npc.isGamblingDenOwner()) {
                           npc.refreshNameplate();
                        }

                        if (npc.nameplate != null) {
                           npc.updateNameplateLocation();
                           Location location = npc.villager.getLocation();
                           npc.nameplate
                              .syncViewers(
                                 players.stream()
                                    .filter(
                                       player -> player.getWorld().equals(location.getWorld())
                                          && player.getLocation().distanceSquared(location) <= 4096.0
                                          && player.canSee(npc.villager)
                                    )
                                    .toList()
                              );
                        }
                     }
                  }
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 1L, 2L);
      }
   }

   public void startTalkingCooldown() {
      this.isTalking = true;
      (new BukkitRunnable() {
         public void run() {
            NPCEntity.this.isTalking = false;
         }
      }).runTaskLater(MetadataHandler.PLUGIN, 60L);
   }

   public void setTimeout() {
      if (!(this.npCsConfigFields.getTimeout() <= 0.0)) {
         (new BukkitRunnable() {
            public void run() {
               NPCEntity.this.remove(RemovalReason.NPC_TIMEOUT);
            }
         }).runTaskLater(MetadataHandler.PLUGIN, (long)(this.npCsConfigFields.getTimeout() * 20.0 * 60.0));
      }
   }

   public void sayGreeting(Player player) {
      new NPCChatBubble(this.selectString(this.npCsConfigFields.getGreetings()), this, player);
   }

   public void sayDialog(Player player) {
      new NPCChatBubble(this.selectString(this.npCsConfigFields.getDialog()), this, player);
   }

   public void sayFarewell(Player player) {
      new NPCChatBubble(this.selectString(this.npCsConfigFields.getFarewell()), this, player);
   }

   private String selectString(List<String> strings) {
      return strings != null && !strings.isEmpty() ? strings.get(ThreadLocalRandom.current().nextInt(strings.size())) : null;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public CustomModel getCustomModel() {
      return this.customModel;
   }

   private class InstancedNPCContainer {
      private final Location location;
      private final NPCEntity npcEntity;

      public InstancedNPCContainer(Location location, NPCEntity npcEntity) {
         this.location = location;
         this.npcEntity = npcEntity;
      }

      @Generated
      public Location getLocation() {
         return this.location;
      }

      @Generated
      public NPCEntity getNpcEntity() {
         return this.npcEntity;
      }
   }
}
