package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ChunkAnalyzer;
import cm.chunkManager.utils.Scheduler;
import java.text.DecimalFormat;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AnalyzerMenu extends Menu {
   private final DecimalFormat df = new DecimalFormat("#.##");

   public AnalyzerMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Analyzer");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      this.setItem(4, GuiUtils.header("Chunk Analyzer", "Complexity & performance analysis"), null);
      if (!GuiUtils.isAdvancedEnabled(this.plugin)) {
         this.setItem(22, GuiUtils.item(Material.BARRIER, ChatColor.RED + "Advanced Features Disabled", ChatColor.GRAY + "Enable in config.yml"), null);
         this.addNav();
         GuiUtils.applyBorder(this.inv, this.getSize());
      } else {
         ChunkAnalyzer analyzer = this.plugin.getChunkAnalyzer();
         if (analyzer == null) {
            this.setItem(22, GuiUtils.item(Material.BARRIER, ChatColor.RED + "Analyzer Not Available"), null);
            this.addNav();
            GuiUtils.applyBorder(this.inv, this.getSize());
         } else {
            Chunk currentChunk = this.player.getLocation().getChunk();
            this.setItem(
               13,
               GuiUtils.item(
                  Material.AMETHYST_SHARD,
                  ChatColor.LIGHT_PURPLE + "Current Chunk",
                  "",
                  ChatColor.GRAY + "Location: " + ChatColor.WHITE + "[" + currentChunk.getX() + ", " + currentChunk.getZ() + "]",
                  "",
                  ChatColor.DARK_GRAY + "▶ Click to analyze"
               ),
               () -> {
                  this.player.closeInventory();
                  this.player.sendMessage(ChatColor.YELLOW + "Analyzing chunk...");
                  Scheduler.runAsync(
                     this.plugin,
                     () -> {
                        ChunkAnalyzer.ChunkAnalysisData data = analyzer.analyzeChunkSync(currentChunk);
                        Scheduler.runGlobal(
                           this.plugin,
                           () -> {
                              this.player.sendMessage(ChatColor.GREEN + "Chunk Analysis [" + currentChunk.getX() + ", " + currentChunk.getZ() + "]");
                              this.player
                                 .sendMessage(ChatColor.GRAY + "Complexity: " + ChatColor.WHITE + data.complexity + " (" + data.getComplexityLevel() + ")");
                              this.player.sendMessage(ChatColor.GRAY + "Priority: " + ChatColor.WHITE + data.priority + "/100");
                              this.player.sendMessage(ChatColor.GRAY + "Impact: " + ChatColor.WHITE + this.df.format(data.performanceImpact) + "%");
                              this.player.sendMessage(ChatColor.GRAY + "Memory: " + ChatColor.WHITE + data.estimatedMemoryUsage / 1024 + " KB");
                              this.player
                                 .sendMessage(
                                    ChatColor.GRAY
                                       + "Mobs: "
                                       + ChatColor.WHITE
                                       + data.mobCount
                                       + ChatColor.GRAY
                                       + " | Passive: "
                                       + ChatColor.WHITE
                                       + data.passiveEntityCount
                                 );
                              this.player.sendMessage(ChatColor.GRAY + "Tile Entities: " + ChatColor.WHITE + data.tileEntityCount);
                           }
                        );
                     }
                  );
               }
            );
            int slot = 28;

            for (World w : Bukkit.getWorlds()) {
               if (!this.plugin.isWorldBlacklisted(w)) {
                  if (slot >= 35) {
                     break;
                  }
                  Material mat = switch (w.getEnvironment()) {
                     case NETHER -> Material.NETHERRACK;
                     case THE_END -> Material.END_STONE;
                     default -> Material.GRASS_BLOCK;
                  };
                  this.setItem(slot++, GuiUtils.item(mat, ChatColor.GREEN + w.getName(), "", ChatColor.DARK_GRAY + "▶ Click to analyze world"), () -> {
                     this.player.closeInventory();
                     this.player.sendMessage(ChatColor.YELLOW + "Analyzing " + w.getName() + "...");
                     Scheduler.runAsync(this.plugin, () -> {
                        Map<String, Object> stats = analyzer.getWorldStatistics(w);
                        Scheduler.runGlobal(this.plugin, () -> {
                           this.player.sendMessage(ChatColor.GREEN + w.getName() + " Analysis:");
                           this.player.sendMessage(ChatColor.GRAY + "Total Chunks: " + ChatColor.WHITE + stats.get("totalChunks"));
                           this.player.sendMessage(ChatColor.GRAY + "Avg Complexity: " + ChatColor.WHITE + this.df.format(stats.get("averageComplexity")));
                           this.player.sendMessage(ChatColor.GRAY + "High Priority: " + ChatColor.WHITE + stats.get("highPriorityChunks"));
                           this.player.sendMessage(ChatColor.GRAY + "Total Mobs: " + ChatColor.WHITE + stats.get("totalMobs"));
                           this.player.sendMessage(ChatColor.GRAY + "Tile Entities: " + ChatColor.WHITE + stats.get("totalTileEntities"));
                           this.player.sendMessage(ChatColor.GRAY + "Memory: " + ChatColor.WHITE + stats.get("totalMemoryUsageMB") + " MB");
                        });
                     });
                  });
               }
            }

            this.addNav();
            GuiUtils.applyBorder(this.inv, this.getSize());
         }
      }
   }

   private void addNav() {
      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
   }
}
