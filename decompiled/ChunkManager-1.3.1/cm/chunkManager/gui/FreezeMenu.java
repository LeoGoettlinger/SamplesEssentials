package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.ChunkFreezer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FreezeMenu extends Menu {
   private int page = 0;
   private static final int PER_PAGE = 36;

   public FreezeMenu(ChunkManager plugin, Player player, GuiManager guiManager) {
      super(plugin, player, guiManager);
   }

   @Override
   public String getTitle() {
      return GuiUtils.title("" + ChatColor.AQUA + ChatColor.BOLD + "Frozen Chunks");
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      ChunkFreezer freezer = this.plugin.getChunkFreezer();
      List<String> chunks = new ArrayList<>(freezer.getFrozenChunks());
      int totalPages = Math.max(1, (int)Math.ceil(chunks.size() / 36.0));
      this.setItem(4, GuiUtils.header("Frozen Chunks", "Total: " + chunks.size(), "Page " + (this.page + 1) + "/" + totalPages), null);
      int startIndex = this.page * 36;
      int slot = 9;

      for (int i = startIndex; i < Math.min(startIndex + 36, chunks.size()); i++) {
         String key = chunks.get(i);
         String[] parts = key.split(";");
         if (parts.length == 3) {
            if (slot >= 45) {
               break;
            }

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + parts[0]);
            lore.add(ChatColor.GRAY + "X: " + ChatColor.WHITE + parts[1] + ChatColor.GRAY + " Z: " + ChatColor.WHITE + parts[2]);
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "▶ Click to unfreeze");
            String world = parts[0];
            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);
            this.setItem(slot, GuiUtils.item(Material.FILLED_MAP, ChatColor.AQUA + parts[0] + " [" + parts[1] + ", " + parts[2] + "]", lore), () -> {
               freezer.unfreeze(world, cx, cz);
               GuiUtils.click(this.player);
               this.refresh();
            });
            slot++;
         }
      }

      this.setItem(
         49,
         GuiUtils.item(
            Material.ICE,
            ChatColor.GREEN + "Freeze Current",
            "",
            ChatColor.GRAY + "Freeze the chunk",
            ChatColor.GRAY + "you are standing in",
            "",
            ChatColor.DARK_GRAY + "▶ Click to freeze"
         ),
         () -> {
            int cxx = this.player.getLocation().getBlockX() >> 4;
            int czx = this.player.getLocation().getBlockZ() >> 4;
            String worldx = this.player.getWorld().getName();
            freezer.freeze(worldx, cxx, czx);
            GuiUtils.click(this.player);
            this.refresh();
         }
      );
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

      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new MainMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }
}
