package github.nighter.smartspawner.commands.near;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.list.gui.management.SpawnerManagementGUI;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NearResultGUI implements Listener {
   private static final int SPAWNERS_PER_PAGE = 45;
   private static final Pattern ID_PATTERN = Pattern.compile("#([A-Za-z0-9]+)");
   private static final Set<Material> SPAWNER_MATERIALS = EnumSet.of(
      Material.PLAYER_HEAD,
      Material.SPAWNER,
      Material.ZOMBIE_HEAD,
      Material.SKELETON_SKULL,
      Material.WITHER_SKELETON_SKULL,
      Material.CREEPER_HEAD,
      Material.PIGLIN_HEAD
   );
   private final SmartSpawner plugin;
   private final LanguageManager languageManager;
   private final SpawnerHighlightManager highlightManager;
   private final SpawnerManagementGUI spawnerManagementGUI;

   public NearResultGUI(SmartSpawner plugin, SpawnerHighlightManager highlightManager) {
      this.plugin = plugin;
      this.languageManager = plugin.getLanguageManager();
      this.highlightManager = highlightManager;
      this.spawnerManagementGUI = new SpawnerManagementGUI(plugin);
   }

   public void openNearResultGUI(Player player, int page) {
      List<SpawnerData> spawners = this.highlightManager.getSessionSpawners(player.getUniqueId());
      if (spawners.isEmpty()) {
         this.plugin.getMessageService().sendMessage(player, "near.no_active_scan");
      } else {
         this.openPage(player, spawners, page);
      }
   }

   private void openPage(Player player, List<SpawnerData> spawners, int page) {
      int total = spawners.size();
      int totalPages = Math.max(1, (int)Math.ceil(total / 45.0));
      page = Math.max(1, Math.min(page, totalPages));
      Map<String, String> titlePlaceholders = new HashMap<>();
      titlePlaceholders.put("world", "Near (" + total + ")");
      titlePlaceholders.put("current", String.valueOf(page));
      titlePlaceholders.put("total", String.valueOf(totalPages));
      String title = this.languageManager.commandGui().title("gui_title_spawner_list", titlePlaceholders);
      Inventory inv = Bukkit.createInventory(new NearResultHolder(player.getUniqueId(), page, totalPages), 54, title);
      int start = (page - 1) * 45;
      int end = Math.min(start + 45, total);

      for (int i = start; i < end; i++) {
         inv.addItem(new ItemStack[]{this.createSpawnerInfoItem(spawners.get(i))});
      }

      if (page > 1) {
         inv.setItem(
            45, this.createNavigationButton(Material.SPECTRAL_ARROW, "general_navigation.previous_page", Map.of("target_page", String.valueOf(page - 1)))
         );
      }

      inv.setItem(49, this.createNavigationButton(Material.RED_STAINED_GLASS_PANE, "general_navigation.close"));
      if (page < totalPages) {
         inv.setItem(53, this.createNavigationButton(Material.SPECTRAL_ARROW, "general_navigation.next_page", Map.of("target_page", String.valueOf(page + 1))));
      }

      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
      player.openInventory(inv);
   }

   @EventHandler
   public void onNearResultClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof NearResultHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
               int slot = event.getSlot();
               int currentPage = holder.getCurrentPage();
               int totalPages = holder.getTotalPages();
               if (slot == 49) {
                  player.closeInventory();
                  player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
               } else if (slot == 45 && currentPage > 1) {
                  List<SpawnerData> spawners = this.highlightManager.getSessionSpawners(holder.getPlayerUUID());
                  this.openPage(player, spawners, currentPage - 1);
               } else if (slot == 53 && currentPage < totalPages) {
                  List<SpawnerData> spawners = this.highlightManager.getSessionSpawners(holder.getPlayerUUID());
                  this.openPage(player, spawners, currentPage + 1);
               } else {
                  if (slot < 45 && this.isSpawnerItem(event.getCurrentItem())) {
                     this.handleSpawnerClick(player, event.getCurrentItem());
                  }
               }
            }
         }
      }
   }

   private void handleSpawnerClick(Player player, ItemStack item) {
      if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
         String displayName = item.getItemMeta().getDisplayName();
         Matcher matcher = ID_PATTERN.matcher(displayName);
         if (matcher.find()) {
            String spawnerId = matcher.group(1);
            SpawnerData spawner = this.plugin.getSpawnerManager().getSpawnerById(spawnerId);
            if (spawner == null) {
               this.plugin.getMessageService().sendMessage(player, "list.teleport_failed");
            } else {
               String worldName = spawner.getSpawnerLocation().getWorld().getName();
               this.spawnerManagementGUI.openManagementMenu(player, spawnerId, worldName, 1);
            }
         }
      }
   }

   private boolean isSpawnerItem(ItemStack item) {
      return item != null && SPAWNER_MATERIALS.contains(item.getType()) && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
   }

   private ItemStack createSpawnerInfoItem(SpawnerData spawner) {
      EntityType entityType = spawner.getEntityType();
      Location loc = spawner.getSpawnerLocation();
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("id", spawner.getSpawnerId());
      placeholders.put("entity", spawner.getDisplayName());
      placeholders.put("size", String.valueOf(spawner.getStackSize()));
      if (spawner.getSpawnerStop().get()) {
         placeholders.put("status", this.languageManager.commandGui().name("spawner_item_list.status.inactive"));
      } else {
         placeholders.put("status", this.languageManager.commandGui().name("spawner_item_list.status.active"));
      }

      placeholders.put("x", String.valueOf(loc.getBlockX()));
      placeholders.put("y", String.valueOf(loc.getBlockY()));
      placeholders.put("z", String.valueOf(loc.getBlockZ()));
      String lastPlayer = spawner.getLastInteractedPlayer();
      placeholders.put("last_player", lastPlayer != null ? lastPlayer : this.languageManager.commandGui().name("spawner_item_list.last_player_none"));
      ItemStack spawnerItem;
      if (entityType == null) {
         spawnerItem = new ItemStack(Material.SPAWNER);
         spawnerItem.editMeta(meta -> {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            meta.setLore(Arrays.asList(this.languageManager.commandGui().lore("spawner_item_list.lore", placeholders)));
         });
      } else {
         spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, meta -> {
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            meta.setLore(Arrays.asList(this.languageManager.commandGui().lore("spawner_item_list.lore", placeholders)));
         });
      }

      ItemTooltipUtil.hideTooltip(spawnerItem);
      return spawnerItem;
   }

   private ItemStack createNavigationButton(Material material, String namePath) {
      return this.createNavigationButton(material, namePath, Collections.emptyMap());
   }

   private ItemStack createNavigationButton(Material material, String namePath, Map<String, String> placeholders) {
      ItemStack button = new ItemStack(material);
      ItemMeta meta = button.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.languageManager.commandGui().name(namePath + ".name", placeholders));
         List<String> lore = this.languageManager.commandGui().loreList(namePath + ".lore", placeholders);
         if (!lore.isEmpty()) {
            meta.setLore(lore);
         }

         button.setItemMeta(meta);
      }

      return button;
   }
}
