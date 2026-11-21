package github.nighter.smartspawner.commands.list;

import com.mojang.brigadier.context.CommandContext;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.commands.list.gui.CrossServerSpawnerData;
import github.nighter.smartspawner.commands.list.gui.list.SpawnerListHolder;
import github.nighter.smartspawner.commands.list.gui.list.UserPreferenceCache;
import github.nighter.smartspawner.commands.list.gui.list.enums.FilterOption;
import github.nighter.smartspawner.commands.list.gui.list.enums.SortOption;
import github.nighter.smartspawner.commands.list.gui.management.SpawnerManagementGUI;
import github.nighter.smartspawner.commands.list.gui.serverselection.ServerSelectionHolder;
import github.nighter.smartspawner.commands.list.gui.worldselection.WorldSelectionHolder;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.config.SpawnerMobHeadTexture;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.data.database.SpawnerDatabaseHandler;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.ItemTooltipUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ListSubCommand extends BaseSubCommand {
   private final SpawnerManager spawnerManager;
   private final LanguageManager languageManager;
   private final MessageService messageService;
   private final UserPreferenceCache userPreferenceCache;
   private final SpawnerManagementGUI spawnerManagementGUI;
   private final NamespacedKey worldNameKey;
   private static final int SPAWNERS_PER_PAGE = 45;
   private static final String EMPTY_REMOTE_LORE_MARKER = "__SMARTSPAWNER_EMPTY_REMOTE_LORE__";

   public ListSubCommand(SmartSpawner plugin) {
      super(plugin);
      this.spawnerManager = plugin.getSpawnerManager();
      this.languageManager = plugin.getLanguageManager();
      this.messageService = plugin.getMessageService();
      this.userPreferenceCache = plugin.getUserPreferenceCache();
      this.spawnerManagementGUI = new SpawnerManagementGUI(plugin);
      this.worldNameKey = new NamespacedKey(plugin, "world_name");
   }

   @Override
   public String getName() {
      return "list";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.list";
   }

   @Override
   public String getDescription() {
      return "Open the spawner list GUI";
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      if (!this.isPlayer(((CommandSourceStack)context.getSource()).getSender())) {
         return 0;
      } else {
         Player player = this.getPlayer(((CommandSourceStack)context.getSource()).getSender());
         if (this.isCrossServerEnabled()) {
            this.openServerSelectionGUI(player);
         } else {
            this.openWorldSelectionGUI(player);
         }

         return 1;
      }
   }

   public boolean isCrossServerEnabled() {
      StorageMode mode = StorageMode.fromConfig(this.plugin.getConfig().getString("database.type"));
      return mode != StorageMode.MYSQL ? false : this.plugin.getConfig().getBoolean("database.sync-across-servers", false);
   }

   public String getCurrentServerName() {
      return this.plugin.getConfig().getString("database.server-name", "server1");
   }

   public void openServerSelectionGUI(Player player) {
      if (!player.hasPermission("smartspawner.command.list")) {
         this.messageService.sendMessage(player, "no_permission");
      } else {
         player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
         SpawnerDatabaseHandler dbHandler = this.getDbHandler();
         if (this.isCrossServerEnabled() && dbHandler != null) {
            dbHandler.getDistinctServerNamesAsync(servers -> this.openServerSelectionGUI(player, servers));
         } else {
            this.openWorldSelectionGUI(player);
         }
      }
   }

   private void openServerSelectionGUI(Player player, Collection<String> servers) {
      Set<String> availableServers = new LinkedHashSet<>();
      availableServers.add(this.getCurrentServerName());
      availableServers.addAll(servers);
      int contentRows = (int)Math.ceil(availableServers.size() / 7.0);
      int size = Math.max(27, (contentRows + 1) * 9);
      size = Math.min(54, size);
      String title = this.languageManager.commandGui().title("gui_title_server_selection");
      Inventory inv = Bukkit.createInventory(new ServerSelectionHolder(), size, title);
      String currentServer = this.getCurrentServerName();
      int slot = 10;

      for (String serverName : availableServers) {
         if (slot >= size) {
            break;
         }

         while (slot < size && (slot % 9 == 0 || slot % 9 == 8)) {
            slot++;
         }

         if (slot >= size) {
            break;
         }

         Material material = serverName.equals(currentServer) ? Material.EMERALD_BLOCK : Material.IRON_BLOCK;
         ItemStack serverItem = this.createServerButton(serverName, material, serverName.equals(currentServer));
         inv.setItem(slot, serverItem);
         slot++;
      }

      player.openInventory(inv);
   }

   private ItemStack createServerButton(String serverName, Material material, boolean isCurrentServer) {
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         Map<String, String> placeholders = Map.of("server", serverName);
         String key = isCurrentServer ? "server_selection.current_server" : "server_selection.remote_server";
         meta.setDisplayName(this.languageManager.commandGui().name(key + ".name", placeholders));
         meta.setLore(this.languageManager.commandGui().loreList(key + ".lore", placeholders));
         item.setItemMeta(meta);
      }

      return item;
   }

   public void openWorldSelectionGUIForServer(Player player, String targetServer) {
      if (!player.hasPermission("smartspawner.command.list")) {
         this.messageService.sendMessage(player, "no_permission");
      } else {
         String currentServer = this.getCurrentServerName();
         if (targetServer.equals(currentServer)) {
            this.openWorldSelectionGUI(player);
         } else {
            SpawnerDatabaseHandler dbHandler = this.getDbHandler();
            if (dbHandler == null) {
               this.messageService.sendMessage(player, "action_failed");
            } else {
               player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
               dbHandler.getWorldsForServerAsync(
                  targetServer,
                  worldCounts -> {
                     if (worldCounts.isEmpty()) {
                        this.messageService.sendMessage(player, "list.no_spawners_found");
                     } else {
                        int size = Math.max(27, (int)Math.ceil((worldCounts.size() + 2) / 7.0) * 9);
                        size = Math.min(54, size);
                        Map<String, String> titlePlaceholders = new HashMap<>();
                        titlePlaceholders.put("server", targetServer);
                        String title = this.languageManager.commandGui().title("gui_title_world_selection_server", titlePlaceholders);
                        Inventory inv = Bukkit.createInventory(new WorldSelectionHolder(targetServer), size, title);
                        int slot = 10;

                        for (Entry<String, SpawnerDatabaseHandler.WorldSpawnerStats> entry : worldCounts.entrySet()) {
                           if (slot >= size - 9) {
                              break;
                           }

                           if (slot % 9 != 0 && slot % 9 != 8) {
                              String worldName = entry.getKey();
                              SpawnerDatabaseHandler.WorldSpawnerStats stats = entry.getValue();
                              Environment environment = this.getEnvironmentForWorldName(worldName);
                              ItemStack worldItem = this.createWorldButton(worldName, environment, stats.total(), stats.totalStacked(), targetServer);
                              inv.setItem(slot, worldItem);
                              slot++;
                           } else {
                              slot++;
                           }
                        }

                        ItemStack backButton = this.createNavigationButton(
                           Material.RED_STAINED_GLASS_PANE, "general_navigation.back", this.previousMenuPlaceholder("server_selection", "Server Selection")
                        );
                        inv.setItem(26, backButton);
                        player.openInventory(inv);
                     }
                  }
               );
            }
         }
      }
   }

   private Environment getEnvironmentForWorldName(String worldName) {
      String normalizedName = worldName.toLowerCase(Locale.ROOT);
      if (normalizedName.equals("world_nether") || normalizedName.endsWith("_nether")) {
         return Environment.NETHER;
      } else {
         return !normalizedName.equals("world_the_end") && !normalizedName.endsWith("_the_end") ? Environment.NORMAL : Environment.THE_END;
      }
   }

   private SpawnerDatabaseHandler getDbHandler() {
      return this.plugin.getSpawnerStorage() instanceof SpawnerDatabaseHandler dbHandler ? dbHandler : null;
   }

   public void openWorldSelectionGUI(Player player) {
      this.openWorldSelectionGUI(player, null);
   }

   private void openWorldSelectionGUI(Player player, String targetServer) {
      if (!player.hasPermission("smartspawner.command.list")) {
         this.messageService.sendMessage(player, "no_permission");
      } else {
         player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
         List<World> worlds = Bukkit.getWorlds()
            .stream()
            .filter(worldx -> this.spawnerManager.countSpawnersInWorld(worldx.getName()) > 0)
            .collect(Collectors.toList());
         List<World> customWorlds = worlds.stream().filter(worldx -> !this.isDefaultWorld(worldx.getName())).collect(Collectors.toList());
         boolean hasCustomWorlds = !customWorlds.isEmpty();
         int size = hasCustomWorlds ? Math.max(27, (int)Math.ceil((worlds.size() + 2) / 7.0) * 9) : 27;
         Inventory inv = Bukkit.createInventory(
            new WorldSelectionHolder(targetServer), size, this.languageManager.commandGui().title("gui_title_world_selection")
         );
         if (!hasCustomWorlds) {
            ItemStack overworldButton = this.createWorldButtonIfWorldExists("world", targetServer);
            ItemStack netherButton = this.createWorldButtonIfWorldExists("world_nether", targetServer);
            ItemStack endButton = this.createWorldButtonIfWorldExists("world_the_end", targetServer);
            if (overworldButton != null) {
               inv.setItem(11, overworldButton);
            }

            if (netherButton != null) {
               inv.setItem(13, netherButton);
            }

            if (endButton != null) {
               inv.setItem(15, endButton);
            }
         } else {
            int slot = 10;
            int row = 1;
            if (this.addWorldButtonIfExists(inv, "world", slot, targetServer)) {
               slot++;
            }

            if (this.addWorldButtonIfExists(inv, "world_nether", slot, targetServer)) {
               slot++;
            }

            if (this.addWorldButtonIfExists(inv, "world_the_end", slot, targetServer)) {
               slot++;
            }

            for (World world : customWorlds) {
               if (slot % 9 == 8) {
                  slot = 9 * ++row + 1;
               }

               if (slot >= size) {
                  break;
               }

               this.addWorldButton(inv, world, slot++, targetServer);
            }
         }

         if (this.isCrossServerEnabled() || targetServer != null) {
            ItemStack backButton = this.createNavigationButton(
               Material.RED_STAINED_GLASS_PANE, "general_navigation.back", this.previousMenuPlaceholder("server_selection", "Server Selection")
            );
            inv.setItem(26, backButton);
         }

         player.openInventory(inv);
      }
   }

   private boolean isDefaultWorld(String worldName) {
      return worldName.equals("world") || worldName.equals("world_nether") || worldName.equals("world_the_end");
   }

   private ItemStack createWorldButtonIfWorldExists(String worldName, String serverName) {
      World world = Bukkit.getWorld(worldName);
      return world != null && this.spawnerManager.countSpawnersInWorld(worldName) > 0 ? this.createWorldButton(world, serverName) : null;
   }

   private boolean addWorldButtonIfExists(Inventory inv, String worldName, int slot, String serverName) {
      World world = Bukkit.getWorld(worldName);
      if (world != null && this.spawnerManager.countSpawnersInWorld(worldName) > 0) {
         this.addWorldButton(inv, world, slot, serverName);
         return true;
      } else {
         return false;
      }
   }

   private void addWorldButton(Inventory inv, World world, int slot, String serverName) {
      inv.setItem(slot, this.createWorldButton(world, serverName));
   }

   private Material getMaterialForWorldType(Environment environment) {
      return switch (environment) {
         case NORMAL -> Material.GRASS_BLOCK;
         case NETHER -> Material.NETHERRACK;
         case THE_END -> Material.END_STONE;
         case CUSTOM -> Material.ENDER_PEARL;
         default -> throw new MatchException(null, null);
      };
   }

   private String formatWorldName(String worldName) {
      return Arrays.stream(worldName.replace('_', ' ').split(" "))
         .filter(word -> !word.isEmpty())
         .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
         .collect(Collectors.joining(" "));
   }

   private String getWorldDisplayName(String worldName) {
      String configKey;
      String defaultValue;
      switch (worldName) {
         case "world":
            configKey = "world_display_names.overworld";
            defaultValue = "Overworld";
            break;
         case "world_nether":
            configKey = "world_display_names.nether";
            defaultValue = "Nether";
            break;
         case "world_the_end":
            configKey = "world_display_names.the_end";
            defaultValue = "The End";
            break;
         default:
            String customFormat = this.languageManager.commandGui().configString("world_display_names.custom", "World {world_name}");
            return this.languageManager.applyOnlyPlaceholders(customFormat, Map.of("world_name", this.formatWorldName(worldName)));
      }

      return this.languageManager.commandGui().configString(configKey, defaultValue);
   }

   private ItemStack createWorldButton(World world, String serverName) {
      String worldName = world.getName();
      int physicalSpawners = this.spawnerManager.countSpawnersInWorld(worldName);
      int totalWithStacks = this.spawnerManager.countTotalSpawnersWithStacks(worldName);
      return this.createWorldButton(worldName, world.getEnvironment(), physicalSpawners, totalWithStacks, serverName);
   }

   private ItemStack createWorldButton(String worldName, Environment environment, int physicalSpawners, int totalWithStacks, String serverName) {
      String path = "world_buttons." + environment.name();
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("world_name", this.getWorldDisplayName(worldName));
      placeholders.put("total", this.languageManager.formatNumber(physicalSpawners));
      placeholders.put("total_stacked", this.languageManager.formatNumber(totalWithStacks));
      if (serverName != null) {
         placeholders.put("server", serverName);
         placeholders.put("remote_world_lore", String.join("\n", this.languageManager.commandGui().loreList("remote_world_lore", placeholders)));
      } else {
         placeholders.put("remote_world_lore", "__SMARTSPAWNER_EMPTY_REMOTE_LORE__");
      }

      ItemStack button = new ItemStack(this.getMaterialForWorldType(environment));
      ItemMeta meta = button.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(this.languageManager.commandGui().name(path + ".name", placeholders));
         List<String> lore = new ArrayList<>(this.languageManager.commandGui().loreWithMultilinePlaceholders(path + ".lore", placeholders));
         lore.removeIf("__SMARTSPAWNER_EMPTY_REMOTE_LORE__"::equals);
         meta.setLore(lore);
         meta.getPersistentDataContainer().set(this.worldNameKey, PersistentDataType.STRING, worldName);
         button.setItemMeta(meta);
      }

      return button;
   }

   public void openSpawnerListGUI(Player player, String worldName, int page) {
      UserPreferenceCache.UserPreference preference = this.userPreferenceCache.getPreference(player.getUniqueId(), worldName);
      if (preference != null) {
         this.openSpawnerListGUI(player, worldName, page, preference.getFilterOption(), preference.getSortOption());
      } else {
         this.openSpawnerListGUI(player, worldName, page, FilterOption.ALL, SortOption.DEFAULT);
      }
   }

   public void saveUserPreference(Player player, String worldName, FilterOption filter, SortOption sort) {
      this.userPreferenceCache.savePreference(player.getUniqueId(), worldName, filter, sort);
   }

   public void openSpawnerListGUI(Player player, String worldName, int page, FilterOption filter, SortOption sortType) {
      this.openSpawnerListGUI(player, worldName, page, filter, sortType, null);
   }

   private void openSpawnerListGUI(Player player, String worldName, int page, FilterOption filter, SortOption sortType, String targetServer) {
      if (!player.hasPermission("smartspawner.command.list")) {
         this.messageService.sendMessage(player, "no_permission");
      } else {
         player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
         List<SpawnerData> worldSpawners = this.spawnerManager
            .getAllSpawners()
            .stream()
            .filter(spawnerx -> spawnerx.getSpawnerLocation().getWorld().getName().equals(worldName))
            .collect(Collectors.toList());
         if (filter == FilterOption.ACTIVE) {
            worldSpawners = worldSpawners.stream().filter(spawnerx -> !spawnerx.getSpawnerStop().get()).collect(Collectors.toList());
         } else if (filter == FilterOption.INACTIVE) {
            worldSpawners = worldSpawners.stream().filter(spawnerx -> spawnerx.getSpawnerStop().get()).collect(Collectors.toList());
         }

         switch (sortType) {
            case STACK_SIZE_ASC:
               worldSpawners.sort(Comparator.comparingInt(SpawnerData::getStackSize));
               break;
            case STACK_SIZE_DESC:
               worldSpawners.sort(Comparator.comparingInt(SpawnerData::getStackSize).reversed());
         }

         int totalPages = (int)Math.ceil(worldSpawners.size() / 45.0);
         page = Math.max(1, Math.min(page, totalPages));
         Map<String, String> titlePlaceholders = new HashMap<>();
         titlePlaceholders.put("world", this.getWorldDisplayName(worldName));
         titlePlaceholders.put("current", String.valueOf(page));
         titlePlaceholders.put("total", String.valueOf(totalPages));
         String titleKey = targetServer == null ? "gui_title_spawner_list" : "gui_title_spawner_list_remote";
         if (targetServer != null) {
            titlePlaceholders.put("server", targetServer);
         }

         String title = this.languageManager.commandGui().title(titleKey, titlePlaceholders);
         Inventory inv = Bukkit.createInventory(new SpawnerListHolder(page, totalPages, worldName, filter, sortType, targetServer), 54, title);
         int startIndex = (page - 1) * 45;
         int endIndex = Math.min(startIndex + 45, worldSpawners.size());

         for (int i = startIndex; i < endIndex; i++) {
            SpawnerData spawner = worldSpawners.get(i);
            inv.addItem(new ItemStack[]{this.createSpawnerInfoItem(spawner, targetServer != null)});
         }

         this.addControlButtons(inv, filter, sortType);
         if (page > 1) {
            inv.setItem(
               45, this.createNavigationButton(Material.SPECTRAL_ARROW, "general_navigation.previous_page", Map.of("target_page", String.valueOf(page - 1)))
            );
         }

         inv.setItem(
            49,
            this.createNavigationButton(
               Material.RED_STAINED_GLASS_PANE, "general_navigation.back", this.previousMenuPlaceholder("world_selection", "World Selection")
            )
         );
         if (page < totalPages) {
            inv.setItem(
               53, this.createNavigationButton(Material.SPECTRAL_ARROW, "general_navigation.next_page", Map.of("target_page", String.valueOf(page + 1)))
            );
         }

         player.openInventory(inv);
      }
   }

   private void addControlButtons(Inventory inv, FilterOption currentFilter, SortOption currentSort) {
      ItemStack filterButton = this.createEnhancedControlButton(Material.CAULDRON, "filter", currentFilter);
      ItemStack sortButton = this.createEnhancedControlButton(Material.HOPPER, "sort", currentSort);
      inv.setItem(48, filterButton);
      inv.setItem(50, sortButton);
   }

   private ItemStack createEnhancedControlButton(Material material, String controlType, Enum<?> currentOption) {
      ItemStack button = new ItemStack(material);
      ItemMeta meta = button.getItemMeta();
      if (meta == null) {
         return button;
      } else {
         Map<String, String> placeholders = new HashMap<>();
         String selectedFormat = this.languageManager.commandGui().name("general.selected_option");
         String unselectedFormat = this.languageManager.commandGui().name("general.unselected_option");
         StringBuilder availableOptions = new StringBuilder();
         boolean first = true;
         if (controlType.equals("filter")) {
            FilterOption currentFilter = (FilterOption)currentOption;

            for (FilterOption option : FilterOption.values()) {
               if (!first) {
                  availableOptions.append("\n");
               }

               String optionName = this.languageManager.commandGui().name("filter.option_name." + option.getName());
               String format = option == currentFilter ? selectedFormat : unselectedFormat;
               String formattedOption = format.replace("{option_name}", optionName);
               availableOptions.append(formattedOption);
               first = false;
            }

            meta.setDisplayName(this.languageManager.commandGui().name("filter.button.name"));
         } else if (controlType.equals("sort")) {
            SortOption currentSort = (SortOption)currentOption;

            for (SortOption option : SortOption.values()) {
               if (!first) {
                  availableOptions.append("\n");
               }

               String optionName = this.languageManager.commandGui().name("sort.option_name." + option.getName());
               String format = option == currentSort ? selectedFormat : unselectedFormat;
               String formattedOption = format.replace("{option_name}", optionName);
               availableOptions.append(formattedOption);
               first = false;
            }

            meta.setDisplayName(this.languageManager.commandGui().name("sort.button.name"));
         }

         placeholders.put("available_options", availableOptions.toString());
         String lorePath = controlType + ".button.lore";
         List<String> lore = this.languageManager.commandGui().loreWithMultilinePlaceholders(lorePath, placeholders);
         meta.setLore(lore);
         button.setItemMeta(meta);
         return button;
      }
   }

   private ItemStack createNavigationButton(Material material, String namePath) {
      return this.createNavigationButton(material, namePath, Collections.emptyMap());
   }

   private ItemStack createNavigationButton(Material material, String namePath, Map<String, String> placeholders) {
      ItemStack button = new ItemStack(material);
      ItemMeta meta = button.getItemMeta();
      if (meta != null) {
         String displayName = this.languageManager.commandGui().name(namePath + ".name", placeholders);
         if (displayName.startsWith("Missing item name: ")) {
            displayName = this.languageManager.commandGui().name(namePath, placeholders);
         }

         meta.setDisplayName(displayName);
         List<String> lore = this.languageManager.commandGui().loreList(namePath + ".lore", placeholders);
         if (!lore.isEmpty()) {
            meta.setLore(lore);
         }

         button.setItemMeta(meta);
      }

      return button;
   }

   private Map<String, String> previousMenuPlaceholder(String menuKey, String defaultName) {
      String menuName = this.languageManager.commandGui().configString("general_navigation.menu_names." + menuKey, defaultName);
      return Map.of("previous_menu", menuName);
   }

   private ItemStack createSpawnerInfoItem(SpawnerData spawner, boolean remote) {
      EntityType entityType = spawner.getEntityType();
      Location loc = spawner.getSpawnerLocation();
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("id", String.valueOf(spawner.getSpawnerId()));
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
      String loreKey = remote ? "spawner_item_list.remote_lore" : "spawner_item_list.lore";
      ItemStack spawnerItem;
      if (entityType == null) {
         spawnerItem = new ItemStack(Material.SPAWNER);
         spawnerItem.editMeta(meta -> {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            List<String> lore = Arrays.asList(this.languageManager.commandGui().lore(loreKey, placeholders));
            meta.setLore(lore);
         });
      } else {
         spawnerItem = SpawnerMobHeadTexture.getSpawnerHead(spawner, meta -> {
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            List<String> lore = Arrays.asList(this.languageManager.commandGui().lore(loreKey, placeholders));
            meta.setLore(lore);
         });
      }

      ItemTooltipUtil.hideTooltip(spawnerItem);
      return spawnerItem;
   }

   public void openSpawnerManagementGUI(Player player, String spawnerId, String worldName, int listPage) {
      this.spawnerManagementGUI.openManagementMenu(player, spawnerId, worldName, listPage);
   }

   public void openSpawnerListGUIForServer(Player player, String targetServer, String worldName, int page) {
      this.openSpawnerListGUIForServer(player, targetServer, worldName, page, FilterOption.ALL, SortOption.DEFAULT);
   }

   public void openSpawnerListGUIForServer(Player player, String targetServer, String worldName, int page, FilterOption filter, SortOption sort) {
      if (!player.hasPermission("smartspawner.command.list")) {
         this.messageService.sendMessage(player, "no_permission");
      } else {
         String currentServer = this.getCurrentServerName();
         if (targetServer.equals(currentServer)) {
            this.openSpawnerListGUI(player, worldName, page, filter, sort, targetServer);
         } else {
            SpawnerDatabaseHandler dbHandler = this.getDbHandler();
            if (dbHandler == null) {
               this.messageService.sendMessage(player, "action_failed");
            } else {
               player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
               dbHandler.getCrossServerSpawnersAsync(
                  targetServer,
                  worldName,
                  filter.name(),
                  sort.name(),
                  spawners -> {
                     if (spawners.isEmpty()) {
                        this.messageService.sendMessage(player, "list.no_spawners_found");
                     } else {
                        int totalPages = (int)Math.ceil(spawners.size() / 45.0);
                        int currentPage = Math.max(1, Math.min(page, totalPages));
                        String worldTitle = this.getWorldDisplayName(worldName);
                        Map<String, String> titlePlaceholders = new HashMap<>();
                        titlePlaceholders.put("world", worldTitle);
                        titlePlaceholders.put("current", String.valueOf(currentPage));
                        titlePlaceholders.put("total", String.valueOf(totalPages));
                        titlePlaceholders.put("server", targetServer);
                        String title = this.languageManager.commandGui().title("gui_title_spawner_list_remote", titlePlaceholders);
                        Inventory inv = Bukkit.createInventory(new SpawnerListHolder(currentPage, totalPages, worldName, filter, sort, targetServer), 54, title);
                        int startIndex = (currentPage - 1) * 45;
                        int endIndex = Math.min(startIndex + 45, spawners.size());

                        for (int i = startIndex; i < endIndex; i++) {
                           CrossServerSpawnerData spawner = spawners.get(i);
                           inv.addItem(new ItemStack[]{this.createCrossServerSpawnerItem(spawner)});
                        }

                        if (currentPage > 1) {
                           inv.setItem(
                              45,
                              this.createNavigationButton(
                                 Material.SPECTRAL_ARROW, "general_navigation.previous_page", Map.of("target_page", String.valueOf(currentPage - 1))
                              )
                           );
                        }

                        this.addControlButtons(inv, filter, sort);
                        inv.setItem(
                           49,
                           this.createNavigationButton(
                              Material.RED_STAINED_GLASS_PANE, "general_navigation.back", this.previousMenuPlaceholder("world_selection", "World Selection")
                           )
                        );
                        if (currentPage < totalPages) {
                           inv.setItem(
                              53,
                              this.createNavigationButton(
                                 Material.SPECTRAL_ARROW, "general_navigation.next_page", Map.of("target_page", String.valueOf(currentPage + 1))
                              )
                           );
                        }

                        player.openInventory(inv);
                     }
                  }
               );
            }
         }
      }
   }

   private ItemStack createCrossServerSpawnerItem(CrossServerSpawnerData spawner) {
      EntityType entityType = spawner.getEntityType();
      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("id", spawner.getSpawnerId());
      placeholders.put("entity", this.languageManager.getFormattedMobName(entityType));
      placeholders.put("size", String.valueOf(spawner.getStackSize()));
      if (!spawner.isActive()) {
         placeholders.put("status", this.languageManager.commandGui().name("spawner_item_list.status.inactive"));
      } else {
         placeholders.put("status", this.languageManager.commandGui().name("spawner_item_list.status.active"));
      }

      placeholders.put("x", String.valueOf(spawner.getLocX()));
      placeholders.put("y", String.valueOf(spawner.getLocY()));
      placeholders.put("z", String.valueOf(spawner.getLocZ()));
      String lastPlayer = spawner.getLastInteractedPlayer();
      placeholders.put("last_player", lastPlayer != null ? lastPlayer : this.languageManager.commandGui().name("spawner_item_list.last_player_none"));
      ItemStack spawnerItem;
      if (entityType == null) {
         spawnerItem = new ItemStack(Material.SPAWNER);
         spawnerItem.editMeta(meta -> {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            meta.setLore(Arrays.asList(this.languageManager.commandGui().lore("spawner_item_list.remote_lore", placeholders)));
         });
      } else {
         spawnerItem = SpawnerMobHeadTexture.getCustomHead(entityType, meta -> {
            meta.setDisplayName(this.languageManager.commandGui().name("spawner_item_list.name", placeholders));
            meta.setLore(Arrays.asList(this.languageManager.commandGui().lore("spawner_item_list.remote_lore", placeholders)));
         });
      }

      ItemTooltipUtil.hideTooltip(spawnerItem);
      return spawnerItem;
   }

   public FilterOption getUserFilter(Player player, String worldName) {
      return this.userPreferenceCache.getUserFilter(player, worldName);
   }

   public SortOption getUserSort(Player player, String worldName) {
      return this.userPreferenceCache.getUserSort(player, worldName);
   }
}
