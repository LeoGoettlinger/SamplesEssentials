package com.artillexstudios.axtrade.trade;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.hooks.HookManager;
import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import com.artillexstudios.axtrade.libs.axapi.gui.SignInput;
import com.artillexstudios.axtrade.libs.axapi.nms.NMSHandlers;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axtrade.libs.axapi.utils.Cooldown;
import com.artillexstudios.axtrade.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.libs.gui.guis.BaseGui;
import com.artillexstudios.axtrade.libs.gui.guis.Gui;
import com.artillexstudios.axtrade.libs.gui.guis.GuiItem;
import com.artillexstudios.axtrade.libs.gui.guis.StorageGui;
import com.artillexstudios.axtrade.safety.SafetyManager;
import com.artillexstudios.axtrade.utils.BlacklistUtils;
import com.artillexstudios.axtrade.utils.NumberUtils;
import com.artillexstudios.axtrade.utils.ShulkerUtils;
import com.artillexstudios.axtrade.utils.TaxUtils;
import com.artillexstudios.axtrade.utils.Utils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TradeGui extends GuiFrame {
   private static final Cooldown<Player> confirmCooldown = PaperUtils.isFolia() ? Cooldown.createSynchronized() : Cooldown.create();
   protected final Trade trade;
   private final TradePlayer player;
   protected final StorageGui gui;
   protected final List<Integer> slots = this.getSlots("own-slots");
   protected final List<Integer> otherSlots = this.getSlots("partner-slots");
   private String currentTitle = "";
   private boolean inSign = false;

   public TradeGui(@NotNull Trade trade, @NotNull TradePlayer player) {
      super(AxTrade.GUIS, player.getPlayer(), trade);
      this.trade = trade;
      this.player = player;
      this.gui = Gui.storage().rows(AxTrade.GUIS.getInt("rows", 6)).title(Component.empty()).disableItemDrop().create();
      this.setGui(this.gui);
      this.gui.setDefaultTopClickAction(this::handleClickTop);
      this.gui.setDragAction(this::handleDrag);
      this.gui.setPlayerInventoryAction(this::handleClickBottom);
      this.gui.setCloseGuiAction(this::handleClose);
      if (!trade.isEnded()) {
         this.update();
         Runnable runnable = () -> {
            this.gui.open(player.getPlayer());
            this.updateTitle();
         };
         Scheduler.get().execute(player.getPlayer(), runnable, runnable, 0L);
         this.opened = true;
      }
   }

   public void update() {
      if (this.player.hasConfirmed()) {
         super.createItem("own.confirm-item.slot", "own.confirm-item.cancel", event -> {
            event.setCancelled(true);
            if (!confirmCooldown.hasCooldown(this.player.getPlayer())) {
               confirmCooldown.addCooldown(this.player.getPlayer(), 50L);
               this.player.cancel();
               this.trade.update();
            }
         }, Map.of(), AxTrade.CONFIG.getBoolean("static-accept-item-amount", true) ? 1 : this.player.getConfirmed());
      } else {
         super.createItem("own.confirm-item.slot", "own.confirm-item.accept", event -> {
            event.setCancelled(true);
            if (!confirmCooldown.hasCooldown(this.player.getPlayer())) {
               confirmCooldown.addCooldown(this.player.getPlayer(), 50L);
               this.player.confirm();
            }
         }, Map.of());
      }

      if (this.player.getOtherPlayer().hasConfirmed()) {
         super.createItem(
            "partner.confirm-item.slot",
            "partner.confirm-item.cancel",
            event -> event.setCancelled(true),
            Map.of("%own-name%", this.player.getPlayer().getName(), "%partner-name%", this.player.getOtherPlayer().getPlayer().getName()),
            AxTrade.CONFIG.getBoolean("static-accept-item-amount", true) ? 1 : this.player.getOtherPlayer().getConfirmed()
         );
      } else {
         super.createItem(
            "partner.confirm-item.slot",
            "partner.confirm-item.accept",
            event -> event.setCancelled(true),
            Map.of("%own-name%", this.player.getPlayer().getName(), "%partner-name%", this.player.getOtherPlayer().getPlayer().getName())
         );
      }

      for (String currencyItem : AxTrade.GUIS.getSection("own").getRoutesAsStrings(false)) {
         String currencyStr = AxTrade.GUIS.getString("own." + currencyItem + ".currency", null);
         if (currencyStr != null) {
            CurrencyHook currencyHook = HookManager.getCurrencyHook(currencyStr);
            if (currencyHook != null) {
               super.createItem(
                  "own." + currencyItem,
                  event -> this.handleCurrencyClick(currencyStr, event),
                  Map.of(
                     "%amount%",
                     NumberUtils.formatNumber(this.player.getCurrency(currencyStr)),
                     "%tax-amount%",
                     NumberUtils.formatNumber(TaxUtils.getTotalAfterTax(this.player.getCurrency(currencyStr), currencyHook)),
                     "%tax-percent%",
                     NumberUtils.formatNumber(TaxUtils.getTaxPercent(currencyHook).doubleValue()),
                     "%tax-fee%",
                     NumberUtils.formatNumber(TaxUtils.getTotalTax(this.player.getCurrency(currencyStr), currencyHook))
                  )
               );
            }
         }
      }

      for (String currencyItemx : AxTrade.GUIS.getSection("partner").getRoutesAsStrings(false)) {
         String currencyStr = AxTrade.GUIS.getString("partner." + currencyItemx + ".currency", null);
         if (currencyStr != null) {
            CurrencyHook currencyHook = HookManager.getCurrencyHook(currencyStr);
            if (currencyHook != null) {
               super.createItem(
                  "partner." + currencyItemx,
                  event -> event.setCancelled(true),
                  Map.of(
                     "%amount%",
                     NumberUtils.formatNumber(this.player.getOtherPlayer().getCurrency(currencyStr)),
                     "%tax-amount%",
                     NumberUtils.formatNumber(TaxUtils.getTotalAfterTax(this.player.getOtherPlayer().getCurrency(currencyStr), currencyHook)),
                     "%tax-percent%",
                     NumberUtils.formatNumber(TaxUtils.getTaxPercent(currencyHook).doubleValue()),
                     "%tax-fee%",
                     NumberUtils.formatNumber(TaxUtils.getTotalTax(this.player.getOtherPlayer().getCurrency(currencyStr), currencyHook))
                  )
               );
            }
         }
      }

      for (int slot : this.otherSlots) {
         this.gui.removeItem(slot);
      }

      if (this.opened) {
         List<ItemStack> otherItems = this.player.getOtherPlayer().getTradeGui().getItems(true);
         int n = 0;

         for (int slot : this.otherSlots) {
            if (otherItems.get(n) != null) {
               this.gui.updateItem(slot, new GuiItem(otherItems.get(n), event -> event.setCancelled(true)));
            }

            n++;
         }
      }
   }

   private void handleClickTop(InventoryClickEvent event) {
      if (this.trade.isEnded()) {
         event.setCancelled(true);
      } else if (confirmCooldown.hasCooldown(this.player.getPlayer())) {
         event.setCancelled(true);
      } else {
         ItemStack it = this.getItem(event);
         if (BlacklistUtils.isBlacklisted(it)) {
            event.setCancelled(true);
            AxTrade.MESSAGEUTILS.sendLang(this.player.getPlayer(), "trade.blacklisted-item");
         } else if (event.getCurrentItem() != null || it == null || !this.checkFull(event)) {
            if (event.getCurrentItem() != null && event.getClick().isRightClick() && Tag.SHULKER_BOXES.isTagged(event.getCurrentItem().getType())) {
               this.handleShulkerClick(event);
            } else if (!this.slots.contains(event.getSlot())) {
               event.setCancelled(true);
               if (event.getCursor() != null) {
                  this.player.getPlayer().getInventory().addItem(new ItemStack[]{event.getCursor().clone()});
                  event.getCursor().setAmount(0);
               }
            } else {
               this.player.cancel();
               Scheduler.get().run(scheduledTask -> this.trade.update());
            }
         }
      }
   }

   private void handleClickBottom(InventoryClickEvent event) {
      if (this.trade.isEnded()) {
         event.setCancelled(true);
      } else {
         ItemStack it = this.getItem(event);
         if (BlacklistUtils.isBlacklisted(it)) {
            event.setCancelled(true);
            AxTrade.MESSAGEUTILS.sendLang(this.player.getPlayer(), "trade.blacklisted-item");
         } else {
            if (event.getCurrentItem() != null) {
               if (this.checkFull(event)) {
                  return;
               }

               if (event.isShiftClick()
                  && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
                  && !this.slots.contains(event.getView().getTopInventory().firstEmpty())) {
                  event.setCancelled(true);

                  for (int i : this.slots) {
                     if (this.gui.getInventory().getItem(i) == null) {
                        this.gui.getInventory().setItem(i, event.getCurrentItem().clone());
                        event.getCurrentItem().setAmount(0);
                        break;
                     }
                  }
               }
            }

            this.player.cancel();
            Scheduler.get().run(scheduledTask -> this.trade.update());
         }
      }
   }

   private void handleDrag(InventoryDragEvent event) {
      if (this.trade.isEnded()) {
         event.setCancelled(true);
      } else {
         boolean ownInv = true;

         for (int s : event.getRawSlots()) {
            if (s <= 53) {
               ownInv = false;
               break;
            }
         }

         Scheduler.get().run(scheduledTask -> this.trade.update());
         if (!ownInv) {
            if (!new HashSet<>(this.slots).containsAll(event.getInventorySlots())) {
               event.setCancelled(true);
            } else {
               this.player.cancel();
            }
         }
      }
   }

   private void handleClose(InventoryCloseEvent event) {
      if (!this.inSign) {
         this.trade.abort();
      }
   }

   private boolean checkFull(Cancellable event) {
      if (!AxTrade.CONFIG.getBoolean("prevent-adding-items-when-inventory-full", true)) {
         return false;
      } else {
         int filledSlots = this.getItems(false).size();
         int emptySlots = this.player.getOtherPlayer().getEmptySlots();
         if (filledSlots >= emptySlots) {
            event.setCancelled(true);
            AxTrade.MESSAGEUTILS.sendLang(this.player.getPlayer(), "trade.inventory-full");
            return true;
         } else {
            return false;
         }
      }
   }

   private void handleCurrencyClick(String currencyStr, InventoryClickEvent event) {
      event.setCancelled(true);
      if (!SafetyManager.CURRENCY_SELECTOR.get()) {
         AxTrade.MESSAGEUTILS.sendLang(this.player.getPlayer(), "safety");
      } else {
         this.player.cancel();
         this.trade.update();
         this.inSign = true;
         this.trade.prepTime = System.currentTimeMillis();
         event.getWhoClicked().closeInventory();
         List<Component> lines = StringUtils.formatList(AxTrade.LANG.getStringList("currency-editor-sign"));
         lines.set(0, Component.empty());
         SignInput sign = new SignInput.Builder().setLines(lines).setHandler((player1, result) -> {
            if (!this.trade.isEnded()) {
               this.trade.prepTime = System.currentTimeMillis();
               String am = result[0];
               TradePlayer.Result addResult = this.player.setCurrency(currencyStr, am);
               if (addResult == TradePlayer.Result.SUCCESS) {
                  AxTrade.MESSAGEUTILS.sendLang(player1, "currency-editor.success");
               } else {
                  switch (addResult) {
                     case NOT_ENOUGH_CURRENCY:
                        AxTrade.MESSAGEUTILS.sendLang(player1, "currency-editor.not-enough");
                        break;
                     default:
                        AxTrade.MESSAGEUTILS.sendLang(player1, "currency-editor.failed");
                  }
               }

               Runnable runnable = () -> {
                  if (!this.trade.isEnded()) {
                     this.gui.open(this.player.getPlayer());
                     this.inSign = false;
                     this.trade.update();
                     this.currentTitle = "";
                     this.player.cancel();
                     this.updateTitle();
                  }
               };
               Scheduler.get().run(this.player.getPlayer(), task -> runnable.run(), runnable);
            }
         }).build(this.player.getPlayer());
         sign.open();
      }
   }

   public void handleShulkerClick(InventoryClickEvent event) {
      event.setCancelled(true);
      if (!SafetyManager.SHULKER_VIEWER.get()) {
         AxTrade.MESSAGEUTILS.sendLang(this.player.getPlayer(), "safety");
      } else {
         this.player.cancel();
         this.trade.update();
         this.inSign = true;
         this.trade.prepTime = System.currentTimeMillis();
         event.getWhoClicked().closeInventory();
         BaseGui shulkerGui = Gui.storage()
            .rows(3)
            .title(StringUtils.format(Utils.getFormattedItemName(event.getCurrentItem())))
            .disableAllInteractions()
            .create();
         shulkerGui.getInventory().setContents(ShulkerUtils.getStorageContents(event.getCurrentItem(), false).toArray(ItemStack[]::new));
         shulkerGui.setCloseGuiAction(e -> Scheduler.get().runLaterAt(this.player.getPlayer().getLocation(), () -> {
            if (!this.trade.isEnded()) {
               this.trade.prepTime = System.currentTimeMillis();
               this.gui.open(this.player.getPlayer());
               this.inSign = false;
               this.trade.update();
               this.currentTitle = "";
               this.updateTitle();
            }
         }, 1L));
         shulkerGui.open(this.player.getPlayer());
      }
   }

   @Nullable
   private ItemStack getItem(InventoryClickEvent event) {
      if (event.getClickedInventory() != null) {
         if (event.getClick() == ClickType.SWAP_OFFHAND && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return this.player.getPlayer().getInventory().getItemInOffHand();
         }

         if (event.getClick() == ClickType.NUMBER_KEY) {
            Inventory inventory = event.getClickedInventory().getType() == InventoryType.PLAYER
               ? event.getView().getTopInventory()
               : event.getView().getBottomInventory();
            return inventory.getItem(event.getHotbarButton());
         }
      }

      return event.getCurrentItem();
   }

   public List<ItemStack> getItems(boolean includeAir) {
      List<ItemStack> items = new ArrayList<>();

      for (int slot : this.slots) {
         ItemStack item = this.gui.getInventory().getItem(slot);
         if (includeAir || item != null && !item.getType().isAir()) {
            items.add(item);
         }
      }

      return items;
   }

   public void updateTitle() {
      String newTitle = AxTrade.GUIS
         .getString("title")
         .replace("%player%", this.player.getOtherPlayer().getPlayer().getName())
         .replace("%own-status%", this.player.hasConfirmed() ? AxTrade.LANG.getString("placeholders.ready") : AxTrade.LANG.getString("placeholders.waiting"))
         .replace(
            "%partner-status%",
            this.player.getOtherPlayer().hasConfirmed() ? AxTrade.LANG.getString("placeholders.ready") : AxTrade.LANG.getString("placeholders.waiting")
         );
      if (!this.currentTitle.equals(newTitle)) {
         this.currentTitle = newTitle;
         Runnable runnable = () -> {
            Inventory topInv = this.player.getPlayer().getOpenInventory().getTopInventory();
            if (topInv.equals(this.gui.getInventory())) {
               NMSHandlers.getNmsHandler().setTitle(this.player.getPlayer().getOpenInventory().getTopInventory(), StringUtils.format(newTitle));
            }
         };
         Scheduler.get().runLater(this.player.getPlayer(), task -> runnable.run(), runnable, 1L);
      }
   }
}
