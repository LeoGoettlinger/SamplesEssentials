package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntityRemoveEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.magmacore.util.ChunkLocationChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class PatrolService implements Listener {
   private static PatrolService instance;
   private final PatrolStateStore stateStore;
   private final Map<UUID, PatrolController> controllers = new LinkedHashMap<>();
   private final Map<Object, PatrolController> byOwner = new IdentityHashMap<>();
   private BukkitTask task;
   private long tick;

   private PatrolService() {
      this.stateStore = new PatrolStateStore(MetadataHandler.PLUGIN.getDataFolder());
   }

   public static void initialize() {
      shutdown();
      instance = new PatrolService();
      Bukkit.getPluginManager().registerEvents(instance, MetadataHandler.PLUGIN);
      instance.task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, instance::tick, 1L, 1L);
   }

   public static void shutdown() {
      if (instance != null) {
         if (instance.task != null) {
            instance.task.cancel();
         }

         HandlerList.unregisterAll(instance);

         for (PatrolController controller : new ArrayList<>(instance.controllers.values())) {
            controller.shutdown();
         }

         instance.stateStore.saveNow();
         instance.controllers.clear();
         instance.byOwner.clear();
         instance = null;
      }
   }

   public static Optional<Location> logicalLocation(Object owner) {
      PatrolController controller = controller(owner);
      return controller == null ? Optional.empty() : Optional.ofNullable(controller.logicalLocation());
   }

   public static Optional<Location> materializationLocation(Object owner) {
      PatrolController controller = controller(owner);
      if (controller != null) {
         return Optional.ofNullable(controller.materializationLocation());
      } else if (instance == null) {
         return Optional.empty();
      } else {
         try {
            PatrolActor actor = actor(owner);
            if (actor != null && actor.route() != null) {
               PatrolStateStore.StoredState restored = instance.stateStore.get(actor.canonicalIdentity(), actor.route()).orElse(null);
               if (restored == null) {
                  return Optional.empty();
               } else {
                  Location safe = restored.safeLocation() == null ? null : restored.safeLocation().resolve();
                  if (safe != null && safe.getWorld() != null && actor.origin().worldName().equals(safe.getWorld().getName())) {
                     return Optional.of(safe);
                  } else {
                     int node = restored.currentNode() >= 0 && restored.currentNode() < actor.route().size() ? restored.currentNode() : 0;
                     return Optional.of(actor.route().node(actor.origin(), node));
                  }
               }
            } else {
               return Optional.empty();
            }
         } catch (RuntimeException var6) {
            return Optional.empty();
         }
      }
   }

   public static boolean isActivelyMoving(NPCEntity npcEntity) {
      PatrolController controller = controller(npcEntity);
      return controller != null && controller.isActivelyMoving();
   }

   public static boolean canMaterialize(Object owner, Location location) {
      if (location == null || location.getWorld() == null) {
         return false;
      } else {
         return !hasConfiguredPatrol(owner)
            ? ChunkLocationChecker.chunkAtLocationIsLoaded(location)
            : NMSManager.getAdapter() != null && NMSManager.getAdapter().isPositionEntityTicking(location);
      }
   }

   public static boolean hasConfiguredPatrol(Object owner) {
      return controller(owner) != null || hasPatrolConfiguration(owner);
   }

   public static void registerPendingNpc(NPCEntity npc) {
      if (instance != null
         && !npc.getNPCsConfigFields().isInstanced()
         && npc.getNPCsConfigFields().getPatrolRoute() != null
         && npc.getSpawnLocation() != null
         && npc.getSpawnLocation().getWorld() != null) {
         instance.register(new NpcPatrolActor(npc));
      }
   }

   public static boolean pause(Object owner) {
      PatrolController controller = controller(owner);
      if (controller == null) {
         return false;
      } else {
         controller.pause();
         return true;
      }
   }

   public static boolean resume(Object owner) {
      PatrolController controller = controller(owner);
      if (controller == null) {
         return false;
      } else {
         controller.resume();
         return true;
      }
   }

   public static boolean hold(Object owner, Vector offset) {
      PatrolController controller = controller(owner);
      return controller != null && controller.moveToOffset(offset, true);
   }

   public static boolean walkTo(Object owner, Vector offset) {
      PatrolController controller = controller(owner);
      return controller != null && controller.moveToOffset(offset, false);
   }

   public static boolean teleport(Object owner, Vector offset) {
      PatrolController controller = controller(owner);
      return controller != null && controller.teleportToOffset(offset);
   }

   public static Map<String, Map<String, Object>> diagnostics() {
      if (instance == null) {
         return Map.of();
      } else {
         Map<String, Map<String, Object>> diagnostics = new LinkedHashMap<>();

         for (PatrolController controller : instance.controllers.values()) {
            diagnostics.put(controller.actor().runtimeId().toString(), controller.diagnostics());
         }

         return Map.copyOf(diagnostics);
      }
   }

   public static Map<String, Object> diagnostics(Object owner) {
      PatrolController controller = controller(owner);
      return controller == null ? Map.of() : controller.diagnostics();
   }

   public static void refresh(Object owner) {
      if (instance != null && owner != null) {
         PatrolController previous = instance.byOwner.remove(owner);
         if (previous != null) {
            instance.controllers.remove(previous.actor().runtimeId(), previous);
            previous.retire(true);
         }

         PatrolActor refreshed = actor(owner);
         if (refreshed != null) {
            instance.register(refreshed);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEliteSpawn(EliteMobSpawnEvent event) {
      if (event.getEliteMobEntity() instanceof CustomBossEntity boss && boss.getCustomBossesConfigFields().getPatrolRoute() != null) {
         this.register(new BossPatrolActor(boss));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onNpcSpawn(NPCEntitySpawnEvent event) {
      NPCEntity npc = event.getNPCEntity();
      if (npc.getNPCsConfigFields().getPatrolRoute() != null) {
         this.register(new NpcPatrolActor(npc));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onCombatEnter(EliteMobEnterCombatEvent event) {
      PatrolController controller = controller(event.getEliteMobEntity());
      if (controller != null) {
         controller.setCombatHeld(true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onCombatExit(EliteMobExitCombatEvent event) {
      PatrolController controller = controller(event.getEliteMobEntity());
      if (controller != null) {
         controller.setCombatHeld(false);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteRemove(EliteMobRemoveEvent event) {
      this.handleRemoval(event.getEliteMobEntity(), event.getRemovalReason());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onNpcRemove(NPCEntityRemoveEvent event) {
      this.handleRemoval(event.getNPCEntity(), event.getRemovalReason());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onWorldLoad(WorldLoadEvent event) {
      World world = event.getWorld();

      for (PatrolController controller : this.controllers.values()) {
         controller.worldLoaded(world);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      for (PatrolController controller : this.controllers.values()) {
         if (controller.actor().origin().worldName().equals(event.getWorld().getName())) {
            controller.worldUnloaded();
         }
      }
   }

   private void tick() {
      this.tick++;
      Iterator<Entry<UUID, PatrolController>> iterator = this.controllers.entrySet().iterator();

      while (iterator.hasNext()) {
         PatrolController controller = iterator.next().getValue();

         boolean remove;
         try {
            remove = controller.tick(this.tick);
         } catch (RuntimeException var7) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Patrol controller failed for " + controller.actor().displayName(), (Throwable)var7);

            try {
               controller.retire(false);
            } catch (RuntimeException var6) {
               MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Patrol cleanup also failed for " + controller.actor().displayName(), (Throwable)var6);
            }

            remove = true;
         }

         if (remove) {
            this.byOwner.remove(controller.actor().owner());
            iterator.remove();
         }
      }

      if (this.tick % 6000L == 0L) {
         this.stateStore.saveIfDirty();
      }
   }

   private void register(PatrolActor actor) {
      PatrolController existing = this.byOwner.get(actor.owner());
      if (existing == null) {
         try {
            PatrolController controller = new PatrolController(actor, this.stateStore);
            PatrolController replaced = this.controllers.put(actor.runtimeId(), controller);
            if (replaced != null) {
               this.byOwner.remove(replaced.actor().owner());
            }

            this.byOwner.put(actor.owner(), controller);
         } catch (RuntimeException var5) {
            Logger.warn("Failed to initialize patrol for " + actor.displayName() + ": " + var5.getMessage());
         }
      }
   }

   private void handleRemoval(Object owner, RemovalReason reason) {
      PatrolController controller = this.byOwner.get(owner);
      if (controller != null) {
         if (keepsLogicalActor(reason, controller.actor())) {
            if (reason == RemovalReason.DEATH
               || reason == RemovalReason.BOSS_TIMEOUT
               || reason == RemovalReason.NPC_TIMEOUT
               || reason == RemovalReason.PHASE_BOSS_RESET
               || reason == RemovalReason.PHASE_BOSS_PHASE_END
               || reason == RemovalReason.ENTITY_REPLACEMENT
               || reason == RemovalReason.EFFECT_TIMEOUT) {
               controller.holdUntilBodyReplacement();
            }
         } else {
            boolean discardState = reason == RemovalReason.REMOVE_COMMAND || reason == RemovalReason.KILL_COMMAND || reason == RemovalReason.ARENA_RESET;
            controller.retire(discardState);
            this.controllers.remove(controller.actor().runtimeId(), controller);
            this.byOwner.remove(owner);
         }
      }
   }

   private static boolean keepsLogicalActor(RemovalReason reason, PatrolActor actor) {
      return switch (reason) {
         case CHUNK_UNLOAD, WORLD_UNLOAD, SHUTDOWN, PHASE_BOSS_RESET, PHASE_BOSS_PHASE_END, EFFECT_TIMEOUT, ENTITY_REPLACEMENT -> true;
         case DEATH, BOSS_TIMEOUT, NPC_TIMEOUT -> actor.persistsWhileDetached();
         default -> false;
      };
   }

   private static PatrolController controller(Object owner) {
      return instance != null && owner != null ? instance.byOwner.get(owner) : null;
   }

   private static PatrolActor actor(Object owner) {
      if (owner instanceof CustomBossEntity boss && boss.getCustomBossesConfigFields().getPatrolRoute() != null) {
         return new BossPatrolActor(boss);
      } else {
         return owner instanceof NPCEntity npc && npc.getNPCsConfigFields().getPatrolRoute() != null ? new NpcPatrolActor(npc) : null;
      }
   }

   private static boolean hasPatrolConfiguration(Object owner) {
      if (owner instanceof CustomBossEntity boss) {
         return boss.getCustomBossesConfigFields().getPatrolRoute() != null;
      } else {
         return owner instanceof NPCEntity npc ? npc.getNPCsConfigFields().getPatrolRoute() != null : false;
      }
   }
}
