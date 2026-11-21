package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public final class ChunkManager {
   private final Graves plugin;
   private final List<ChunkManager.ChunkTypeResolver> resolvers = new CopyOnWriteArrayList<>();
   private volatile ChunkManager.ChunkType overrideType;
   private volatile ChunkManager.ChunkType cachedDetectedType;

   public ChunkManager(Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
   }

   public void registerChunkTypeResolver(ChunkManager.ChunkTypeResolver resolver) {
      if (resolver != null) {
         this.resolvers.add(resolver);
         this.cachedDetectedType = null;
      }
   }

   public void setChunkTypeOverride(ChunkManager.ChunkType type) {
      this.overrideType = type;
      this.cachedDetectedType = null;
   }

   public ChunkManager.ChunkType getChunkType() {
      ChunkManager.ChunkType override = this.overrideType;
      if (override != null) {
         return override;
      } else {
         ChunkManager.ChunkType cached = this.cachedDetectedType;
         if (cached != null) {
            return cached;
         } else {
            ChunkManager.ChunkType detected = this.detectChunkType();
            this.cachedDetectedType = detected;
            return detected;
         }
      }
   }

   public boolean ensureLoadedAndExecute(Location anchor, Location location, boolean generate, boolean allowForceLoad, Runnable task) {
      if (location == null) {
         return false;
      } else {
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            if (anchor == null) {
               anchor = location;
            }

            Location useAnchor = anchor;
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            if (world.isChunkLoaded(chunkX, chunkZ)) {
               this.execute(useAnchor, task);
               return true;
            } else {
               ChunkManager.ChunkType type = this.getChunkType().effective();
               boolean folia = type == ChunkManager.ChunkType.FOLIA;
               CompletableFuture<?> async = this.tryGetChunkAtAsync(world, chunkX, chunkZ, generate);
               if (async != null) {
                  async.whenComplete((ok, ex) -> {
                     if (ex != null) {
                        if (!folia) {
                           this.runMainThreadLoad(world, chunkX, chunkZ, generate, allowForceLoad, () -> this.execute(useAnchor, task));
                        } else {
                           this.plugin.getLogger().severe(ex.getMessage());
                           this.plugin.logStackTrace(ex);
                        }
                     } else {
                        this.execute(useAnchor, task);
                     }
                  });
                  return true;
               } else if (folia) {
                  return false;
               } else {
                  this.runMainThreadLoad(world, chunkX, chunkZ, generate, allowForceLoad, () -> this.execute(useAnchor, task));
                  return true;
               }
            }
         }
      }
   }

   private void execute(Location anchor, Runnable task) {
      if (this.getChunkType().effective() == ChunkManager.ChunkType.FOLIA) {
         this.plugin.getSchedulerManager().execute(anchor, task);
      } else {
         this.plugin.getSchedulerManager().runTask(task);
      }
   }

   private ChunkManager.ChunkType detectChunkType() {
      for (ChunkManager.ChunkTypeResolver resolver : this.resolvers) {
         try {
            ChunkManager.ChunkType resolved = resolver.resolve(this.plugin);
            if (resolved != null) {
               return resolved;
            }
         } catch (Throwable var6) {
            this.plugin.getLogger().severe(var6.getMessage());
            this.plugin.logStackTrace(var6);
         }
      }

      try {
         if (this.plugin.getVersionManager().isFolia()) {
            return ChunkManager.ChunkType.FOLIA;
         } else {
            return this.plugin.getVersionManager().isPaper() ? ChunkManager.ChunkType.PAPER : ChunkManager.ChunkType.SPIGOT;
         }
      } catch (Throwable var5) {
         this.plugin.getLogger().severe(var5.getMessage());
         this.plugin.logStackTrace(var5);

         try {
            Method m = World.class.getMethod("getChunkAtAsync", int.class, int.class, boolean.class, boolean.class);
            if (m != null) {
               return ChunkManager.ChunkType.PAPER;
            }
         } catch (Throwable var4) {
         }

         return ChunkManager.ChunkType.UNKNOWN;
      }
   }

   private CompletableFuture<?> tryGetChunkAtAsync(World world, int chunkX, int chunkZ, boolean generate) {
      try {
         Method m = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class, boolean.class);
         if (m.invoke(world, chunkX, chunkZ, generate, true) instanceof CompletableFuture<?> cf) {
            return cf;
         }
      } catch (NoSuchMethodException var8) {
      } catch (Throwable var9) {
         this.plugin.getLogger().severe(var9.getMessage());
         this.plugin.logStackTrace(var9);
      }

      return null;
   }

   private void runMainThreadLoad(World world, int chunkX, int chunkZ, boolean generate, boolean allowForceLoad, Runnable onLoaded) {
      this.plugin.getSchedulerManager().runTask(() -> {
         Chunk chunk = null;
         boolean didForceLoad = false;

         try {
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
               world.loadChunk(chunkX, chunkZ, false);
            }

            if (!world.isChunkLoaded(chunkX, chunkZ) && generate) {
               world.loadChunk(chunkX, chunkZ, true);
            }

            if (allowForceLoad) {
               try {
                  chunk = world.getChunkAt(chunkX, chunkZ);
                  if (!chunk.isForceLoaded()) {
                     chunk.setForceLoaded(true);
                     didForceLoad = true;
                  }
               } catch (Throwable var19) {
                  this.plugin.getLogger().severe(var19.getMessage());
                  this.plugin.logStackTrace(var19);
               }
            }

            onLoaded.run();
         } catch (Throwable var20) {
            this.plugin.getLogger().severe(var20.getMessage());
            this.plugin.logStackTrace(var20);
            onLoaded.run();
         } finally {
            if (didForceLoad) {
               try {
                  chunk.setForceLoaded(false);
               } catch (Throwable var18) {
                  this.plugin.getLogger().severe(var18.getMessage());
                  this.plugin.logStackTrace(var18);
               }
            }
         }
      });
   }

   public static enum ChunkType {
      SPIGOT,
      PAPER,
      FOLIA,
      UNKNOWN;

      public ChunkManager.ChunkType effective() {
         return this == UNKNOWN ? SPIGOT : this;
      }
   }

   @FunctionalInterface
   public interface ChunkTypeResolver {
      ChunkManager.ChunkType resolve(Graves var1);
   }
}
