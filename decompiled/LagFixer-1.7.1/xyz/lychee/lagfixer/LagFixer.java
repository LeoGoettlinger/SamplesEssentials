package xyz.lychee.lagfixer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lychee.lagfixer.libs.kyori.adventure.platform.bukkit.BukkitAudiences;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.MetricsManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.managers.UpdaterManager;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class LagFixer extends JavaPlugin {
   private int pluginExtensions;
   private static LagFixer instance;
   private final ArrayList<AbstractManager> managers = new ArrayList<>();
   private final ColoredLogger logger = new ColoredLogger();
   private BukkitAudiences audiences;

   public void onEnable() {
      instance = this;
      this.audiences = BukkitAudiences.create(this);
      this.managers.clear();
      this.logger.sendHeader(this.getDescription().getVersion());
      this.loadManager(new ErrorsManager(this));
      this.loadManager(new ConfigManager(this));
      this.loadManager(new SupportManager(this));
      this.loadManager(new HookManager(this));
      this.loadManager(new UpdaterManager(this));
      this.loadManager(new ModuleManager(this));
      this.loadManager(new CommandManager(this));
      this.loadManager(new MetricsManager(this));
   }

   public void onDisable() {
      for (Iterator<AbstractManager> it = this.managers.iterator(); it.hasNext(); it.remove()) {
         AbstractManager manager = it.next();

         try {
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fDisabling manager...");
            TimingUtil t = TimingUtil.startNew();
            manager.disable();
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fDisabled manager in &e" + t.stop() + "&f!");
         } catch (Exception var4) {
            this.printError(var4);
         }
      }

      SupportManager support = SupportManager.getInstance();
      support.getResourceMonitor().stop();
      support.getWorldsMonitor().stop();
   }

   public void loadManager(AbstractManager manager) {
      this.pluginExtensions = this.registerPluginExtensions();
      if (manager.isEnabled()) {
         this.managers.add(manager);

         try {
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fEnabling manager...");
            TimingUtil t = TimingUtil.startNew();
            manager.load();
            this.getLogger().info("&8(&e" + manager.getClass().getSimpleName() + "&8) &7-> &fEnabled manager in &e" + t.stop() + "&f!");
         } catch (Exception var3) {
            this.printError(var3);
         }
      }
   }

   public void printError(Throwable ex) {
      if (ErrorsManager.getInstance() != null && ErrorsManager.getInstance().checkError(ex)) {
         this.getLogger().log(Level.WARNING, ex.getMessage(), ex);
      }
   }

   @Generated
   public ArrayList<AbstractManager> getManagers() {
      return this.managers;
   }

   @Generated
   public ColoredLogger getLogger() {
      return this.logger;
   }

   @Generated
   public BukkitAudiences getAudiences() {
      return this.audiences;
   }

   @Generated
   public static LagFixer getInstance() {
      return instance;
   }
}
