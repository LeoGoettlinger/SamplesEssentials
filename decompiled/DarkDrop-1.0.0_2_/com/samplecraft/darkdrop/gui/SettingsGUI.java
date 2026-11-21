package com.samplecraft.darkdrop.gui;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.model.BadLootType;
import com.samplecraft.darkdrop.model.DropSettings;
import com.samplecraft.darkdrop.model.DropTarget;
import com.samplecraft.darkdrop.util.MessageUtil;
import com.samplecraft.darkdrop.util.PositionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SettingsGUI implements InventoryHolder {
   public static final int SIZE = 27;
   public static final int SLOT_GOOD_CHANCE = 10;
   public static final int SLOT_BAD_CHANCE = 11;
   public static final int SLOT_GOOD_AMOUNT = 12;
   public static final int SLOT_BAD_AMOUNT = 14;
   public static final int SLOT_BAD_LOOT_TYPE = 15;
   public static final int SLOT_GOOD_LOOT_OPEN = 16;
   public static final int SLOT_GOOD_COMMANDS_OPEN = 19;
   public static final int SLOT_CONFIRM = 22;
   public static final int SLOT_CANCEL = 4;
   private final DarkDrop plugin;
   private final Player player;
   private final DropSettings settings;
   private final SettingsGUI.Context context;
   private Inventory inventory;

   public SettingsGUI(DarkDrop plugin, Player player, SettingsGUI.Context context) {
      this.plugin = plugin;
      this.player = player;
      this.context = context;
      DropSettings saved = plugin.getSettingsManager().getOrCreate(player.getUniqueId());
      this.settings = saved.copy();
   }

   public void open() {
      Component title = MessageUtil.toComponent(this.plugin.getConfigManager().raw("menu-title"));
      this.inventory = Bukkit.createInventory(this, 27, title);
      this.render();
      this.player.openInventory(this.inventory);
   }

   public void refresh() {
      if (this.inventory != null) {
         this.render();
         this.player.updateInventory();
      }
   }

   private void render() {
      this.inventory.clear();
      this.inventory
         .setItem(
            10,
            this.chanceItem(
               Material.EMERALD,
               "&aChance: Guter Loot",
               this.settings.getGoodChance(),
               "&7Klick: +5 / Shift-Klick: +1",
               "&cRechtsklick: -5 / Shift-Rechtsklick: -1"
            )
         );
      this.inventory
         .setItem(
            11,
            this.chanceItem(
               Material.REDSTONE,
               "&cChance: Schlechter Loot",
               this.settings.getBadChance(),
               "&7Klick: +5 / Shift-Klick: +1",
               "&cRechtsklick: -5 / Shift-Rechtsklick: -1"
            )
         );
      this.inventory.setItem(12, this.amountItem(Material.GOLD_INGOT, "&aMenge: Guter Loot", this.settings.getGoodAmount()));
      this.inventory.setItem(14, this.amountItem(Material.IRON_NUGGET, "&cMenge: Schlechter Loot", this.settings.getBadAmount()));
      this.inventory.setItem(15, this.badLootItem(this.settings.getBadLootType()));
      this.inventory.setItem(16, this.goodLootOpenItem());
      this.inventory.setItem(19, this.commandsOpenItem());
      this.inventory
         .setItem(22, this.simpleItem(Material.LIME_CONCRETE, "&a&lBestaetigen & Start", "&7Speichert die Einstellungen", "&7und startet den DarkDrop."));
      this.inventory.setItem(4, this.simpleItem(Material.BARRIER, "&cAbbrechen", "&7Verwirft die Eingaben."));
   }

   private ItemStack chanceItem(Material mat, String title, double value, String... lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent(title));
      List<Component> loreList = new ArrayList<>();
      loreList.add(MessageUtil.toComponent("&7Aktuell: &e" + String.format("%.1f", value)));
      loreList.add(
         MessageUtil.toComponent(
            "&7Normalisiert: &e"
               + String.format("%.1f%%", this.settings.normalizedGoodPercent())
               + " / &c"
               + String.format("%.1f%%", this.settings.normalizedBadPercent())
         )
      );

      for (String l : lore) {
         loreList.add(MessageUtil.toComponent(l));
      }

      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack amountItem(Material mat, String title, int value) {
      ItemStack item = new ItemStack(mat);
      item.setAmount(Math.max(1, Math.min(64, value)));
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent(title));
      List<Component> loreList = new ArrayList<>();
      loreList.add(MessageUtil.toComponent("&7Aktuell: &e" + value));
      loreList.add(MessageUtil.toComponent("&7Klick: +1 / Shift-Klick: +5"));
      loreList.add(MessageUtil.toComponent("&cRechtsklick: -1 / Shift-Rechtsklick: -5"));
      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack badLootItem(BadLootType type) {
      Material mat = switch (type) {
         case BAD_ITEM -> Material.DEAD_BUSH;
         case FIRE -> Material.FLINT_AND_STEEL;
         case LIGHTNING -> Material.LIGHTNING_ROD;
         case EXPLOSION -> Material.TNT;
         case POISON -> Material.POTION;
         case VOID_EFFECT -> Material.ENDER_EYE;
         case WITHER -> Material.WITHER_SKELETON_SKULL;
         case NOTHING -> Material.STRUCTURE_VOID;
      };
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&cSchlechter Loot: " + type.getDisplayName()));
      List<Component> loreList = new ArrayList<>();
      loreList.add(MessageUtil.toComponent("&7" + type.getDescription()));
      loreList.add(MessageUtil.toComponent("&7Klick: naechste Option"));
      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack goodLootOpenItem() {
      ItemStack item = new ItemStack(Material.CHEST);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aGute Loot-Vorlagen"));
      List<Component> loreList = new ArrayList<>();
      loreList.add(MessageUtil.toComponent("&7Aktuelle Vorlagen: &e" + this.settings.getGoodLootItems().size()));
      loreList.add(MessageUtil.toComponent("&7Klicke, um Items als Vorlagen"));
      loreList.add(MessageUtil.toComponent("&7hinzuzufuegen oder zu entfernen."));
      loreList.add(MessageUtil.toComponent("&8Items werden kopiert - nicht verbraucht."));
      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack commandsOpenItem() {
      ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent("&aCommands als guter Loot"));
      List<Component> loreList = new ArrayList<>();
      loreList.add(MessageUtil.toComponent("&7Aktuelle Commands: &e" + this.settings.getGoodLootCommands().size()));
      loreList.add(MessageUtil.toComponent("&7Aktiviert: &e" + (this.settings.isUseCommandsAsGoodLoot() ? "&aJa" : "&cNein")));
      loreList.add(MessageUtil.toComponent("&7Klicke, um Commands zu verwalten."));
      loreList.add(MessageUtil.toComponent(""));
      loreList.add(MessageUtil.toComponent("&7Platzhalter: &f{player} {x} {y}"));
      loreList.add(MessageUtil.toComponent("&f{z} {world} {amount}"));
      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack simpleItem(Material mat, String title, String... lore) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.displayName(MessageUtil.toComponent(title));
      List<Component> loreList = new ArrayList<>();

      for (String l : lore) {
         loreList.add(MessageUtil.toComponent(l));
      }

      meta.lore(loreList);
      item.setItemMeta(meta);
      return item;
   }

   public void handleClick(int slot, boolean right, boolean shift) {
      int amountStep = shift ? 5 : 1;
      double chanceStep = shift ? 1.0 : 5.0;
      int dir = right ? -1 : 1;
      switch (slot) {
         case 4:
            this.player.closeInventory();
            this.player.sendMessage(this.plugin.getConfigManager().component("settings-cancelled"));
            return;
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 13:
         case 17:
         case 18:
         case 20:
         case 21:
         default:
            return;
         case 10:
            this.settings.setGoodChance(this.settings.getGoodChance() + dir * chanceStep);
            break;
         case 11:
            this.settings.setBadChance(this.settings.getBadChance() + dir * chanceStep);
            break;
         case 12:
            this.settings.setGoodAmount(this.settings.getGoodAmount() + dir * amountStep);
            break;
         case 14:
            this.settings.setBadAmount(this.settings.getBadAmount() + dir * amountStep);
            break;
         case 15:
            this.settings.setBadLootType(this.settings.getBadLootType().next());
            break;
         case 16:
            this.plugin.getSettingsManager().put(this.player.getUniqueId(), this.settings);
            GoodLootGUI lootGui = new GoodLootGUI(this.plugin, this);
            lootGui.open();
            return;
         case 19:
            this.plugin.getSettingsManager().put(this.player.getUniqueId(), this.settings);
            CommandInputGUI cmdGui = new CommandInputGUI(this.plugin, this);
            cmdGui.open();
            return;
         case 22:
            this.confirmAndStart();
            return;
      }

      this.refresh();
   }

   public void confirmAndStart() {
      this.plugin.getSettingsManager().put(this.player.getUniqueId(), this.settings);
      this.plugin.getSettingsManager().saveAll();
      this.player.closeInventory();
      this.player.sendMessage(this.plugin.getConfigManager().component("settings-saved"));
      DropTarget target = this.context.toTarget(this.player);
      if (target == null) {
         this.player.sendMessage(this.plugin.getConfigManager().component("random-failed"));
      } else {
         this.plugin.getDropManager().startDrop(this.player, this.settings, target);
      }
   }

   public DropSettings getSettings() {
      return this.settings;
   }

   public Player getPlayer() {
      return this.player;
   }

   public SettingsGUI.Context getContext() {
      return this.context;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public static class Context {
      public final SettingsGUI.Context.ContextType type;
      public final double x;
      public final double y;
      public final double z;
      public final UUID worldId;

      private Context(SettingsGUI.Context.ContextType type, double x, double y, double z, UUID worldId) {
         this.type = type;
         this.x = x;
         this.y = y;
         this.z = z;
         this.worldId = worldId;
      }

      public static SettingsGUI.Context player(Player p) {
         return new SettingsGUI.Context(
            SettingsGUI.Context.ContextType.PLAYER, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getWorld().getUID()
         );
      }

      public static SettingsGUI.Context coords(double x, double y, double z, UUID worldId) {
         return new SettingsGUI.Context(SettingsGUI.Context.ContextType.COORDS, x, y, z, worldId);
      }

      public static SettingsGUI.Context random(Location center) {
         return new SettingsGUI.Context(SettingsGUI.Context.ContextType.RANDOM, center.getX(), center.getY(), center.getZ(), center.getWorld().getUID());
      }

      public DropTarget toTarget(Player p) {
         World world = Bukkit.getWorld(this.worldId);
         if (world == null) {
            return null;
         } else {
            switch (this.type) {
               case PLAYER: {
                  Location loc = p.getLocation();
                  return new DropTarget(loc.clone(), loc.clone(), DropTarget.TargetMode.PLAYER);
               }
               case RANDOM:
                  Location center = new Location(world, this.x, this.y, this.z);
                  Location safe = PositionUtil.findRandomSafeLocation(DarkDrop.getInstance(), world, center);
                  if (safe == null) {
                     return null;
                  }

                  return new DropTarget(safe.clone(), safe.clone(), DropTarget.TargetMode.RANDOM);
               case COORDS: {
                  Location loc = new Location(world, this.x, this.y, this.z);
                  return new DropTarget(loc.clone(), loc.clone(), DropTarget.TargetMode.COORDS);
               }
               default:
                  return null;
            }
         }
      }

      public static enum ContextType {
         PLAYER,
         RANDOM,
         COORDS;
      }
   }
}
