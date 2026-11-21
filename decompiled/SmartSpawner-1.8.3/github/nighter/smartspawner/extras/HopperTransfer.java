package github.nighter.smartspawner.extras;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import github.nighter.smartspawner.utils.BlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HopperTransfer {
   private final SmartSpawner plugin;
   private final SpawnerManager spawnerManager;
   private final SpawnerGuiViewManager guiManager;

   public HopperTransfer(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerManager = plugin.getSpawnerManager();
      this.guiManager = plugin.getSpawnerGuiViewManager();
   }

   public void process(BlockPos hopperPos) {
      Location hopperLoc = hopperPos.toLocation();
      if (hopperLoc != null) {
         Block hopperBlock = hopperLoc.getBlock();
         if (hopperBlock.getType() == Material.HOPPER) {
            Block spawnerBlock = hopperBlock.getRelative(BlockFace.UP);
            if (spawnerBlock.getType() == Material.SPAWNER) {
               this.transferItems(hopperLoc, spawnerBlock.getLocation());
            }
         }
      }
   }

   private void transferItems(Location hopperLoc, Location spawnerLoc) {
      SpawnerData spawner = this.spawnerManager.getSpawnerByLocation(spawnerLoc);
      if (spawner != null) {
         ReentrantLock lock = spawner.getInventoryLock();
         if (lock.tryLock()) {
            try {
               VirtualInventory virtualInv = spawner.getVirtualInventory();
               if (virtualInv == null) {
                  return;
               }

               if (hopperLoc.getBlock().getState(false) instanceof Hopper hopper) {
                  Inventory hopperInv = hopper.getInventory();
                  int transferred = 0;
                  int rangeStart = 0;
                  int rangeSize = Math.max(this.plugin.getHopperConfig().getStackPerTransfer(), 9);

                  List<ItemStack> removed;
                  for (removed = new ArrayList<>(); transferred < this.plugin.getHopperConfig().getStackPerTransfer(); rangeStart += rangeSize) {
                     Int2ObjectMap<ItemStack> displayItems = virtualInv.getDisplayRange(rangeStart, rangeSize);
                     if (displayItems.isEmpty()) {
                        break;
                     }

                     ObjectIterator var14 = displayItems.values().iterator();

                     while (var14.hasNext()) {
                        ItemStack item = (ItemStack)var14.next();
                        if (transferred >= this.plugin.getHopperConfig().getStackPerTransfer()) {
                           break;
                        }

                        if (item != null && item.getType() != Material.AIR) {
                           ItemStack clone = item.clone();
                           int originalAmount = clone.getAmount();
                           HashMap<Integer, ItemStack> leftovers = hopperInv.addItem(new ItemStack[]{clone});
                           int insertedAmount = originalAmount;
                           if (!leftovers.isEmpty()) {
                              insertedAmount = originalAmount - leftovers.values().iterator().next().getAmount();
                           }

                           if (insertedAmount > 0) {
                              ItemStack toRemove = item.clone();
                              toRemove.setAmount(insertedAmount);
                              removed.add(toRemove);
                              transferred++;
                           }
                        }
                     }
                  }

                  if (!removed.isEmpty()) {
                     spawner.removeItemsAndUpdateSellValue(removed);
                     this.guiManager.updateSpawnerMenuViewers(spawner);
                  }

                  return;
               }
            } catch (Exception var24) {
               this.plugin.getLogger().log(Level.WARNING, "Error transferring items from spawner to hopper at " + hopperLoc, (Throwable)var24);
               return;
            } finally {
               lock.unlock();
            }
         }
      }
   }
}
