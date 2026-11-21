package com.fastasyncworldedit.bukkit.listener;

import com.fastasyncworldedit.bukkit.FaweBukkit;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.util.FaweTimer;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.Iterator;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

@Deprecated(
   since = "2.0.0"
)
public abstract class ChunkListener implements Listener {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected int rateLimit = 0;
   protected Location lastCancelPos;
   private final int[] badLimit = new int[]{
      Settings.settings().TICK_LIMITER.PHYSICS_MS, Settings.settings().TICK_LIMITER.FALLING, Settings.settings().TICK_LIMITER.ITEMS
   };
   @Deprecated(
      since = "2.0.0"
   )
   public static boolean physicsFreeze = false;
   @Deprecated(
      since = "2.0.0"
   )
   public static boolean itemFreeze = false;
   protected final Long2ObjectOpenHashMap<Boolean> badChunks = new Long2ObjectOpenHashMap();
   private final Long2ObjectOpenHashMap<int[]> counter = new Long2ObjectOpenHashMap();
   private int lastX = Integer.MIN_VALUE;
   private int lastZ = Integer.MIN_VALUE;
   private int[] lastCount;
   protected int physSkip;
   protected boolean physCancel;
   protected long physCancelPair;
   protected long physStart;
   protected long physTick;

   public ChunkListener() {
      if (Settings.settings().TICK_LIMITER.ENABLED) {
         PluginManager plm = Bukkit.getPluginManager();
         Plugin plugin = Fawe.<FaweBukkit>platform().getPlugin();
         plm.registerEvents(this, plugin);
         TaskManager.taskManager().repeat(() -> {
            Location tmpLoc = this.lastCancelPos;
            if (tmpLoc != null) {
               LOGGER.info("[FAWE Tick Limiter] Detected and cancelled physics lag source at {}", tmpLoc);
            }

            this.rateLimit--;
            physicsFreeze = false;
            itemFreeze = false;
            this.lastZ = Integer.MIN_VALUE;
            this.physSkip = 0;
            this.physCancelPair = Long.MIN_VALUE;
            this.physCancel = false;
            this.lastCancelPos = null;
            this.counter.clear();
            Iterator i$ = this.badChunks.long2ObjectEntrySet().iterator();

            while (i$.hasNext()) {
               Entry<Boolean> entry = (Entry<Boolean>)i$.next();
               long key = entry.getLongKey();
               int x = MathMan.unpairIntX(key);
               int z = MathMan.unpairIntY(key);
               this.counter.put(key, this.badLimit);
            }

            this.badChunks.clear();
         }, Settings.settings().TICK_LIMITER.INTERVAL);
      }
   }

   protected abstract int getDepth(Exception ex);

   protected abstract StackTraceElement getElement(Exception ex, int index);

   @Deprecated(
      since = "2.0.0"
   )
   public int[] getCount(int cx, int cz) {
      if (this.lastX == cx && this.lastZ == cz) {
         return this.lastCount;
      } else {
         this.lastX = cx;
         this.lastZ = cz;
         long pair = MathMan.pairInt(cx, cz);
         int[] tmp = this.lastCount = (int[])this.counter.get(pair);
         if (tmp == null) {
            this.lastCount = tmp = new int[3];
            this.counter.put(pair, tmp);
         }

         return tmp;
      }
   }

   @Deprecated(
      since = "2.0.0"
   )
   public void cleanup(Chunk chunk) {
      for (Entity entity : chunk.getEntities()) {
         if (entity.getType() == EntityType.ITEM) {
            entity.remove();
         }
      }
   }

