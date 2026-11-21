package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.menus.premade.ItemEnchantmentMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.instanced.dungeons.EnchantmentDungeonInstance;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentAcquisition;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentProgression;
import com.magmaguy.elitemobs.items.upgradesystem.UpgradeSystem;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemEnchantmentMenu extends EliteMenu {
   private static final String MENU_NAME = ItemEnchantmentMenuConfig.getMenuName();
   private static final int CANCEL_SLOT = ItemEnchantmentMenuConfig.getCancelSlot();
   private static final ItemStack cancelButton = ItemEnchantmentMenuConfig.getCancelButton();
   private static final int CONFIRM_SLOT = ItemEnchantmentMenuConfig.getConfirmSlot();
   private static final ItemStack confirmButton = ItemEnchantmentMenuConfig.getConfirmButton();
   private static final int INFO_SLOT = ItemEnchantmentMenuConfig.getInfoSlot();
   private static final ItemStack infoButton = ItemEnchantmentMenuConfig.getInfoButton();
   private static final int ITEM_SLOT = ItemEnchantmentMenuConfig.getItemSlot();
   private static final int ENCHANTED_BOOK_SLOT = ItemEnchantmentMenuConfig.getEnchantedBookSlot();
   private static final int ITEM_INFO_SLOT = ItemEnchantmentMenuConfig.getItemInfoSlot();
   private static final ItemStack itemInfoItemButton = ItemEnchantmentMenuConfig.getItemInfoButton();
   private static final int ENCHANTED_BOOK_INFO_SLOT = ItemEnchantmentMenuConfig.getEnchantedBookInfoSlot();
   private static final ItemStack enchantedBookInfoButton = ItemEnchantmentMenuConfig.getEnchantedBookInfoButton();
   private static final List<String> confirmButtonLore = confirmButton.getItemMeta().getLore();
   private static final int LUCKY_TICKET_SLOT = ItemEnchantmentMenuConfig.getLuckyTicketSlot();
   private static final int LUCKY_TICKET_INFO_SLOT = ItemEnchantmentMenuConfig.getLuckyTicketInfoSlot();
   private static final ItemStack luckyTicketInfoButton = ItemEnchantmentMenuConfig.getLuckyTicketInfoButton();

   public ItemEnchantmentMenu(Player player) {
      String name = MENU_NAME;
      if (DefaultConfig.useResourcePackModels()) {
         name = ChatColor.WHITE + "\udb83\udef1\udb83\ude01\udb83\udef5           " + MENU_NAME;
      }

      Inventory inventory = Bukkit.createInventory(player, 54, name);
      ItemEnchantmentMenu.ItemEnchantMenuEvents.menus.add(inventory);
      inventory.setItem(INFO_SLOT, infoButton);
      inventory.setItem(ENCHANTED_BOOK_INFO_SLOT, enchantedBookInfoButton);
      inventory.setItem(ITEM_INFO_SLOT, itemInfoItemButton);
      inventory.setItem(CANCEL_SLOT, cancelButton);
      inventory.setItem(CONFIRM_SLOT, confirmButton);
      inventory.setItem(LUCKY_TICKET_INFO_SLOT, luckyTicketInfoButton);
      updateConfirmButton(inventory);
      player.openInventory(inventory);
   }

   private static void updateConfirmButton(Inventory inventory) {
      ItemStack newButton = confirmButton.clone();
      ItemMeta itemMeta = newButton.getItemMeta();
      List<String> newLore = new ArrayList<>();

      try {
         ItemStack item = inventory.getItem(ITEM_SLOT);
         EnchantmentProgression.Quote quote = EnchantmentProgression.quote(item, inventory.getItem(LUCKY_TICKET_SLOT) != null);

         for (String string : confirmButtonLore) {
            newLore.add(
               string.replace("$price", EconomyHandler.formatCurrency(item == null ? 0.0 : quote.price()))
                  .replace("$currencyName", EconomySettingsConfig.getCurrencyName())
                  .replace("$successChance", Double.toString(Round.twoDecimalPlaces(quote.success() * 100.0)))
                  .replace("$criticalFailureChance", Double.toString(Round.twoDecimalPlaces(quote.criticalFailure() * 100.0)))
                  .replace("$challengeChance", Double.toString(Round.twoDecimalPlaces(quote.challenge() * 100.0)))
                  .replace("$failureChance", Double.toString(Round.twoDecimalPlaces(quote.failure() * 100.0)))
            );
         }
      } catch (RuntimeException var8) {
         newLore.add(ChatColor.RED + "This item or the enchantment settings are invalid.");
      }

      itemMeta.setLore(newLore);
      newButton.setItemMeta(itemMeta);
      inventory.setItem(CONFIRM_SLOT, newButton);
   }

   public static void broadcastEnchantmentMessage(ItemStack upgradedItem, Player upgradingPlayer, String message) {
      if (SpecialItemSystemsConfig.isAnnounceImportantEnchantments() && EnchantmentProgression.weight(upgradedItem) > 10) {
         if (!message.contains("$itemName")) {
            Bukkit.getOnlinePlayers()
               .forEach(
                  player -> player.spigot()
                     .sendMessage(
                        ChatMessageType.CHAT,
                        TextComponent.fromLegacyText(
                           ChatColorConverter.convert(
                              message.replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName())
                           )
                        )
                     )
               );
         } else {
            TextComponent itemName = ShareItem.hoverableItemTextComponent(upgradedItem);
            String[] text = message.replace("$itemName", "itemName").split("itemName");
            BaseComponent[] baseComponent1 = TextComponent.fromLegacyText(
               ChatColorConverter.convert(text[0].replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName()))
            );
            BaseComponent[] baseComponent2 = TextComponent.fromLegacyText(
               ChatColorConverter.convert(text[1].replace("$playerName", upgradingPlayer.getName()).replace("$player", upgradingPlayer.getDisplayName()))
            );
            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder.append(baseComponent1);
            componentBuilder.append(itemName);
            componentBuilder.append(baseComponent2);
            Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.CHAT, componentBuilder.create()));
         }
      }
   }

   private static enum Chance {
      SUCCESS,
      CHALLENGE,
      FAILURE,
      CRITICAL_FAILURE;
   }

   public static class ItemEnchantMenuEvents implements Listener {
      private static final Set<Inventory> menus = new HashSet<>();
      private static final Set<Inventory> processing = new HashSet<>();
      private final DoubleSupplier outcomeRandom;

      public ItemEnchantMenuEvents() {
         this(() -> ThreadLocalRandom.current().nextDouble());
      }

      ItemEnchantMenuEvents(DoubleSupplier outcomeRandom) {
         this.outcomeRandom = Objects.requireNonNull(outcomeRandom);
      }

      public static void shutdown() {
         for (Inventory inventory : new ArrayList<>(menus)) {
            menus.remove(inventory);

            for (HumanEntity viewer : new ArrayList(inventory.getViewers())) {
               EliteMenu.cancel(
                  viewer,
                  inventory,
                  viewer.getInventory(),
                  List.of(ItemEnchantmentMenu.ITEM_SLOT, ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT, ItemEnchantmentMenu.LUCKY_TICKET_SLOT)
               );
               viewer.closeInventory();
            }
         }

         menus.clear();
         processing.clear();
      }

      @EventHandler(
         priority = EventPriority.HIGHEST,
         ignoreCancelled = true
      )
      public void onInventoryInteract(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, menus)) {
            event.setCancelled(true);
            if (!processing.contains(event.getView().getTopInventory())) {
               if (SharedShopElements.itemNullPointerPrevention(event)) {
                  Inventory inventory = event.getView().getTopInventory();
                  ItemStack previousInput = inventory.getItem(ItemEnchantmentMenu.ITEM_SLOT);
                  ItemStack previousBook = inventory.getItem(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT);
                  if (EliteMenu.isTopMenu(event)) {
                     this.handleTopInventory(event);
                  } else {
                     this.handleBottomInventory(event);
                  }

                  if (menus.contains(inventory) && event.getWhoClicked().getOpenInventory().getTopInventory() == inventory) {
                     ItemStack input = inventory.getItem(ItemEnchantmentMenu.ITEM_SLOT);
                     ItemStack book = inventory.getItem(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT);
                     if (input != null && book != null && (!Objects.equals(previousInput, input) || !Objects.equals(previousBook, book))) {
                        try {
                           UpgradeSystem.preview(input, book);
                        } catch (EnchantmentItems.LimitExceededException var8) {
                           event.getWhoClicked().sendMessage(ItemEnchantmentMenuConfig.getEnchantmentLimitMessage());
                           event.getWhoClicked().closeInventory();
                        } catch (RuntimeException var9) {
                           Logger.warn("Rejected enchantment preview: " + var9.getMessage());
                           event.getWhoClicked().sendMessage(ItemEnchantmentMenuConfig.getIncompatibleEnchantmentMessage());
                           event.getWhoClicked().closeInventory();
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryDrag(InventoryDragEvent event) {
         if (menus.contains(event.getView().getTopInventory())) {
            int topInventorySize = event.getView().getTopInventory().getSize();

            for (int rawSlot : event.getRawSlots()) {
               if (rawSlot < topInventorySize) {
                  event.setCancelled(true);
                  return;
               }
            }
         }
      }

      private void handleTopInventory(InventoryClickEvent event) {
         int clickedSlot = event.getSlot();
         if (clickedSlot == ItemEnchantmentMenu.CANCEL_SLOT) {
            event.getWhoClicked().closeInventory();
         } else if (clickedSlot == ItemEnchantmentMenu.CONFIRM_SLOT) {
            this.confirm(event);
         } else if (clickedSlot == ItemEnchantmentMenu.ITEM_SLOT
            || clickedSlot == ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT
            || clickedSlot == ItemEnchantmentMenu.LUCKY_TICKET_SLOT) {
            EliteMenu.moveItemDown(event.getView().getTopInventory(), clickedSlot, event.getWhoClicked());
            event.getClickedInventory().clear(clickedSlot);
            if (clickedSlot == ItemEnchantmentMenu.ITEM_SLOT && event.getView().getTopInventory().getItem(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT) != null) {
               EliteMenu.moveItemDown(event.getView().getTopInventory(), ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT, event.getWhoClicked());
            }

            ItemEnchantmentMenu.updateConfirmButton(event.getInventory());
         }
      }

      private void handleBottomInventory(InventoryClickEvent event) {
         if (EliteEnchantmentItems.isEliteEnchantmentBook(event.getCurrentItem())) {
            if (event.getInventory().getItem(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT) == null) {
               EliteMenu.moveOneItemUp(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT, event);
            }
         } else if (EliteEnchantmentItems.isEliteLuckyTicket(event.getCurrentItem())
            && event.getView().getTopInventory().getItem(ItemEnchantmentMenu.LUCKY_TICKET_SLOT) == null) {
            EliteMenu.moveOneItemUp(ItemEnchantmentMenu.LUCKY_TICKET_SLOT, event);
            ItemEnchantmentMenu.updateConfirmButton(event.getInventory());
         } else if (ItemTagger.isEliteItem(event.getCurrentItem()) && event.getInventory().getItem(ItemEnchantmentMenu.ITEM_SLOT) == null) {
            EliteMenu.moveOneItemUp(ItemEnchantmentMenu.ITEM_SLOT, event);
            ItemEnchantmentMenu.updateConfirmButton(event.getInventory());
         }
      }

      private void confirm(InventoryClickEvent event) {
         Inventory inventory = event.getView().getTopInventory();
         if (processing.add(inventory)) {
            EnchantmentAcquisition attempt = null;
            Player player = (Player)event.getWhoClicked();

            try {
               ItemStack input = inventory.getItem(ItemEnchantmentMenu.ITEM_SLOT);
               ItemStack book = inventory.getItem(ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT);
               if (input == null || book == null) {
                  player.sendMessage(ItemEnchantmentMenuConfig.getMissingItemsMessage());
                  return;
               }

               attempt = new EnchantmentAcquisition(player, input, book, inventory.getItem(ItemEnchantmentMenu.LUCKY_TICKET_SLOT));
               if (attempt.purchase(
                  inventory,
                  ItemEnchantmentMenu.ITEM_SLOT,
                  ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT,
                  ItemEnchantmentMenu.LUCKY_TICKET_SLOT,
                  () -> menus.contains(inventory) && player.getOpenInventory().getTopInventory() == inventory
               )) {
                  player.closeInventory();
                  switch (this.rollChance(attempt.quote())) {
                     case SUCCESS:
                        attempt.success();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeSuccessMessage());
                        ItemEnchantmentMenu.broadcastEnchantmentMessage(attempt.upgraded(), player, SpecialItemSystemsConfig.getSuccessAnnouncement());
                        return;
                     case CHALLENGE:
                        if (!EnchantmentDungeonInstance.setupRandomEnchantedChallengeDungeon(player, attempt)) {
                           attempt.success();
                           player.sendMessage(DefaultConfig.getEnchantmentChallengeSuccessMessage());
                           ItemEnchantmentMenu.broadcastEnchantmentMessage(attempt.upgraded(), player, SpecialItemSystemsConfig.getSuccessAnnouncement());
                        } else if (attempt.isOwned()) {
                           player.sendMessage(DefaultConfig.getEnchantmentChallengeStartMessage());
                           player.sendMessage(DefaultConfig.getEnchantmentChallengeConsequencesMessage());
                           ItemEnchantmentMenu.broadcastEnchantmentMessage(attempt.original(), player, SpecialItemSystemsConfig.getChallengeAnnouncement());
                           return;
                        }

                        return;
                     case FAILURE:
                        attempt.failure();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeFailureMessage());
                        return;
                     case CRITICAL_FAILURE:
                        attempt.criticalFailure();
                        player.sendMessage(DefaultConfig.getEnchantmentChallengeCriticalFailureMessage());
                        ItemEnchantmentMenu.broadcastEnchantmentMessage(attempt.original(), player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
                        return;
                     default:
                        return;
                  }
               }

               player.sendMessage(ChatColor.RED + "The enchantment purchase was not completed.");
            } catch (EnchantmentItems.LimitExceededException var11) {
               if (attempt != null) {
                  attempt.abort("enchantment limit changed: " + var11.getMessage());
               }

               player.sendMessage(ItemEnchantmentMenuConfig.getEnchantmentLimitMessage());
               return;
            } catch (RuntimeException var12) {
               if (attempt != null) {
                  attempt.abort("enchantment operation failed: " + var12);
               }

               player.sendMessage(ChatColor.RED + "The enchantment attempt could not be completed.");
               MetadataHandler.PLUGIN.getLogger().warning("Enchantment attempt failed: " + var12);
               return;
            } finally {
               processing.remove(inventory);
            }
         }
      }

      private ItemEnchantmentMenu.Chance rollChance(EnchantmentProgression.Quote quote) {
         double rolled = this.outcomeRandom.getAsDouble();
         if (Double.isFinite(rolled) && !(rolled < 0.0) && !(rolled >= 1.0)) {
            double threshold = quote.success();
            if (rolled < threshold) {
               return ItemEnchantmentMenu.Chance.SUCCESS;
            } else {
               threshold += quote.criticalFailure();
               if (rolled < threshold) {
                  return ItemEnchantmentMenu.Chance.CRITICAL_FAILURE;
               } else {
                  threshold += quote.challenge();
                  return rolled < threshold ? ItemEnchantmentMenu.Chance.CHALLENGE : ItemEnchantmentMenu.Chance.FAILURE;
               }
            }
         } else {
            throw new IllegalStateException("Invalid enchantment outcome sample");
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            menus.remove(event.getInventory());
            EliteMenu.cancel(
               event.getPlayer(),
               event.getView().getTopInventory(),
               event.getView().getBottomInventory(),
               new ArrayList<>(List.of(ItemEnchantmentMenu.ITEM_SLOT, ItemEnchantmentMenu.ENCHANTED_BOOK_SLOT, ItemEnchantmentMenu.LUCKY_TICKET_SLOT))
            );
         }
      }
   }
}
