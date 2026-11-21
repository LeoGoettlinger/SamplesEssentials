package com.magmaguy.elitemobs.entitytracker;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.NPCEntitySpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.TemporaryBlockManager;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityRemoveEvent.Cause;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityTracker implements Listener {
   private static final HashMap<UUID, EliteEntity> eliteMobEntities = new HashMap<>();
   private static final HashMap<UUID, NPCEntity> npcEntities = new HashMap<>();
   private static BukkitTask ManagedEntityTask = null;

   public static void registerEliteMob(EliteEntity eliteEntity) {
      tryRegisterEliteMob(eliteEntity);
   }

   public static boolean tryRegisterEliteMob(EliteEntity eliteEntity) {
      LivingEntity body = eliteEntity.getLivingEntity();
      if (body != null && !body.isDead()) {
         EliteMobSpawnEvent eliteMobSpawnEvent = new EliteMobSpawnEvent(eliteEntity);
         new EventCaller(eliteMobSpawnEvent);
         if (!eliteMobSpawnEvent.isCancelled() && eliteEntity.getLivingEntity() == body && !body.isDead()) {
            PersistentTagger.tagElite(body, eliteEntity.getEliteUUID());
            eliteMobEntities.put(eliteEntity.getEliteUUID(), eliteEntity);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void registerEliteMob(EliteEntity eliteEntity, LivingEntity livingEntity) {
      if (eliteEntity.getLivingEntity() != livingEntity) {
         throw new IllegalArgumentException("Elite actor must be initialized before registration");
      } else {
         registerEliteMob(eliteEntity);
      }
   }

   public static boolean isEliteMob(Entity entity) {
      return PersistentTagger.isEliteEntity(entity);
   }

   @Nullable
   public static EliteEntity getEliteMobEntity(Entity entity) {
      return PersistentTagger.getEliteEntity(entity);
   }

   public static void registerVisualEffects(Entity entity) {
      PersistentTagger.tagVisualEffect(entity);
   }

   public static boolean isVisualEffect(Entity entity) {
      return PersistentTagger.isVisualEffect(entity);
   }

   public static boolean unregisterEliteEntity(Entity entity, RemovalReason removalReason) {
      EliteEntity eliteEntity = getEliteMobEntity(entity);
      if (eliteEntity == null) {
         return false;
      } else {
         eliteEntity.remove(removalReason);
         return true;
      }
   }

   public static void registerNPCEntity(NPCEntity npc) {
      NPCEntitySpawnEvent npcEntitySpawnEvent = new NPCEntitySpawnEvent(npc.getVillager(), npc);
      new EventCaller(npcEntitySpawnEvent);
      if (!npcEntitySpawnEvent.isCancelled()) {
         npcEntities.put(npc.getUuid(), npc);
         PersistentTagger.tagNPC(npc.getVillager(), npc.getUuid());
      }
   }

   public static boolean isNPCEntity(Entity entity) {
      return PersistentTagger.isNPC(entity);
   }

   public static NPCEntity getNPCEntity(Entity entity) {
      return PersistentTagger.getNPC(entity);
   }

   public static boolean unregisterVisualEffect(Entity entity) {
      if (isVisualEffect(entity)) {
         entity.remove();
         return true;
      } else {
         return false;
      }
   }

   public static void addTemporaryBlock(Block block, int ticks, Material replacementMaterial) {
      if (!TemporaryBlockManager.isTemporaryBlock(block) || !block.getType().equals(Material.RED_BANNER)) {
         TemporaryBlockManager.addTemporaryBlock(block, ticks, replacementMaterial);
      }
   }

   public static boolean isTemporaryBlock(Block block) {
      return TemporaryBlockManager.isTemporaryBlock(block);
   }

   public static void removeTemporaryBlock(Block block) {
      TemporaryBlockManager.removeTemporaryBlock(block);
   }

   public static void registerProjectileEntity(Projectile projectile) {
      PersistentTagger.tagEliteProjectile(projectile);
   }

   public static boolean isProjectileEntity(Entity entity) {
      return PersistentTagger.isEliteProjectile(entity);
   }

   public static boolean unregisterNPCEntity(Entity entity, RemovalReason removalReason) {
      NPCEntity npcEntity = getNPCEntity(entity);
      if (npcEntity == null) {
         return false;
      } else {
         npcEntity.remove(removalReason);
         return true;
      }
   }

   public static boolean unregisterProjectileEntity(Entity entity) {
      if (isProjectileEntity(entity)) {
         entity.remove();
         return true;
      } else {
         return false;
      }
   }

   public static void wipeChunk(Chunk chunk, RemovalReason removalReason) {
      for (Entity entity : chunk.getEntities()) {
         unregister(entity, removalReason);
      }
   }

   public static void wipeWorld(World world, RemovalReason removalReason) {
      for (Chunk chunk : world.getLoadedChunks()) {
         wipeChunk(chunk, removalReason);
      }
   }

   public static void unregister(Entity entity, RemovalReason removalReason) {
      if (!unregisterEliteEntity(entity, removalReason)) {
         if (!unregisterVisualEffect(entity)) {
            if (!unregisterProjectileEntity(entity)) {
               if (unregisterNPCEntity(entity, removalReason)) {
               }
            }
         }
      }
   }

   public static void wipeShutdown() {
      if (ManagedEntityTask != null) {
         ManagedEntityTask.cancel();
      }

      for (EliteEntity eliteEntity : ((HashMap)eliteMobEntities.clone()).values()) {
         eliteEntity.remove(RemovalReason.SHUTDOWN);
      }

      getEliteMobEntities().clear();

      for (NPCEntity npcEntity : ((HashMap)npcEntities.clone()).values()) {
         npcEntity.remove(RemovalReason.SHUTDOWN);
      }

      getNpcEntities().clear();
      TemporaryBlockManager.shutdown();

      for (World world : Bukkit.getWorlds()) {
         for (Entity entity : world.getEntities()) {
            unregister(entity, RemovalReason.SHUTDOWN);
         }
      }

      CrashFix.knownSessionChunks.clear();
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      wipeWorld(event.getWorld(), RemovalReason.WORLD_UNLOAD);
      if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
         for (Entity entity : event.getWorld().getEntities()) {
            DisguiseEntity.undisguise(entity);
         }
      }

      purgeWorldReferences(event.getWorld());
      scheduleWorldRetentionCanary(event.getWorld());
   }

   private static void purgeWorldReferences(World world) {
      for (EliteEntity eliteEntity : new ArrayList<>(eliteMobEntities.values())) {
         if (eliteEntity.referencesWorld(world)) {
            eliteEntity.remove(RemovalReason.WORLD_UNLOAD);
         }
      }

      eliteMobEntities.values().removeIf(eliteEntityx -> eliteEntityx.referencesWorld(world));

      for (NPCEntity npcEntity : new ArrayList<>(npcEntities.values())) {
         if (npcEntity.referencesWorld(world)) {
            npcEntity.remove(RemovalReason.WORLD_UNLOAD);
         }
      }

      npcEntities.values().removeIf(npcEntityx -> npcEntityx.referencesWorld(world));
   }

   private static void scheduleWorldRetentionCanary(World world) {
      String worldName = world.getName();
      UUID worldUUID = world.getUID();
      long unloadedAtNanos = System.nanoTime();
      WeakReference<World> reference = new WeakReference<>(world);
      Long canaryTicksOverride = Long.getLong("elitemobs.worldRetentionCanaryTicks");
      long delayTicks = canaryTicksOverride != null ? canaryTicksOverride : 6000L;
      Bukkit.getScheduler()
         .runTaskLater(
            MetadataHandler.PLUGIN,
            () -> {
               if (Bukkit.getWorld(worldUUID) == null) {
                  if (canaryTicksOverride != null) {
                     System.gc();
                  }

                  if (reference.get() == null) {
                     if (canaryTicksOverride != null) {
                        Logger.info("World retention canary: " + worldName + " was garbage collected after unloading.");
                     }
                  } else {
                     long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - unloadedAtNanos);
                     Logger.warn(
                        "World "
                           + worldName
                           + " ("
                           + worldUUID
                           + ") has not been garbage collected "
                           + elapsedSeconds
                           + " seconds after unloading. This does not confirm a memory leak or identify a responsible plugin; garbage collection may not have examined the world yet."
                     );
                     Logger.warn(
                        "If this repeats, capture a heap dump while the server is still running. On the server host/container, as the server OS user, run the JDK command: jcmd "
                           + ProcessHandle.current().pid()
                           + " GC.heap_dump elitemobs-heap-"
                           + System.currentTimeMillis()
                           + ".hprof (capture can pause the server and create a large file; keep the dump private)."
                     );
                     Logger.warn(
                        "In Eclipse Memory Analyzer, locate this world by name/UUID and inspect Paths to GC Roots, excluding weak references, to identify retaining fields, tasks or registries in any plugin or the server. If the world was collected during capture, no retaining path will remain."
                     );
                  }
               }
            },
            delayTicks
         );
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onMine(BlockBreakEvent event) {
      if (isTemporaryBlock(event.getBlock())) {
         event.setDropItems(false);
         removeTemporaryBlock(event.getBlock());
      }
   }

   public static void managedEntityWatchdog() {
      ManagedEntityTask = (new BukkitRunnable() {
         public void run() {
            new HashSet<>(EntityTracker.eliteMobEntities.values()).forEach(value -> {
               if (value.getLivingEntity() != null && !value.getLivingEntity().isValid()) {
                  LivingEntity removedBody = value.getLivingEntity();
                  if (!EliteMindServiceModule.suspendForChunkUnload(value, removedBody)) {
                     value.remove(RemovalReason.CHUNK_UNLOAD);
                  }
               }
            });
            new HashSet<>(EntityTracker.npcEntities.values()).forEach(value -> {
               if (value.getVillager() != null && !value.getVillager().isValid()) {
                  value.remove(RemovalReason.CHUNK_UNLOAD);
               }
            });
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onRemove(EntityRemoveEvent event) {
      EliteEntity eliteEntity = getEliteMobEntity(event.getEntity());
      if (event.getCause().equals(Cause.UNLOAD)) {
         if (eliteEntity == null
            || !(event.getEntity() instanceof LivingEntity livingEntity && EliteMindServiceModule.suspendForChunkUnload(eliteEntity, livingEntity))) {
            unregister(event.getEntity(), RemovalReason.CHUNK_UNLOAD);
         }
      }
   }

   @Generated
   public static HashMap<UUID, EliteEntity> getEliteMobEntities() {
      return eliteMobEntities;
   }

   @Generated
   public static HashMap<UUID, NPCEntity> getNpcEntities() {
      return npcEntities;
   }
}
