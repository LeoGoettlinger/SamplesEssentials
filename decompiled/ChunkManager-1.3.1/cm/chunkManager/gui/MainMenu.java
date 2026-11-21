package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ChunkPreGen;
import cm.chunkManager.components.ChunkVisualizer;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainMenu extends Menu {
   public MainMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.AQUA + ChatColor.BOLD + "ChunkManager");
   }

   @Override
   public int getSize() {
      return 36;
   }

   @Override
   public void build() {
      this.setItem(4, GuiUtils.header("ChunkManager", "Server chunk optimization", ChatColor.DARK_GRAY + "v" + this.plugin.getDescription().getVersion()), null);
      this.setItem(
         10,
         GuiUtils.action("Performance", Material.CLOCK, ChatColor.AQUA, "View server performance", "and chunk statistics"),
         () -> this.guiManager.openMenu(this.player, new StatsMenu(this.plugin, this.player, this.guiManager))
      );
      this.setItem(
         12,
         GuiUtils.action("Settings", Material.COMPARATOR, ChatColor.GOLD, "Configure chunk", "management settings"),
         () -> this.guiManager.openMenu(this.player, new SettingsMenu(this.plugin, this.player, this.guiManager))
      );
      this.setItem(
         14,
         GuiUtils.action("Regions", Material.FILLED_MAP, ChatColor.GREEN, "View region activity", "and optimization data"),
         () -> this.guiManager.openMenu(this.player, new RegionMenu(this.plugin, this.player, this.guiManager))
      );
      this.setItem(
         16,
         GuiUtils.action("Analyzer", Material.AMETHYST_SHARD, ChatColor.LIGHT_PURPLE, "Analyze chunk complexity", "and performance impact"),
         () -> this.guiManager.openMenu(this.player, new AnalyzerMenu(this.plugin, this.player, this.guiManager))
      );
      ChunkVisualizer viz = this.plugin.getChunkVisualizer();
      boolean vizOn = viz != null && viz.isViewing(this.player);
      this.setItem(20, GuiUtils.toggle("Visualizer", vizOn, Material.ENDER_EYE, "Toggle chunk border", "particle visualization"), () -> {
         if (viz != null) {
            viz.toggle(this.player);
            GuiUtils.click(this.player);
            this.refresh();
         }
      });
      this.setItem(
         22,
         GuiUtils.action("Frozen Chunks", Material.ICE, ChatColor.AQUA, "View and manage", "frozen chunks"),
         () -> this.guiManager.openMenu(this.player, new FreezeMenu(this.plugin, this.player, this.guiManager))
      );
      ChunkPreGen preGen = this.plugin.getChunkPreGen();
      if (preGen != null && preGen.isActive()) {
         Map<String, String> progress = preGen.getProgress();
         this.setItem(
            24,
            GuiUtils.item(
               Material.DIAMOND_PICKAXE,
               ChatColor.LIGHT_PURPLE + "Pre-Generation",
               "",
               ChatColor.GREEN + "Running: " + ChatColor.WHITE + progress.get("percent") + "%",
               ChatColor.GRAY + progress.get("generated") + "/" + progress.get("total"),
               ChatColor.GRAY + "World: " + progress.get("world")
            ),
            null
         );
      } else {
         this.setItem(
            24, GuiUtils.item(Material.DIAMOND_PICKAXE, ChatColor.LIGHT_PURPLE + "Pre-Generation", "", ChatColor.GRAY + "Use /cmpregen to start"), null
         );
      }

      this.setItem(26, GuiUtils.action("Top Chunks", Material.GOLD_INGOT, ChatColor.GOLD, "View most expensive", "loaded chunks"), () -> {
         GuiUtils.click(this.player);
         this.guiManager.openMenu(this.player, new TopChunksMenu(this.plugin, this.player, this.guiManager));
      });
      this.setItem(31, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
