package net.coreprotect.paper.listener;

import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.listener.player.InventoryChangeListener;
import net.coreprotect.utility.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public final class CopperGolemChestListener implements Listener {
   private static final String COPPER_GOLEM_NAME = "COPPER_GOLEM";
   private static final String USERNAME = "#copper_golem";
   private static final long INITIAL_DELAY_TICKS = 5L;
   private static final long POLL_INTERVAL_TICKS = 15L;
   private static final long MAX_POLL_DURATION_TICKS = 600L;
   private static final long MIN_THROTTLE_MILLIS = 2800L;
   private final CoreProtect plugin;
   private final Map<CopperGolemChestListener.TransactionKey, CopperGolemChestListener.PendingTransaction> pendingTransactions = new ConcurrentHashMap<>();
   private final Map<CopperGolemChestListener.TransactionKey, Long> throttleUntil = new ConcurrentHashMap<>();

   public CopperGolemChestListener(CoreProtect plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onValidate(ItemTransportingEntityValidateTargetEvent event) {
      if (event.isAllowed()) {
         Entity entity = event.getEntity();
         if (entity != null && entity.getType() != null && "COPPER_GOLEM".equals(entity.getType().name())) {
            CopperGolem copperGolem = (CopperGolem)entity;
            Material heldMaterial = this.getHeldItemMaterial(copperGolem);
            Block block = event.getBlock();
            if (block != null) {
               BlockState blockState = block.getState();
               if (blockState instanceof InventoryHolder) {
                  Location location = blockState.getLocation();
                  if (location != null && location.getWorld() != null) {
                     if (Config.getConfig(location.getWorld()).ITEM_TRANSACTIONS) {
                        this.scheduleTransaction(copperGolem, blockState, heldMaterial);
                     }
                  }
               }
            }
         }
      }
   }

   private void scheduleTransaction(CopperGolem copperGolem, BlockState blockState, Material heldMaterial) {
      Location location = blockState.getLocation();
      if (location != null && copperGolem != null) {
         CopperGolemChestListener.TransactionKey transactionKey = CopperGolemChestListener.TransactionKey.of(location);
         Location targetLocation = location.clone();
         ItemStack[] baselineState = this.captureInventoryState(targetLocation);
         if (baselineState != null) {
            if (this.shouldMonitorInteraction(blockState.getType(), baselineState, heldMaterial)) {
               CopperGolemChestListener.PendingTransaction existing = this.pendingTransactions.get(transactionKey);
               if (existing != null) {
                  existing.refresh(baselineState);
               } else if (!this.isThrottled(transactionKey)) {
                  CopperGolemChestListener.PendingTransaction scheduled = new CopperGolemChestListener.PendingTransaction(
                     transactionKey, targetLocation, baselineState
                  );
                  this.pendingTransactions.put(transactionKey, scheduled);
                  this.throttleUntil.put(transactionKey, System.currentTimeMillis() + 2800L);
                  scheduled.start();
               }
            }
         }
      }
   }

   private ItemStack[] captureInventoryState(Location location) {
      if (location != null && location.getWorld() != null) {
         BlockState blockState = location.getBlock().getState();
         if (!(blockState instanceof InventoryHolder)) {
            return null;
         } else {
            InventoryHolder inventoryHolder = (InventoryHolder)blockState;
            Inventory inventory = inventoryHolder.getInventory();
            return inventory == null ? null : ItemUtils.getContainerState(inventory.getContents());
         }
      } else {
         return null;
      }
   }

   private boolean hasInventoryChanged(ItemStack[] previousState, ItemStack[] currentState) {
      if (previousState != null && currentState != null) {
         if (previousState.length != currentState.length) {
            return true;
         } else {
            for (int i = 0; i < previousState.length; i++) {
               ItemStack previousItem = previousState[i];
               ItemStack currentItem = currentState[i];
               if (previousItem != null || currentItem != null) {
                  if (previousItem == null || currentItem == null) {
                     return true;
                  }

                  if (!previousItem.equals(currentItem)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private boolean shouldMonitorInteraction(Material blockType, ItemStack[] baselineState, Material heldMaterial) {
      boolean isCopperChest = BukkitAdapter.ADAPTER.isCopperChest(blockType);
      boolean isStandardChest = blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST;
      boolean golemHoldingItem = heldMaterial != null && heldMaterial != Material.AIR;
      if (!isCopperChest && !isStandardChest) {
         return false;
      } else if (!isCopperChest) {
         if (!golemHoldingItem) {
            return false;
         } else {
            return this.isInventoryEmpty(baselineState) ? true : this.containsMaterial(baselineState, heldMaterial);
         }
      } else {
         return !golemHoldingItem && !this.isInventoryEmpty(baselineState);
      }
   }

   private boolean isInventoryEmpty(ItemStack[] state) {
      if (state == null) {
         return true;
      } else {
         for (ItemStack item : state) {
            if (!this.isEmptyItem(item)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isEmptyItem(ItemStack item) {
      return item == null || item.getType().isAir() || item.getAmount() <= 0;
   }

   private Material getHeldItemMaterial(CopperGolem copperGolem) {
      if (copperGolem == null) {
         return null;
      } else {
         EntityEquipment equipment = copperGolem.getEquipment();
         if (equipment == null) {
            return null;
         } else {
            ItemStack mainHand = equipment.getItemInMainHand();
            return this.isEmptyItem(mainHand) ? null : mainHand.getType();
         }
      }
   }

   private boolean containsMaterial(ItemStack[] state, Material material) {
      if (state != null && material != null) {
         for (ItemStack item : state) {
            if (item != null && item.getType() == material && item.getAmount() > 0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isThrottled(CopperGolemChestListener.TransactionKey transactionKey) {
      Long eligibleAt = this.throttleUntil.get(transactionKey);
      if (eligibleAt == null) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         if (eligibleAt <= now) {
            this.throttleUntil.remove(transactionKey, eligibleAt);
            return false;
         } else {
            return true;
         }
      }
   }

   private final class PendingTransaction implements Runnable {
      private final CopperGolemChestListener.TransactionKey transactionKey;
      private final Location targetLocation;
      private ItemStack[] baselineState;
      private BukkitTask task;
      private long ticksElapsed;
      private long ticksSinceLastTrigger;

      private PendingTransaction(CopperGolemChestListener.TransactionKey transactionKey, Location targetLocation, ItemStack[] baselineState) {
         this.transactionKey = transactionKey;
         this.targetLocation = targetLocation;
         this.baselineState = baselineState;
      }

      private void start() {
         this.task = CopperGolemChestListener.this.plugin.getServer().getScheduler().runTaskTimer(CopperGolemChestListener.this.plugin, this, 5L, 15L);
      }

      private void refresh(ItemStack[] newBaselineState) {
         this.ticksSinceLastTrigger = 0L;
         if (newBaselineState != null) {
            this.baselineState = newBaselineState;
         }
      }

      @Override
      public void run() {
         long increment = this.ticksElapsed == 0L ? 5L : 15L;
         this.ticksElapsed += increment;
         this.ticksSinceLastTrigger += increment;
         if (this.ticksSinceLastTrigger > 600L) {
            this.cancelAndRemove();
         } else {
            ItemStack[] currentState = CopperGolemChestListener.this.captureInventoryState(this.targetLocation);
            if (currentState == null) {
               this.cancelAndRemove();
            } else {
               boolean stateChanged = CopperGolemChestListener.this.hasInventoryChanged(this.baselineState, currentState);
               if (stateChanged) {
                  InventoryChangeListener.inventoryTransaction("#copper_golem", this.targetLocation, this.baselineState);
                  this.baselineState = ItemUtils.getContainerState(currentState);
               }
            }
         }
      }

      private void cancel() {
         if (this.task != null) {
            this.task.cancel();
         }
      }

      private void cancelAndRemove() {
         this.cancel();
         CopperGolemChestListener.this.pendingTransactions.remove(this.transactionKey, this);
      }
   }

   private static final class TransactionKey {
      private final UUID worldId;
      private final int x;
      private final int y;
      private final int z;

      private TransactionKey(UUID worldId, int x, int y, int z) {
         this.worldId = worldId;
         this.x = x;
         this.y = y;
         this.z = z;
      }

      private static CopperGolemChestListener.TransactionKey of(Location location) {
         if (location != null && location.getWorld() != null) {
            return new CopperGolemChestListener.TransactionKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
         } else {
            throw new IllegalArgumentException("Location must have world");
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof CopperGolemChestListener.TransactionKey)) {
            return false;
         } else {
            CopperGolemChestListener.TransactionKey other = (CopperGolemChestListener.TransactionKey)obj;
            return this.worldId.equals(other.worldId) && this.x == other.x && this.y == other.y && this.z == other.z;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.worldId, this.x, this.y, this.z);
      }
   }
}
