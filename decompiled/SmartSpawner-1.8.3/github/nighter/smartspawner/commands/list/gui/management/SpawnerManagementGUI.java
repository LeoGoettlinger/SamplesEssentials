package github.nighter.smartspawner.commands.list.gui.management;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerManagementGUI {
   private static final int INVENTORY_SIZE = 27;
   private static final int TELEPORT_SLOT = 10;
   private static final int OPEN_SPAWNER_SLOT = 12;
   private static final int REMOVE_SLOT = 16;
   private static final int BACK_SLOT = 26;
   private final LanguageManager languageManager;
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;

   public SpawnerManagementGUI(SmartSpawner plugin) {
      this.languageManager = plugin.getLanguageManager();
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
   }

   public void openManagementMenu(Player player, String spawnerId, String worldName, int listPage) {
      SpawnerData spawner = this.spawnerManager.getSpawnerById(spawnerId);
      if (spawner == null) {
         this.messageService.sendMessage(player, "list.teleport_failed");
      } else {
         String title = this.languageManager.commandGui().title("gui_title_spawner_management");
         Inventory inv = Bukkit.createInventory(new SpawnerManagementHolder(spawnerId, worldName, listPage), 27, title);
         player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
         this.createActionItem(inv, 10, "spawner_management.teleport", Material.ENDER_PEARL);
         this.createActionItem(inv, 12, "spawner_management.open_spawner", Material.ENDER_EYE);
         this.createActionItem(inv, 16, "spawner_management.remove", Material.BARRIER);
         this.createActionItem(
            inv, 26, "general_navigation.back", Material.RED_STAINED_GLASS_PANE, this.previousMenuPlaceholder("spawner_list", "Spawner List")
         );
         player.openInventory(inv);
      }
   }

   private void createActionItem(Inventory inv, int slot, String langKey, Material material) {
      this.createActionItem(inv, slot, langKey, material, Map.of());
   }

   private void createActionItem(Inventory inv, int slot, String langKey, Material material, Map<String, String> placeholders) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.languageManager.commandGui().name(langKey + ".name", placeholders));
         List<String> lore = Arrays.asList(this.languageManager.commandGui().lore(langKey + ".lore", placeholders));
         meta.setLore(lore);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
         item.setItemMeta(meta);
      }

      if (item.getType() == Material.SPAWNER) {
         ItemTooltipUtil.hideTooltip(item);
      }

      inv.setItem(slot, item);
   }

   private Map<String, String> previousMenuPlaceholder(String menuKey, String defaultName) {
      String menuName = this.languageManager.commandGui().configString("general_navigation.menu_names." + menuKey, defaultName);
      return Map.of("previous_menu", menuName);
   }
}
