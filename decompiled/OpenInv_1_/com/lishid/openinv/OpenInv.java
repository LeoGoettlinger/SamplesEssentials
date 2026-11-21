package com.lishid.openinv;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.FoliaWrappedJavaPlugin;
import com.lishid.openinv.command.ClearInvCommand;
import com.lishid.openinv.command.ContainerSettingCommand;
import com.lishid.openinv.command.OpenInvCommand;
import com.lishid.openinv.command.SearchContainerCommand;
import com.lishid.openinv.command.SearchEnchantCommand;
import com.lishid.openinv.command.SearchInvCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.listener.ContainerListener;
import com.lishid.openinv.listener.ToggleListener;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.config.ConfigUpdater;
import com.lishid.openinv.util.lang.LangMigrator;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.setting.PlayerToggle;
import com.lishid.openinv.util.setting.PlayerToggles;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInv extends FoliaWrappedJavaPlugin implements IOpenInv {
   private InternalAccessor accessor;
   private Config config;
   private InventoryManager inventoryManager;
   private LanguageManager languageManager;
   private PlayerLoader playerLoader;
   private boolean isSpigot = false;

   public PlayerLoader getPlayerLoader() {
      return this.playerLoader;
   }

   public void reloadConfig() {
      super.reloadConfig();
      this.config.reload(this.getConfig());
      this.languageManager.reload();
      if (this.accessor != null && this.accessor.isSupported()) {
         this.accessor.reload(this.getConfig());
      }
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (this.isSpigot && this.accessor.isSupported()) {
         return false;
      } else {
         this.sendVersionError(sender::sendMessage);
         return true;
      }
   }

   public void onDisable() {
      this.inventoryManager.evictAll();

      try {
         this.playerLoader.getProfileStore().shutdown();
      } catch (Exception var2) {
         this.getLogger().log(Level.WARNING, "Failed to shut down profile store correctly", (Throwable)var2);
      }
   }

   public void onEnable() {
      this.saveDefaultConfig();
      Path dataFolder = this.getDataFolder().toPath();
      new LangMigrator(dataFolder, dataFolder.resolve("locale"), this.getLogger()).migrate();
      this.config = new Config();
      this.languageManager = new LanguageManager(this, "en");
      this.accessor = new InternalAccessor(this.getLogger(), this.languageManager);
      this.reloadConfig();
      this.inventoryManager = new InventoryManager(this, this.config, this.accessor);
      this.playerLoader = new PlayerLoader(this, this.config, this.inventoryManager, this.accessor, this.getLogger());

      try {
         Class.forName("org.bukkit.entity.Player$Spigot");
         this.isSpigot = true;
      } catch (ClassNotFoundException var3) {
         this.isSpigot = false;
      }

      if (this.isSpigot && this.accessor.isSupported()) {
         this.reloadConfig();
         new ConfigUpdater(this).checkForUpdates();
         this.registerEvents();
         this.registerCommands();
      } else {
         this.sendVersionError(this.getLogger()::warning);
      }
   }

   private void registerEvents() {
      PluginManager pluginManager = this.getServer().getPluginManager();
      pluginManager.registerEvents(this.playerLoader, this);
      pluginManager.registerEvents(this.inventoryManager, this);
      pluginManager.registerEvents(new ContainerListener(this.accessor, this.languageManager), this);
      pluginManager.registerEvents(new ToggleListener(), this);
   }

   private void registerCommands() {
      this.setCommandExecutor(new OpenInvCommand(this, this.config, this.inventoryManager, this.languageManager, this.playerLoader), "openinv", "openender");
      this.setCommandExecutor(new SearchContainerCommand(this, this.languageManager), "searchcontainer");
      this.setCommandExecutor(new SearchInvCommand(this.languageManager), "searchinv", "searchender");
      this.setCommandExecutor(new SearchEnchantCommand(this.languageManager), "searchenchant");
      this.setCommandExecutor(new ClearInvCommand(this, this.config, this.inventoryManager, this.languageManager, this.playerLoader), "clearinv", "clearender");
      ContainerSettingCommand settingCommand = new ContainerSettingCommand(this.languageManager);

      for (PlayerToggle toggle : PlayerToggles.get()) {
         this.setCommandExecutor(settingCommand, toggle.getName().toLowerCase(Locale.ENGLISH));
      }
   }

   private void setCommandExecutor(@NotNull CommandExecutor executor, @NotNull String... commands) {
      for (String commandName : commands) {
         PluginCommand command = this.getCommand(commandName);
         if (command != null) {
            command.setExecutor(executor);
         }
      }
   }

   private void sendVersionError(@NotNull Consumer<String> messageMethod) {
      if (!this.accessor.isSupported()) {
         messageMethod.accept("Your server version (" + this.accessor.getVersion() + ") is not supported.");
         messageMethod.accept("Please check https://github.com/Jikoo/OpenInv/wiki/Supported-Versions to find a matching OI version.");
      }

      if (!this.isSpigot) {
         messageMethod.accept("OpenInv requires that you use Spigot or a Spigot fork. Per the 1.14 update thread");
         messageMethod.accept("(https://www.spigotmc.org/threads/369724/ \"A Note on CraftBukkit\"), if you are");
         messageMethod.accept("encountering an inconsistency with vanilla that prevents you from using Spigot,");
         messageMethod.accept("that is considered a Spigot bug and should be reported as such.");
      }
   }

   @Override
   public boolean isSupportedVersion() {
      return this.accessor != null && this.accessor.isSupported();
   }

   @Override
   public boolean disableSaving() {
      return this.config.isSaveDisabled();
   }

   @Override
   public boolean disableOfflineAccess() {
      return this.config.isOfflineDisabled();
   }

   @Override
   public boolean noArgsOpensSelf() {
      return this.config.doesNoArgsOpenSelf();
   }

   @NotNull
   @Override
   public IAnySilentContainer getAnySilentContainer() {
      return this.accessor.getAnySilentContainer();
   }

   @Override
   public boolean getAnyContainerStatus(@NotNull OfflinePlayer offline) {
      return PlayerToggles.any().is(offline.getUniqueId());
   }

   @Override
   public void setAnyContainerStatus(@NotNull OfflinePlayer offline, boolean status) {
      PlayerToggles.any().set(offline.getUniqueId(), status);
   }

   @Override
   public boolean getSilentContainerStatus(@NotNull OfflinePlayer offline) {
      return PlayerToggles.silent().is(offline.getUniqueId());
   }

   @Override
   public void setSilentContainerStatus(@NotNull OfflinePlayer offline, boolean status) {
      PlayerToggles.silent().set(offline.getUniqueId(), status);
   }

   @NotNull
   @Override
   public ISpecialEnderChest getSpecialEnderChest(@NotNull Player player, boolean online) {
      return this.inventoryManager.getEnderChest(player);
   }

   @NotNull
   @Override
   public ISpecialPlayerInventory getSpecialInventory(@NotNull Player player, boolean online) {
      return this.inventoryManager.getInventory(player);
   }

   @Deprecated(
      forRemoval = true
   )
   @Nullable
   @Override
   public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {
      Permissions edit = null;
      HumanEntity target = inventory.getPlayer();
      boolean ownContainer = player.equals(target);
      if (inventory instanceof ISpecialPlayerInventory) {
         edit = ownContainer ? Permissions.INVENTORY_EDIT_SELF : Permissions.INVENTORY_EDIT_OTHER;
      } else if (inventory instanceof ISpecialEnderChest) {
         edit = ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_EDIT_OTHER;
      }

      boolean viewOnly = edit != null && !edit.hasPermission(player);
      if (ownContainer) {
         return this.accessor.openInventory(player, inventory, viewOnly);
      } else {
         AccessEqualMode accessMode = AccessEqualMode.getByPerm(player, this.config);
         int level = 4;

         while (level > 0) {
            String permission = "openinv.access.level." + level;
            if (!target.hasPermission(permission)) {
               if (!player.hasPermission(permission)) {
                  level--;
                  continue;
               }
            } else {
               if (!player.hasPermission(permission) || accessMode == AccessEqualMode.DENY) {
                  return null;
               }

               if (accessMode == AccessEqualMode.VIEW) {
                  viewOnly = true;
               }
            }
            break;
         }

         return this.openInventory(player, inventory, viewOnly);
      }
   }

   @Nullable
   @Override
   public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory, boolean viewOnly) {
      return this.accessor.openInventory(player, inventory, viewOnly);
   }

   @Override
   public boolean isPlayerLoaded(@NotNull UUID playerUuid) {
      return this.inventoryManager.getLoadedPlayer(playerUuid) != null;
   }

   @Nullable
   @Override
   public Player loadPlayer(@NotNull OfflinePlayer offline) {
      return this.playerLoader.load(offline);
   }

   @Nullable
   @Override
   public OfflinePlayer matchPlayer(@NotNull String name) {
      return this.playerLoader.match(name);
   }

   @Override
   public void unload(@NotNull OfflinePlayer offline) {
      this.inventoryManager.unload(offline.getUniqueId());
   }
}
