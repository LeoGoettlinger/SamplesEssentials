package xyz.lychee.lagfixer.managers;

import java.util.HashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.menu.ConfigMenu;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;
import xyz.lychee.lagfixer.modules.AbilityLimiterModule;
import xyz.lychee.lagfixer.modules.ConsoleFilterModule;
import xyz.lychee.lagfixer.modules.EntityLimiterModule;
import xyz.lychee.lagfixer.modules.ExplosionOptimizerModule;
import xyz.lychee.lagfixer.modules.InstantLeafDecayModule;
import xyz.lychee.lagfixer.modules.LagShieldModule;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;
import xyz.lychee.lagfixer.modules.RedstoneLimiterModule;
import xyz.lychee.lagfixer.modules.TrashDisposalModule;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class ModuleManager extends AbstractManager {
   private static ModuleManager instance;
   private final HashMap<String, AbstractModule> modules = new HashMap<>();

   public ModuleManager(LagFixer plugin) {
      super(plugin);
      instance = this;
      this.addAll(
         new MobAiReducerModule(plugin, this),
         new LagShieldModule(plugin, this),
         new RedstoneLimiterModule(plugin, this),
         new EntityLimiterModule(plugin, this),
         new ConsoleFilterModule(plugin, this),
         new WorldCleanerModule(plugin, this),
         new VehicleMotionReducerModule(plugin, this),
         new InstantLeafDecayModule(plugin, this),
         new AbilityLimiterModule(plugin, this),
         new ExplosionOptimizerModule(plugin, this),
         new AFKOptimizerModule(plugin, this),
         new TrashDisposalModule(plugin, this)
      );
   }

   public <T extends AbstractModule> T get(Class<T> clazz) {
      return clazz.cast(this.modules.get(clazz.getSimpleName()));
   }

   public <T extends AbstractModule> T get(String name) {
      return (T)(this.modules.containsKey(name) ? this.modules.get(name) : null);
   }

   private void addAll(AbstractModule... arrModules) {
      for (AbstractModule module : arrModules) {
         this.modules.put(module.getClass().getSimpleName(), module);
      }
   }

   @Override
   public void load() {
      for (AbstractModule module : this.modules.values()) {
         try {
            TimingUtil t = TimingUtil.startNew();
            boolean success = module.loadAllConfig();
            boolean enabled = module.getConfig().getBoolean(module.getName() + ".enabled");
            if (enabled) {
               if (success) {
                  module.load();
                  this.getPlugin().getLogger().info(" &8• &rSuccessfully loaded module " + module.getName() + " in " + t.stop() + "!");
               } else {
                  this.getPlugin()
                     .getLogger()
                     .info(" &8• &rSkipping unsupported module " + module.getName() + " for " + Bukkit.getServer().getBukkitVersion() + "!");
               }
            }

            module.setLoaded(success && enabled);
         } catch (Exception var6) {
            module.setLoaded(false);
            this.getPlugin().getLogger().info(" &8• &cSkipping module " + module.getName() + ", reason: " + var6.getMessage());
            this.getPlugin().printError(var6);
         }

         ConfigMenu menu = module.getMenu();
         menu.load();
         menu.updateAll();
      }

      if (this.getPlugin().getConfig().isSet("modules")) {
         this.getPlugin().saveConfig();
      }
   }

   @Override
   public void disable() {
      for (AbstractModule module : this.modules.values()) {
         if (module.isLoaded()) {
            try {
               TimingUtil t = TimingUtil.startNew();
               module.disable();
               this.getPlugin().getLogger().info(" • Successfully disabled module " + module.getName() + " in " + t.stop() + "!");
            } catch (Exception var4) {
               this.getPlugin().getLogger().info(" • Error with disabling module " + module.getName() + ", reason: " + var4.getMessage());
               this.getPlugin().printError(var4);
            }
         }
      }
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   @Generated
   public HashMap<String, AbstractModule> getModules() {
      return this.modules;
   }

   @Generated
   public static ModuleManager getInstance() {
      return instance;
   }
}
