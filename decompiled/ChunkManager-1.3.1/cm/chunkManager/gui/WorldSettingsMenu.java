package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.WorldSettingsManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WorldSettingsMenu extends Menu {
   private int page = 0;
   private static final int WORLDS_PER_PAGE = 21;

   public WorldSettingsMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.AQUA + ChatColor.BOLD + "Worlds");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      List<World> worlds = Bukkit.getWorlds();
      int totalPages = (int)Math.ceil(worlds.size() / 21.0);
      ItemStack headerItem = new ItemStack(Material.GLOBE_BANNER_PATTERN);
      ItemMeta headerMeta = headerItem.getItemMeta();
      if (headerMeta != null) {
         headerMeta.setDisplayName("" + ChatColor.AQUA + ChatColor.BOLD + "World Settings");
         List<String> lore = new ArrayList<>();
         lore.add("");
         lore.add(ChatColor.GRAY + "Configure each world individually");
         if (wsm.isMultiverseEnabled()) {
            lore.add("");
            lore.add(ChatColor.GREEN + "✓ Multiverse detected");
         }

         headerMeta.setLore(lore);
         headerItem.setItemMeta(headerMeta);
      }

      this.setItem(4, headerItem, null);
      int startIndex = this.page * 21;
      int slot = 10;

      for (int i = startIndex; i < Math.min(startIndex + 21, worlds.size()); i++) {
         World world = worlds.get(i);
         WorldSettingsManager.WorldSettings settings = wsm.getSettings(world);
         String displayName = wsm.getWorldDisplayName(world);
         if (slot == 17 || slot == 26 || slot == 35) {
            slot++;
         }

         if (slot > 43) {
            break;
         }

         this.setItem(
            slot,
            this.buildWorldItem(world, displayName, settings),
            () -> this.guiManager.openMenu(this.player, new WorldEditMenu(this.plugin, this.player, this.guiManager, world))
         );
         slot++;
      }

      if (this.page > 0) {
         this.setItem(48, GuiUtils.item(Material.ARROW, ChatColor.YELLOW + "← Previous"), () -> {
            this.page--;
            GuiUtils.click(this.player);
            this.refresh();
         });
      }

      if (this.page < totalPages - 1) {
         this.setItem(50, GuiUtils.item(Material.ARROW, ChatColor.YELLOW + "Next →"), () -> {
            this.page++;
            GuiUtils.click(this.player);
            this.refresh();
         });
      }

      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new SettingsMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }

   private ItemStack buildWorldItem(World world, String displayName, WorldSettingsManager.WorldSettings settings) {
      Material mat = switch (world.getEnvironment()) {
         case NETHER -> Material.NETHERRACK;
         case THE_END -> Material.END_STONE;
         default -> Material.GRASS_BLOCK;
      };
      String status = settings.enabled ? ChatColor.GREEN + "●" : ChatColor.RED + "●";
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.GRAY + "Radius: " + ChatColor.WHITE + settings.chunkRadius);
      lore.add(ChatColor.GRAY + "Max/Tick: " + ChatColor.WHITE + settings.maxChunksPerTick);
      lore.add(ChatColor.GRAY + "Priority: " + ChatColor.WHITE + settings.priority);
      lore.add("");
      lore.add(ChatColor.GRAY + "Smart Unload: " + (settings.smartUnloading ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "▶ Click to configure");
      return GuiUtils.item(mat, status + " " + ChatColor.WHITE + displayName, lore);
   }
}
