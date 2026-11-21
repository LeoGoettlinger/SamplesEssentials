package xyz.lychee.lagfixer.commands;

import lombok.Generated;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.menu.HardwareMenu;
import xyz.lychee.lagfixer.menu.MainMenu;
import xyz.lychee.lagfixer.menu.ModulesMenu;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class MenuCommand extends CommandManager.Subcommand {
   private static MenuCommand instance;
   private MainMenu mainMenu;
   private ModulesMenu modulesMenu;
   private HardwareMenu hardwareMenu;

   public MenuCommand(CommandManager commandManager) {
      super(commandManager, "menu", "open lagfixer menu, and edit configuration", "gui");
      instance = this;
   }

   @Override
   public void load() {
      LagFixer plugin = this.getCommandManager().getPlugin();
      this.mainMenu = new MainMenu(plugin, 27, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &fMenu! &8| &eLagFixer"));
      this.mainMenu.load();
      this.modulesMenu = new ModulesMenu(plugin, 45, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &fModules! &8| &eLagFixer"));
      this.modulesMenu.load();

      try {
         this.hardwareMenu = new HardwareMenu(plugin, 27, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &fHardware! &8| &eLagFixer"));
         this.hardwareMenu.load();
      } catch (Throwable var3) {
      }
   }

   @Override
   public void unload() {
      this.mainMenu.unload();
      this.modulesMenu.unload();
      if (this.hardwareMenu != null) {
         this.hardwareMenu.unload();
      }
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (sender instanceof Player) {
         ((Player)sender).openInventory(this.mainMenu.getInv());
      } else {
         Component text = Language.getMainValue("player_only", true);
         if (text != null) {
            LagFixer.getInstance().getAudiences().sender(sender).sendMessage(text);
         }
      }

      return true;
   }

   @Generated
   public MainMenu getMainMenu() {
      return this.mainMenu;
   }

   @Generated
   public ModulesMenu getModulesMenu() {
      return this.modulesMenu;
   }

   @Generated
   public HardwareMenu getHardwareMenu() {
      return this.hardwareMenu;
   }

   @Generated
   public static MenuCommand getInstance() {
      return instance;
   }
}
