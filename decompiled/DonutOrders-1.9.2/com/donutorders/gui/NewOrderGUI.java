package com.donutorders.gui;

import com.donutorders.DonutOrders;
import com.donutorders.manager.GUIManager;
import com.donutorders.util.ChatInputParser;
import com.donutorders.util.EnchantOrderUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class NewOrderGUI extends BaseGUI {
   private static final int PAGE_SIZE = 45;
   private static final int SLOT_PREV = 45;
   private static final int SLOT_CLEAR = 46;
   private static final int SLOT_SEARCH = 48;
   private static final int SLOT_BACK = 49;
   private static final int SLOT_NEXT = 53;
   private static final int SLOT_NO_RESULTS = 22;
   private final GUIManager guiManager;
   private final List<Material> materials;
   private final String searchQuery;
   private final int page;
   private final int maxPage;

   public NewOrderGUI(GUIManager guiManager) {
      this(guiManager, 0, null);
   }

   public NewOrderGUI(GUIManager guiManager, int page) {
      this(guiManager, page, null);
   }

   public NewOrderGUI(GUIManager guiManager, int page, String searchQuery) {
      super(Bukkit.createInventory(null, 54, buildTitle(searchQuery)));
      this.guiManager = guiManager;
      this.searchQuery = normalizeSearchQuery(searchQuery);
      this.materials = filterMaterials(this.loadAllowedMaterials(), this.searchQuery);
      this.page = page;
      this.maxPage = this.materials.isEmpty() ? 0 : Math.max(0, (this.materials.size() - 1) / 45);
      this.build();
   }

   private static String buildTitle(String searchQuery) {
      String normalized = normalizeSearchQuery(searchQuery);
      return normalized == null
         ? MessageHelper.get("gui.new-order.title", "ɴᴇᴡ ᴏʀᴅᴇʀ")
         : MessageHelper.getNamed("gui.new-order.title-filtered", "&fɴᴇᴡ ᴏʀᴅᴇʀ &8| &e{query}", "query", normalized);
   }

   private static String normalizeSearchQuery(String searchQuery) {
      return searchQuery != null && !searchQuery.isBlank() ? searchQuery.trim() : null;
   }

   public static List<Material> filterMaterials(List<Material> materials, String searchQuery) {
      String query = normalizeSearchQuery(searchQuery);
      if (query == null) {
         return new ArrayList<>(materials);
      } else {
         String lowerQuery = query.toLowerCase(Locale.ROOT);
         return materials.stream().filter(mat -> matchesMaterialSearch(mat, lowerQuery)).collect(Collectors.toList());
      }
   }

   private static boolean matchesMaterialSearch(Material material, String lowerQuery) {
      String enumName = material.name().replace('_', ' ').toLowerCase(Locale.ROOT);
      String prettyName = ItemUtils.prettyName(material).toLowerCase(Locale.ROOT);
      return enumName.contains(lowerQuery) || prettyName.contains(lowerQuery);
   }

   private List<Material> loadAllowedMaterials() {
      return DonutOrders.getInstance().getAllowedItemsManager().getAllowedMaterials();
   }

   private void build() {
      if (this.materials.isEmpty() && this.searchQuery != null) {
         this.inventory
            .setItem(
               22,
               ItemUtils.createGuiItem(
                  Material.BARRIER,
                  MessageHelper.get("gui.new-order.no-results.name", "&c&lɴᴏ ʀᴇꜱᴜʟᴛꜱ"),
                  MessageHelper.getList("gui.new-order.no-results.lore", "query", this.searchQuery)
               )
            );
      } else {
         int start = this.page * 45;
         int end = Math.min(start + 45, this.materials.size());

         for (int i = start; i < end; i++) {
            Material mat = this.materials.get(i);
            this.inventory
               .setItem(
                  i - start,
                  ItemUtils.createGuiItem(
                     mat,
                     MessageHelper.getNamed("gui.new-order.material.name", "&f{item}", "item", ItemUtils.prettyName(mat)),
                     MessageHelper.getList("gui.new-order.material.lore")
                  )
               );
         }
      }

      if (this.page > 0) {
         this.inventory
            .setItem(
               45,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.previous.name", "&7« ᴘʀᴇᴠɪᴏᴜꜱ"),
                  MessageHelper.getList("buttons.previous.lore", "page", String.valueOf(this.page), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      if (this.page < this.maxPage) {
         this.inventory
            .setItem(
               53,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.next.name", "&7ɴᴇxᴛ »"),
                  MessageHelper.getList("buttons.next.lore", "page", String.valueOf(this.page + 2), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      if (this.searchQuery != null) {
         this.inventory
            .setItem(
               46,
               ItemUtils.createGuiItem(
                  Material.RED_STAINED_GLASS_PANE,
                  MessageHelper.get("gui.new-order.clear-search.name", "&c&lᴄʟᴇᴀʀ ꜱᴇᴀʀᴄʜ"),
                  MessageHelper.getList("gui.new-order.clear-search.lore", "query", this.searchQuery)
               )
            );
      }

      this.inventory
         .setItem(
            48,
            ItemUtils.createGuiItem(
               Material.COMPASS, MessageHelper.get("gui.new-order.search.name", "&e&lꜱᴇᴀʀᴄʜ"), MessageHelper.getList("gui.new-order.search.lore")
            )
         );
      this.inventory
         .setItem(
            49,
            ItemUtils.createGuiItem(
               Material.BARRIER, MessageHelper.get("gui.new-order.back.name", "&c&lʙᴀᴄᴋ"), MessageHelper.getList("gui.new-order.back.lore")
            )
         );
      this.fillEmpty();
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 45 && this.page > 0) {
         this.guiManager.openNewOrderPicker(player, this.page - 1);
      } else if (slot == 53 && this.page < this.maxPage) {
         this.guiManager.openNewOrderPicker(player, this.page + 1);
      } else if (slot == 46 && this.searchQuery != null) {
         this.guiManager.openNewOrderPickerWithSearch(player, null);
      } else if (slot == 48) {
         this.beginSearchFlow(player);
      } else if (slot == 49) {
         this.guiManager.openYourOrders(player, 0);
      } else {
         int matIndex = this.page * 45 + slot;
         if (slot < 45 && matIndex < this.materials.size()) {
            Material selectedMat = this.materials.get(matIndex);
            if (EnchantOrderUtils.needsEnchantPicker(selectedMat)) {
               this.guiManager.openEnchantPicker(player, 0);
               return;
            }

            ItemStack template = new ItemStack(selectedMat, 1);
            beginAmountPriceFlow(this.guiManager, player, template);
         }
      }
   }

   private void beginSearchFlow(Player player) {
      player.closeInventory();
      String cancelWord = MessageHelper.cancelKeyword();
      String prompt = MessageHelper.get("gui.new-order.search-prompt", "&eᴇɴᴛᴇʀ ᴀɴ ɪᴛᴇᴍ ɴᴀᴍᴇ ᴛᴏ ꜱᴇᴀʀᴄʜ (ᴏʀ &c{0}&e):", cancelWord);
      this.guiManager.getChatInput().requestInput(player, prompt, input -> this.guiManager.openNewOrderPickerWithSearch(player, input), () -> {
         MessageHelper.send(player, "chat-input-cancelled", "&7ɪɴᴘᴜᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
         this.guiManager.openNewOrderPicker(player, this.page);
      });
   }

   public static void beginAmountPriceFlow(GUIManager guiManager, Player player, ItemStack template) {
      GUIManager.PlayerGUIState state = guiManager.getState(player.getUniqueId());
      if (state != null) {
         state.selectedItem = template;
      }

      player.closeInventory();
      String cancelWord = MessageHelper.cancelKeyword();
      String amtPrompt = MessageHelper.get("chat-prompt-amount", "&eᴇɴᴛᴇʀ ᴛʜᴇ ᴀᴍᴏᴜɴᴛ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʙᴜʏ (ᴏʀ &c{0}&e):", cancelWord);
      guiManager.getChatInput().requestInput(player, amtPrompt, amountStr -> handleAmountInput(guiManager, player, template, amountStr), () -> {
         MessageHelper.send(player, "chat-input-cancelled", "&7ɪɴᴘᴜᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
         guiManager.openYourOrders(player, 0);
      });
   }

   private static void handleAmountInput(GUIManager guiManager, Player player, ItemStack template, String raw) {
      OptionalInt parsed = ChatInputParser.parseAmount(raw);
      if (parsed.isEmpty()) {
         MessageHelper.send(player, "chat-input-invalid-number", "&cɪɴᴠᴀʟɪᴅ ɴᴜᴍʙᴇʀ. ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀ ᴘᴏꜱɪᴛɪᴠᴇ ᴡʜᴏʟᴇ ɴᴜᴍʙᴇʀ.");
         String cancelWord = MessageHelper.cancelKeyword();
         guiManager.getChatInput()
            .requestInput(
               player,
               MessageHelper.get("chat-prompt-amount", "&eᴇɴᴛᴇʀ ᴛʜᴇ ᴀᴍᴏᴜɴᴛ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʙᴜʏ (ᴏʀ &c{0}&e):", cancelWord),
               s -> handleAmountInput(guiManager, player, template, s),
               () -> guiManager.openYourOrders(player, 0)
            );
      } else {
         int amount = parsed.getAsInt();
         GUIManager.PlayerGUIState state = guiManager.getState(player.getUniqueId());
         if (state != null) {
            state.pendingAmount = amount;
         }

         String cancelWord = MessageHelper.cancelKeyword();
         String pricePrompt = MessageHelper.get("chat-prompt-price", "&eᴇɴᴛᴇʀ ᴘʀɪᴄᴇ ᴘᴇʀ ɪᴛᴇᴍ (ᴏʀ &c{0}&e):", cancelWord);
         guiManager.getChatInput().requestInput(player, pricePrompt, priceStr -> handlePriceInput(guiManager, player, template, amount, priceStr), () -> {
            MessageHelper.send(player, "chat-input-cancelled", "&7ɪɴᴘᴜᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ.");
            guiManager.openYourOrders(player, 0);
         });
      }
   }

   private static void handlePriceInput(GUIManager guiManager, Player player, ItemStack template, int amount, String raw) {
      OptionalDouble parsed = ChatInputParser.parsePrice(raw);
      if (parsed.isEmpty()) {
         MessageHelper.send(player, "chat-input-invalid-price", "&cɪɴᴠᴀʟɪᴅ ᴘʀɪᴄᴇ. ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀ ᴘᴏꜱɪᴛɪᴠᴇ ɴᴜᴍʙᴇʀ.");
         String cancelWord = MessageHelper.cancelKeyword();
         guiManager.getChatInput()
            .requestInput(
               player,
               MessageHelper.get("chat-prompt-price", "&eᴇɴᴛᴇʀ ᴘʀɪᴄᴇ ᴘᴇʀ ɪᴛᴇᴍ (ᴏʀ &c{0}&e):", cancelWord),
               s -> handlePriceInput(guiManager, player, template, amount, s),
               () -> guiManager.openYourOrders(player, 0)
            );
      } else {
         double price = parsed.getAsDouble();
         guiManager.getOrderManager()
            .createOrder(
               player,
               template,
               amount,
               price,
               (success, errorMsg) -> {
                  if (success) {
                     MessageHelper.sendPrefixed(
                        player,
                        "order-created",
                        "&aᴏʀᴅᴇʀ ᴄʀᴇᴀᴛᴇᴅ!",
                        ItemUtils.describeOrderItem(template),
                        String.valueOf(amount),
                        NumberFormatter.formatPrice(price),
                        NumberFormatter.formatPrice(price * amount)
                     );
                  } else {
                     player.sendMessage(errorMsg != null ? errorMsg : MessageHelper.get("order-create-failed", "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴄʀᴇᴀᴛᴇ ᴏʀᴅᴇʀ. ᴘʟᴇᴀꜱᴇ ᴛʀʏ ᴀɢᴀɪɴ."));
                  }

                  guiManager.openYourOrders(player, 0);
               }
            );
      }
   }
}
