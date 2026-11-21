package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.EliteMobTargetPlayerEvent;
import com.magmaguy.elitemobs.api.ElitePhaseSwitchEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptQueryResult;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseState;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class LuaElitePower extends ElitePower {
   private final LuaPowerConfigFields luaPowerConfigFields;
   private ScriptInstance instance = null;
   private ScriptableBoss scriptableBoss = null;
   private final Map<String, Long> successfulEventHooks = new LinkedHashMap<>();
   private final Map<String, Long> failedEventHooks = new LinkedHashMap<>();
   private long acceptedMindActions;
   private long deferredMindActions;
   private long rejectedMindActions;
   private final ElitePowerPauseState powerPauseState = new ElitePowerPauseState();

   public LuaElitePower(LuaPowerConfigFields luaPowerConfigFields) {
      super((PowersConfigFields)luaPowerConfigFields);
      this.luaPowerConfigFields = luaPowerConfigFields;
   }

   @Override
   public void applyPowers(LivingEntity livingEntity) {
      this.initializeInstance(false);
   }

   @Override
   public int getExecutionPriority() {
      return this.luaPowerConfigFields.getLuaPowerDefinition().getPriority();
   }

   public void check(Event event, EliteEntity eliteEntity, Player player) {
      this.check(event, eliteEntity, (LivingEntity)player);
   }

   public void checkReinforcementDamage(EliteMobDamagedByPlayerEvent event) {
      EliteEntity owner = this.getOwnerEntity();
      if (owner != null && !event.isCancelled() && !this.powerPauseState.isPaused() && !owner.getPowerSuppression().isSuppressed()) {
         ScriptHook hook = ScriptableBoss.ON_REINFORCEMENT_DAMAGED_BY_PLAYER;
         if (this.luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            this.initializeInstance(false);
            if (this.instance != null) {
               this.instance.handleEvent(hook, event, event.getEliteMobEntity().getLivingEntity(), event.getPlayer());
               this.recordEventHook(hook, !this.instance.isClosed());
            } else {
               this.recordEventHook(hook, false);
            }
         }
      }
   }

   public void check(Event event, EliteEntity eliteEntity, LivingEntity directTarget) {
      if (!this.powerPauseState.isPaused() && !eliteEntity.getPowerSuppression().isSuppressed()) {
         ScriptHook hook = this.mapHook(event);
         if (hook == ScriptHook.ON_SPAWN) {
            this.initializeInstance(false);
         }

         if (hook != null && this.luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            this.initializeInstance(false);
            if (this.instance != null) {
               this.instance.handleEvent(hook, event, directTarget, directTarget);
               this.recordEventHook(hook, !this.instance.isClosed());
            } else {
               this.recordEventHook(hook, false);
            }
         }
      }
   }

   public ElitePowerActionResult handleMindAction(ElitePowerActionRequest request) {
      if (!this.powerPauseState.isPaused() && (this.getOwnerEntity() == null || !this.getOwnerEntity().getPowerSuppression().isSuppressed())) {
         if (!this.luaPowerConfigFields.getLuaPowerDefinition().supportsHook(ScriptableBoss.ON_MIND_ACTION)) {
            return ElitePowerActionResult.REJECTED;
         } else {
            this.initializeInstance(false);
            if (this.instance != null && !this.instance.isClosed() && this.scriptableBoss != null) {
               ScriptQueryResult query = this.scriptableBoss.handleMindAction(this.instance, request);
               if (query.kind() == ScriptQueryResult.Kind.UNHANDLED
                  || query.kind() == ScriptQueryResult.Kind.NIL
                  || query.kind() == ScriptQueryResult.Kind.FAILED) {
                  return this.recordMindAction(ElitePowerActionResult.REJECTED);
               } else if (query.kind() != ScriptQueryResult.Kind.STRING) {
                  Logger.warn("Lua power " + this.getFileName() + " returned a non-string on_mind_action result; expected accepted, deferred, or rejected.");
                  this.instance.shutdown();
                  return this.recordMindAction(ElitePowerActionResult.REJECTED);
               } else {
                  try {
                     return this.recordMindAction(ElitePowerActionResult.valueOf(query.stringValue().orElseThrow().toUpperCase(Locale.ROOT)));
                  } catch (IllegalArgumentException var4) {
                     Logger.warn("Lua power " + this.getFileName() + " returned invalid on_mind_action result '" + query.stringValue().orElse("") + "'.");
                     this.instance.shutdown();
                     return this.recordMindAction(ElitePowerActionResult.REJECTED);
                  }
               }
            } else {
               return this.recordMindAction(ElitePowerActionResult.REJECTED);
            }
         }
      } else {
         return ElitePowerActionResult.REJECTED;
      }
   }

   public EliteLuaPowerActivity activitySnapshot(EliteLuaPowerProgram program) {
      if (program == null) {
         throw new IllegalArgumentException("program cannot be null");
      } else {
         return new EliteLuaPowerActivity(
            program.key(),
            program.revision(),
            this.successfulEventHooks,
            this.failedEventHooks,
            this.acceptedMindActions,
            this.deferredMindActions,
            this.rejectedMindActions,
            this.instance != null && !this.instance.isClosed()
         );
      }
   }

   @Override
   public void closeRuntime() {
      ScriptInstance closing = this.instance;
      if (closing != null) {
         this.instance = null;
         this.scriptableBoss = null;
         closing.shutdown();
      }
   }

   public void startRuntimeOrThrow() {
      this.initializeInstance(true);
   }

   public boolean isRuntimeActive() {
      return this.instance != null && !this.instance.isClosed();
   }

   public void setRuntimePaused(boolean paused) {
      this.setRuntimePauseReason(ElitePowerPauseReason.MIND_SERVICE, paused);
   }

   public void setRuntimePauseReason(ElitePowerPauseReason reason, boolean paused) {
      this.powerPauseState.set(reason, paused);
      if (this.instance != null && !this.instance.isClosed()) {
         this.instance.setPaused(this.powerPauseState.isPaused());
      }
   }

   private void initializeInstance(boolean failFast) {
      EliteEntity ownerEntity = this.getOwnerEntity();
      if (ownerEntity != null && ownerEntity.getLivingEntity() != null) {
         if (this.instance == null || this.instance.isClosed()) {
            if (this.instance == null || !this.instance.isClosed()) {
               try {
                  this.scriptableBoss = new ScriptableBoss(ownerEntity);
                  this.instance = new ScriptInstance(this.luaPowerConfigFields.getLuaPowerDefinition(), this.scriptableBoss);
                  this.instance.start();
                  this.instance.setPaused(this.powerPauseState.isPaused());
               } catch (Exception var6) {
                  if (this.instance != null) {
                     try {
                        this.instance.shutdown();
                     } catch (RuntimeException var5) {
                     }
                  }

                  this.instance = null;
                  this.scriptableBoss = null;
                  if (failFast) {
                     throw new IllegalStateException("Failed to initialize Lua power " + this.getFileName(), var6);
                  }

                  Logger.warn("Failed to initialize Lua power " + this.getFileName() + ".");
                  var6.printStackTrace();
               }
            } else if (failFast) {
               throw new IllegalStateException("Lua power previously failed: " + this.getFileName());
            }
         }
      } else if (failFast) {
         throw new IllegalStateException("Lua power has no loaded owner entity");
      }
   }

   private ScriptHook mapHook(Event event) {
      if (event instanceof EliteMobSpawnEvent) {
         return ScriptHook.ON_SPAWN;
      } else if (event instanceof EliteMobDamagedByPlayerEvent) {
         return ScriptableBoss.ON_DAMAGED_BY_PLAYER;
      } else if (event instanceof EliteMobDamagedByEliteMobEvent) {
         return ScriptableBoss.ON_DAMAGED_BY_ELITE;
      } else if (event instanceof EliteMobDamagedEvent) {
         return ScriptableBoss.ON_DAMAGED;
      } else if (event instanceof PlayerDamagedByEliteMobEvent) {
         return ScriptableBoss.ON_PLAYER_DAMAGED;
      } else if (event instanceof EliteMobEnterCombatEvent) {
         return ScriptableBoss.ON_ENTER_COMBAT;
      } else if (event instanceof EliteMobExitCombatEvent) {
         return ScriptableBoss.ON_EXIT_COMBAT;
      } else if (event instanceof EliteMobHealEvent) {
         return ScriptableBoss.ON_HEAL;
      } else if (event instanceof EliteMobTargetPlayerEvent) {
         return ScriptableBoss.ON_TARGET;
      } else if (event instanceof EliteMobDeathEvent) {
         return ScriptableBoss.ON_DEATH;
      } else if (event instanceof ElitePhaseSwitchEvent) {
         return ScriptableBoss.ON_PHASE_SWITCH;
      } else if (event instanceof ScriptZoneEnterEvent) {
         return ScriptHook.ON_ZONE_ENTER;
      } else {
         return event instanceof ScriptZoneLeaveEvent ? ScriptHook.ON_ZONE_LEAVE : null;
      }
   }

   private void recordEventHook(ScriptHook hook, boolean succeeded) {
      Map<String, Long> counters = succeeded ? this.successfulEventHooks : this.failedEventHooks;
      counters.compute(hook.getKey(), (ignored, current) -> increment(current));
   }

   private ElitePowerActionResult recordMindAction(ElitePowerActionResult result) {
      switch (result) {
         case ACCEPTED:
            this.acceptedMindActions = increment(this.acceptedMindActions);
            break;
         case DEFERRED:
            this.deferredMindActions = increment(this.deferredMindActions);
            break;
         case REJECTED:
            this.rejectedMindActions = increment(this.rejectedMindActions);
      }

      return result;
   }

   private static long increment(Long value) {
      return value == null ? 1L : increment(value.longValue());
   }

   private static long increment(long value) {
      return value == Long.MAX_VALUE ? Long.MAX_VALUE : value + 1L;
   }

   @Generated
   public LuaPowerConfigFields getLuaPowerConfigFields() {
      return this.luaPowerConfigFields;
   }
}
