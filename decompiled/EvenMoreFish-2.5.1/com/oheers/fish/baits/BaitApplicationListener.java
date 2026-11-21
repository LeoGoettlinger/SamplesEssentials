package com.oheers.fish.baits;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BaitApplicationListener implements Listener {
   @EventHandler
   public void onClickEvent(InventoryClickEvent event) {
      ItemStack potentialFishingRod = event.getCurrentItem();
      ItemStack cursor = event.getCursor();
      if (!MainConfig.getInstance().shouldProtectBaitedRods() || !this.anvilCheck(event)) {
         if (BaitNBTManager.isBaitObject(cursor) && potentialFishingRod != null && event.getClickedInventory() instanceof PlayerInventory) {
            if (potentialFishingRod.getType().equals(Material.FISHING_ROD)) {
               if (!Checks.canUseRod(potentialFishingRod)) {
                  ConfigMessage.BAIT_INVALID_ROD.getMessage().send(event.getWhoClicked());
               } else {
                  GameMode gameMode = event.getWhoClicked().getGameMode();
                  if (!gameMode.equals(GameMode.SURVIVAL) && !gameMode.equals(GameMode.ADVENTURE)) {
                     ConfigMessage.BAIT_WRONG_GAMEMODE.getMessage().send(event.getWhoClicked());
                  } else {
                     BaitHandler bait = BaitManager.getInstance().getBait(BaitNBTManager.getBaitName(event.getCursor()));
                     if (bait != null) {
                        this.updateNbt(potentialFishingRod);

                        ApplicationResult result;
                        try {
                           if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                              result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, event.getCursor().getAmount());
                              EvenMoreFish.getInstance().getMetricsManager().incrementBaitsApplied(event.getCursor().getAmount());
                           } else {
                              result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, 1);
                              EvenMoreFish.getInstance().getMetricsManager().incrementBaitsApplied(1);
                           }
                        } catch (MaxBaitsReachedException var9) {
                           ConfigMessage.BAITS_MAXED.getMessage().send(event.getWhoClicked());
                           result = var9.getRecoveryResult();
                        } catch (MaxBaitReachedException var10) {
                           result = var10.getRecoveryResult();
                           EMFMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
                           message.setBait(bait);
                           message.send(event.getWhoClicked());
                        }

                        ItemStack resultRod = result.fishingRod();
                        if (!resultRod.isEmpty()) {
                           event.setCancelled(true);
                           event.setCurrentItem(resultRod);
                           int cursorModifier = result.cursorItemModifier();
                           if (cursor.getAmount() - cursorModifier == 0) {
                              event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                           } else {
                              cursor.setAmount(cursor.getAmount() + cursorModifier);
                              event.getWhoClicked().setItemOnCursor(cursor);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean anvilCheck(InventoryClickEvent event) {
      if (!(event.getClickedInventory() instanceof AnvilInventory inv && event.getWhoClicked() instanceof Player player)) {
         return false;
      } else if (event.getSlot() == 2 && BaitNBTManager.isBaitedRod(inv.getItem(1))) {
         event.setCancelled(true);
         player.closeInventory();
         ConfigMessage.BAIT_ROD_PROTECTION.getMessage().send(player);
         return true;
      } else {
         return false;
      }
   }

   private void updateNbt(final ItemStack fishingRod) {
      if (fishingRod != null && !fishingRod.isEmpty()) {
         ItemMeta meta = fishingRod.getItemMeta();
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         NamespacedKey key = NbtKeys.EMF_APPLIED_BAIT.get();
         String appliedBaitString = (String)pdc.get(key, PersistentDataType.STRING);
         if (appliedBaitString != null) {
            pdc.remove(key);
            fishingRod.setItemMeta(meta);
            NBTHolder<ItemStack> modernHolder = EvenMoreFish.getInstance().getVersionProvider().createItemStackNbtHolder(fishingRod);
            modernHolder.setString(NbtKeys.EMF_APPLIED_BAIT.get(), appliedBaitString);
         }
      }
   }
}
