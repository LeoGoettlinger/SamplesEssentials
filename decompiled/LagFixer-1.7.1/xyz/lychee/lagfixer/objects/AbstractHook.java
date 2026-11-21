package xyz.lychee.lagfixer.objects;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;

public abstract class AbstractHook {
   private final LagFixer plugin;
   private final HookManager manager;
   private final String name;

   public AbstractHook(LagFixer plugin, String name, HookManager manager) {
      this.plugin = plugin;
      this.manager = manager;
      this.name = name;
   }

   public boolean isSupported() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(this.name);
      return plugin != null && plugin.isEnabled();
   }

   public abstract void load() throws Exception;

   public abstract void disable();

   @Generated
   public LagFixer getPlugin() {
      return this.plugin;
   }

   @Generated
   public HookManager getManager() {
      return this.manager;
   }

   @Generated
   public String getName() {
      return this.name;
   }
}
