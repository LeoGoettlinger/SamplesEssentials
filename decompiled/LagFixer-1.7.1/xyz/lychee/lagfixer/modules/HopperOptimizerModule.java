package xyz.lychee.lagfixer.modules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractFork;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

public class HopperOptimizerModule extends AbstractModule implements Listener {
   private final Set<Hopper> trackedHoppers = ConcurrentHashMap.newKeySet();
   private final Map<String, LongAdder> chunkHopperCount = new ConcurrentHashMap<>();
   private final Map<Hopper, Long> hopperLastActivity = new ConcurrentHashMap<>();
   private final Map<Hopper, Long> hopperLastTransfer = new ConcurrentHashMap<>();
   private final Map<Hopper, Integer> hopperTransferCount = new ConcurrentHashMap<>();
   private HopperOptimizerModule.NMS hopperOptimizer;
   private boolean smartThrottling;
   private boolean chunkLimitEnabled;
   private boolean emptyHopperOptimization;
   private int maxHoppersPerChunk;
   private int checkInterval;
   private int emptyHopperCheckDelay;
   private long inactiveThresholdMs;
   private BukkitTask optimizationTask;
   private BukkitTask cleanupTask;
   private BukkitTask resetTask;

   public HopperOptimizerModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.HIGH,
         "HopperOptimizer",
         new String[]{
            "Optymalizuje działanie lejków zmniejszając ich obciążenie serwera.",
            "Wprowadza inteligentne ograniczenia transferu przedmiotów.",
            "Zapobiega nadmiernej liczbie lejków w chunkach.",
            "Poprawia wydajność serwera przy dużej ilości lejków."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGViODFlZjg5MDIzNzk2NTBiYTc5ZjQ1NzIzZDZiOWM4ODgzODhhMDBmYzRlMTkyZjM0NTRmZTE5Mzg4MmVlMSJ9fX0="
      );
   }

   private void startOptimizationTasks() {
      AbstractFork fork = SupportManager.getInstance().getFork();
      this.optimizationTask = fork.runTimer(true, this::optimizeHoppers, 50L, this.checkInterval, TimeUnit.MILLISECONDS);
      this.cleanupTask = fork.runTimer(true, this::cleanupInactiveHoppers, 100L, 200L, TimeUnit.MILLISECONDS);
      this.resetTask = fork.runTimer(true, this::resetTransferCounters, 1L, 1L, TimeUnit.SECONDS);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onInventoryMoveItem(InventoryMoveItemEvent event) {
      if (event.getSource().getType() == InventoryType.HOPPER && event.getSource().getHolder() instanceof Hopper hopper) {
         if (this.shouldBlockTransfer(hopper, event)) {
            event.setCancelled(true);
            return;
         }

         this.trackHopperActivity(hopper);
      }

      if (event.getDestination().getType() == InventoryType.HOPPER) {
         InventoryHolder holder = event.getDestination().getHolder();
         if (holder instanceof Hopper) {
            this.trackHopperActivity((Hopper)holder);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      if (!event.isCancelled() && event.getBlock().getType() == Material.HOPPER) {
         Block block = event.getBlock();
         BlockState state = block.getState();
         if (state instanceof Hopper) {
            this.trackHopper((Hopper)state);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onBlockBreak(BlockBreakEvent event) {
      if (!event.isCancelled() && event.getBlock().getType() == Material.HOPPER) {
         BlockState state = event.getBlock().getState();
         if (state instanceof Hopper) {
            this.removeHopper((Hopper)state);
         }
      }
   }

   private boolean shouldBlockTransfer(Hopper hopper, InventoryMoveItemEvent event) {
      String chunkKey = this.getChunkKey(hopper);
      LongAdder count = this.chunkHopperCount.get(chunkKey);
      if (this.chunkLimitEnabled && count != null && count.intValue() >= this.maxHoppersPerChunk && !this.trackedHoppers.contains(hopper)) {
         return true;
      } else if (this.smartThrottling) {
         long currentTime = System.currentTimeMillis();
         Long lastTransfer = this.hopperLastTransfer.get(hopper);
         boolean isEmpty = this.isInventoryEmpty(hopper.getInventory());
         if (isEmpty && this.emptyHopperOptimization && lastTransfer != null && currentTime - lastTransfer < this.emptyHopperCheckDelay) {
            return true;
         } else {
            Long lastActivity = this.hopperLastActivity.get(hopper);
            return lastActivity != null
               && currentTime - lastActivity > this.inactiveThresholdMs
               && lastTransfer != null
               && currentTime - lastTransfer < this.emptyHopperCheckDelay * 2L;
         }
      } else {
         return false;
      }
   }

   private boolean isInventoryEmpty(Inventory inventory) {
      ItemStack[] contents = inventory.getContents();

      for (ItemStack item : contents) {
         if (item != null && item.getType() != Material.AIR) {
            return false;
         }
      }

      return true;
   }

   private void trackHopperActivity(Hopper hopper) {
      if (hopper != null && hopper.isPlaced()) {
         long currentTime = System.currentTimeMillis();
         this.hopperLastActivity.put(hopper, currentTime);
         this.hopperLastTransfer.put(hopper, currentTime);
         this.hopperTransferCount.merge(hopper, 1, Integer::sum);
      }
   }

   private void trackHopper(Hopper hopper) {
      if (hopper != null && hopper.isPlaced()) {
         this.trackedHoppers.add(hopper);
         this.hopperLastActivity.put(hopper, System.currentTimeMillis());
         if (this.chunkLimitEnabled) {
            String chunkKey = this.getChunkKey(hopper);
            this.chunkHopperCount.computeIfAbsent(chunkKey, k -> new LongAdder()).increment();
         }
      }
   }

   private void removeHopper(Hopper hopper) {
      this.trackedHoppers.remove(hopper);
      this.hopperLastActivity.remove(hopper);
      this.hopperLastTransfer.remove(hopper);
      this.hopperTransferCount.remove(hopper);
      if (this.chunkLimitEnabled) {
         String chunkKey = this.getChunkKey(hopper);
         LongAdder count = this.chunkHopperCount.get(chunkKey);
         if (count != null) {
            count.decrement();
            if (count.intValue() <= 0) {
               this.chunkHopperCount.remove(chunkKey);
            }
         }
      }
   }

   private void optimizeHoppers() {
      for (Hopper hopper : new HashSet<>(this.trackedHoppers)) {
         this.optimizeHopper(hopper);
      }
   }

   private void optimizeHopper(Hopper hopper) {
      if (hopper != null) {
         if (hopper.isPlaced() && hopper.getChunk().isLoaded()) {
            int cooldown = this.calculateOptimalCooldown(hopper);
            this.hopperOptimizer.hopperCooldown(hopper, cooldown);
         } else {
            this.removeHopper(hopper);
         }
      }
   }

   private int calculateOptimalCooldown(Hopper hopper) {
      if (!this.smartThrottling) {
         return 8;
      } else {
         boolean isEmpty = this.isInventoryEmpty(hopper.getInventory());
         if (isEmpty && this.emptyHopperOptimization) {
            return Math.max(24, 20);
         } else {
            Long lastActivity = this.hopperLastActivity.get(hopper);
            if (lastActivity != null) {
               long timeSinceActivity = System.currentTimeMillis() - lastActivity;
               if (timeSinceActivity > this.inactiveThresholdMs * 2L) {
                  return Math.min(32, 50);
               }

               if (timeSinceActivity > this.inactiveThresholdMs) {
                  return Math.min(16, 25);
               }
            }

            return 8;
         }
      }
   }

   private void resetTransferCounters() {
      this.hopperTransferCount.clear();
   }

   private void cleanupInactiveHoppers() {
      long currentTime = System.currentTimeMillis();
      AtomicInteger removedCount = new AtomicInteger(0);
      this.trackedHoppers.removeIf(hopper -> {
         if (hopper == null) {
            removedCount.incrementAndGet();
            return true;
         } else if (hopper.isPlaced() && hopper.getChunk().isLoaded()) {
            Long lastActivity = this.hopperLastActivity.get(hopper);
            if (lastActivity != null && currentTime - lastActivity > 300000L) {
               this.removeHopper(hopper);
               removedCount.incrementAndGet();
               return true;
            } else {
               return false;
            }
         } else {
            removedCount.incrementAndGet();
            return true;
         }
      });
      if (this.chunkLimitEnabled) {
         this.chunkHopperCount.entrySet().removeIf(entry -> {
            String[] parts = entry.getKey().split(":");
            if (parts.length != 3) {
               return true;
            } else {
               String worldName = parts[0];
               int chunkX = Integer.parseInt(parts[1]);
               int chunkZ = Integer.parseInt(parts[2]);
               World world = Bukkit.getWorld(worldName);
               return world == null || !world.isChunkLoaded(chunkX, chunkZ);
            }
         });
      }
   }

   private String getChunkKey(Hopper hopper) {
      Chunk chunk = hopper.getChunk();
      return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
   }

   @Override
   public void load() {
      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
      this.startOptimizationTasks();
   }

   @Override
   public boolean loadConfig() {
      this.hopperOptimizer = ReflectionUtils.createInstance("HopperOptimizer", this);
      this.smartThrottling = this.getSection().getBoolean("smart_throttling.enabled", true);
      this.chunkLimitEnabled = this.getSection().getBoolean("chunk_limiting.enabled", false);
      this.emptyHopperOptimization = this.getSection().getBoolean("empty_hopper_optimization", true);
      this.maxHoppersPerChunk = this.getSection().getInt("chunk_limiting.maxHoppersPerChunk", 16);
      this.checkInterval = this.getSection().getInt("checkInterval", 2000);
      this.emptyHopperCheckDelay = this.getSection().getInt("emptyHopperCheckDelay", 100);
      this.inactiveThresholdMs = this.getSection().getLong("inactiveThresholdMs", 30000L);
      return this.hopperOptimizer != null;
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.optimizationTask != null) {
         this.optimizationTask.cancel();
      }

      if (this.cleanupTask != null) {
         this.cleanupTask.cancel();
      }

      if (this.resetTask != null) {
         this.resetTask.cancel();
      }
   }

   public abstract static class NMS implements Listener {
      private final HopperOptimizerModule module;

      public NMS(HopperOptimizerModule module) {
         this.module = module;
      }

      public abstract void hopperCooldown(Hopper var1, int var2);

      @Generated
      public HopperOptimizerModule getModule() {
         return this.module;
      }
   }
}
