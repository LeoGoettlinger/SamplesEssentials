package github.nighter.smartspawner.spawner.lootgen;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class SpawnerLootGenerator {
   private final SmartSpawner plugin;
   private final SpawnerGuiViewManager spawnerGuiViewManager;
   private final SpawnerManager spawnerManager;

   public SpawnerLootGenerator(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerGuiViewManager = plugin.getSpawnerGuiViewManager();
      this.spawnerManager = plugin.getSpawnerManager();
   }

   public void spawnLootToSpawner(SpawnerData spawner) {
      if (!spawner.isSelling()) {
         boolean lockAcquired = spawner.getLootGenerationLock().tryLock();
         if (lockAcquired) {
            try {
               try {
                  if (!spawner.getDataLock().tryLock(50L, TimeUnit.MILLISECONDS)) {
                     return;
                  }
               } catch (InterruptedException var20) {
                  Thread.currentThread().interrupt();
                  return;
               }

               long currentTime = System.currentTimeMillis();

               long spawnTime;
               int minMobs;
               int maxMobs;
               try {
                  int usedSlots = spawner.getVirtualInventory().getUsedSlots();
                  int maxSlots = spawner.getMaxSpawnerLootSlots();
                  if (usedSlots >= maxSlots && spawner.getSpawnerExp() >= spawner.getMaxStoredExp()) {
                     if (!spawner.getIsAtCapacity()) {
                        spawner.setIsAtCapacity(true);
                     }

                     return;
                  }

                  minMobs = spawner.getMinMobs();
                  maxMobs = spawner.getMaxMobs();
                  spawnTime = currentTime;
               } finally {
                  spawner.getDataLock().unlock();
               }

               Scheduler.runTaskAsync(() -> {
                  LootResult loot = this.generateLoot(minMobs, maxMobs, spawner);
                  if (!loot.items().isEmpty() || loot.experience() != 0L) {
                     Scheduler.runLocationTask(spawner.getSpawnerLocation(), () -> {
                        boolean updateLockAcquired = spawner.getLootGenerationLock().tryLock();
                        if (updateLockAcquired) {
                           try {
                              boolean changed = false;
                              if (loot.experience() > 0L && spawner.getSpawnerExp() < spawner.getMaxStoredExp()) {
                                 long currentExp = spawner.getSpawnerExp();
                                 long maxExp = spawner.getMaxStoredExp();
                                 long newExpLong = currentExp + loot.experience();
                                 long newExp = Math.min(newExpLong, maxExp);
                                 if (newExp != currentExp) {
                                    spawner.setSpawnerExp(newExp);
                                    changed = true;
                                 }
                              }

                              if (!loot.items().isEmpty()) {
                                 Map<ItemSignature, Long> lootToAdd = loot.items();
                                 int maxSlotsx = spawner.getMaxSpawnerLootSlots();
                                 int totalRequiredSlots = this.calculateRequiredSlots(lootToAdd, spawner.getVirtualInventory());
                                 if (totalRequiredSlots > maxSlotsx) {
                                    lootToAdd = this.limitLootToAvailableSlots(lootToAdd, spawner);
                                 }

                                 if (!lootToAdd.isEmpty()) {
                                    spawner.addItemsAndUpdateSellValue(lootToAdd);
                                    changed = true;
                                 }
                              }

                              if (changed) {
                                 boolean updateDataLockAcquired = spawner.getDataLock().tryLock();
                                 if (updateDataLockAcquired) {
                                    try {
                                       spawner.setLastSpawnTime(spawnTime);
                                    } finally {
                                       spawner.getDataLock().unlock();
                                    }
                                 }

                                 spawner.updateCapacityStatus();
                                 this.handleGuiUpdates(spawner);
                                 this.spawnerManager.markSpawnerModified(spawner.getSpawnerId());
                                 return;
                              }
                           } finally {
                              spawner.getLootGenerationLock().unlock();
                           }
                        }
                     });
                  }
               });
            } finally {
               spawner.getLootGenerationLock().unlock();
            }
         }
      }
   }

   public LootResult generateLoot(int minMobs, int maxMobs, SpawnerData spawner) {
      int mobCount = ThreadLocalRandom.current().nextInt(maxMobs - minMobs + 1) + minMobs;
      long totalExperience = (long)spawner.getEntityExperienceValue() * mobCount;
      List<LootItem> validItems = spawner.getValidLootItems();
      if (validItems.isEmpty()) {
         return new LootResult(Collections.emptyMap(), totalExperience);
      } else {
         Map<ItemSignature, Long> consolidatedLoot = new HashMap<>();
         boolean shouldApproximateLoot = Config.get().isApproximateLoot();
         int approximationThreshold = Config.get().getApproximationThreshold();

         for (LootItem lootItem : validItems) {
            long totalAmount;
            if (shouldApproximateLoot && this.shouldApproximate(lootItem.chance(), mobCount, approximationThreshold)) {
               totalAmount = this.generateApproximatedLoot(lootItem, mobCount);
            } else {
               totalAmount = this.generateExactLoot(lootItem, mobCount);
            }

            if (totalAmount > 0L) {
               ItemStack prototype = lootItem.createItemStack();
               if (prototype != null && prototype.getType() != Material.AIR) {
                  ItemSignature signature = VirtualInventory.getSignature(prototype);
                  consolidatedLoot.merge(signature, totalAmount, Long::sum);
               }
            }
         }

         return new LootResult(consolidatedLoot, totalExperience);
      }
   }

   private boolean shouldApproximate(double chance, int mobCount, int approximationThreshold) {
      return chance <= 0.0 ? false : mobCount > 97.5 / chance * approximationThreshold;
   }

   private int generateExactLoot(LootItem lootItem, int mobCount) {
      int successfulDrops = 0;
      ThreadLocalRandom random = ThreadLocalRandom.current();
      double p = lootItem.chance() / 100.0;

      for (int i = 0; i < mobCount; i++) {
         if (random.nextDouble() < p) {
            successfulDrops++;
         }
      }

      int totalAmount = 0;

      for (int ix = 0; ix < successfulDrops; ix++) {
         totalAmount += lootItem.generateAmount(random);
      }

      return totalAmount;
   }

   private int generateApproximatedLoot(LootItem lootItem, int mobCount) {
      double p = lootItem.chance() / 100.0;
      double expectedDrops = mobCount * p;
      double avgAmount = lootItem.getAverageAmount();
      double jitter = p != 1.0 ? 0.95 + ThreadLocalRandom.current().nextDouble() * 0.1 : 1.0;
      return (int)Math.round(expectedDrops * avgAmount * jitter);
   }

   private Map<ItemSignature, Long> limitLootToAvailableSlots(Map<ItemSignature, Long> loot, SpawnerData spawner) {
      VirtualInventory inventory = spawner.getVirtualInventory();
      int maxSlots = spawner.getMaxSpawnerLootSlots();
      if (maxSlots <= 0) {
         return Collections.emptyMap();
      } else {
         Map<ItemSignature, Long> simulatedInventory = new HashMap<>(inventory.getConsolidatedItems());
         Map<ItemSignature, Long> acceptedLoot = new HashMap<>(loot.size());
         int usedSlots = this.calculateSlots(simulatedInventory);
         List<Entry<ItemSignature, Long>> entries = new ArrayList<>(loot.entrySet());
         entries.sort(Comparator.comparing(entryx -> ((ItemSignature)entryx.getKey()).getMaterial().name()));

         for (Entry<ItemSignature, Long> entry : entries) {
            ItemSignature signature = entry.getKey();
            long amount = entry.getValue();
            int maxStackSize = signature.getMaxStackSize();
            long currentAmount = simulatedInventory.getOrDefault(signature, 0L);
            int oldSlots = this.slotsFor(currentAmount, maxStackSize);
            int newSlots = this.slotsFor(currentAmount + amount, maxStackSize);
            int slotDelta = newSlots - oldSlots;
            if (usedSlots + slotDelta <= maxSlots) {
               acceptedLoot.put(signature, amount);
               simulatedInventory.put(signature, currentAmount + amount);
               usedSlots += slotDelta;
            } else {
               int remainingSlots = Math.max(0, maxSlots - usedSlots);
               long maxAddAmount = (long)(oldSlots + remainingSlots) * maxStackSize - currentAmount;
               if (maxAddAmount > 0L) {
                  long acceptedAmount = (int)Math.min(maxAddAmount, amount);
                  if (acceptedAmount > 0L) {
                     acceptedLoot.put(signature, acceptedAmount);
                     simulatedInventory.put(signature, currentAmount + acceptedAmount);
                     usedSlots = this.calculateSlots(simulatedInventory);
                  }
               }
            }
         }

         return acceptedLoot;
      }
   }

   private int calculateRequiredSlots(Map<ItemSignature, Long> loot, VirtualInventory inventory) {
      Map<ItemSignature, Long> simulatedItems = new HashMap<>(inventory.getConsolidatedItems());

      for (Entry<ItemSignature, Long> entry : loot.entrySet()) {
         simulatedItems.merge(entry.getKey(), entry.getValue(), Long::sum);
      }

      return this.calculateSlots(simulatedItems);
   }

   private int calculateSlots(Map<ItemSignature, Long> items) {
      int total = 0;

      for (Entry<ItemSignature, Long> entry : items.entrySet()) {
         total += this.slotsFor(entry.getValue(), entry.getKey().getMaxStackSize());
      }

      return total;
   }

   private int slotsFor(long amount, int maxStackSize) {
      return amount <= 0L ? 0 : (int)((amount + maxStackSize - 1L) / maxStackSize);
   }

   private Map<ItemSignature, Long> copyLoot(Map<ItemSignature, Long> loot) {
      if (loot != null && !loot.isEmpty()) {
         Map<ItemSignature, Long> copy = new HashMap<>(loot.size());

         for (Entry<ItemSignature, Long> entry : loot.entrySet()) {
            ItemSignature signature = entry.getKey();
            Long amount = entry.getValue();
            if (amount > 0L) {
               copy.merge(signature, amount, Long::sum);
            }
         }

         return copy;
      } else {
         return Collections.emptyMap();
      }
   }

   private void handleGuiUpdates(SpawnerData spawner) {
      this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
      if (Config.get().isSpawnerGenerateLootParticlesEnabled()) {
         Location loc = spawner.getSpawnerLocation();
         World world = loc.getWorld();
         if (world != null) {
            Scheduler.runLocationTask(loc, () -> world.spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.0));
         }
      }

      if (this.plugin.getConfig().getBoolean("hologram.enabled", false)) {
         spawner.updateHologramData();
      }
   }

   public void preGenerateLoot(SpawnerData spawner, SpawnerLootGenerator.LootGenerationCallback callback) {
      if (!spawner.getLootGenerationLock().tryLock()) {
         callback.onLootGenerated(Collections.emptyMap(), 0L);
      } else {
         try {
            try {
               if (!spawner.getDataLock().tryLock(50L, TimeUnit.MILLISECONDS)) {
                  callback.onLootGenerated(Collections.emptyMap(), 0L);
                  return;
               }
            } catch (InterruptedException var17) {
               Thread.currentThread().interrupt();
               callback.onLootGenerated(Collections.emptyMap(), 0L);
               return;
            }

            int minMobs;
            int maxMobs;
            try {
               int usedSlots = spawner.getVirtualInventory().getUsedSlots();
               int maxSlots = spawner.getMaxSpawnerLootSlots();
               boolean atCapacity = usedSlots >= maxSlots && spawner.getSpawnerExp() >= spawner.getMaxStoredExp();
               if (atCapacity) {
                  callback.onLootGenerated(Collections.emptyMap(), 0L);
                  return;
               }

               minMobs = spawner.getMinMobs();
               maxMobs = spawner.getMaxMobs();
            } finally {
               spawner.getDataLock().unlock();
            }

            Scheduler.runTaskAsync(() -> {
               LootResult loot = this.generateLoot(minMobs, maxMobs, spawner);
               callback.onLootGenerated(this.copyLoot(loot.items()), loot.experience());
            });
         } finally {
            spawner.getLootGenerationLock().unlock();
         }
      }
   }

   public void addPreGeneratedLoot(SpawnerData spawner, Map<ItemSignature, Long> items, long experience) {
      this.addPreGeneratedLoot(spawner, items, experience, System.currentTimeMillis());
   }

   public void addPreGeneratedLoot(SpawnerData spawner, Map<ItemSignature, Long> items, long experience, long spawnTime) {
      if (items != null && !items.isEmpty() || experience != 0L) {
         Location spawnerLocation = spawner.getSpawnerLocation();
         if (spawnerLocation != null) {
            Scheduler.runLocationTask(spawnerLocation, () -> {
               if (spawner.getLootGenerationLock().tryLock()) {
                  try {
                     try {
                        if (!spawner.getDataLock().tryLock(50L, TimeUnit.MILLISECONDS)) {
                           return;
                        }
                     } catch (InterruptedException var19) {
                        Thread.currentThread().interrupt();
                        return;
                     }

                     try {
                        int usedSlots = spawner.getVirtualInventory().getUsedSlots();
                        int maxSlots = spawner.getMaxSpawnerLootSlots();
                        boolean isCompletelyFull = usedSlots >= maxSlots && spawner.getSpawnerExp() >= spawner.getMaxStoredExp();
                        if (isCompletelyFull) {
                           return;
                        }
                     } finally {
                        spawner.getDataLock().unlock();
                     }

                     Scheduler.runTaskAsync(() -> {
                        boolean changed = false;
                        if (experience > 0L && spawner.getSpawnerExp() < spawner.getMaxStoredExp()) {
                           long currentExp = spawner.getSpawnerExp();
                           long maxExp = spawner.getMaxStoredExp();
                           long newExpLong = currentExp + experience;
                           long newExp = Math.min(newExpLong, maxExp);
                           if (newExp != currentExp) {
                              spawner.setSpawnerExp(newExp);
                              changed = true;
                           }
                        }

                        if (items != null && !items.isEmpty()) {
                           Map<ItemSignature, Long> lootToAdd = this.copyLoot(items);
                           if (!lootToAdd.isEmpty()) {
                              int maxSlotsx = spawner.getMaxSpawnerLootSlots();
                              int totalRequiredSlots = this.calculateRequiredSlots(lootToAdd, spawner.getVirtualInventory());
                              if (totalRequiredSlots > maxSlotsx) {
                                 lootToAdd = this.limitLootToAvailableSlots(lootToAdd, spawner);
                              }

                              if (!lootToAdd.isEmpty()) {
                                 spawner.addItemsAndUpdateSellValue(lootToAdd);
                                 changed = true;
                              }
                           }
                        }

                        if (changed) {
                           if (spawner.getDataLock().tryLock()) {
                              try {
                                 spawner.setLastSpawnTime(spawnTime);
                              } finally {
                                 spawner.getDataLock().unlock();
                              }
                           }

                           spawner.updateCapacityStatus();
                           this.handleGuiUpdates(spawner);
                           this.spawnerManager.markSpawnerModified(spawner.getSpawnerId());
                        }
                     });
                  } finally {
                     spawner.getLootGenerationLock().unlock();
                  }
               }
            });
         }
      }
   }

   @FunctionalInterface
   public interface LootGenerationCallback {
      void onLootGenerated(Map<ItemSignature, Long> var1, long var2);
   }
}
