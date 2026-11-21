package github.nighter.smartspawner.spawner.properties;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class VirtualInventory {
   private final Map<ItemSignature, Long> consolidatedItems;
   private int maxSlots;
   private List<Entry<ItemSignature, Long>> sortedEntriesCache;
   private Material preferredSortMaterial;

   public VirtualInventory(int maxSlots) {
      this.maxSlots = maxSlots;
      this.consolidatedItems = new ConcurrentHashMap<>();
      this.sortedEntriesCache = null;
      this.preferredSortMaterial = null;
   }

   public static ItemSignature getSignature(ItemStack item) {
      return new ItemSignature(item);
   }

   public void setMaxSlots(int maxSlots) {
      this.maxSlots = Math.max(0, maxSlots);
   }

   public void addItem(ItemStack item, long amount) {
      if (item != null && amount > 0L) {
         ItemSignature signature = getSignature(item);
         this.consolidatedItems.merge(signature, amount, Long::sum);
         this.sortedEntriesCache = null;
      }
   }

   public void addItems(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         boolean changed = false;

         for (Entry<ItemSignature, Long> entry : items.entrySet()) {
            ItemSignature signature = entry.getKey();
            Long amountValue = entry.getValue();
            if (amountValue > 0L) {
               this.consolidatedItems.merge(signature, amountValue, Long::sum);
               changed = true;
            }
         }

         if (changed) {
            this.sortedEntriesCache = null;
         }
      }
   }

   public void addConsolidatedItem(ItemStack template, long amount) {
      this.addItem(template, amount);
   }

   public boolean removeItems(Map<ItemSignature, Long> items) {
      if (items != null && !items.isEmpty()) {
         Map<ItemSignature, Long> toRemove = new HashMap<>(items.size());

         for (Entry<ItemSignature, Long> entry : items.entrySet()) {
            ItemSignature signature = entry.getKey();
            Number amountValue = entry.getValue();
            if (signature != null && amountValue != null) {
               long amount = amountValue.longValue();
               if (amount > 0L) {
                  toRemove.merge(signature, amount, Long::sum);
               }
            }
         }

         if (toRemove.isEmpty()) {
            return true;
         } else {
            for (Entry<ItemSignature, Long> entryx : toRemove.entrySet()) {
               if (this.consolidatedItems.getOrDefault(entryx.getKey(), 0L) < entryx.getValue()) {
                  return false;
               }
            }

            for (Entry<ItemSignature, Long> entryxx : toRemove.entrySet()) {
               this.consolidatedItems.computeIfPresent(entryxx.getKey(), (key, current) -> {
                  long remaining = current - entry.getValue();
                  return remaining <= 0L ? null : remaining;
               });
            }

            this.sortedEntriesCache = null;
            return true;
         }
      } else {
         return true;
      }
   }

   public Int2ObjectMap<ItemStack> getDisplayPage(int page, int pageSize) {
      if (pageSize <= 0) {
         return Int2ObjectMaps.emptyMap();
      } else {
         int safePage = Math.max(1, page);
         int startSlot = (safePage - 1) * pageSize;
         return this.buildDisplaySection(startSlot, pageSize);
      }
   }

   public Int2ObjectMap<ItemStack> getDisplayRange(int startSlot, int maxResults) {
      return this.buildDisplaySection(startSlot, maxResults);
   }

   public Map<ItemSignature, Long> getConsolidatedItems() {
      return new HashMap<>(this.consolidatedItems);
   }

   public int getUsedSlots() {
      if (this.consolidatedItems.isEmpty()) {
         return 0;
      } else {
         int estimatedSlots = 0;

         for (Entry<ItemSignature, Long> entry : this.consolidatedItems.entrySet()) {
            long amount = entry.getValue();
            int maxStackSize = entry.getKey().getMaxStackSize();
            estimatedSlots += (int)Math.ceil((double)amount / maxStackSize);
            if (estimatedSlots >= this.maxSlots) {
               return this.maxSlots;
            }
         }

         return estimatedSlots;
      }
   }

   public void sortItems(Material preferredMaterial) {
      this.preferredSortMaterial = preferredMaterial;
      this.sortedEntriesCache = null;
      if (!this.consolidatedItems.isEmpty()) {
         if (preferredMaterial != null) {
            this.sortedEntriesCache = this.consolidatedItems.entrySet().stream().sorted((e1, e2) -> {
               boolean e1Preferred = e1.getKey().getMaterial() == preferredMaterial;
               boolean e2Preferred = e2.getKey().getMaterial() == preferredMaterial;
               if (e1Preferred && !e2Preferred) {
                  return -1;
               } else {
                  return !e1Preferred && e2Preferred ? 1 : e1.getKey().getMaterialName().compareTo(e2.getKey().getMaterialName());
               }
            }).collect(Collectors.toList());
         } else {
            this.sortedEntriesCache = this.consolidatedItems
               .entrySet()
               .stream()
               .sorted(Comparator.comparing(e -> e.getKey().getMaterialName()))
               .collect(Collectors.toList());
         }
      }
   }

   private Int2ObjectMap<ItemStack> buildDisplaySection(int startSlot, int maxResults) {
      if (maxResults <= 0 || startSlot >= this.maxSlots) {
         return Int2ObjectMaps.emptyMap();
      } else if (this.consolidatedItems.isEmpty()) {
         return Int2ObjectMaps.emptyMap();
      } else {
         int safeStart = Math.max(0, startSlot);
         int sectionLimit = Math.min(maxResults, this.maxSlots - safeStart);
         if (sectionLimit <= 0) {
            return Int2ObjectMaps.emptyMap();
         } else {
            Int2ObjectOpenHashMap<ItemStack> section = new Int2ObjectOpenHashMap(Math.min(sectionLimit, 45));
            List<Entry<ItemSignature, Long>> sortedEntries = this.getSortedEntries();
            int currentGlobalSlot = 0;
            int relativeSlot = 0;

            for (Entry<ItemSignature, Long> entry : sortedEntries) {
               if (relativeSlot >= sectionLimit || currentGlobalSlot >= this.maxSlots) {
                  break;
               }

               ItemSignature sig = entry.getKey();
               int maxStackSize = sig.getMaxStackSize();
               if (maxStackSize > 0) {
                  long totalAmount = entry.getValue();
                  int stacksForEntry = (int)Math.min(2147483647L, (totalAmount + maxStackSize - 1L) / maxStackSize);
                  if (currentGlobalSlot + stacksForEntry <= safeStart) {
                     currentGlobalSlot += stacksForEntry;
                  } else {
                     int stacksToSkip = Math.max(0, safeStart - currentGlobalSlot);
                     long remainingAmount = totalAmount - (long)stacksToSkip * maxStackSize;

                     for (currentGlobalSlot += stacksToSkip;
                        remainingAmount > 0L && relativeSlot < sectionLimit && currentGlobalSlot < this.maxSlots;
                        currentGlobalSlot++
                     ) {
                        ItemStack displayItem = sig.getTemplate();
                        displayItem.setAmount((int)Math.min(remainingAmount, (long)maxStackSize));
                        section.put(relativeSlot++, displayItem);
                        remainingAmount -= maxStackSize;
                     }
                  }
               }
            }

            return Int2ObjectMaps.unmodifiable(section);
         }
      }
   }

   private List<Entry<ItemSignature, Long>> getSortedEntries() {
      if (this.sortedEntriesCache == null) {
         this.sortedEntriesCache = new ArrayList<>(this.consolidatedItems.entrySet());
         this.sortEntries(this.sortedEntriesCache);
      }

      return this.sortedEntriesCache;
   }

   private void sortEntries(List<Entry<ItemSignature, Long>> entries) {
      if (this.preferredSortMaterial != null) {
         entries.sort((e1, e2) -> {
            boolean e1Preferred = e1.getKey().getMaterial() == this.preferredSortMaterial;
            boolean e2Preferred = e2.getKey().getMaterial() == this.preferredSortMaterial;
            if (e1Preferred && !e2Preferred) {
               return -1;
            } else {
               return !e1Preferred && e2Preferred ? 1 : e1.getKey().getMaterialName().compareTo(e2.getKey().getMaterialName());
            }
         });
      } else {
         entries.sort(Comparator.comparing(e -> e.getKey().getMaterialName()));
      }
   }

   @Generated
   public int getMaxSlots() {
      return this.maxSlots;
   }
}
