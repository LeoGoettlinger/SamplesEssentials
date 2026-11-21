package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EliteScript extends ElitePower implements Cloneable, ScriptRuntimeOwner, ScriptExecutable {
   private final EliteScriptBlueprint scriptBlueprint;
   protected ScriptActions scriptActions;
   private ScriptEvents scriptEvents;
   private ScriptZone scriptZone;
   private ScriptCooldowns scriptCooldowns;
   private ScriptConditions scriptConditions;
   protected Map<String, ScriptExecutable> eliteScriptMap;
   private boolean halt = false;
   private boolean closed = false;
   private boolean finishDeathActions = false;

   public EliteScript(EliteScriptBlueprint scriptBlueprint, Map<String, ScriptExecutable> eliteScriptMap, EliteEntity eliteEntity) {
      super(scriptBlueprint.getCustomConfigFields());
      this.scriptBlueprint = scriptBlueprint;
      if (!this.halt) {
         this.eliteScriptMap = eliteScriptMap;
         this.scriptEvents = new ScriptEvents(scriptBlueprint.getScriptEventsBlueprint());
         this.scriptConditions = new ScriptConditions(scriptBlueprint.getScriptConditionsBlueprint(), this, false);
         this.scriptZone = new ScriptZone(scriptBlueprint.getScriptZoneBlueprint(), this);
         this.initializeCustomEvents(eliteEntity);
         this.scriptActions = new ScriptActions(scriptBlueprint.getScriptActionsBlueprint(), eliteScriptMap, this);
         this.scriptCooldowns = new ScriptCooldowns(scriptBlueprint.getScriptCooldownsBlueprint(), this);
         eliteScriptMap.put(scriptBlueprint.getScriptName(), this);
      }
   }

   public static List<EliteScript> generateBossScripts(List<EliteScriptBlueprint> blueprints, EliteEntity eliteEntity) {
      HashMap<String, ScriptExecutable> powerMap = new HashMap<>();
      return blueprints.stream().map(eliteScriptBlueprint -> new EliteScript(eliteScriptBlueprint, powerMap, eliteEntity)).collect(Collectors.toList());
   }

   public void check(Event event, EliteEntity eliteEntity, Player player) {
      if (!this.suppressed(eliteEntity)) {
         if (this.getPowerCooldownTime() <= 0 && this.getGlobalCooldownTime() <= 0 || this.scriptCooldowns == null || !super.isInCooldown(eliteEntity)) {
            if (this.scriptEvents.isTargetEvent(event.getClass())) {
               if (this.scriptConditions == null || this.scriptConditions.meetsPreActionConditions(eliteEntity, player)) {
                  this.scriptActions.runScripts(eliteEntity, player, event);
                  this.doCooldownTicks(eliteEntity);
               }
            }
         }
      }
   }

   public void check(Event event, EliteEntity eliteEntity, LivingEntity damager) {
      if (!this.suppressed(eliteEntity)) {
         if (this.getPowerCooldownTime() <= 0 && this.getGlobalCooldownTime() <= 0 || this.scriptCooldowns == null || !super.isInCooldown(eliteEntity)) {
            if (this.scriptEvents.isTargetEvent(event.getClass())) {
               if (this.scriptConditions == null || this.scriptConditions.meetsPreActionConditions(eliteEntity, damager)) {
                  this.scriptActions.runScripts(eliteEntity, damager, event);
                  this.doCooldownTicks(eliteEntity);
               }
            }
         }
      }
   }

   @Override
   public void check(EliteEntity eliteEntity, LivingEntity directTarget, ScriptActionData previousScriptActionData) {
      if (!this.suppressed(eliteEntity, previousScriptActionData)) {
         if (this.scriptConditions == null || this.scriptConditions.meetsPreActionConditions(eliteEntity, directTarget)) {
            this.scriptActions.runScripts(previousScriptActionData);
            this.doCooldownTicks(eliteEntity);
         }
      }
   }

   @Override
   public void check(Location landingLocation, ScriptActionData previousScriptActionData) {
      if (!this.suppressed(previousScriptActionData.getEliteEntity(), previousScriptActionData)) {
         if (this.scriptConditions == null || this.scriptConditions.meetsPreActionConditions(previousScriptActionData.getEliteEntity(), null)) {
            this.scriptActions.runScripts(previousScriptActionData, landingLocation);
            this.doCooldownTicks(previousScriptActionData.getEliteEntity());
         }
      }
   }

   public void initializeCustomEvents(EliteEntity eliteEntity) {
      if (this.closed) {
         this.scriptActions.close(false);
         this.scriptZone = new ScriptZone(this.scriptBlueprint.getScriptZoneBlueprint(), this);
         this.scriptActions = new ScriptActions(this.scriptBlueprint.getScriptActionsBlueprint(), this.eliteScriptMap, this);
         this.finishDeathActions = false;
         this.closed = false;
      }

      if (this.scriptEvents.getScriptEventsBlueprint().isZoneListener()) {
         this.scriptZone.setZoneListener(true);
         this.scriptZone.startZoneListener(eliteEntity);
      }
   }

   @Override
   public void closeRuntime() {
      this.closeRuntime(RemovalReason.OTHER);
   }

   public void closeRuntime(RemovalReason removalReason) {
      this.finishDeathActions = removalReason == RemovalReason.DEATH && (!this.closed || this.finishDeathActions);
      this.closed = true;

      try {
         if (this.scriptActions != null) {
            this.scriptActions.close(this.finishDeathActions);
         }
      } finally {
         if (this.scriptZone != null) {
            this.scriptZone.shutdown();
         }
      }
   }

   private boolean suppressed(EliteEntity eliteEntity) {
      return this.suppressed(eliteEntity, null);
   }

   private boolean suppressed(EliteEntity eliteEntity, ScriptActionData previousData) {
      return eliteEntity == null
         || eliteEntity.getPowerSuppression().isSuppressed()
         || this.closed && (!this.finishDeathActions || previousData == null || !previousData.originatesFromDeath());
   }

   @Generated
   @Override
   public ScriptZone getScriptZone() {
      return this.scriptZone;
   }

   @Generated
   public Map<String, ScriptExecutable> getEliteScriptMap() {
      return this.eliteScriptMap;
   }

   @Generated
   public void setHalt(boolean halt) {
      this.halt = halt;
   }

   public static class EliteScriptEvents implements Listener {
      @EventHandler
      public void onSpawn(EliteMobSpawnEvent event) {
         if (event.getEliteMobEntity() instanceof CustomBossEntity customBossEntity) {
            customBossEntity.getElitePowers().forEach(elitePower -> {
               if (elitePower instanceof EliteScript eliteScript) {
                  eliteScript.initializeCustomEvents(event.getEliteMobEntity());
               }
            });
         }
      }
   }
}
