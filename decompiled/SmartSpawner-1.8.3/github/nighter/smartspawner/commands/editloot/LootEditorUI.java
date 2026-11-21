package github.nighter.smartspawner.commands.editloot;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LootEditorUI {
   static final int LOOT_SIZE = 27;
   static final int LOOT_START = 0;
   static final int LOOT_END = 26;
   static final int CAPTURE_SIZE = 27;
   static final int CAPTURE_SLOT = 13;
   static final int CAPTURE_CANCEL = 18;
   static final int CAPTURE_CONFIRM = 26;
   private final SmartSpawner plugin;
   private final LootEditorService service;

   public LootEditorUI(SmartSpawner plugin, LootEditorService service) {
      this.plugin = plugin;
      this.service = service;
   }

   private LanguageManager lang() {
      return this.plugin.getLanguageManager();
   }

   public void openLootList(Player player, LootEditorTarget target, String entryKey) {
      Map<String, String> titlePlaceholders = new HashMap<>();
      titlePlaceholders.put("entry", entryKey.substring(entryKey.lastIndexOf(46) + 1));
      Inventory inventory = Bukkit.createInventory(
         new LootListHolder(target, entryKey), 27, this.lang().commandGui().title(target.getTitleKey(), titlePlaceholders)
      );
      int slot = 0;

      for (LootEditorService.LootView loot : this.service.readLootList(target, entryKey)) {
         if (slot > 26) {
            break;
         }

         inventory.setItem(slot++, this.buildLootIcon(loot));
      }

      if (slot <= 26) {
         inventory.setItem(slot, this.simpleItem(Material.LIME_STAINED_GLASS_PANE, "editloot.add_loot", Map.of()));
      }

      player.openInventory(inventory);
   }

   private ItemStack buildLootIcon(LootEditorService.LootView loot) {
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("label", loot.key());
      placeholders.put("amount", loot.amountLabel());
      placeholders.put("chance", trim(loot.chance()));
      String durability = loot.durabilityLabel();
      placeholders.put("durability", durability == null ? "-" : durability);
      if (loot.isBroken()) {
         placeholders.put("item", loot.rawItem());
         return this.simpleItem(Material.BARRIER, "editloot.loot_broken", placeholders);
      } else {
         ItemStack icon = loot.preview().clone();
         ItemMeta meta = icon.getItemMeta();
         if (meta != null) {
            meta.setDisplayName(this.lang().commandGui().name("editloot.loot_entry.name", placeholders));
            List<Component> lore = this.lang()
               .commandGui()
               .loreWithItemName("editloot.loot_entry.lore", placeholders, "{item}", this.lang().getItemDisplayName(loot.preview()));
            if (!lore.isEmpty()) {
               meta.lore(lore);
            }

            icon.setItemMeta(meta);
         }

         return icon;
      }
   }

   public void openItemCapture(Player player, LootCaptureHolder holder) {
      String titleKey = holder.getPurpose() == LootCaptureHolder.Purpose.ADD_LOOT ? "editloot.capture_add_title" : "editloot.capture_replace_title";
      Inventory inventory = Bukkit.createInventory(holder, 27, this.lang().commandGui().title(titleKey, Map.of()));
      ItemStack filler = this.simpleItem(Material.GRAY_STAINED_GLASS_PANE, "editloot.capture_hint", Map.of());

      for (int i = 0; i < 27; i++) {
         inventory.setItem(i, filler.clone());
      }

      inventory.setItem(13, null);
      inventory.setItem(18, this.simpleItem(Material.RED_STAINED_GLASS_PANE, "editloot.cancel", Map.of()));
      inventory.setItem(26, this.simpleItem(Material.LIME_STAINED_GLASS_PANE, "editloot.confirm", Map.of()));
      player.openInventory(inventory);
   }

   private ItemStack simpleItem(Material material, String key, Map<String, String> placeholders) {
      ItemStack item = new ItemStack(material);
      this.applyText(item, key, placeholders);
      return item;
   }

   private void applyText(ItemStack item, String key, Map<String, String> placeholders) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.lang().commandGui().name(key + ".name", placeholders));
         List<String> lore = new ArrayList<>(this.lang().commandGui().loreList(key + ".lore", placeholders));
         if (!lore.isEmpty()) {
            meta.setLore(lore);
         }

         item.setItemMeta(meta);
      }
   }

   private static String trim(double value) {
      return value == Math.floor(value) ? String.valueOf((long)value) : String.valueOf(value);
   }
}
