package cm.chunkManager.gui;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.components.WorldSettingsManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WorldEditMenu extends Menu {
   private final World world;
   private final String worldName;

   public WorldEditMenu(ChunkManager plugin, Player player, GuiManager guiManager, World world) {
      super(plugin, player, guiManager);
      this.world = world;
      this.worldName = world.getName();
   }

   @Override
   public String getTitle() {
      String displayName = this.plugin.getWorldSettingsManager().getWorldDisplayName(this.world);
      return GuiUtils.title("" + ChatColor.WHITE + ChatColor.BOLD + displayName);
   }

   @Override
   public int getSize() {
      return 54;
   }

   @Override
   public void build() {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      WorldSettingsManager.WorldSettings settings = wsm.getSettings(this.worldName);
      this.setItem(4, this.buildHeader(settings), null);
      this.setItem(11, GuiUtils.toggle("Enabled", settings.enabled, Material.LEVER), () -> {
         wsm.updateEnabled(this.worldName, !settings.enabled);
         GuiUtils.click(this.player);
         this.refresh();
      });
      this.setItem(13, GuiUtils.toggle("Smart Unload", settings.smartUnloading, Material.HOPPER), () -> {
         wsm.updateSmartUnloading(this.worldName, !settings.smartUnloading);
         GuiUtils.click(this.player);
         this.refresh();
      });
      this.setItem(15, this.buildInfo(), null);
      this.setItem(
         20,
         this.buildValue("Chunk Radius", settings.chunkRadius, 1, 10, Material.ENDER_EYE, ChatColor.GREEN),
         () -> this.adjustRadius(1),
         () -> this.adjustRadius(-1)
      );
      this.setItem(
         22,
         this.buildValue("Max/Tick", settings.maxChunksPerTick, 1, 20, Material.MINECART, ChatColor.GOLD),
         () -> this.adjustMaxChunks(1),
         () -> this.adjustMaxChunks(-1)
      );
      this.setItem(
         24,
         this.buildValue("Safety Radius", settings.safetyRadius, 1, 8, Material.SHIELD, ChatColor.AQUA),
         () -> this.adjustSafety(1),
         () -> this.adjustSafety(-1)
      );
      this.setItem(
         30,
         this.buildValue("Priority", settings.priority, 1, 10, Material.GOLD_INGOT, ChatColor.YELLOW),
         () -> this.adjustPriority(1),
         () -> this.adjustPriority(-1)
      );
      this.setItem(45, GuiUtils.back(), () -> this.guiManager.openMenu(this.player, new WorldSettingsMenu(this.plugin, this.player, this.guiManager)));
      this.setItem(53, GuiUtils.close(), () -> this.player.closeInventory());
      GuiUtils.applyBorder(this.inv, this.getSize());
   }

   private void adjustRadius(int delta) {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      WorldSettingsManager.WorldSettings settings = wsm.getSettings(this.worldName);
      int newVal = Math.max(1, Math.min(10, settings.chunkRadius + delta));
      if (newVal != settings.chunkRadius) {
         wsm.updateRadius(this.worldName, newVal);
         GuiUtils.click(this.player);
         this.refresh();
      }
   }

   private void adjustMaxChunks(int delta) {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      WorldSettingsManager.WorldSettings settings = wsm.getSettings(this.worldName);
      int newVal = Math.max(1, Math.min(20, settings.maxChunksPerTick + delta));
      if (newVal != settings.maxChunksPerTick) {
         wsm.updateMaxChunks(this.worldName, newVal);
         GuiUtils.click(this.player);
         this.refresh();
      }
   }

   private void adjustSafety(int delta) {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      WorldSettingsManager.WorldSettings settings = wsm.getSettings(this.worldName);
      int newVal = Math.max(1, Math.min(8, settings.safetyRadius + delta));
      if (newVal != settings.safetyRadius) {
         wsm.updateSafetyRadius(this.worldName, newVal);
         GuiUtils.click(this.player);
         this.refresh();
      }
   }

   private void adjustPriority(int delta) {
      WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
      WorldSettingsManager.WorldSettings settings = wsm.getSettings(this.worldName);
      int newVal = Math.max(1, Math.min(10, settings.priority + delta));
      if (newVal != settings.priority) {
         wsm.updatePriority(this.worldName, newVal);
         GuiUtils.click(this.player);
         this.refresh();
      }
   }

   private ItemStack buildHeader(WorldSettingsManager.WorldSettings settings) {
      Material mat = switch (this.world.getEnvironment()) {
         case NETHER -> Material.NETHERRACK;
         case THE_END -> Material.END_STONE;
         default -> Material.GRASS_BLOCK;
      };
      String displayName = this.plugin.getWorldSettingsManager().getWorldDisplayName(this.world);
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + this.world.getEnvironment().name());
      lore.add(ChatColor.GRAY + "Status: " + (settings.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
      return GuiUtils.item(mat, "" + ChatColor.AQUA + ChatColor.BOLD + displayName, lore);
   }

   private ItemStack buildValue(String name, int value, int min, int max, Material icon, ChatColor color) {
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.WHITE + "Value: " + ChatColor.AQUA + value);
      lore.add(ChatColor.DARK_GRAY + "Range: " + min + "-" + max);
      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "Left-click +1 | Right-click -1");
      return GuiUtils.item(icon, color + name, lore);
   }

   private ItemStack buildInfo() {
      int chunks = 0;
      int players = 0;
      int entities = 0;

      try {
         chunks = this.world.getLoadedChunks().length;
         players = this.world.getPlayers().size();
         if (GuiUtils.canAccessEntities()) {
            entities = this.world.getEntities().size();
         }
      } catch (Exception var5) {
      }

      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.GRAY + "Loaded Chunks: " + ChatColor.WHITE + chunks);
      lore.add(ChatColor.GRAY + "Players: " + ChatColor.WHITE + players);
      lore.add(ChatColor.GRAY + "Entities: " + ChatColor.WHITE + entities);
      return GuiUtils.item(Material.BOOK, ChatColor.WHITE + "World Info", lore);
   }
}