   @Deprecated(
      since = "2.0.0"
   )
   public final void reset() {
      this.physSkip = 0;
      this.physStart = System.currentTimeMillis();
      this.physCancel = false;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockExplodeEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockBurnEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockCanBuildEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockDamageEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockDispenseEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockExpEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockFadeEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockFromToEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockGrowEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockIgniteEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockPlaceEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(FurnaceBurnEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(FurnaceSmeltEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(LeavesDecayEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(NotePlayEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(SignChangeEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void event(BlockRedstoneEvent event) {
      this.reset();
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPhysics(BlockPhysicsEvent event) {
      if (physicsFreeze) {
         event.setCancelled(true);
      } else {
         if (this.physCancel) {
            Block block = event.getBlock();
            long pair = MathMan.pairInt(block.getX() >> 4, block.getZ() >> 4);
            if (this.physCancelPair == pair) {
               event.setCancelled(true);
               return;
            }

            if (this.badChunks.containsKey(pair)) {
               this.physCancelPair = pair;
               event.setCancelled(true);
               return;
            }
         } else {
            if ((++this.physSkip & 1023) != 0) {
               return;
            }

            FaweTimer timer = Fawe.instance().getTimer();
            if (timer.getTick() != this.physTick) {
               this.physTick = timer.getTick();
               this.physStart = System.currentTimeMillis();
               return;
            }

            if (System.currentTimeMillis() - this.physStart < Settings.settings().TICK_LIMITER.PHYSICS_MS) {
               return;
            }
         }

         Exception e = new Exception();
         int depth = this.getDepth(e);
         if (depth >= 256 && this.containsSetAir(e, event)) {
            Block blockx = event.getBlock();
            int cx = blockx.getX() >> 4;
            int cz = blockx.getZ() >> 4;
            this.physCancelPair = MathMan.pairInt(cx, cz);
            if (this.rateLimit <= 0) {
               this.rateLimit = 20;
               this.lastCancelPos = blockx.getLocation();
            }

            this.cancelNearby(cx, cz);
            event.setCancelled(true);
            this.physCancel = true;
         } else {
            this.physSkip = 1;
            this.physCancel = false;
         }
      }
   }

   protected boolean containsSetAir(Exception e, BlockPhysicsEvent event) {
      for (int frame = 25; frame < 35; frame++) {
         StackTraceElement elem = this.getElement(e, frame);
         if (elem != null) {
            String methodName = elem.getMethodName();
            if (methodName.charAt(0) == 's' && methodName.length() == 6 || methodName.length() == 14) {
               return true;
            }
         }
      }

      return false;
   }

   protected void cancelNearby(int cx, int cz) {
      this.cancel(cx, cz);
      this.cancel(cx + 1, cz);
      this.cancel(cx - 1, cz);
      this.cancel(cx, cz + 1);
      this.cancel(cx, cz - 1);
      this.cancel(cx - 1, cz - 1);
      this.cancel(cx - 1, cz + 1);
      this.cancel(cx + 1, cz - 1);
      this.cancel(cx + 1, cz + 1);
   }

   private void cancel(int cx, int cz) {
      long key = MathMan.pairInt(cx, cz);
      this.badChunks.put(key, true);
      this.counter.put(key, this.badLimit);
      int[] count = this.getCount(cx, cz);
      count[0] = Integer.MAX_VALUE;
      count[1] = Integer.MAX_VALUE;
      count[2] = Integer.MAX_VALUE;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onBlockChange(EntityChangeBlockEvent event) {
      if (physicsFreeze) {
         event.setCancelled(true);
      } else {
         Block block = event.getBlock();
         int x = block.getX();
         int z = block.getZ();
         int cx = x >> 4;
         int cz = z >> 4;
         int[] count = this.getCount(cx, cz);
         if (count[1] >= Settings.settings().TICK_LIMITER.FALLING) {
            event.setCancelled(true);
         } else {
            if (event.getEntityType() == EntityType.FALLING_BLOCK && ++count[1] >= Settings.settings().TICK_LIMITER.FALLING) {
               if (Fawe.instance().getTimer().getTPS() < 18.0) {
                  this.cancelNearby(cx, cz);
                  if (this.rateLimit <= 0) {
                     this.rateLimit = 20;
                     this.lastCancelPos = block.getLocation();
                  }

                  event.setCancelled(true);
               } else {
                  count[1] = 0;
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onItemSpawn(ItemSpawnEvent event) {
      if (physicsFreeze) {
         event.setCancelled(true);
      } else {
         Location loc = event.getLocation();
         int cx = loc.getBlockX() >> 4;
         int cz = loc.getBlockZ() >> 4;
         int[] count = this.getCount(cx, cz);
         if (count[2] >= Settings.settings().TICK_LIMITER.ITEMS) {
            event.setCancelled(true);
         } else {
            if (++count[2] >= Settings.settings().TICK_LIMITER.ITEMS) {
               this.cleanup(loc.getChunk());
               this.cancelNearby(cx, cz);
               if (this.rateLimit <= 0) {
                  this.rateLimit = 20;
                  LOGGER.warn("[FAWE `tick-limiter`] Detected and cancelled item lag source at {}", loc);
               }

               event.setCancelled(true);
            }
         }
      }
   }
}
