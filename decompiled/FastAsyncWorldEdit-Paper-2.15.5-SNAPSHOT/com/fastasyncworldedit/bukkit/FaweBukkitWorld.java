package com.fastasyncworldedit.bukkit;

import com.fastasyncworldedit.bukkit.adapter.NMSAdapter;
import com.fastasyncworldedit.bukkit.util.WorldUnloadedException;
import com.fastasyncworldedit.core.math.IntPair;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class FaweBukkitWorld extends BukkitWorld {
   private static final Map<World, FaweBukkitWorld> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
   private final ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> SENDING_CHUNKS = new ConcurrentHashMap<>();

   private FaweBukkitWorld(final World world) {
      super(world);
   }

   public static FaweBukkitWorld of(World world) {
      return CACHE.compute(world, (__, val) -> {
         if (val == null) {
            return new FaweBukkitWorld(world);
         } else {
            val.updateReference();
            return (FaweBukkitWorld)val;
         }
      });
   }

   public static FaweBukkitWorld of(String worldName) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         throw new UnsupportedOperationException("Unable to find org.bukkit.World instance for " + worldName + ". Is it loaded?");
      } else {
         return of(world);
      }
   }

   public static ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> getWorldSendingChunksMap(FaweBukkitWorld world) {
      return world.SENDING_CHUNKS;
   }

   public static ConcurrentHashMap<IntPair, NMSAdapter.ChunkSendLock> getWorldSendingChunksMap(String worldName) {
      return of(worldName).SENDING_CHUNKS;
   }

   private void updateReference() {
      World world = this.getWorld();
      World bukkitWorld = Bukkit.getWorld(this.worldNameRef);
      if (bukkitWorld == null) {
         throw new WorldUnloadedException(this.worldNameRef);
      } else {
         if (bukkitWorld != world) {
            this.worldRef = new WeakReference<>(bukkitWorld);
         }
      }
   }
}
