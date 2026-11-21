package simplexity.simpleplayerfreeze;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import simplexity.simpleplayerfreeze.commands.FreezeAll;
import simplexity.simpleplayerfreeze.commands.FreezePlayer;
import simplexity.simpleplayerfreeze.commands.FreezeSpy;
import simplexity.simpleplayerfreeze.commands.ReloadConfig;
import simplexity.simpleplayerfreeze.commands.UnfreezeAll;
import simplexity.simpleplayerfreeze.commands.UnfreezePlayer;
import simplexity.simpleplayerfreeze.configs.ConfigHandler;
import simplexity.simpleplayerfreeze.hooks.discordsrv.DiscordSrvRegistration;
import simplexity.simpleplayerfreeze.hooks.placeholderapi.IsFrozenPlaceholder;
import simplexity.simpleplayerfreeze.listeners.AttackListener;
import simplexity.simpleplayerfreeze.listeners.ChatListener;
import simplexity.simpleplayerfreeze.listeners.CommandListener;
import simplexity.simpleplayerfreeze.listeners.CraftingListener;
import simplexity.simpleplayerfreeze.listeners.DropListener;
import simplexity.simpleplayerfreeze.listeners.EXPPickupListener;
import simplexity.simpleplayerfreeze.listeners.FreezeListener;
import simplexity.simpleplayerfreeze.listeners.InteractListener;
import simplexity.simpleplayerfreeze.listeners.InventoryOpenListener;
import simplexity.simpleplayerfreeze.listeners.ItemConsumeListener;
import simplexity.simpleplayerfreeze.listeners.JoinListener;
import simplexity.simpleplayerfreeze.listeners.LeaveListener;
import simplexity.simpleplayerfreeze.listeners.MoveListener;
import simplexity.simpleplayerfreeze.listeners.PickupListener;
import simplexity.simpleplayerfreeze.listeners.SwitchItemListener;
import simplexity.simpleplayerfreeze.listeners.WorldEnterListener;

public final class SimplePlayerFreeze extends JavaPlugin {
   public static SimplePlayerFreeze instance;
   public static Server server;
   public static ConsoleCommandSender sfConsoleSender;

   public void onEnable() {
      instance = this;
      server = this.getServer();
      sfConsoleSender = server.getConsoleSender();
      boolean papiEnabled = this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
      boolean discordSrvEnabled = this.getServer().getPluginManager().isPluginEnabled("DiscordSRV");
      this.getCommand("freeze").setExecutor(new FreezePlayer());
      this.getCommand("unfreeze").setExecutor(new UnfreezePlayer());
      this.getCommand("freezereload").setExecutor(new ReloadConfig());
      this.getCommand("freezespy").setExecutor(new FreezeSpy());
      this.getCommand("freezeall").setExecutor(new FreezeAll());
      this.getCommand("unfreezeall").setExecutor(new UnfreezeAll());
      if (papiEnabled) {
         new IsFrozenPlaceholder().register();
      }

      if (discordSrvEnabled) {
         DiscordSrvRegistration.registerListener(this);
      }

      this.registerListeners();
      this.saveDefaultConfig();
      this.reloadConfig();
      ConfigHandler.getInstance().reloadConfigSettings();
   }

   private void registerListeners() {
      this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
      this.getServer().getPluginManager().registerEvents(new DropListener(), this);
      this.getServer().getPluginManager().registerEvents(new EXPPickupListener(), this);
      this.getServer().getPluginManager().registerEvents(new InteractListener(), this);
      this.getServer().getPluginManager().registerEvents(new CraftingListener(), this);
      this.getServer().getPluginManager().registerEvents(new LeaveListener(), this);
      this.getServer().getPluginManager().registerEvents(new JoinListener(), this);
      this.getServer().getPluginManager().registerEvents(new MoveListener(), this);
      this.getServer().getPluginManager().registerEvents(new PickupListener(), this);
      this.getServer().getPluginManager().registerEvents(new SwitchItemListener(), this);
      this.getServer().getPluginManager().registerEvents(new ItemConsumeListener(), this);
      this.getServer().getPluginManager().registerEvents(new CommandListener(), this);
      this.getServer().getPluginManager().registerEvents(new AttackListener(), this);
      this.getServer().getPluginManager().registerEvents(new InventoryOpenListener(), this);
      this.getServer().getPluginManager().registerEvents(new FreezeListener(), this);
      this.getServer().getPluginManager().registerEvents(new WorldEnterListener(), this);
   }

   public static ConsoleCommandSender getSFConsoleSender() {
      return sfConsoleSender;
   }

   public static SimplePlayerFreeze getInstance() {
      return instance;
   }
}
