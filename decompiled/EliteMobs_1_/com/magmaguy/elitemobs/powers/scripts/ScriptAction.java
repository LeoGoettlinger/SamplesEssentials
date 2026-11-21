package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.InstanceEffectPolicy;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptActionBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.ActionType;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ScriptAction {
   private static final int DEFAULT_BOSS_BAR_DURATION_TICKS = 100;
   private static final Set<UUID> invulnerablePlayers = ConcurrentHashMap.newKeySet();
   private final ScriptActionBlueprint blueprint;
   private final ScriptTargets scriptTargets;
   private final ScriptConditions scriptConditions;
   private final ScriptParticles scriptParticles;
   private final Map<String, ScriptExecutable> eliteScriptMap;
   private final ScriptRuntimeOwner runtimeOwner;
   private final ScriptTargets finalScriptTargets;
   private final ScriptWorldActionExecutor worldActions;
   private final Set<ScriptAction.ScheduledAction> scheduledActions = new HashSet<>();
   private boolean closed;
   private boolean finishDeathActions;
   private static final ThreadLocal<Integer> scriptDamageDepth = ThreadLocal.withInitial(() -> 0);
   private static final int MAX_SCRIPT_DAMAGE_DEPTH = 3;

   public ScriptAction(ScriptActionBlueprint blueprint, Map<String, ScriptExecutable> eliteScriptMap, ScriptRuntimeOwner runtimeOwner) {
      this.blueprint = blueprint;
      this.scriptTargets = new ScriptTargets(blueprint.getScriptTargets(), runtimeOwner);
      this.finalScriptTargets = blueprint.getFinalTarget() != null ? new ScriptTargets(blueprint.getFinalTarget(), runtimeOwner) : null;
      this.scriptConditions = new ScriptConditions(blueprint.getConditionsBlueprint(), runtimeOwner, true);
      this.scriptParticles = new ScriptParticles(blueprint.getScriptParticlesBlueprint());
      this.eliteScriptMap = eliteScriptMap;
      this.runtimeOwner = runtimeOwner;
      this.worldActions = new ScriptWorldActionExecutor(this);
   }

   public void runScript(EliteEntity eliteEntity, LivingEntity directTarget, Event event) {
      if (this.blueprint.getActionType() == null) {
         Logger.warn(
            "Script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "' does not have a valid action! Every action must define a valid action for the script to work."
         );
      } else {
         ScriptActionData scriptActionData = new ScriptActionData(eliteEntity, directTarget, this.scriptTargets, this.runtimeOwner.getScriptZone(), event);
         this.scriptTask(scriptActionData);
      }
   }

   public void runScript(ScriptActionData previousScriptActionData) {
      if (this.blueprint.getActionType() == null) {
         Logger.warn(
            "Script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "' does not have a valid action! Every action must define a valid action for the script to work."
         );
      } else {
         ScriptActionData scriptActionData = new ScriptActionData(this.scriptTargets, this.runtimeOwner.getScriptZone(), previousScriptActionData);
         this.scriptTask(scriptActionData);
      }
   }

   public void runScript(ScriptActionData previousScriptActionData, Location landingLocation) {
      ScriptActionData scriptActionData = new ScriptActionData(this.scriptTargets, this.runtimeOwner.getScriptZone(), previousScriptActionData, landingLocation);
      this.scriptTask(scriptActionData);
   }

   private void scriptTask(final ScriptActionData scriptActionData) {
      if (!this.powerSuppressed(scriptActionData)) {
         this.scriptTargets.cacheTargets(scriptActionData);
         if (this.finalScriptTargets != null) {
            this.finalScriptTargets.cacheTargets(scriptActionData);
         }

         if (this.blueprint.getWait().getValue() > 0) {
            (new ScriptAction.ScheduledAction(scriptActionData) {
               @Override
               protected void runAction() {
                  this.cancel();
                  ScriptAction.this.runScriptTask(scriptActionData);
               }
            }).schedule(this.blueprint.getWait().getValue().intValue(), 1L);
         } else {
            this.runScriptTask(scriptActionData);
         }
      }
   }

   private void runScriptTask(final ScriptActionData scriptActionData) {
      if (!this.powerSuppressed(scriptActionData)) {
         if (this.blueprint.getRepeatEvery().getValue() > 0) {
            (new ScriptAction.ScheduledAction(scriptActionData) {
                  int counter = 0;

                  @Override
                  protected void runAction() {
                     this.counter++;
                     if (ScriptAction.this.blueprint.getTimes().getValue() > 0 && this.counter > ScriptAction.this.blueprint.getTimes().getValue()) {
                        this.cancel();
                     } else if (ScriptAction.this.blueprint.getTimes().getValue() < 0 && !scriptActionData.getEliteEntity().isValid()) {
                        this.cancel();
                     } else {
                        Location entityLocation = scriptActionData.getEliteEntity().getLocation();
                        if (entityLocation == null || entityLocation.getWorld() == null) {
                           this.cancel();
                        } else if (ScriptAction.this.zoneOriginWorldLost(scriptActionData)) {
                           Logger.warn(
                              "Cancelling repeating script action '"
                                 + ScriptAction.this.blueprint.getScriptName()
                                 + "' in '"
                                 + ScriptAction.this.blueprint.getScriptFilename()
                                 + "' — zone shape origin world is null."
                           );
                           this.cancel();
                        } else if (ScriptAction.this.blueprint.getConditionsBlueprint() != null
                           && !ScriptAction.this.scriptConditions.meetsActionConditions(scriptActionData)) {
                           this.cancel();
                        } else {
                           ScriptAction.this.runActions(scriptActionData);
                        }
                     }
                  }
               })
               .schedule(0L, this.blueprint.getRepeatEvery().getValue().intValue());
         } else {
            if (this.blueprint.getConditionsBlueprint() != null && !this.scriptConditions.meetsActionConditions(scriptActionData)) {
               return;
            }

            this.runActions(scriptActionData);
         }
      }
   }

   private boolean zoneOriginWorldLost(ScriptActionData data) {
      if (!this.blueprint.getScriptTargets().isZoneTarget()) {
         return false;
      } else {
         ScriptActionData zoneData = this.scriptTargets.resolveZoneData(data);
         return zoneData.getScriptZone().originWorldLost(zoneData);
      }
   }

   private void runActions(ScriptActionData scriptActionData) {
      if (!this.powerSuppressed(scriptActionData)) {
         if (this.blueprint.getActionType() == null) {
            Logger.warn(
               "Failed to determine action type in script '" + this.blueprint.getScriptName() + "' for file '" + this.blueprint.getScriptFilename() + "'"
            );
         } else {
            switch (this.blueprint.getActionType()) {
               case TELEPORT:
                  this.runTeleport(scriptActionData);
                  break;
               case MESSAGE:
                  this.runMessage(scriptActionData);
                  break;
               case ACTION_BAR_MESSAGE:
                  this.runActionBarMessage(scriptActionData);
                  break;
               case TITLE_MESSAGE:
                  this.runTitleMessage(scriptActionData);
                  break;
               case BOSS_BAR_MESSAGE:
                  this.runBossBarMessage(scriptActionData);
                  break;
               case POTION_EFFECT:
                  this.runPotionEffect(scriptActionData);
                  break;
               case DAMAGE:
                  this.runDamage(scriptActionData);
                  break;
               case SET_ON_FIRE:
                  this.runSetOnFire(scriptActionData);
                  break;
               case VISUAL_FREEZE:
                  this.runVisualFreeze(scriptActionData);
                  break;
               case PLACE_BLOCK:
                  this.runPlaceBlock(scriptActionData);
                  break;
               case RUN_COMMAND_AS_PLAYER:
                  this.runPlayerCommand(scriptActionData);
                  break;
               case RUN_COMMAND_AS_CONSOLE:
                  this.runConsoleCommand(scriptActionData);
                  break;
               case STRIKE_LIGHTNING:
                  this.worldActions.runStrikeLightning(scriptActionData);
                  break;
               case SPAWN_PARTICLE:
                  this.worldActions.runSpawnParticle(scriptActionData);
                  break;
               case SET_MOB_AI:
                  this.worldActions.runSetMobAI(scriptActionData);
                  break;
               case SET_MOB_AWARE:
                  this.worldActions.runSetMobAware(scriptActionData);
                  break;
               case PLAY_SOUND:
                  this.worldActions.runPlaySound(scriptActionData);
                  break;
               case PUSH:
                  this.worldActions.runPush(scriptActionData);
                  break;
               case SUMMON_REINFORCEMENT:
                  this.worldActions.runSummonReinforcement(scriptActionData);
                  break;
               case RUN_SCRIPT:
                  this.runAdditionalScripts(scriptActionData);
                  break;
               case SPAWN_FIREWORKS:
                  this.worldActions.runSpawnFireworks(scriptActionData);
                  break;
               case MAKE_INVULNERABLE:
                  this.worldActions.runMakeInvulnerable(scriptActionData);
                  break;
               case TAG:
                  this.worldActions.runTag(scriptActionData);
                  break;
               case UNTAG:
                  this.worldActions.runUntag(scriptActionData);
                  break;
               case SET_TIME:
                  this.worldActions.runSetTime(scriptActionData);
                  break;
               case SET_WEATHER:
                  this.worldActions.runSetWeather(scriptActionData);
                  break;
               case PLAY_ANIMATION:
                  this.runPlayAnimation(scriptActionData);
                  break;
               case SPAWN_FALLING_BLOCK:
                  this.runSpawnFallingBlock(scriptActionData);
                  break;
               case MODIFY_DAMAGE:
                  this.runModifyDamage(scriptActionData);
                  break;
               case SUMMON_ENTITY:
                  this.runSummonEntity(scriptActionData);
                  break;
               case NAVIGATE:
                  this.runNavigate(scriptActionData);
                  break;
               case SCALE:
                  this.runScale(scriptActionData);
                  break;
               case SET_FACING:
                  this.setFacing(scriptActionData);
                  break;
               case HEAL:
                  this.runHeal(scriptActionData);
                  break;
               case REMOVE_ELITE:
                  this.runRemoveElite(scriptActionData);
                  break;
               default:
                  Logger.warn(
                     "Unknown action type '"
                        + this.blueprint.getActionType()
                        + "' in script '"
                        + this.blueprint.getScriptName()
                        + "' for file '"
                        + this.blueprint.getScriptFilename()
                        + "'"
                  );
            }

            if (!this.blueprint.getActionType().equals(ActionType.RUN_SCRIPT)) {
               this.runAdditionalScripts(scriptActionData);
            }
         }
      }
   }

   private boolean powerSuppressed(ScriptActionData scriptActionData) {
      return scriptActionData == null
         || scriptActionData.getEliteEntity() == null
         || this.closed && !this.canFinishAfterDeath(scriptActionData)
         || scriptActionData.getEliteEntity().getPowerSuppression().isSuppressed();
   }

   private boolean canFinishAfterDeath(ScriptActionData data) {
      return this.finishDeathActions
         && data != null
         && data.originatesFromDeath()
         && !data.repeatsActionInChain()
         && this.sourceWorldIsLoaded(data)
         && (this.blueprint.getRepeatEvery().getValue() <= 0 || this.blueprint.getTimes().getValue() > 0);
   }

   private boolean sourceWorldIsLoaded(ScriptActionData data) {
      Location location = data.getEliteEntity() == null ? null : data.getEliteEntity().getLocation();
      return location != null && location.getWorld() != null && Bukkit.getWorld(location.getWorld().getUID()) != null;
   }

   boolean canRunContinuation(ScriptActionData data) {
      return !this.powerSuppressed(data);
   }

   void close(boolean finishDeathActions) {
      this.finishDeathActions = finishDeathActions && (!this.closed || this.finishDeathActions);
      this.closed = true;

      for (ScriptAction.ScheduledAction action : new ArrayList<>(this.scheduledActions)) {
         if (!this.canFinishAfterDeath(action.data)) {
            action.cancel();
         }
      }
   }

   protected Collection<LivingEntity> getTargets(ScriptActionData scriptActionData) {
      Collection<LivingEntity> livingTargets = this.scriptConditions.validateEntities(scriptActionData, this.scriptTargets.getTargetEntities(scriptActionData));
      this.scriptTargets.setAnonymousTargets(new ArrayList<>(livingTargets), scriptActionData);
      if (this.blueprint.isDebug()) {
         livingTargets.forEach(livingTarget -> Logger.showLocation(livingTarget.getLocation()));
      }

      return livingTargets;
   }

   protected Collection<Location> getLocationTargets(ScriptActionData scriptActionData) {
      Collection<Location> locationTargets = this.scriptConditions.validateLocations(scriptActionData, this.scriptTargets.getTargetLocations(scriptActionData));
      this.scriptTargets.setAnonymousTargets(new ArrayList<>(locationTargets), scriptActionData);
      if (this.blueprint.isDebug()) {
         locationTargets.forEach(Logger::showLocation);
      }

      return locationTargets;
   }

   protected Collection<Location> getFinalLocationTargets(ScriptActionData scriptActionData) {
      if (this.finalScriptTargets == null) {
         return Collections.emptyList();
      } else {
         Collection<Location> locationTargets = this.scriptConditions
            .validateLocations(scriptActionData, this.finalScriptTargets.getTargetLocations(scriptActionData));
         this.finalScriptTargets.setAnonymousTargets(new ArrayList<>(locationTargets), scriptActionData);
         if (this.blueprint.isDebug()) {
            locationTargets.forEach(Logger::showLocation);
         }

         return locationTargets;
      }
   }

   ScriptParticles scriptParticles() {
      return this.scriptParticles;
   }

   ScriptRuntimeOwner runtimeOwner() {
      return this.runtimeOwner;
   }

   private void runTeleport(ScriptActionData scriptActionData) {
      Collection<LivingEntity> targets = this.getTargets(scriptActionData);
      Collection<Location> destinations = this.getFinalLocationTargets(scriptActionData);
      if (destinations.isEmpty()) {
         Logger.warn("Failed to get teleport destination for script '" + this.blueprint.getScriptName() + "' because there is no set FinalTarget!");
      } else {
         Location destination = destinations.iterator().next();
         targets.forEach(target -> {
            try {
               if (!(target instanceof Player player && PlayerData.getMatchInstance(player) != null)) {
                  target.teleport(destination);
               } else if (InstanceEffectPolicy.canAffect(scriptActionData.getEliteEntity(), player)) {
                  InstancePlayerMovement.teleportForMatch(player, destination, PlayerData.getMatchInstance(player), false);
               }
            } catch (Exception var5) {
               Logger.warn("Failed to teleport entity '" + target.getName() + "' in script '" + this.blueprint.getScriptName() + "': " + var5.getMessage());
            }
         });
      }
   }

   private void runMessage(ScriptActionData scriptActionData) {
      String message = ChatColorConverter.convert(this.parsePlaceholders(scriptActionData, this.blueprint.getSValue()));
      this.getTargets(scriptActionData).forEach(target -> target.sendMessage(message));
   }

   private void runTitleMessage(ScriptActionData scriptActionData) {
      if (this.blueprint.getTitle().isEmpty() && this.blueprint.getSubtitle().isEmpty()) {
         Logger.warn(
            "TITLE_MESSAGE action does not have any titles or subtitles for script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'"
         );
      } else {
         String title = ChatColorConverter.convert(this.parsePlaceholders(scriptActionData, this.blueprint.getTitle()));
         String subtitle = ChatColorConverter.convert(this.parsePlaceholders(scriptActionData, this.blueprint.getSubtitle()));
         this.getTargets(scriptActionData)
            .forEach(
               target -> {
                  if (target instanceof Player player) {
                     player.sendTitle(
                        title, subtitle, this.blueprint.getFadeIn().getValue(), this.blueprint.getDuration().getValue(), this.blueprint.getFadeOut().getValue()
                     );
                  } else {
                     Logger.warn(
                        "TITLE_MESSAGE actions must target players! Problematic script: '"
                           + this.blueprint.getScriptName()
                           + "' in file '"
                           + this.blueprint.getScriptFilename()
                           + "'"
                     );
                  }
               }
            );
      }
   }

   private void runActionBarMessage(ScriptActionData scriptActionData) {
      if (this.blueprint.getSValue().isEmpty()) {
         Logger.warn(
            "ACTION_BAR_MESSAGE action does not have a sValue for script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'"
         );
      } else {
         String message = ChatColorConverter.convert(this.parsePlaceholders(scriptActionData, this.blueprint.getSValue()));
         this.getTargets(scriptActionData)
            .forEach(
               target -> {
                  if (target instanceof Player player) {
                     ActionBarCompositor.show(player, ActionBarCompositor.Source.SCRIPT, message);
                  } else {
                     Logger.warn(
                        "ACTION_BAR_MESSAGE actions must target players! Problematic script: '"
                           + this.blueprint.getScriptName()
                           + "' in file '"
                           + this.blueprint.getScriptFilename()
                           + "'"
                     );
                  }
               }
            );
      }
   }

   private void runBossBarMessage(ScriptActionData scriptActionData) {
      if (this.blueprint.getSValue().isEmpty()) {
         Logger.warn(
            "BOSS_BAR_MESSAGE action does not have a valid sValue for script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'"
         );
      } else {
         String message = ChatColorConverter.convert(this.parsePlaceholders(scriptActionData, this.blueprint.getSValue()));
         BossBar bossBar = Bukkit.createBossBar(message, this.blueprint.getBarColor(), this.blueprint.getBarStyle(), new BarFlag[0]);
         Set<Player> viewers = new HashSet<>();

         for (LivingEntity target : this.getTargets(scriptActionData)) {
            if (target instanceof Player player) {
               BossBarOrderManager.show(player, bossBar);
               viewers.add(player);
            } else {
               Logger.warn(
                  "BOSS_BAR_MESSAGE actions must target players! Problematic script: '"
                     + this.blueprint.getScriptName()
                     + "' in file '"
                     + this.blueprint.getScriptFilename()
                     + "'"
               );
            }
         }

         if (!viewers.isEmpty()) {
            int configuredDuration = this.blueprint.getDuration().getValue();
            int duration = configuredDuration > 0 ? configuredDuration : 100;
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
               viewers.forEach(playerx -> BossBarOrderManager.hide(playerx, bossBar));
               bossBar.removeAll();
            }, duration);
         }
      }
   }

   private void runPotionEffect(ScriptActionData scriptActionData) {
      PotionEffect effect = new PotionEffect(
         this.blueprint.getPotionEffectType(), this.blueprint.getDuration().getValue(), this.blueprint.getAmplifier().getValue()
      );
      this.getTargets(scriptActionData).forEach(target -> {
         if (InstanceEffectPolicy.canAffect(scriptActionData.getEliteEntity(), target)) {
            target.addPotionEffect(effect);
         }
      });
   }

   private void runAdditionalScripts(ScriptActionData scriptActionData) {
      if (this.blueprint.getScripts() != null && !this.blueprint.getScripts().isEmpty()) {
         if (this.blueprint.isOnlyRunOneScript()) {
            String scriptName = this.blueprint.getScripts().get(ThreadLocalRandom.current().nextInt(this.blueprint.getScripts().size()));
            ScriptExecutable script = this.eliteScriptMap.get(scriptName);
            if (script != null) {
               script.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
            } else {
               Logger.warn(
                  "Failed to find script '"
                     + scriptName
                     + "' for script '"
                     + this.blueprint.getScriptName()
                     + "' in file '"
                     + this.blueprint.getScriptFilename()
                     + "'"
               );
            }
         } else {
            for (String scriptName : this.blueprint.getScripts()) {
               ScriptExecutable script = this.eliteScriptMap.get(scriptName);
               if (script != null) {
                  script.check(scriptActionData.getEliteEntity(), scriptActionData.getDirectTarget(), scriptActionData);
               } else {
                  Logger.warn(
                     "Failed to find script '"
                        + scriptName
                        + "' for script '"
                        + this.blueprint.getScriptName()
                        + "' in file '"
                        + this.blueprint.getScriptFilename()
                        + "'"
                  );
               }
            }
         }
      } else {
         if (this.blueprint.getActionType().equals(ActionType.RUN_SCRIPT)) {
            Logger.warn(
               "No scripts found to run in RUN_SCRIPT action in script '"
                  + this.blueprint.getScriptName()
                  + "' in file '"
                  + this.blueprint.getScriptFilename()
                  + "'"
            );
         }
      }
   }

   public static void shutdown() {
      TimedScriptStateManager.shutdown();
      invulnerablePlayers.clear();
      scriptDamageDepth.remove();
   }

   private void runDamage(ScriptActionData scriptActionData) {
      int currentDepth = scriptDamageDepth.get();
      if (currentDepth >= 3) {
         Logger.warn(
            "Maximum script damage depth (3) reached in script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'. Preventing infinite recursion."
         );
      } else {
         double damageAmount = this.blueprint.getAmount().getValue().floatValue();
         double multiplier = this.blueprint.getMultiplier().getValue().floatValue();

         try {
            scriptDamageDepth.set(currentDepth + 1);
            this.getTargets(scriptActionData).forEach(target -> {
               if (target instanceof Player) {
                  CombatDamageContext.runEliteToPlayerMultiplier(multiplier, () -> {
                     if (scriptActionData.getEliteEntity().getLivingEntity() != null) {
                        target.damage(damageAmount, scriptActionData.getEliteEntity().getLivingEntity());
                     } else {
                        target.damage(damageAmount);
                     }
                  });
               } else if (scriptActionData.getEliteEntity().getLivingEntity() != null) {
                  target.damage(damageAmount, scriptActionData.getEliteEntity().getLivingEntity());
               } else {
                  target.damage(damageAmount);
               }
            });
         } finally {
            scriptDamageDepth.set(currentDepth);
            if (currentDepth == 0) {
               scriptDamageDepth.remove();
            }
         }
      }
   }

   private void runSetOnFire(ScriptActionData scriptActionData) {
      int duration = this.blueprint.getDuration().getValue();
      this.getTargets(scriptActionData)
         .stream()
         .filter(target -> InstanceEffectPolicy.canAffect(scriptActionData.getEliteEntity(), target))
         .forEach(target -> target.setFireTicks(duration));
   }

   private void runVisualFreeze(ScriptActionData scriptActionData) {
      int freezeTicks = this.blueprint.getAmount().getValue().intValue();
      this.getTargets(scriptActionData)
         .stream()
         .filter(target -> InstanceEffectPolicy.canAffect(scriptActionData.getEliteEntity(), target))
         .forEach(target -> target.setFreezeTicks(target.getFreezeTicks() + freezeTicks));
   }

   private void runPlaceBlock(ScriptActionData scriptActionData) {
      this.getLocationTargets(scriptActionData).forEach(location -> {
         Block block = location.getBlock();
         if (this.blueprint.getDuration().getValue() > 0) {
            EntityTracker.addTemporaryBlock(block, this.blueprint.getDuration().getValue(), this.blueprint.getMaterial());
         } else {
            block.setType(this.blueprint.getMaterial());
         }
      });
   }

   private void runPlayerCommand(ScriptActionData scriptActionData) {
      String command = this.parsePlaceholders(scriptActionData, this.blueprint.getSValue());
      this.getTargets(scriptActionData)
         .forEach(
            target -> {
               if (target instanceof Player player) {
                  String targetCommand = command.replace("$targetName", player.getName());
                  player.performCommand(targetCommand);
               } else {
                  Logger.warn(
                     "RUN_COMMAND_AS_PLAYER action must target players! Problematic script: '"
                        + this.blueprint.getScriptName()
                        + "' in file '"
                        + this.blueprint.getScriptFilename()
                        + "'"
                  );
               }
            }
         );
   }

   private void runConsoleCommand(ScriptActionData scriptActionData) {
      String command = this.parsePlaceholders(scriptActionData, this.blueprint.getSValue());
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
   }

   private String parsePlaceholders(ScriptActionData scriptActionData, String template) {
      if (template != null && !template.isEmpty()) {
         EliteEntity eliteEntity = scriptActionData.getEliteEntity();
         LivingEntity directTarget = scriptActionData.getDirectTarget();
         Player player = directTarget instanceof Player ? (Player)directTarget : null;
         String playerName = player != null ? player.getName() : "Unknown";
         Location playerLocation = player != null ? player.getLocation() : new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
         String bossName = eliteEntity != null ? eliteEntity.getName() : "Unknown";
         Location bossLocation = eliteEntity != null && eliteEntity.getLocation() != null
            ? eliteEntity.getLocation()
            : new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
         int bossLevel = eliteEntity != null ? eliteEntity.getLevel() : 0;
         String bossWorldName = bossLocation.getWorld() != null ? bossLocation.getWorld().getName() : "Unknown";
         return template.replace("$playerName", playerName)
            .replace("$playerX", String.valueOf(playerLocation.getX()))
            .replace("$playerY", String.valueOf(playerLocation.getY()))
            .replace("$playerZ", String.valueOf(playerLocation.getZ()))
            .replace("$bossName", bossName)
            .replace("$bossX", String.valueOf(bossLocation.getX()))
            .replace("$bossY", String.valueOf(bossLocation.getY()))
            .replace("$bossZ", String.valueOf(bossLocation.getZ()))
            .replace("$bossLevel", String.valueOf(bossLevel))
            .replace("$bossWorldName", bossWorldName);
      } else {
         return template;
      }
   }

   private void runPlayAnimation(ScriptActionData scriptActionData) {
      this.getTargets(scriptActionData).forEach(target -> {
         if (EntityTracker.getEliteMobEntity(target) instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
            customBossEntity.getCustomModel().playAnimationByName(this.blueprint.getSValue());
         }
      });
   }

   private void runSpawnFallingBlock(ScriptActionData scriptActionData) {
      this.getLocationTargets(scriptActionData)
         .forEach(
            location -> {
               try {
                  FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, this.blueprint.getMaterial().createBlockData());
                  fallingBlock.setDropItem(false);
                  fallingBlock.setHurtEntities(false);
                  Vector velocity = this.blueprint.getScriptRelativeVectorBlueprint() != null
                     ? new ScriptRelativeVector(this.blueprint.getScriptRelativeVectorBlueprint(), this.runtimeOwner, location).getVector(scriptActionData)
                     : this.blueprint.getVValue();
                  if (velocity != null) {
                     fallingBlock.setVelocity(velocity);
                  }

                  ScriptListener.fallingBlocks.put(fallingBlock, new FallingEntityDataPair(this, scriptActionData));
               } catch (Exception var5) {
                  Logger.warn(
                     "Failed to spawn falling block at location '" + location + "' in script '" + this.blueprint.getScriptName() + "': " + var5.getMessage()
                  );
               }
            }
         );
   }

   private void runModifyDamage(ScriptActionData scriptActionData) {
      if (scriptActionData.getEvent() instanceof EliteDamageEvent eliteDamageEvent) {
         double newDamage = eliteDamageEvent.getDamage() * this.blueprint.getMultiplier().getValue().floatValue();
         eliteDamageEvent.setDamage(newDamage);
      }
   }

   private void runSummonEntity(ScriptActionData scriptActionData) {
      EntityType entityType;
      try {
         entityType = EntityType.valueOf(this.blueprint.getSValue().toUpperCase());
      } catch (IllegalArgumentException var5) {
         Logger.warn(
            "Invalid entity type '"
               + this.blueprint.getSValue()
               + "' in script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'"
         );
         return;
      }

      Class<? extends Entity> entityClass = entityType.getEntityClass();
      if (entityClass == null) {
         Logger.warn(
            "getEntityClass() returned null for EntityType "
               + entityType
               + " in script '"
               + this.blueprint.getScriptName()
               + "' in file '"
               + this.blueprint.getScriptFilename()
               + "'"
         );
      } else {
         Collection<Location> locationTargets = this.getLocationTargets(scriptActionData);
         if (!locationTargets.isEmpty()) {
            locationTargets.forEach(
               location -> {
                  if (location != null && location.getWorld() != null) {
                     Vector velocity = this.blueprint.getScriptRelativeVectorBlueprint() != null
                        ? new ScriptRelativeVector(this.blueprint.getScriptRelativeVectorBlueprint(), this.runtimeOwner, location).getVector(scriptActionData)
                        : this.blueprint.getVValue();
                     boolean isProjectile = Projectile.class.isAssignableFrom(entityClass);
                     boolean hasLivingEntity = scriptActionData.getEliteEntity().getLivingEntity() != null;

                     try {
                        final Entity entity;
                        if (hasLivingEntity && isProjectile) {
                           entity = scriptActionData.getEliteEntity().getLivingEntity().launchProjectile(entityClass.asSubclass(Projectile.class), velocity);
                           ((Projectile)entity).setShooter(scriptActionData.getEliteEntity().getLivingEntity());
                           if (entity instanceof Fireball fireball) {
                              fireball.setDirection(velocity);
                           }
                        } else {
                           entity = location.getWorld().spawn(location, entityClass);
                           if (velocity != null) {
                              entity.setVelocity(velocity);
                           }
                        }

                        if (this.blueprint.getDuration().getValue() != null && this.blueprint.getDuration().getValue() > 0 && entity != null) {
                           (new BukkitRunnable() {
                              public void run() {
                                 if (entity != null && entity.isValid()) {
                                    entity.remove();
                                 }
                              }
                           }).runTaskLater(MetadataHandler.PLUGIN, this.blueprint.getDuration().getValue().intValue());
                        }

                        if (!this.blueprint.getLandingScripts().isEmpty()) {
                           final FallingEntityDataPair dataPair = new FallingEntityDataPair(this, scriptActionData);
                           (new BukkitRunnable() {
                              final int maxTicks = 6000;
                              int counter = 0;

                              public void run() {
                                 if (!entity.isValid() || entity.isOnGround() || this.counter > 6000) {
                                    ScriptListener.runEvent(dataPair, entity.getLocation());
                                    this.cancel();
                                 }

                                 this.counter++;
                              }
                           }).runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
                        }
                     } catch (Exception var9) {
                        Logger.warn(
                           "Failed to summon entity in script '"
                              + this.blueprint.getScriptName()
                              + "' in file '"
                              + this.blueprint.getScriptFilename()
                              + "': "
                              + var9.getMessage()
                        );
                     }
                  }
               }
            );
         }
      }
   }

   private void runNavigate(ScriptActionData scriptActionData) {
      Collection<LivingEntity> targets = this.getTargets(scriptActionData);
      if (this.finalScriptTargets == null) {
         Logger.warn("Failed to get navigation destination for script '" + this.blueprint.getScriptName() + "' because there is no set FinalTarget!");
      } else {
         Collection<Location> destinations = this.getFinalLocationTargets(scriptActionData);
         if (!destinations.isEmpty()) {
            Location destination = destinations.iterator().next();
            double speed = this.blueprint.getVelocity().getValue().floatValue();
            boolean avoidObstacles = this.blueprint.getBValue() != null && this.blueprint.getBValue();
            int duration = this.blueprint.getDuration().getValue();
            targets.forEach(target -> {
               if (EntityTracker.getEliteMobEntity(target) instanceof CustomBossEntity customBossEntity) {
                  Navigation.navigateTo(customBossEntity, speed, destination, avoidObstacles, duration);
               }
            });
         }
      }
   }

   private void runScale(ScriptActionData scriptActionData) {
      double scaleValue = this.blueprint.getScale().getValue().floatValue();
      int duration = this.blueprint.getDuration().getValue();
      this.getTargets(scriptActionData).forEach(target -> {
         AttributeInstance attribute = AttributeManager.getAttributeInstance(target, "generic_scale");
         if (attribute != null) {
            TimedScriptStateManager.apply(target.getUniqueId(), "generic_scale", scaleValue, duration, attribute::getBaseValue, attribute::setBaseValue);
         }
      });
   }

   private void setFacing(ScriptActionData scriptActionData) {
      Vector direction = this.blueprint.getScriptRelativeVectorBlueprint() != null
         ? new ScriptRelativeVector(this.blueprint.getScriptRelativeVectorBlueprint(), this.runtimeOwner, scriptActionData.getEliteEntity().getLocation())
            .getVector(scriptActionData)
         : this.blueprint.getVValue();
      if (direction == null) {
         Logger.warn(
            "Tried to set direction in "
               + this.getBlueprint().getScriptFilename()
               + " but no configuration for vvalue or relative vector are set in "
               + this.blueprint.getScriptName()
               + " !"
         );
      } else {
         this.getTargets(scriptActionData).forEach(target -> {
            Location location = target.getLocation();
            location.setDirection(direction);
            target.teleport(location);
         });
      }
   }

   private void runHeal(ScriptActionData scriptActionData) {
      double healAmount = this.blueprint.getAmount().getValue().floatValue();
      this.getTargets(scriptActionData).forEach(target -> {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null) {
            eliteEntity.heal(healAmount);
         } else {
            double maxHp = AttributeManager.getAttributeBaseValue(target, "generic_max_health");
            double newHp = Math.min(target.getHealth() + healAmount, maxHp);
            target.setHealth(newHp);
         }
      });
   }

   private void runRemoveElite(ScriptActionData scriptActionData) {
      this.getTargets(scriptActionData).forEach(target -> {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null) {
            eliteEntity.remove(RemovalReason.OTHER);
         }
      });
   }

   @Generated
   public static Set<UUID> getInvulnerablePlayers() {
      return invulnerablePlayers;
   }

   @Generated
   public ScriptActionBlueprint getBlueprint() {
      return this.blueprint;
   }

   @Generated
   public Map<String, ScriptExecutable> getEliteScriptMap() {
      return this.eliteScriptMap;
   }

   private abstract class ScheduledAction extends BukkitRunnable {
      private final ScriptActionData data;

      private ScheduledAction(ScriptActionData data) {
         this.data = data;
      }

      final void schedule(long delay, long period) {
         if (!ScriptAction.this.closed || ScriptAction.this.canFinishAfterDeath(this.data)) {
            ScriptAction.this.scheduledActions.add(this);

            try {
               this.runTaskTimer(MetadataHandler.PLUGIN, delay, period);
            } catch (Error | RuntimeException var6) {
               ScriptAction.this.scheduledActions.remove(this);
               throw var6;
            }
         }
      }

      public final void run() {
         if (ScriptAction.this.closed && !ScriptAction.this.canFinishAfterDeath(this.data)) {
            this.cancel();
         } else if (!ScriptAction.this.powerSuppressed(this.data)) {
            try {
               this.runAction();
            } catch (Error | RuntimeException var2) {
               this.cancel();
               throw var2;
            }
         }
      }

      protected abstract void runAction();

      public synchronized void cancel() throws IllegalStateException {
         try {
            super.cancel();
         } finally {
            ScriptAction.this.scheduledActions.remove(this);
         }
      }
   }
}
