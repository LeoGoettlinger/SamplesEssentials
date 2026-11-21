package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ChunkCostTracker;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TopChunksMenu extends Menu {
   private final DecimalFormat df = new DecimalFormat("#.##");

   public TopChunksMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("Top Chunks");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      this.setItem(4, GuiUtils.header("Top Chunks", "Most expensive loaded chunks"), null);
      ChunkCostTracker tracker = this.plugin.getCostTracker();
      List<ChunkCostTracker.ChunkCost> topChunks = (List<ChunkCostTracker.ChunkCost>)(tracker != null ? tracker.getTopChunks(10) : new ArrayList<>());
      if (topChunks.isEmpty()) {
         this.setItem(22, GuiUtils.item(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + "No data available"), null);
      } else {
         int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21};

         for (int i = 0; i < topChunks.size() && i < slots.length; i++) {
            ChunkCostTracker.ChunkCost cost = topChunks.get(i);
            int rank = i + 1;
            Material mat;
            if (rank <= 3) {
               mat = Material.REDSTONE_BLOCK;
            } else if (rank <= 6) {
               mat = Material.GOLD_BLOCK;
            } else {
               mat = Material.IRON_BLOCK;
            }

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + "[" + cost.x + ", " + cost.z + "]");
            lore.add(ChatColor.GRAY + "Cost: " + ChatColor.RED + this.df.format(cost.costScore));
            lore.add(ChatColor.GRAY + "Entities: " + ChatColor.WHITE + cost.entityCount);
            lore.add(ChatColor.GRAY + "Tile Entities: " + ChatColor.WHITE + cost.tileEntityCount);
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Click to teleport");
            String name = ChatColor.YELLOW + "#" + rank + " " + cost.worldName;
            int cx = cost.x;
            int cz = cost.z;
            String worldName = cost.worldName;
            this.setItem(slots[i], GuiUtils.item(mat, name, lore), () -> {
               World world = Bukkit.getWorld(worldName);
               if (world != null) {
                  this.player.closeInventory();
                  this.player.teleport(new Location(world, cx * 16 + 8, 100.0, cz * 16 + 8));
               }
            });
         }
      }

      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
