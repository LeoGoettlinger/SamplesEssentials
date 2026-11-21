package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.mind.EliteMindService;
import com.magmaguy.elitemobs.api.power.ElitePowerActionPosition;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.dungeons.CombatContent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.ai.MindActionContext;
import com.magmaguy.elitemobs.magmacore.ai.MindActionRequest;
import com.magmaguy.elitemobs.magmacore.ai.MindActionResult;
import com.magmaguy.elitemobs.magmacore.ai.MindFailure;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import com.magmaguy.elitemobs.magmacore.ai.MindPosition;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;

public final class EliteMindServiceModule {
   private static EliteMindServiceImpl implementation;
   private static NMSAdapter adapter;
   private static BukkitTask mindTickTask;

   private EliteMindServiceModule() {
   }

   public static Function<Consumer<LivingEntity>, LivingEntity> behaviorBodyFactory(CustomBossEntity actor, Location location) {
      CustomBossesConfigFields fields = actor.getCustomBossesConfigFields();
      if (fields.isAi() && !fields.isNeutral()) {
         String filename = fields.getBehavior();
         if (filename == null) {
            if (fields.getEntityType() == EntityType.ENDERMAN && isDungeonEncounter(actor, location)) {
               filename = "basic_melee.lua";
            } else {
               EliteMobProperties properties = EliteMobProperties.getPluginData(fields.getEntityType());
               filename = properties == null ? null : properties.getBehavior();
            }
         }

         if (filename == null) {
            return null;
         } else {
            String reference = filename.trim();
            if (!reference.isEmpty() && !reference.equalsIgnoreCase("native")) {
               EliteMindServiceImpl current = requireImplementation();
               current.validateBehavior(reference);
               return configure -> current.spawnBehaviorBody(actor, location, reference, configure);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean isDungeonEncounter(CustomBossEntity actor, Location location) {
      Set<EliteEntity> visited = new HashSet<>();

      for (EliteEntity owner = actor; owner != null && visited.add(owner); owner = owner.getSummoningEntity()) {
         if (owner instanceof InstancedBossEntity) {
            return true;
         }

         if (owner instanceof CustomBossEntity boss && boss.getEmPackage() instanceof CombatContent) {
            return true;
         }
      }

      if (location != null && location.getWorld() != null) {
         World world = location.getWorld();
         return DungeonInstance.forWorld(world.getUID()) != null || EMPackage.getContent(world.getName()) instanceof CombatContent;
      } else {
         return false;
      }
   }

   public static EliteEntity spawnBehaviorElite(Location location, EntityType type, int level, SpawnReason reason, Set<PowersConfigFields> powers) {
      return requireImplementation().spawnBehaviorElite(location, type, level, reason, powers);
   }

   public static void detachBehavior(EliteEntity actor) {
      EliteMindServiceImpl.detachBehavior(actor);
   }

   private static EliteMindServiceImpl requireImplementation() {
      if (implementation == null) {
         throw new IllegalStateException("EliteMobs behavior service is unavailable");
      } else {
         return implementation;
      }
   }

   public static void initialize() {
      if (implementation == null) {
         NMSAdapter selectedAdapter = NMSManager.getAdapter();
         if (selectedAdapter != null) {
            EliteLuaPowerServiceImpl luaPowerService = EliteLuaPowerServiceModule.implementation();
            if (luaPowerService == null) {
               throw new IllegalStateException("Lua power service must initialize before the Mind service");
            } else {
               MindHost mindHost;
               try {
                  mindHost = selectedAdapter.createMindHost(
                     new NamespacedKey(MetadataHandler.PLUGIN, "native_mind"), EliteMindServiceModule::reportFailure, EliteMindServiceModule::dispatchAction
                  );
               } catch (UnsupportedOperationException var11) {
                  Logger.info("Native Mind interface is unavailable on this Minecraft version.");
                  return;
               }

               EliteMindServiceImpl candidate = null;

               try {
                  candidate = new EliteMindServiceImpl(mindHost, luaPowerService);
                  adapter = selectedAdapter;
                  implementation = candidate;
                  Bukkit.getPluginManager().registerEvents(candidate, MetadataHandler.PLUGIN);
                  Bukkit.getServicesManager().register(EliteMindService.class, candidate, MetadataHandler.PLUGIN, ServicePriority.Normal);
                  candidate.reconcileLoadedBodies();
                  mindTickTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, mindHost::tick, 1L, 1L);
               } catch (Error | RuntimeException var12) {
                  BukkitTask failedTickTask = mindTickTask;
                  mindTickTask = null;
                  if (failedTickTask != null) {
                     failedTickTask.cancel();
                  }

                  if (candidate != null) {
                     try {
                        Bukkit.getServicesManager().unregister(EliteMindService.class, candidate);
                     } catch (RuntimeException var10) {
                        var12.addSuppressed(var10);
                     }

                     try {
                        HandlerList.unregisterAll(candidate);
                     } catch (RuntimeException var9) {
                        var12.addSuppressed(var9);
                     }

                     try {
                        candidate.shutdown();
                     } catch (RuntimeException var8) {
                        var12.addSuppressed(var8);
                     }
                  }

                  implementation = null;
                  adapter = null;

                  try {
                     selectedAdapter.shutdownMindHost();
                  } catch (RuntimeException var7) {
                     var12.addSuppressed(var7);
                  }

                  throw var12;
               }
            }
         }
      }
   }

   public static void shutdown() {
      EliteMindServiceImpl current = implementation;
      NMSAdapter selectedAdapter = adapter;
      BukkitTask currentTickTask = mindTickTask;
      implementation = null;
      adapter = null;
      mindTickTask = null;
      if (currentTickTask != null) {
         currentTickTask.cancel();
      }

      if (current != null) {
         try {
            Bukkit.getServicesManager().unregister(EliteMindService.class, current);
         } catch (RuntimeException var7) {
            Logger.warn("Failed to unregister the EliteMobs Mind service: " + var7.getMessage());
         }

         try {
            HandlerList.unregisterAll(current);
         } catch (RuntimeException var6) {
            Logger.warn("Failed to unregister EliteMobs Mind listeners: " + var6.getMessage());
         }

         try {
            current.shutdown();
         } catch (RuntimeException var5) {
            Logger.warn("Failed to finish EliteMobs Mind service shutdown: " + var5.getMessage());
         }
      }

      if (selectedAdapter != null) {
         try {
            selectedAdapter.shutdownMindHost();
         } catch (RuntimeException var4) {
            Logger.warn("Failed to shut down the native EliteMobs Mind host: " + var4.getMessage());
         }
      }
   }

   public static boolean suspendForChunkUnload(EliteEntity eliteEntity, LivingEntity removedBody) {
      EliteMindServiceImpl current = implementation;
      return current != null && current.suspendForChunkUnload(eliteEntity, removedBody);
   }

   public static boolean hasNaturalSpawnProviders() {
      EliteMindServiceImpl current = implementation;
      return current != null && current.hasNaturalSpawnProviders();
   }

   public static boolean replaceNaturalSpawn(CreatureSpawnEvent event, int suggestedLevel) {
      EliteMindServiceImpl current = implementation;
      return current != null && current.replaceNaturalSpawn(event, suggestedLevel);
   }

   public static Optional<EliteEntity> spawnInternal(InternalMindActorSpawnRequest request) {
      EliteMindServiceImpl current = implementation;
      return current == null ? Optional.empty() : Optional.of(current.spawnInternal(request));
   }

   public static boolean clearInternal(EliteEntity actor) {
      EliteMindServiceImpl current = implementation;
      return current != null && current.clearProgram(actor);
   }

   private static void reportFailure(MindFailure failure) {
      String cause = failure.cause() == null ? "" : " (" + failure.cause().getClass().getSimpleName() + ": " + failure.cause().getMessage() + ")";
      Logger.warn("Mind " + failure.programIdentifier() + " callback " + failure.callbackIdentifier() + " failed with " + failure.kind() + cause);
   }

   private static MindActionResult dispatchAction(MindActionContext context, MindActionRequest request) {
      EliteLuaPowerServiceImpl luaPowerService = EliteLuaPowerServiceModule.implementation();
      if (luaPowerService == null) {
         return MindActionResult.REJECTED;
      } else {
         NamespacedKey actionKey = NamespacedKey.fromString(request.identifier());
         if (actionKey == null) {
            return MindActionResult.REJECTED;
         } else {
            ElitePowerActionResult result = luaPowerService.dispatchMindAction(
               context.logicalOwner(),
               context.body().entity().getUniqueId(),
               actionKey,
               toPublicPayload(request.payload()),
               context.gameTick(),
               context.generation()
            );
            return MindActionResult.valueOf(result.name());
         }
      }
   }

   private static Map<String, Object> toPublicPayload(Map<String, ?> nativePayload) {
      LinkedHashMap<String, Object> converted = new LinkedHashMap<>();

      for (Entry<String, ?> entry : nativePayload.entrySet()) {
         Object value = entry.getValue();
         converted.put(
            entry.getKey(),
            value instanceof MindPosition position ? new ElitePowerActionPosition(position.world(), position.x(), position.y(), position.z()) : value
         );
      }

      return converted;
   }
}
