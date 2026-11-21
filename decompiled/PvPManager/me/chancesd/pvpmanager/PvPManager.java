package me.chancesd.pvpmanager;

import java.util.Objects;
import java.util.function.Function;
import me.chancesd.pvpmanager.command.Announce;
import me.chancesd.pvpmanager.command.Newbie;
import me.chancesd.pvpmanager.command.PM;
import me.chancesd.pvpmanager.command.PvP;
import me.chancesd.pvpmanager.command.PvPGlobal;
import me.chancesd.pvpmanager.command.PvPInfo;
import me.chancesd.pvpmanager.command.PvPList;
import me.chancesd.pvpmanager.command.PvPOverride;
import me.chancesd.pvpmanager.command.PvPStatus;
import me.chancesd.pvpmanager.command.Tag;
import me.chancesd.pvpmanager.command.Untag;
import me.chancesd.pvpmanager.library.metrics.CustomMetrics;
import me.chancesd.pvpmanager.listener.BlockedActionsListener;
import me.chancesd.pvpmanager.listener.EntityListener;
import me.chancesd.pvpmanager.listener.EntityListener1_20_4;
import me.chancesd.pvpmanager.listener.EntityListener1_9;
import me.chancesd.pvpmanager.listener.PlayerListener;
import me.chancesd.pvpmanager.listener.PlayerListener1_11;
import me.chancesd.pvpmanager.manager.ConfigManager;
import me.chancesd.pvpmanager.manager.DependencyManager;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.manager.StorageManager;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.manager.WorldManager;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.plugin.SDPlugin;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.ChatColor;

public class PvPManager extends SDPlugin {
   private ConfigManager configM;
   private PlayerManager playerManager;
   private UpdateManager updateManager;
   private StorageManager storageManager;
   private DependencyManager dependencyManager;
   private WorldManager worldManager;
   private EntityListener entityListener;
   private static PvPManager instance;

   @Override
   public void onPluginLoad() {
      Log.setup(this.getLogger(), "§7[§4§lPvP§8§lManager§7]");
      instance = this;
      this.dependencyManager = new DependencyManager();
      this.dependencyManager.onLoadSetup();
   }

   @Override
   public void onPluginEnable() {
      long start = System.currentTimeMillis();
      if (ScheduleUtils.isFolia()) {
         Log.infoColor(ChatColor.AQUA + "Running on Folia. Folia support is experimental");
         Log.infoColor(ChatColor.AQUA + "Please report any errors you find, as not many servers use Folia");
      }

      this.configM = new ConfigManager(this);
      Lang.setup(this);
      this.storageManager = new StorageManager(this);
      this.dependencyManager.onEnableSetup();
      this.updateManager = new UpdateManager(this);
      this.worldManager = new WorldManager(this);
      this.playerManager = new PlayerManager(this);
      this.startListeners();
      this.registerCommands();
      this.startMetrics();
      Log.infoColor(ChatColor.GREEN + this.getDescription().getFullName() + " enabled " + ChatColor.GRAY + "(" + (System.currentTimeMillis() - start) + " ms)");
   }

   @Override
   public void onPluginDisable() {
      this.playerManager.handlePluginDisable();
      this.dependencyManager.onDisableCleanup();
      this.storageManager.shutdown();
      instance = null;
   }

   private void startListeners() {
      if (MCVersion.isAtLeast(MCVersion.V1_9)) {
         this.registerListener(new EntityListener1_9(this.playerManager));
      }

      this.entityListener = new EntityListener(this.playerManager);
      this.registerListener(this.entityListener);
      if (MCVersion.isAtLeast(MCVersion.V1_11_2)) {
         this.registerListener(new PlayerListener1_11(this.playerManager));
      }

      this.registerListener(new PlayerListener(this.playerManager));
      this.registerListener(new BlockedActionsListener(this.playerManager));
      if (MCVersion.isAtLeast(MCVersion.V1_20_4)) {
         new EntityListener1_20_4(this.playerManager);
      }

      this.dependencyManager.startListeners(this);
   }

   private void registerCommands() {
      Function var10000 = sender -> Lang.ERROR_PERMISSION.msg();
      Function var10001 = sender -> Lang.ERROR_NOT_PLAYER.msg();
      Lang var10002 = Lang.ERROR_PLAYER_NOT_FOUND;
      Objects.requireNonNull(Lang.ERROR_PLAYER_NOT_FOUND);
      Lang var1 = var10002;
      BaseCommand.setGlobalDefaults(var10000, var10001, xva$0 -> var1.msg(xva$0));
      this.registerCommand("pvp", cmd -> new PvP(cmd, this.playerManager));
      this.registerCommand("newbie", cmd -> new Newbie(cmd, this.playerManager));
      this.registerCommand("pvpmanager", cmd -> new PM(cmd, this));
      this.registerCommand("pvpoverride", cmd -> new PvPOverride(cmd, this.playerManager));
      this.registerCommand("pvpinfo", cmd -> new PvPInfo(cmd, this.playerManager));
      this.registerCommand("pvplist", cmd -> new PvPList(cmd, this.playerManager));
      this.registerCommand("pvpstatus", cmd -> new PvPStatus(cmd, this.playerManager));
      this.registerCommand("pvptag", cmd -> new Tag(cmd, this.playerManager));
      this.registerCommand("untag", cmd -> new Untag(cmd, this.playerManager));
      this.registerCommand("announce", Announce::new);
      this.registerCommand("pvpglobal", cmd -> new PvPGlobal(cmd, this.playerManager));
   }

   private void startMetrics() {
      new CustomMetrics(this);
   }

   public EntityListener getEntityListener() {
      return this.entityListener;
   }

   public ConfigManager getConfigM() {
      return this.configM;
   }

   public PlayerManager getPlayerManager() {
      return this.playerManager;
   }

   public UpdateManager getUpdateManager() {
      return this.updateManager;
   }

   public StorageManager getStorageManager() {
      return this.storageManager;
   }

   public DependencyManager getDependencyManager() {
      return this.dependencyManager;
   }

   public WorldManager getWorldManager() {
      return this.worldManager;
   }

   public static PvPManager getInstance() {
      return instance;
   }
}
