package de.mistrx.buildpaste.main;

import de.mistrx.buildpaste.commands.BuildpasteCommand;
import de.mistrx.buildpaste.commands.ConnectAccountsCommand;
import de.mistrx.buildpaste.commands.ConstructCommand;
import de.mistrx.buildpaste.commands.GetPositionSelectorCommand;
import de.mistrx.buildpaste.commands.PasteCommand;
import de.mistrx.buildpaste.commands.RemovePosCommand;
import de.mistrx.buildpaste.commands.SetBuildCommand;
import de.mistrx.buildpaste.commands.SetFirstPosCommand;
import de.mistrx.buildpaste.commands.SetSecondPosCommand;
import de.mistrx.buildpaste.commands.ShareBuildCommand;
import de.mistrx.buildpaste.commands.UndoPasteCommand;
import de.mistrx.buildpaste.commands.UploadCommand;
import de.mistrx.buildpaste.listeners.ItemClickListener;
import de.mistrx.buildpaste.listeners.JoinServerListener;
import de.mistrx.buildpaste.listeners.ServerStartEvent;
import de.mistrx.buildpaste.util.CommandAutocomplete;
import de.mistrx.buildpaste.util.Functions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   private int pluginBridge;

   public void onEnable() {
      this.pluginBridge = this.preparePluginBridge();
      this.getCommand("paste").setExecutor(new PasteCommand());
      this.getCommand("connectaccounts").setExecutor(new ConnectAccountsCommand());
      this.getCommand("pos1").setExecutor(new SetFirstPosCommand());
      this.getCommand("pos2").setExecutor(new SetSecondPosCommand());
      this.getCommand("undopaste").setExecutor(new UndoPasteCommand());
      this.getCommand("selector").setExecutor(new GetPositionSelectorCommand());
      this.getCommand("removepos").setExecutor(new RemovePosCommand());
      this.getCommand("upload").setExecutor(new UploadCommand());
      this.getCommand("setbuild").setExecutor(new SetBuildCommand());
      this.getCommand("sharebuild").setExecutor(new ShareBuildCommand());
      this.getCommand("buildpaste").setExecutor(new BuildpasteCommand());
      this.getCommand("construct").setExecutor(new ConstructCommand());
      this.getCommand("buildpaste").setTabCompleter(new CommandAutocomplete());
      this.getCommand("paste").setTabCompleter(new CommandAutocomplete());
      PluginManager pluginManager = Bukkit.getPluginManager();
      pluginManager.registerEvents(new JoinServerListener(), this);
      pluginManager.registerEvents(new ItemClickListener(), this);
      pluginManager.registerEvents(new ServerStartEvent(), this);
      Functions.init(this);
   }
}
