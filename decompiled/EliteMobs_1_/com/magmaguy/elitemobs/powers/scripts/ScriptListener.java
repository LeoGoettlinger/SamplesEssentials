package com.magmaguy.elitemobs.powers.scripts;

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
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.HashMap;
import java.util.function.BiConsumer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityRemoveEvent;

public class ScriptListener implements Listener {
   public static HashMap<FallingBlock, FallingEntityDataPair> fallingBlocks = new HashMap<>();
   private static final HashMap<FallingBlock, ScriptListener.LuaLandingCallback> luaFallingBlocks = new HashMap<>();
   public static HashMap<Entity, FallingEntityDataPair> fallingEntities = new HashMap<>();

   public static void registerLuaFallingBlock(Object owner, FallingBlock entity, BiConsumer<FallingBlock, Location> callback) {
      luaFallingBlocks.put(entity, new ScriptListener.LuaLandingCallback(owner, callback));
   }

   public static void removeLuaFallingBlocks(Object owner) {
      luaFallingBlocks.values().removeIf(registration -> registration.owner() == owner);
   }

   public static void forgetFallingEntity(Entity entity) {
      luaFallingBlocks.remove(entity);
      fallingBlocks.remove(entity);
      fallingEntities.remove(entity);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityRemove(EntityRemoveEvent event) {
      forgetFallingEntity(event.getEntity());
   }

   public static void shutdown() {
      fallingBlocks.clear();
      luaFallingBlocks.clear();
      fallingEntities.clear();
   }

   public static void runEvent(FallingEntityDataPair fallingEntityDataPair, Location landingLocation) {
      if (fallingEntityDataPair.getScriptAction().canRunContinuation(fallingEntityDataPair.getScriptActionData())) {
         for (String string : fallingEntityDataPair.getScriptAction().getBlueprint().getLandingScripts()) {
            ScriptExecutable iteratedScript = fallingEntityDataPair.getScriptAction().getEliteScriptMap().get(string);
            if (iteratedScript == null) {
               Logger.warn("Elite script " + string + " does not exist for landing scripts!");
               return;
            }

            iteratedScript.check(landingLocation, fallingEntityDataPair.getScriptActionData());
         }
      }
   }

   @EventHandler
   public void onEliteMobDamagedByPlayerEvent(EliteMobDamagedByPlayerEvent event) {
      if (!event.isCancelled()) {
         this.runEvent(event, event.getEliteMobEntity(), event.getPlayer());
         EliteEntity summoner = event.getEliteMobEntity().getSummoningEntity();
         if (summoner != null && summoner.exists() && !event.isCancelled()) {
            for (ElitePower power : summoner.getElitePowersInExecutionOrder()) {
               if (event.isCancelled()) {
                  break;
               }

               if (power instanceof LuaElitePower luaPower) {
                  luaPower.checkReinforcementDamage(event);
               }
            }
         }
      }
   }

   @EventHandler
   public void onEliteMobDamagedByEliteMobEvent(EliteMobDamagedByEliteMobEvent event) {
      if (!event.isCancelled()) {
         this.runEventGeneric(event, event.getDamagee(), event.getDamager().getLivingEntity());
      }
   }

   @EventHandler
   public void onEliteMobDamagedEvent(EliteMobDamagedEvent event) {
      if (!event.isCancelled()) {
         this.runEvent(event, event.getEliteEntity());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobsEnterCombatEvent(EliteMobEnterCombatEvent event) {
      this.runEvent(event, event.getEliteMobEntity(), event.getTargetEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobDeathEvent(EliteMobDeathEvent event) {
      this.runEvent(event, event.getEliteEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobExitCombatEvent(EliteMobExitCombatEvent event) {
      this.runEvent(event, event.getEliteMobEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobHealEvent(EliteMobHealEvent event) {
      if (!event.isCancelled()) {
         this.runEvent(event, event.getEliteEntity());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobTargetPlayerEvent(EliteMobTargetPlayerEvent event) {
      this.runEvent(event, event.getEliteMobEntity(), event.getPlayer());
   }

   @EventHandler
   public void onPlayerDamagedByEliteMobEvent(PlayerDamagedByEliteMobEvent event) {
      if (!event.isCancelled()) {
         this.runEvent(event, event.getEliteMobEntity(), event.getPlayer());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteMobSpawnEvent(EliteMobSpawnEvent event) {
      if (!event.isCancelled()) {
         this.runEvent(event, event.getEliteMobEntity());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onElitePhaseSwitchEvent(ElitePhaseSwitchEvent event) {
      this.runEvent(event, event.getCustomBossEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
      FallingEntityDataPair fallingEntityDataPair = fallingBlocks.remove(event.getEntity());
      ScriptListener.LuaLandingCallback luaCallback = luaFallingBlocks.remove(event.getEntity());
      if (fallingEntityDataPair != null || luaCallback != null) {
         event.setCancelled(true);
         if (fallingEntityDataPair != null) {
            ScriptAction scriptAction = fallingEntityDataPair.getScriptAction();
            if (scriptAction != null) {
               runEvent(fallingEntityDataPair, event.getBlock().getLocation());
            }
         }

         if (luaCallback != null) {
            luaCallback.callback().accept((FallingBlock)event.getEntity(), event.getBlock().getLocation());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onZoneEnterEvent(ScriptZoneEnterEvent event) {
      this.runEventGeneric(event, event.getEliteEntity(), event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onZoneLeaveEvent(ScriptZoneLeaveEvent event) {
      this.runEventGeneric(event, event.getEliteEntity(), event.getEntity());
   }

   private void runEvent(Event event, EliteEntity eliteEntity) {
      for (ElitePower elitePower : eliteEntity.getElitePowersInExecutionOrder()) {
         if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
         }

         if (elitePower instanceof EliteScript eliteScript) {
            eliteScript.check(event, eliteEntity, null);
         } else if (elitePower instanceof LuaElitePower luaElitePower) {
            luaElitePower.check(event, eliteEntity, (LivingEntity)null);
         }
      }
   }

   private void runEventGeneric(Event event, EliteEntity eliteEntity, LivingEntity directTarget) {
      for (ElitePower elitePower : eliteEntity.getElitePowersInExecutionOrder()) {
         if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
         }

         if (elitePower instanceof EliteScript eliteScript) {
            eliteScript.check(event, eliteEntity, directTarget);
         } else if (elitePower instanceof LuaElitePower luaElitePower) {
            luaElitePower.check(event, eliteEntity, directTarget);
         }
      }
   }

   private void runEvent(Event event, EliteEntity eliteEntity, Player player) {
      for (ElitePower elitePower : eliteEntity.getElitePowersInExecutionOrder()) {
         if (event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
         }

         if (elitePower instanceof EliteScript eliteScript) {
            eliteScript.check(event, eliteEntity, player);
         } else if (elitePower instanceof LuaElitePower luaElitePower) {
            luaElitePower.check(event, eliteEntity, player);
         }
      }
   }

   private record LuaLandingCallback(Object owner, BiConsumer<FallingBlock, Location> callback) {
   }
}
