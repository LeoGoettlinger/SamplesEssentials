package net.coreprotect.listener.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public final class CraftItemListener extends Queue implements Listener {
   protected static void logCraftedItem(Location location, String user, ItemStack itemStack, int action) {
      if (Config.getConfig(location.getWorld()).ITEM_TRANSACTIONS && itemStack != null) {
         String loggingItemId = user.toLowerCase(Locale.ROOT) + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
         int itemId = getItemId(loggingItemId);
         if (action == 12) {
            List<ItemStack> list = ConfigHandler.itemsBuy.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsBuy.put(loggingItemId, list);
         } else if (action == 11) {
            List<ItemStack> list = ConfigHandler.itemsSell.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsSell.put(loggingItemId, list);
         } else if (action == 10) {
            List<ItemStack> list = ConfigHandler.itemsCreate.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsCreate.put(loggingItemId, list);
         } else {
            List<ItemStack> list = ConfigHandler.itemsDestroy.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsDestroy.put(loggingItemId, list);
         }

         int time = (int)(System.currentTimeMillis() / 1000L) + 1;
         Queue.queueItemTransaction(user, location.clone(), time, 0, itemId);
      }
   }

   protected static void playerCraftItem(InventoryClickEvent event, boolean isTrade) {
      if (event.getResult() != Result.DENY && event.getSlotType() == SlotType.RESULT) {
         HumanEntity player = event.getWhoClicked();
         if (event.getClick() != ClickType.NUMBER_KEY || player.getInventory().getItem(event.getHotbarButton()) == null) {
            if (event.getClick() != ClickType.DROP && event.getClick() != ClickType.CONTROL_DROP || event.getCursor().getType() == Material.AIR) {
               Inventory bottomInventory = player.getInventory();
               if (bottomInventory.getType() == InventoryType.PLAYER) {
                  Recipe recipe = null;
                  if (!isTrade && event instanceof CraftItemEvent) {
                     recipe = ((CraftItemEvent)event).getRecipe();
                  } else if (isTrade && event.getInventory() instanceof MerchantInventory) {
                     recipe = ((MerchantInventory)event.getInventory()).getSelectedRecipe();
                  }

                  if (recipe != null) {
                     ItemStack addItem = recipe.getResult().clone();
                     int amount = addItem.getAmount();
                     if (amount != 0) {
                        int amountMultiplier = 1;
                        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                           int newMultiplier = Integer.MIN_VALUE;

                           for (ItemStack item : event.getInventory().getContents()) {
                              if (item != null && item.getType() != Material.AIR && !item.equals(recipe.getResult())) {
                                 if (!isTrade) {
                                    if (newMultiplier == Integer.MIN_VALUE || item.getAmount() < newMultiplier) {
                                       newMultiplier = item.getAmount();
                                    }
                                 } else {
                                    int merchantAmount = newMultiplier;
                                    MerchantRecipe merchantRecipe = (MerchantRecipe)recipe;

                                    for (ItemStack ingredient : merchantRecipe.getIngredients()) {
                                       if (ingredient.isSimilar(item)) {
                                          ItemStack adjusted = BukkitAdapter.ADAPTER.adjustIngredient(merchantRecipe, ingredient);
                                          if (adjusted == null) {
                                             return;
                                          }

                                          merchantAmount = item.getAmount() / adjusted.getAmount();
                                          break;
                                       }
                                    }

                                    int merchantUsesLeft = merchantRecipe.getMaxUses() - merchantRecipe.getUses();
                                    if (merchantUsesLeft < merchantAmount) {
                                       merchantAmount = merchantUsesLeft;
                                    }

                                    if (newMultiplier == Integer.MIN_VALUE || merchantAmount < newMultiplier) {
                                       newMultiplier = merchantAmount;
                                    }
                                 }
                              }
                           }

                           amountMultiplier = newMultiplier == Integer.MIN_VALUE ? 1 : newMultiplier;
                           int addAmount = amount * amountMultiplier;
                           Inventory virtualInventory = Bukkit.createInventory(null, 36);
                           virtualInventory.setStorageContents(bottomInventory.getStorageContents());
                           addItem.setAmount(1);
                           int addSuccess = 0;

                           for (int i = 0; i < addAmount && virtualInventory.addItem(new ItemStack[]{addItem}).isEmpty(); i++) {
                              addSuccess++;
                           }

                           if (addSuccess < addAmount) {
                              addAmount = (int)(Math.ceil((double)addSuccess / amount) * amount);
                              amountMultiplier = addAmount / amount;
                           }

                           virtualInventory.clear();
                           addItem.setAmount(addAmount);
                        }

                        List<ItemStack> oldItems = new ArrayList<>();
                        if (recipe instanceof ShapelessRecipe) {
                           oldItems.addAll(((ShapelessRecipe)recipe).getIngredientList());
                        } else if (recipe instanceof ShapedRecipe) {
                           oldItems.addAll(((ShapedRecipe)recipe).getIngredientMap().values());
                        } else if (recipe instanceof MerchantRecipe) {
                           oldItems.addAll(((MerchantRecipe)recipe).getIngredients());
                        }

                        if (addItem.getAmount() > 0) {
                           Location location = !isTrade && event.getInventory().getLocation() != null
                              ? event.getInventory().getLocation()
                              : player.getLocation();

                           for (ItemStack oldItem : oldItems) {
                              if (oldItem != null && oldItem.getType() != Material.AIR) {
                                 ItemStack removedItem = isTrade ? BukkitAdapter.ADAPTER.adjustIngredient((MerchantRecipe)recipe, oldItem) : oldItem.clone();
                                 if (removedItem == null) {
                                    return;
                                 }

                                 removedItem.setAmount(removedItem.getAmount() * amountMultiplier);
                                 logCraftedItem(location, player.getName(), removedItem, isTrade ? 11 : 9);
                              }
                           }

                           logCraftedItem(location, player.getName(), addItem, isTrade ? 12 : 10);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   protected void onCraftItem(CraftItemEvent event) {
      playerCraftItem(event, false);
   }
}
