package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.utils.ChunkVectorizer;
import com.magmaguy.elitemobs.utils.PersistentVanillaData;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class CrashFix implements Listener {
   private static final HashSet<ChunkVectorizer.Key> temporarilyCachedChunks = new HashSet<>();
   public static String key = "EliteMobsCullable";
   public static String fallingBlocksKey = "VisualFallingBlocks";
   public static HashSet<ChunkVectorizer.Key> knownSessionChunks = new HashSet<>();

   public static void persistentTracker(Entity entity) {
      PersistentVanillaData.write(entity, key, "delete_me");
   }

   public static void registerVisualFallingBlock(Entity entity) {
      PersistentVanillaData.write(entity, fallingBlocksKey, "delete_me");
   }

   public static boolean isPersistentEntity(Entity entity) {
      return PersistentVanillaData.hasString(entity, key);
   }

   public static boolean isVisualFallingBlock(Entity entity) {
      return PersistentVanillaData.hasString(entity, fallingBlocksKey);
   }

   public static void shutdown() {
      temporarilyCachedChunks.clear();
      knownSessionChunks.clear();
   }

   public static void startupCheck() {
      for (World world : Bukkit.getWorlds()) {
         for (Chunk chunk : world.getLoadedChunks()) {
            chunkCheck(chunk);
            knownSessionChunks.add(ChunkVectorizer.key(chunk));
         }
      }
   }

   private static void chunkCheck(Chunk chunk) {
      for (Entity entity : chunk.getEntities()) {
         if (isPersistentEntity(entity)) {
            entity.remove();
         }
      }
   }

   private static void delayedChunkCheck(Chunk chunk, final ChunkVectorizer.Key hashedChunk) {
      final Entity[] entities = (Entity[])chunk.getEntities().clone();
      (new BukkitRunnable() {
         public void run() {
            for (Entity entity : entities) {
               if (CrashFix.isPersistentEntity(entity)) {
                  entity.remove();
               }
            }

            CrashFix.temporarilyCachedChunks.remove(hashedChunk);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, 1L);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onChunkLoad(ChunkLoadEvent event) {
      ChunkVectorizer.Key hashedChunk = ChunkVectorizer.key(event.getChunk());
      if (!knownSessionChunks.contains(hashedChunk) && temporarilyCachedChunks.add(hashedChunk)) {
         knownSessionChunks.add(hashedChunk);
         delayedChunkCheck(event.getChunk(), hashedChunk);
      }
   }
}
