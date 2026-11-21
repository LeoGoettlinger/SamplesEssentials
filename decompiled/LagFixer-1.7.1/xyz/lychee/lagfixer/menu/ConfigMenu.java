package xyz.lychee.lagfixer.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class ConfigMenu extends AbstractMenu {
   private final Map<UUID, ConfigMenu.ConfigChange> playerChanges = new HashMap<>();
   private final AbstractModule module;
   private final File configFile;

   public ConfigMenu(LagFixer plugin, int size, AbstractModule module) {
      super(plugin, size, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &fConfig! &8| &eLagFixer"), -1, true);
      this.module = module;
      this.configFile = new File(this.getPlugin().getDataFolder(), "modules/" + module.getName() + ".yml");

      for (int i = size - 9; i < size - 1; i++) {
         if (this.getInv().getItem(i) == null) {
            this.getInv().setItem(i, getBorder());
         }
      }

      this.getInv().setItem(size - 1, getBack());
      this.itemClickEvent(size - 5, () -> this.module.isLoaded() ? getEnabled() : getDisabled(), null);
      ConfigurationSection base = this.module.getSection();
      ConfigurationSection defaults = base.getDefaultSection() != null ? base.getDefaultSection() : base;
      int slot = 0;

      for (Entry<String, Object> entry : defaults.getValues(true).entrySet()) {
         if (!(entry.getValue() instanceof ConfigurationSection)) {
            String key = entry.getKey();
            this.itemClickEvent(slot++, () -> {
               Object val = base.get(key);
               List<String> lore = new ArrayList<>(List.of("&7Current value:"));
               (val instanceof Collection<?> c ? c : Collections.singletonList(val)).forEach(o -> lore.add(" &8{*} &e" + o));
               lore.addAll(List.of("", "&bRight click for default value!", "&aLeft click to change value!"));
               return this.module.getBaseSkull().copy().setName("&f&lKey: &e&l" + key).setLore(lore).build();
            }, e -> {
               HumanEntity human = e.getWhoClicked();
               if (e.isRightClick()) {
                  MessageUtils.sendMessage(true, human, "&fDefault value of &e" + key + " &fis:\n &8{*} &e" + defaults.get(key));
               } else {
                  human.closeInventory();
                  this.playerChanges.put(human.getUniqueId(), new ConfigMenu.ConfigChange(this.module, key, this.module.getSection().get(key)));
                  MessageUtils.sendMessage(true, human, "Enter new value (-cancel to cancel):");
                  if (base.get(key) instanceof Collection) {
                     MessageUtils.sendMessage(false, human, "&fExisting values will be toggled.");
                  }
               }
            });
         }
      }
   }

   @Override
   public void update() {
   }

   @EventHandler
   public void onChat(AsyncPlayerChatEvent e) {
      Player player = e.getPlayer();
      ConfigMenu.ConfigChange change = this.playerChanges.remove(player.getUniqueId());
      if (change != null) {
         e.setCancelled(true);
         if (!e.getMessage().equalsIgnoreCase("-cancel") && !e.getMessage().equalsIgnoreCase("cancel")) {
            try {
               this.updateConfigValue(change, e.getMessage());
               change.getModule().getConfig().save(this.configFile);
               change.getModule().loadAllConfig();
               Object newValue = change.getModule().getSection().get(change.getKey());
               MessageUtils.sendMessage(true, player, "&fConfiguration saved!\n &8{*} &e" + change.getValue() + " &8→ &e" + newValue);
               change.getModule().getMenu().updateAll();
               this.openModuleMenu(player, change.getModule());
            } catch (Exception var5) {
               MessageUtils.sendMessage(true, player, "&cError saving configuration!");
            }
         } else {
            this.openModuleMenu(player, change.getModule());
            MessageUtils.sendMessage(true, player, "Config editor cancelled!");
         }
      }
   }

   private void updateConfigValue(ConfigMenu.ConfigChange change, String message) {
      ConfigurationSection section = change.getModule().getSection();
      String key = change.getKey();
      if (section.isString(key)) {
         section.set(key, message);
      } else if (section.isInt(key)) {
         section.set(key, Integer.parseInt(message));
      } else if (section.isDouble(key)) {
         section.set(key, Double.parseDouble(message));
      } else if (section.isLong(key)) {
         section.set(key, Long.parseLong(message));
      } else if (section.isBoolean(key)) {
         section.set(key, Boolean.parseBoolean(message));
      } else if (section.isList(key)) {
         List<String> list = section.getStringList(key);
         if (list.contains(message)) {
            list.remove(message);
         } else {
            list.add(message);
         }

         section.set(key, list);
      }
   }

   private void openModuleMenu(Player player, AbstractModule module) {
      SupportManager.getInstance().getFork().runNow(false, player.getLocation(), () -> player.openInventory(module.getMenu().getInv()));
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.playerChanges.remove(e.getPlayer().getUniqueId());
   }

   @Override
   public void handleClick(InventoryClickEvent e, ItemStack item) {
      if (item.getType() == Material.PLAYER_HEAD) {
         HumanEntity human = e.getWhoClicked();
         int slot = e.getSlot();
         int topSize = e.getView().getTopInventory().getSize();
         if (slot == topSize - 5) {
            try {
               boolean newState = !this.module.isLoaded();
               String path = this.module.getName() + ".enabled";
               if (newState) {
                  this.module.load();
                  this.module.loadAllConfig();
                  MessageUtils.sendMessage(true, human, "Enabled module &e" + this.module.getName());
               } else {
                  this.module.disable();
                  MessageUtils.sendMessage(true, human, "Disabled module &e" + this.module.getName());
               }

               this.module.setLoaded(newState);
               this.module.getConfig().set(path, newState);
               this.module.getConfig().save(this.configFile);
               this.updateAll();
            } catch (Exception var8) {
               MessageUtils.sendMessage(true, human, "Error toggling module!");
               this.getPlugin().printError(var8);
            }
         }
      }
   }

   @Override
   public AbstractMenu previousMenu() {
      return MenuCommand.getInstance().getModulesMenu();
   }

   public static class ConfigChange {
      private final AbstractModule module;
      private final String key;
      private final Object value;

      @Generated
      public ConfigChange(AbstractModule module, String key, Object value) {
         this.module = module;
         this.key = key;
         this.value = value;
      }

      @Generated
      public AbstractModule getModule() {
         return this.module;
      }

      @Generated
      public String getKey() {
         return this.key;
      }

      @Generated
      public Object getValue() {
         return this.value;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ConfigMenu.ConfigChange other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$module = this.getModule();
            Object other$module = other.getModule();
            if (this$module == null ? other$module == null : this$module.equals(other$module)) {
               Object this$key = this.getKey();
               Object other$key = other.getKey();
               if (this$key == null ? other$key == null : this$key.equals(other$key)) {
                  Object this$value = this.getValue();
                  Object other$value = other.getValue();
                  return this$value == null ? other$value == null : this$value.equals(other$value);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof ConfigMenu.ConfigChange;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $module = this.getModule();
         result = result * 59 + ($module == null ? 43 : $module.hashCode());
         Object $key = this.getKey();
         result = result * 59 + ($key == null ? 43 : $key.hashCode());
         Object $value = this.getValue();
         return result * 59 + ($value == null ? 43 : $value.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "ConfigMenu.ConfigChange(module=" + this.getModule() + ", key=" + this.getKey() + ", value=" + this.getValue() + ")";
      }
   }
}
